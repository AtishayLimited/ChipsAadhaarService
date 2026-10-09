package com.chips.aadhaar.controller;
import java.time.*;
import java.util.Map;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import com.chips.aadhaar.dto.DashboardResponse;
import com.chips.aadhaar.dto.EditRequest;
import com.chips.aadhaar.dto.EditResponse;
import com.chips.aadhaar.dto.TransactionPage;
import com.chips.aadhaar.service.SubAuaDashboardService;
import com.chips.aadhaar.service.SubAuaEditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
@RestController
@RequestMapping("/api/sub-aua/{clientId}")
@Tag(name="Sub-AUA Edit and Transaction Dashboard")
public class SubAuaDetailsController {
    private final SubAuaEditService edit;
    private final SubAuaDashboardService dashboard;
    public SubAuaDetailsController(SubAuaEditService edit,SubAuaDashboardService dashboard) { this.edit=edit;this.dashboard=dashboard; }
    @GetMapping
    @Operation(summary="Load Edit form; fields absent from DB are null")
    public EditResponse details(@PathVariable String clientId) {return edit.get(clientId);}
    @PutMapping
    @Operation(summary="Save Edit form; does not change client ID or credentials")
    public EditResponse update(@PathVariable String clientId,@Valid @RequestBody EditRequest request) {return edit.update(clientId,request);}
    @GetMapping("/dashboard")
    @Operation(summary="Filtered dashboard cards and charts; default is last 30 calendar days")
    public DashboardResponse dashboard(@PathVariable String clientId,
        @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate to,
        @RequestParam(required=false) @DateTimeFormat(pattern="HH:mm") LocalTime fromTime,
        @RequestParam(required=false) @DateTimeFormat(pattern="HH:mm") LocalTime toTime,
        @RequestParam(required=false) String gateway,@RequestParam(required=false) String errorCode) {
        return dashboard.dashboard(clientId,dashboard.filter(from,to,fromTime,toTime,gateway,errorCode));
    }
    @GetMapping("/transactions")
    @Operation(summary="Paginated request/response log with filters and transaction search")
    public TransactionPage transactions(@PathVariable String clientId,
        @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate to,
        @RequestParam(required=false) @DateTimeFormat(pattern="HH:mm") LocalTime fromTime,
        @RequestParam(required=false) @DateTimeFormat(pattern="HH:mm") LocalTime toTime,
        @RequestParam(required=false) String gateway,@RequestParam(required=false) String errorCode,
        @RequestParam(required=false) String search,@RequestParam(defaultValue="0") int page,
        @RequestParam(defaultValue="100") int size) {
        return dashboard.transactions(clientId,dashboard.filter(from,to,fromTime,toTime,gateway,errorCode),search,page,size);
    }
    @GetMapping("/filter-options")
    @Operation(summary="Available gateway/error-code values in selected date range")
    public Map<String,Object> options(@PathVariable String clientId,
        @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate to) {
        return dashboard.options(clientId,dashboard.filter(from,to,null,null,null,null));
    }
}
