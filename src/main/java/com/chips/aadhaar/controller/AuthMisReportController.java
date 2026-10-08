package com.chips.aadhaar.controller;



import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chips.aadhaar.dto.AuthMisReportDto.*;
import com.chips.aadhaar.service.AuthMisReportService;


@RestController
@RequestMapping("/api/authmis")
public class AuthMisReportController {

    private final AuthMisReportService authMisReportService;

    public AuthMisReportController(AuthMisReportService authMisReportService) {
        this.authMisReportService = authMisReportService;
    }

    // =========================================================
    // 1. DASHBOARD
    // =========================================================

    @GetMapping("/dashboard")
    public ResponseEntity<AuthMisDashboardResponse> getDashboard(

            @RequestParam(name = "fromDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,

            @RequestParam(name = "toDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate,

            @RequestParam(name = "clientId", required = false)
            String clientId,

            @RequestParam(name = "authType", required = false)
            String authType,

            @RequestParam(name = "asaGateway", required = false)
            String asaGateway) {

        return ResponseEntity.ok(
                authMisReportService.getDashboard(
                        fromDate,
                        toDate,
                        clientId,
                        authType,
                        asaGateway
                )
        );
    }

    // =========================================================
    // 2. SUB AUA TRANSACTIONS
    // =========================================================

    @GetMapping("/sub-aua-transactions")
    public ResponseEntity<SubAuaTransactionResponse> getSubAuaTransactions(

            @RequestParam(name = "fromDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,

            @RequestParam(name = "toDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate,

            @RequestParam(name = "clientId", required = false)
            String clientId,

            @RequestParam(name = "page", defaultValue = "0")
            int page,

            @RequestParam(name = "size", defaultValue = "10")
            int size) {

        return ResponseEntity.ok(
                authMisReportService.getSubAuaTransactions(
                        fromDate,
                        toDate,
                        clientId,
                        page,
                        size
                )
        );
    }

    // =========================================================
    // 3. TRANSACTION DETAILS
    // =========================================================

    @GetMapping("/transaction-details")
    public ResponseEntity<TransactionDetailResponse> getTransactionDetails(

            @RequestParam(name = "fromDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,

            @RequestParam(name = "toDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate,

            @RequestParam(name = "aua", required = false)
            String aua,

            @RequestParam(name = "authType", required = false)
            String authType,

            @RequestParam(name = "status", required = false)
            String status,

            @RequestParam(name = "search", required = false)
            String search,

            @RequestParam(name = "page", defaultValue = "0")
            int page,

            @RequestParam(name = "size", defaultValue = "10")
            int size) {

        return ResponseEntity.ok(
                authMisReportService.getTransactionDetails(
                        fromDate,
                        toDate,
                        aua,
                        authType,
                        status,
                        search,
                        page,
                        size
                )
        );
    }

    // =========================================================
    // 4. MINUTE WISE TRANSACTIONS
    // =========================================================

    @GetMapping("/minute-wise-transactions")
    public ResponseEntity<MinuteWiseTransactionResponse> getMinuteWiseTransactions(

            @RequestParam(name = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,

            @RequestParam(name = "fromTime", required = false)
            @DateTimeFormat(pattern = "HH:mm")
            LocalTime fromTime,

            @RequestParam(name = "toTime", required = false)
            @DateTimeFormat(pattern = "HH:mm")
            LocalTime toTime,

            @RequestParam(name = "service", required = false)
            String service) {

        return ResponseEntity.ok(
                authMisReportService.getMinuteWiseTransactions(
                        date,
                        fromTime,
                        toTime,
                        service
                )
        );
    }

    // =========================================================
    // 5. AVG RESPONSE TIME - SUCCESS
    // =========================================================

    @GetMapping("/avg-response-time-success")
    public ResponseEntity<AvgResponseTimeResponse> getAvgResponseTimeSuccess(

            @RequestParam(name = "fromDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,

            @RequestParam(name = "toDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate,

            @RequestParam(name = "subAuaCode", required = false)
            String subAuaCode,

            @RequestParam(name = "authMode", required = false)
            String authMode,

            @RequestParam(name = "page", defaultValue = "0")
            int page,

            @RequestParam(name = "size", defaultValue = "10")
            int size) {

        return ResponseEntity.ok(
                authMisReportService.getReport(
                        fromDate,
                        toDate,
                        subAuaCode,
                        authMode,
                        page,
                        size
                )
        );
    }

    // =========================================================
    // 6. AVG RESPONSE TIME - ERROR
    // =========================================================

    @GetMapping("/avg-response-time-error")
    public ResponseEntity<AvgResponseTimeErrorResponse> getAvgResponseTimeError(

            @RequestParam(name = "fromDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,

            @RequestParam(name = "toDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate,

            @RequestParam(name = "subAuaCode", required = false)
            String subAuaCode,

            @RequestParam(name = "authMode", required = false)
            String authMode,

            @RequestParam(name = "errorCode", required = false)
            String errorCode,

            @RequestParam(name = "page", defaultValue = "0")
            int page,

            @RequestParam(name = "size", defaultValue = "10")
            int size) {

        return ResponseEntity.ok(
                authMisReportService.getReport(
                        fromDate,
                        toDate,
                        subAuaCode,
                        authMode,
                        errorCode,
                        page,
                        size
                )
        );
    }

    // =========================================================
    // 7. SUSPECTED AADHAAR
    // =========================================================

    @GetMapping("/suspected-aadhaar")
    public ResponseEntity<SuspectedAadhaarResponse> getSuspectedAadhaarReport(

            @RequestParam(name = "fromDate", required = false)
            String fromDate,

            @RequestParam(name = "toDate", required = false)
            String toDate,

            @RequestParam(name = "risk", required = false)
            String risk,

            @RequestParam(name = "status", required = false)
            String status,

            @RequestParam(name = "auaCode", required = false)
            String auaCode,

            @RequestParam(name = "subAua", required = false)
            String subAua,

            @RequestParam(name = "authMode", required = false)
            String authMode,

            @RequestParam(name = "page", defaultValue = "0")
            int page,

            @RequestParam(name = "size", defaultValue = "10")
            int size) {

        return ResponseEntity.ok(
                authMisReportService.getSuspectedAadhaarReport(
                        fromDate,
                        toDate,
                        risk,
                        status,
                        auaCode,
                        subAua,
                        authMode,
                        page,
                        size
                )
        );
    }

    // =========================================================
    // 8. ODD TIME TRANSACTIONS
    // =========================================================

    @GetMapping("/odd-time-transactions")
    public ResponseEntity<OddTimeTransactionResponse> getOddTimeTransactions(

            @RequestParam(name = "fromDate", required = false)
            String fromDate,

            @RequestParam(name = "toDate", required = false)
            String toDate,

            @RequestParam(name = "fromTime", required = false, defaultValue = "23:00")
            String fromTime,

            @RequestParam(name = "toTime", required = false, defaultValue = "05:00")
            String toTime,

            @RequestParam(name = "authMode", required = false)
            String authMode,

            @RequestParam(name = "result", required = false)
            String result,

            @RequestParam(name = "page", defaultValue = "0")
            int page,

            @RequestParam(name = "size", defaultValue = "10")
            int size) {

        return ResponseEntity.ok(
                authMisReportService.getOddTimeTransactions(
                        fromDate,
                        toDate,
                        fromTime,
                        toTime,
                        authMode,
                        result,
                        page,
                        size
                )
        );
    }
}
