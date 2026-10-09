package com.chips.aadhaar.service;
import java.time.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.chips.aadhaar.dto.DashboardFilter;
import com.chips.aadhaar.dto.DashboardResponse;
import com.chips.aadhaar.dto.TransactionPage;
import com.chips.aadhaar.repository.ClientMasterSubAuaRepository;

@Service
@Transactional(readOnly=true)
public class SubAuaDashboardService {
    private final ClientMasterSubAuaRepository repository;
    private final SubAuaEditService editService;
    private final ZoneId zone;
    public SubAuaDashboardService(ClientMasterSubAuaRepository repository,SubAuaEditService editService,
            @Value("${app.dashboard.zone:Asia/Kolkata}") String zone) {
        this.repository=repository;this.editService=editService;this.zone=ZoneId.of(zone);
    }
    public DashboardFilter filter(LocalDate from,LocalDate to,LocalTime ft,LocalTime tt,String gateway,String error) {
        return DashboardFilter.of(from,to,ft,tt,gateway,error,zone);
    }
    public DashboardResponse dashboard(String id,DashboardFilter f) {
        var client=editService.requireClient(id);
        var gateways=repository.dashboardGroups(id,f,"gateway");
        for(var g:gateways) {
            long total=((Number)g.get("requests")).longValue(), success=((Number)g.get("success")).longValue();
            g.put("successPercentage",total==0?0.0:Math.round(success*1000.0/total)/10.0);
        }
        return new DashboardResponse(id,client.getClientName(),client.getActiveStatus(),f,
            repository.dashboardSummary(id,f),gateways,
            fillDaily(repository.dashboardGroups(id,f,"daily"),f,List.of("total","success","failed","pending")),
            fillHourly(repository.dashboardGroups(id,f,"hourly")),repository.dashboardGroups(id,f,"errors"),
            fillDaily(repository.storageTrend(id,f),f,List.of("ekycPackets","vaultRecords")),
            Map.of("auth","Request time and request gateway; errorCode from matched response.err",
                "storage","Own created_at and own gateway; error filter uses linked transaction_id/vault_reference"),
            "For each response, nearest preceding request with same client_id and txn; earliest response per request. Inferred because schema has no request FK.");
    }
    public TransactionPage transactions(String id,DashboardFilter f,String search,int page,int size) {
        editService.requireClient(id);
        if(page<0 || size<1 || size>100 || (long)page*size>Integer.MAX_VALUE || (search!=null&&search.length()>100))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid page, size (1..100), or search length (max 100)");
        return repository.transactionLog(id,f,search,page,size);
    }
    public Map<String,Object> options(String id,DashboardFilter f) {
        editService.requireClient(id);return repository.filterOptions(id,f);
    }
    private List<Map<String,Object>> fillDaily(List<Map<String,Object>> input,DashboardFilter f,List<String> fields) {
        Map<String,Map<String,Object>> existing=new HashMap<>();for(var row:input)existing.put(row.get("date").toString(),row);
        List<Map<String,Object>> out=new ArrayList<>();
        for(LocalDate day=f.start().toLocalDate();!day.isAfter(f.endExclusive().minusNanos(1).toLocalDate());day=day.plusDays(1)) {
            Map<String,Object> row=new LinkedHashMap<>();row.put("date",day);
            for(String field:fields)row.put(field,existing.getOrDefault(day.toString(),Map.of()).getOrDefault(field,0L));out.add(row);
        }return out;
    }
    private List<Map<String,Object>> fillHourly(List<Map<String,Object>> input) {
        Map<Integer,Object> counts=new HashMap<>();for(var row:input)counts.put(((Number)row.get("hour")).intValue(),row.get("total"));
        List<Map<String,Object>> out=new ArrayList<>();for(int h=0;h<24;h++)out.add(Map.of("hour",h,"total",counts.getOrDefault(h,0L)));return out;
    }
}
