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



/**
 * Consolidated controller for all AuthMIS dashboard and report APIs.
 * The menu order is intentionally kept in sync with the AuthMIS frontend.
 */
@RestController
@RequestMapping("/api/authmis")
public class AuthMisReportController {

    private final AuthMisReportService service;

    public AuthMisReportController(AuthMisReportService service) {
        this.service = service;
    }

    // 01 - AuthMIS Dashboard
    @GetMapping("/dashboard")
    public ResponseEntity<AuthMisDashboardResponse> getDashboard(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String clientId,
            @RequestParam(required = false) String authType,
            @RequestParam(required = false) String asaGateway) {
        return ResponseEntity.ok(service.getDashboard(fromDate, toDate, clientId, authType, asaGateway));
    }

    // 02 - Sub-AUA-Wise Transactions
    @GetMapping("/sub-aua-transactions")
    public ResponseEntity<SubAuaTransactionResponse> getSubAuaTransactions(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String clientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(service.getSubAuaTransactions(fromDate, toDate, clientId, page, size));
    }

    // 03 - Transaction Detail Log
    @GetMapping("/transaction-details")
    public ResponseEntity<TransactionDetailResponse> getTransactionDetails(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String aua,
            @RequestParam(required = false) String authType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(service.getTransactionDetails(fromDate, toDate, aua, authType, status, search, page, size));
    }

    // 04 - Minute-Wise Transaction Report
    @GetMapping("/minute-wise-transactions")
    public ResponseEntity<MinuteWiseTransactionResponse> getMinuteWiseTransactions(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(pattern = "HH:mm") LocalTime fromTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "HH:mm") LocalTime toTime,
            @RequestParam(name = "service", required = false) String serviceName) {
        return ResponseEntity.ok(service.getMinuteWiseTransactions(date, fromTime, toTime, serviceName));
    }

    // 05 - Average Response Time / Success Count
    @GetMapping("/avg-response-time-success")
    public ResponseEntity<AvgResponseTimeResponse> getAvgResponseTimeSuccess(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String subAuaCode,
            @RequestParam(required = false) String authMode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(service.getReport(fromDate, toDate, subAuaCode, authMode, page, size));
    }

    // 06 - Average Response Time / Error Count
    @GetMapping("/avg-response-time-error")
    public ResponseEntity<AvgResponseTimeErrorResponse> getAvgResponseTimeError(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String subAuaCode,
            @RequestParam(required = false) String authMode,
            @RequestParam(required = false, name = "errorCode") String errorCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(service.getReport(fromDate, toDate, subAuaCode, authMode, errorCode, page, size));
    }

    // 07 - Suspected Aadhaar Report
    @GetMapping("/suspected-aadhaar")
    public ResponseEntity<SuspectedAadhaarResponse> getSuspectedAadhaarReport(
            @RequestParam String fromDate,
            @RequestParam String toDate,
            @RequestParam(required = false) String risk,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String auaCode,
            @RequestParam(required = false) String subAua,
            @RequestParam(required = false) String authMode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(service.getSuspectedAadhaarReport(fromDate, toDate, risk, status, auaCode, subAua, authMode, page, size));
    }

    // 08 - Odd-Time Transaction Report
    @GetMapping("/odd-time-transactions")
    public ResponseEntity<OddTimeTransactionResponse> getOddTimeTransactions(
            @RequestParam String fromDate,
            @RequestParam String toDate,
            @RequestParam(required = false, defaultValue = "23:00") String fromTime,
            @RequestParam(required = false, defaultValue = "05:00") String toTime,
            @RequestParam(required = false) String authMode,
            @RequestParam(required = false) String result,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(service.getOddTimeTransactions(fromDate, toDate, fromTime, toTime, authMode, result, page, size));
    }

    // 09 - Block Suspected Aadhaar UID
    // TODO: The current source does not contain a block/update API. Keep this menu action disabled until the backend action is implemented.
}
