package com.chips.aadhaar.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Consolidated DTO container for all AuthMIS report APIs.
 * Each nested DTO belongs to a specific AuthMIS dashboard/report response.
 */
public final class AuthMisReportDto {

    public static record AsaPerformanceDto(
            String asaGateway,
            long requests,
            long responses,
            long successful,
            long failed,
            double successPercentage,
            double averageResponseTime
    ) {
    }

    public static record AuthMisDashboardResponse(
            DashboardSummaryDto summary,
            List<AuthenticationTrendDto> authenticationTrend,
            List<AuthenticationTypeDto> authenticationTypes,
            List<TopErrorDto> topErrors,
            List<AsaPerformanceDto> asaPerformance,
            List<ClientPerformanceDto> clientPerformance
    ) {
    }

    public static record AuthenticationTrendDto(
            LocalDate date,
            long successful,
            long failed,
            long total
    ) {
    }

    public static record AuthenticationTypeDto(
            String authType,
            long total,
            long successful,
            long failed,
            double successPercentage
    ) {
    }

    public static record AvgResponseTimeErrorRecordDto(
    
            String date,
            String subAuaCode,
            String authMode,
            String errorCode,
            String errorDescription,
            long errorCount,
            double averageResponseTime,
            double percentageOfTotalTransactions
    
    ) {
    }

    public static record AvgResponseTimeErrorResponse(
    
            AvgResponseTimeErrorSummaryDto summary,
            List<AvgResponseTimeErrorRecordDto> records,
            int page,
            int size,
            long totalRecords,
            int totalPages
    
    ) {
    }

    public static record AvgResponseTimeErrorSummaryDto(
    
            long totalErrorTransactions,
            double weightedAverageResponseTime,
            String topErrorCode,
            String topErrorDescription,
            long records
    
    ) {
    }

    public static record AvgResponseTimeRecordDto(
    
            String date,
            String subAuaCode,
            String authMode,
            long successCount,
            double averageResponseTime,
            double minResponseTime,
            double maxResponseTime,
            double p99ResponseTime
    
    ) {
    }

    public static record AvgResponseTimeResponse(
    
            AvgResponseTimeSummaryDto summary,
            List<AvgResponseTimeRecordDto> records,
            int page,
            int size,
            long totalRecords,
            int totalPages
    
    ) {
    }

    public static record AvgResponseTimeSummaryDto(
    
            long totalSuccessfulTransactions,
            double weightedAverageResponseTime,
            double maxP99Latency,
            long recordsShown
    
    ) {
    }

    public static record ClientPerformanceDto(
            String clientId,
            String clientName,
            long totalTransactions,
            long successful,
            long failed,
            long timeout,
            double successRate,
            long biometric,
            long otp,
            long ekyc
    ) {
    }

    public static record DashboardSummaryDto(
            long totalTransactions,
            long successful,
            long failed,
            long timeout,
            double successPercentage,
            double failurePercentage,
            double timeoutPercentage
    ) {
    }

    public static record MinuteWiseSummaryDto(
    
            long totalTransactions,
            long successful,
            long errors,
            double averageResponseTime
    
    ) {
    }

    public static record MinuteWiseTransactionDto(
    
            String date,
            String time,
            String service,
            String subAuaCode,
            long totalTransactions,
            long success,
            long errors,
            double successRate,
            double averageResponseTime
    
    ) {
    }

    public static record MinuteWiseTransactionResponse(
    
            MinuteWiseSummaryDto summary,
            List<MinuteWiseTransactionDto> records,
            int totalMinutes
    
    ) {
    }

    public static class OddTimeTransactionRecordDto {
    
        private long sno;
    
        private String transactionId;
        private String maskedAadhaar;
    
        private String subAua;
        private String authMode;
    
        private String date;
        private String time;
    
        private String result;
        private String errorCode;
    
        private String ipAddress;
        private String deviceId;
    
        public OddTimeTransactionRecordDto() {
        }
    
        public long getSno() {
            return sno;
        }
    
        public void setSno(long sno) {
            this.sno = sno;
        }
    
        public String getTransactionId() {
            return transactionId;
        }
    
        public void setTransactionId(String transactionId) {
            this.transactionId = transactionId;
        }
    
        public String getMaskedAadhaar() {
            return maskedAadhaar;
        }
    
        public void setMaskedAadhaar(String maskedAadhaar) {
            this.maskedAadhaar = maskedAadhaar;
        }
    
        public String getSubAua() {
            return subAua;
        }
    
        public void setSubAua(String subAua) {
            this.subAua = subAua;
        }
    
        public String getAuthMode() {
            return authMode;
        }
    
        public void setAuthMode(String authMode) {
            this.authMode = authMode;
        }
    
        public String getDate() {
            return date;
        }
    
        public void setDate(String date) {
            this.date = date;
        }
    
        public String getTime() {
            return time;
        }
    
        public void setTime(String time) {
            this.time = time;
        }
    
        public String getResult() {
            return result;
        }
    
        public void setResult(String result) {
            this.result = result;
        }
    
        public String getErrorCode() {
            return errorCode;
        }
    
        public void setErrorCode(String errorCode) {
            this.errorCode = errorCode;
        }
    
        public String getIpAddress() {
            return ipAddress;
        }
    
        public void setIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
        }
    
        public String getDeviceId() {
            return deviceId;
        }
    
        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }
    }

    public static class OddTimeTransactionResponse {
    
        private OddTimeTransactionSummaryDto summary;
    
        private List<OddTimeTransactionRecordDto> records;
    
        private int page;
        private int size;
    
        private long totalRecords;
        private int totalPages;
    
        public OddTimeTransactionResponse() {
        }
    
        public OddTimeTransactionSummaryDto getSummary() {
            return summary;
        }
    
        public void setSummary(OddTimeTransactionSummaryDto summary) {
            this.summary = summary;
        }
    
        public List<OddTimeTransactionRecordDto> getRecords() {
            return records;
        }
    
        public void setRecords(List<OddTimeTransactionRecordDto> records) {
            this.records = records;
        }
    
        public int getPage() {
            return page;
        }
    
        public void setPage(int page) {
            this.page = page;
        }
    
        public int getSize() {
            return size;
        }
    
        public void setSize(int size) {
            this.size = size;
        }
    
        public long getTotalRecords() {
            return totalRecords;
        }
    
        public void setTotalRecords(long totalRecords) {
            this.totalRecords = totalRecords;
        }
    
        public int getTotalPages() {
            return totalPages;
        }
    
        public void setTotalPages(int totalPages) {
            this.totalPages = totalPages;
        }
    }

    public static class OddTimeTransactionSummaryDto {
    
        private long oddTimeTxns;
        private long failures;
        private long flagged;
        private long successAtOddHours;
    
        public OddTimeTransactionSummaryDto() {
        }
    
        public long getOddTimeTxns() {
            return oddTimeTxns;
        }
    
        public void setOddTimeTxns(long oddTimeTxns) {
            this.oddTimeTxns = oddTimeTxns;
        }
    
        public long getFailures() {
            return failures;
        }
    
        public void setFailures(long failures) {
            this.failures = failures;
        }
    
        public long getFlagged() {
            return flagged;
        }
    
        public void setFlagged(long flagged) {
            this.flagged = flagged;
        }
    
        public long getSuccessAtOddHours() {
            return successAtOddHours;
        }
    
        public void setSuccessAtOddHours(long successAtOddHours) {
            this.successAtOddHours = successAtOddHours;
        }
    }

    public static record SubAuaTransactionDto(
            long serialNo,
            String subAuaCode,
            String subAuaName,
            String district,
            String block,
            String stationType,
            long totalTransactions,
            long successful,
            long failed,
            long timeout,
            double successRate
    ) {
    }

    public static record SubAuaTransactionResponse(
    
            List<SubAuaTransactionDto> records,
            int page,
            int size,
            long totalRecords,
            int totalPages
    
    ) {
    }

    public static class SuspectedAadhaarRecordDto {
    
        private String auaCode;
        private String subAua;
        private String authMode;
    
        private long failures24h;
        private long totalAttempts24h;
        private long distinctIps;
    
        private String firstSeen;
        private String lastSeen;
    
        private String risk;
        private String status;
    
        private boolean canBlock;
        private boolean canClear;
    
        public SuspectedAadhaarRecordDto() {
        }
    
        public String getAuaCode() {
            return auaCode;
        }
    
        public void setAuaCode(String auaCode) {
            this.auaCode = auaCode;
        }
    
        public String getSubAua() {
            return subAua;
        }
    
        public void setSubAua(String subAua) {
            this.subAua = subAua;
        }
    
        public String getAuthMode() {
            return authMode;
        }
    
        public void setAuthMode(String authMode) {
            this.authMode = authMode;
        }
    
        public long getFailures24h() {
            return failures24h;
        }
    
        public void setFailures24h(long failures24h) {
            this.failures24h = failures24h;
        }
    
        public long getTotalAttempts24h() {
            return totalAttempts24h;
        }
    
        public void setTotalAttempts24h(long totalAttempts24h) {
            this.totalAttempts24h = totalAttempts24h;
        }
    
        public long getDistinctIps() {
            return distinctIps;
        }
    
        public void setDistinctIps(long distinctIps) {
            this.distinctIps = distinctIps;
        }
    
        public String getFirstSeen() {
            return firstSeen;
        }
    
        public void setFirstSeen(String firstSeen) {
            this.firstSeen = firstSeen;
        }
    
        public String getLastSeen() {
            return lastSeen;
        }
    
        public void setLastSeen(String lastSeen) {
            this.lastSeen = lastSeen;
        }
    
        public String getRisk() {
            return risk;
        }
    
        public void setRisk(String risk) {
            this.risk = risk;
        }
    
        public String getStatus() {
            return status;
        }
    
        public void setStatus(String status) {
            this.status = status;
        }
    
        public boolean isCanBlock() {
            return canBlock;
        }
    
        public void setCanBlock(boolean canBlock) {
            this.canBlock = canBlock;
        }
    
        public boolean isCanClear() {
            return canClear;
        }
    
        public void setCanClear(boolean canClear) {
            this.canClear = canClear;
        }
    }

    public static class SuspectedAadhaarResponse {
    
        private SuspectedAadhaarSummaryDto summary;
    
        private List<SuspectedAadhaarRecordDto> records;
    
        private int page;
        private int size;
        private long totalRecords;
        private int totalPages;
    
        public SuspectedAadhaarResponse() {
        }
    
        public SuspectedAadhaarSummaryDto getSummary() {
            return summary;
        }
    
        public void setSummary(SuspectedAadhaarSummaryDto summary) {
            this.summary = summary;
        }
    
        public List<SuspectedAadhaarRecordDto> getRecords() {
            return records;
        }
    
        public void setRecords(List<SuspectedAadhaarRecordDto> records) {
            this.records = records;
        }
    
        public int getPage() {
            return page;
        }
    
        public void setPage(int page) {
            this.page = page;
        }
    
        public int getSize() {
            return size;
        }
    
        public void setSize(int size) {
            this.size = size;
        }
    
        public long getTotalRecords() {
            return totalRecords;
        }
    
        public void setTotalRecords(long totalRecords) {
            this.totalRecords = totalRecords;
        }
    
        public int getTotalPages() {
            return totalPages;
        }
    
        public void setTotalPages(int totalPages) {
            this.totalPages = totalPages;
        }
    }

    public static class SuspectedAadhaarSummaryDto {
    
        private long totalSuspected;
        private long highRisk;
        private long currentlyBlocked;
        private long cleared;
    
        public SuspectedAadhaarSummaryDto() {
        }
    
        public long getTotalSuspected() {
            return totalSuspected;
        }
    
        public void setTotalSuspected(long totalSuspected) {
            this.totalSuspected = totalSuspected;
        }
    
        public long getHighRisk() {
            return highRisk;
        }
    
        public void setHighRisk(long highRisk) {
            this.highRisk = highRisk;
        }
    
        public long getCurrentlyBlocked() {
            return currentlyBlocked;
        }
    
        public void setCurrentlyBlocked(long currentlyBlocked) {
            this.currentlyBlocked = currentlyBlocked;
        }
    
        public long getCleared() {
            return cleared;
        }
    
        public void setCleared(long cleared) {
            this.cleared = cleared;
        }
    }

    public static record TopErrorDto(
            String errorCode,
            long count,
            double failurePercentage
    ) {
    }

    public static record TransactionDetailDto(
    
            long serialNo,
            String transactionId,
            String auaCode,
            String subAuaCode,
            String residentName,
            String maskedAadhaar,
            String authType,
            String status,
            String errorCode,
            String errorDescription,
            String district,
            LocalDateTime transactionTime,
            String asaGateway,
            String responseTime
    ) {
    }

    public static record TransactionDetailResponse(
    
            TransactionDetailSummaryDto summary,
            List<TransactionDetailDto> records,
            int page,
            int size,
            long totalRecords,
            int totalPages
    
    ) {
    }

    public static record TransactionDetailSummaryDto(
    
            long success,
            long failure,
            long timeout,
            long total
    
    ) {
    }
}
