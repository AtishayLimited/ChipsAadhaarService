package com.chips.aadhaar.service;

import java.time.LocalDate;
import java.time.LocalTime;

import com.chips.aadhaar.dto.AuthMisReportDto.*;



/**
 * Service contract for all AuthMIS reports.
 *
 * The method signatures exactly match AuthMisReportServiceImpl.
 */
public interface AuthMisReportService {

    AuthMisDashboardResponse getDashboard(
            LocalDate fromDate,
            LocalDate toDate,
            String clientId,
            String authType,
            String asaGateway);

    AvgResponseTimeResponse getReport(
            LocalDate fromDate,
            LocalDate toDate,
            String subAuaCode,
            String authMode,
            int page,
            int size);

    AvgResponseTimeErrorResponse getReport(
            LocalDate fromDate,
            LocalDate toDate,
            String subAuaCode,
            String authMode,
            String errorCode,
            int page,
            int size);

    MinuteWiseTransactionResponse getMinuteWiseTransactions(
            LocalDate date,
            LocalTime fromTime,
            LocalTime toTime,
            String service);

    OddTimeTransactionResponse getOddTimeTransactions(
            String fromDate,
            String toDate,
            String fromTime,
            String toTime,
            String authMode,
            String result,
            int page,
            int size);

    SubAuaTransactionResponse getSubAuaTransactions(
            LocalDate fromDate,
            LocalDate toDate,
            String clientId,
            int page,
            int size);

    SuspectedAadhaarResponse getSuspectedAadhaarReport(
            String fromDate,
            String toDate,
            String risk,
            String status,
            String auaCode,
            String subAua,
            String authMode,
            int page,
            int size);

    TransactionDetailResponse getTransactionDetails(
            LocalDate fromDate,
            LocalDate toDate,
            String aua,
            String authType,
            String status,
            String search,
            int page,
            int size);
}
