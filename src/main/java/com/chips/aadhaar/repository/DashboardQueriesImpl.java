package com.chips.aadhaar.repository;
import java.util.*;
import java.sql.Timestamp;
import java.time.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import com.chips.aadhaar.dto.*;

// Spring Data JPA repository fragment. All queries run through JPA EntityManager.
// MySQL 8 native aggregate queries keep the large authentication tables in the DB.
public class DashboardQueriesImpl implements DashboardQueries {
    @PersistenceContext private EntityManager em;
    private static final String GATEWAY="COALESCE(NULLIF(UPPER(TRIM(q.asa_gateway)),''),'UNKNOWN')";
    private record Sql(String cte, Map<String,Object> parameters) {}
    private Sql selected(String clientId, DashboardFilter f) {
        Map<String,Object> p=new LinkedHashMap<>();
        p.put("clientId",clientId);p.put("start",f.start());p.put("end",f.endExclusive());
        String where="q.client_id=:clientId AND q.req_ts>=:start AND q.req_ts<:end";
        if(f.gateway()!=null){ where+=" AND "+GATEWAY+"=:gateway";p.put("gateway",f.gateway()); }
        String error="";
        if(f.errorCode()!=null){error=" WHERE response_id IS NOT NULL AND error_code=:errorCode";p.put("errorCode",f.errorCode());}
        String cte="""
            WITH matched AS (
                SELECT q.sno AS request_id, q.txn, q.req_ts, q.auth_mode,
                COALESCE(NULLIF(UPPER(TRIM(q.asa_gateway)),''),'UNKNOWN') AS gateway,
                r.sno AS response_id, LOWER(TRIM(r.ret)) AS ret,
                COALESCE(NULLIF(TRIM(r.err),''),'NA') AS error_code,
                r.creation_date AS response_time,
                CASE WHEN TRIM(r.time_dif) REGEXP '^[0-9]+([.][0-9]+)?$'
                     THEN CAST(r.time_dif AS DECIMAL(20,3)) ELSE NULL END AS response_time_ms,
                CASE WHEN r.sno IS NULL THEN 'PENDING'
                     WHEN LOWER(TRIM(r.ret))='y' THEN 'SUCCESS'
                     WHEN LOWER(TRIM(r.ret))='n' THEN 'FAILED'
                     ELSE 'UNKNOWN' END AS status
                FROM auth_req q
                LEFT JOIN auth_res r ON r.sno=(
                    SELECT r2.sno FROM auth_res r2
                    WHERE r2.client_id=q.client_id AND r2.txn=q.txn
                      AND r2.creation_date>=q.req_ts
                      AND q.sno=(SELECT q2.sno FROM auth_req q2
                        WHERE q2.client_id=r2.client_id AND q2.txn=r2.txn
                          AND q2.req_ts<=r2.creation_date
                        ORDER BY q2.req_ts DESC,q2.sno DESC LIMIT 1)
                    ORDER BY r2.creation_date,r2.sno LIMIT 1)
                WHERE %s
            ), selected AS (SELECT * FROM matched%s)
            """.formatted(where,error);
        return new Sql(cte,p);
    }
    private Query query(String sql,Map<String,Object> p) {
        Query q=em.createNativeQuery(sql);p.forEach(q::setParameter);return q;
    }
    @SuppressWarnings("unchecked")
    private List<Map<String,Object>> rows(Sql sql,String body,String...names) {
        List<?> result=query(sql.cte()+body,sql.parameters()).getResultList();
        List<Map<String,Object>> out=new ArrayList<>();
        for(Object row:result) {
            Object[] values=row instanceof Object[] a?a:new Object[]{row};
            Map<String,Object> m=new LinkedHashMap<>();
            for(int i=0;i<names.length;i++)m.put(names[i],normalize(values[i]));out.add(m);
        }return out;
    }
    private Object normalize(Object value) {
        if(value instanceof Timestamp t)return t.toLocalDateTime();
        if(value instanceof java.sql.Date d)return d.toLocalDate();
        return value;
    }
    @Override public Map<String,Object> dashboardSummary(String id,DashboardFilter f) {
        Sql s=selected(id,f);
        Map<String,Object> m=rows(s,"""
            SELECT COUNT(*),COALESCE(SUM(status='SUCCESS'),0),COALESCE(SUM(status='FAILED'),0),
                COALESCE(SUM(status='PENDING'),0),COUNT(response_id),COALESCE(SUM(status='UNKNOWN'),0)
            FROM selected
            ""","totalAuthRequests","successfulAuth","failedAuth","pendingAuth","authResponses","unknownResponses").get(0);
        long total=((Number)m.get("totalAuthRequests")).longValue();
        long success=((Number)m.get("successfulAuth")).longValue();
        m.put("successPercentage",total==0?0.0:Math.round(success*1000.0/total)/10.0);
        m.put("ekycPackets",storageCount(id,f,"sub_aua_ekyc_packet"));
        m.put("vaultRecords",storageCount(id,f,"aadhaar_vault_record"));
        return m;
    }
    @Override public List<Map<String,Object>> dashboardGroups(String id,DashboardFilter f,String group) {
        Sql s=selected(id,f);
        return switch(group) {
            case "gateway" -> rows(s,"""
                SELECT gateway,COUNT(*),COUNT(response_id),COALESCE(SUM(status='SUCCESS'),0),
                    COALESCE(SUM(status='FAILED'),0),COALESCE(SUM(status='PENDING'),0)
                FROM selected GROUP BY gateway ORDER BY gateway
                ""","gateway","requests","responses","success","failed","pending");
            case "daily" -> rows(s,"""
                SELECT DATE(req_ts),COUNT(*),COALESCE(SUM(status='SUCCESS'),0),
                    COALESCE(SUM(status='FAILED'),0),COALESCE(SUM(status='PENDING'),0)
                FROM selected GROUP BY DATE(req_ts) ORDER BY DATE(req_ts)
                ""","date","total","success","failed","pending");
            case "hourly" -> rows(s,"SELECT HOUR(req_ts),COUNT(*) FROM selected GROUP BY HOUR(req_ts) ORDER BY HOUR(req_ts)","hour","total");
            case "errors" -> rows(s,"SELECT error_code,COUNT(*) FROM selected WHERE response_id IS NOT NULL GROUP BY error_code ORDER BY COUNT(*) DESC,error_code","errorCode","count");
            default -> throw new IllegalArgumentException("Unsupported group");
        };
    }
    private record StorageSql(String sql,Map<String,Object> parameters) {}
    private StorageSql storage(String id,DashboardFilter f,String table,String fields,String suffix) {
        // table is supplied only by the two internal constants, never by an HTTP parameter.
        Map<String,Object> p=new LinkedHashMap<>();p.put("clientId",id);p.put("start",f.start());p.put("end",f.endExclusive());
        String prefix="";
        String where="s.sub_aua_id=:clientId AND s.created_at>=:start AND s.created_at<:end";
        if(f.gateway()!=null){where+=" AND COALESCE(NULLIF(UPPER(TRIM(s.asa_gateway)),''),'UNKNOWN')=:gateway";p.put("gateway",f.gateway());}
        if(f.errorCode()!=null) {
            Sql selected=selected(id,f);prefix=selected.cte();p=selected.parameters();
            if(table.equals("sub_aua_ekyc_packet"))
                where+=" AND EXISTS (SELECT 1 FROM selected a WHERE a.txn=s.transaction_id)";
            else where+=" "+"""
                AND EXISTS (SELECT 1 FROM sub_aua_ekyc_packet k
                    WHERE k.sub_aua_id=s.sub_aua_id AND k.vault_reference=s.vault_reference
                    AND EXISTS (SELECT 1 FROM selected a WHERE a.txn=k.transaction_id))
                """;
        }
        return new StorageSql(prefix+"SELECT "+fields+" FROM "+table+" s WHERE "+where+suffix,p);
    }
    private long storageCount(String id,DashboardFilter f,String table) {
        var s=storage(id,f,table,"COUNT(*)","");
        return ((Number)query(s.sql(),s.parameters()).getSingleResult()).longValue();
    }
    @Override public List<Map<String,Object>> storageTrend(String id,DashboardFilter f) {
        Map<LocalDate,Map<String,Object>> days=new TreeMap<>();
        for(String table:List.of("sub_aua_ekyc_packet","aadhaar_vault_record")) {
            var s=storage(id,f,table,"DATE(s.created_at),COUNT(*)"," GROUP BY DATE(s.created_at) ORDER BY DATE(s.created_at)");
            for(Object row:query(s.sql(),s.parameters()).getResultList()) {
                Object[] a=(Object[])row;Object date=normalize(a[0]);
                LocalDate day=date instanceof LocalDate d?d:LocalDate.parse(date.toString());
                Map<String,Object> m=days.computeIfAbsent(day,d->{var v=new LinkedHashMap<String,Object>();v.put("date",d);v.put("ekycPackets",0L);v.put("vaultRecords",0L);return v;});
                m.put(table.equals("sub_aua_ekyc_packet")?"ekycPackets":"vaultRecords",((Number)a[1]).longValue());
            }
        }return new ArrayList<>(days.values());
    }
    @Override public TransactionPage transactionLog(String id,DashboardFilter f,String search,int page,int size) {
        Sql s=selected(id,f);String where="";
        if(search!=null&&!search.isBlank()) {
            where=" WHERE LOCATE(:search,txn)>0";s.parameters().put("search",search.trim());
        }
        long count=((Number)query(s.cte()+"SELECT COUNT(*) FROM selected"+where,s.parameters()).getSingleResult()).longValue();
        List<?> raw=query(s.cte()+"""
            SELECT request_id,txn,req_ts,auth_mode,gateway,status,
                CASE WHEN response_id IS NULL THEN NULL ELSE error_code END,response_time,response_time_ms
            FROM selected
            """+where+" ORDER BY req_ts DESC,request_id DESC",s.parameters()).setFirstResult(page*size).setMaxResults(size).getResultList();
        String[] names={"requestId","transactionId","requestTime","authMode","gateway","status","errorCode","responseTime","responseTimeMs"};
        List<Map<String,Object>> content=new ArrayList<>();
        for(Object row:raw) {Object[] a=(Object[])row;Map<String,Object> m=new LinkedHashMap<>();for(int i=0;i<names.length;i++)m.put(names[i],normalize(a[i]));content.add(m);}
        return new TransactionPage(page,size,count,(count+size-1)/size,content);
    }
    @Override public Map<String,Object> filterOptions(String id,DashboardFilter f) {
        Sql s=selected(id,new DashboardFilter(f.start(),f.endExclusive(),null,null));
        Map<String,Object> result=new LinkedHashMap<>();
        result.put("gateways",rows(s,"SELECT DISTINCT gateway FROM selected ORDER BY gateway","value").stream().map(m->m.get("value")).toList());
        result.put("errorCodes",rows(s,"SELECT DISTINCT error_code FROM selected WHERE response_id IS NOT NULL ORDER BY error_code","value").stream().map(m->m.get("value")).toList());
        return result;
    }
}
