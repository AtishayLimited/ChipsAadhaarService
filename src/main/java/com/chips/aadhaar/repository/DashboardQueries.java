package com.chips.aadhaar.repository;
import java.util.*;
import com.chips.aadhaar.dto.*;
public interface DashboardQueries {
    Map<String,Object> dashboardSummary(String clientId, DashboardFilter filter);
    List<Map<String,Object>> dashboardGroups(String clientId, DashboardFilter filter, String group);
    List<Map<String,Object>> storageTrend(String clientId, DashboardFilter filter);
    TransactionPage transactionLog(String clientId, DashboardFilter filter, String search, int page, int size);
    Map<String,Object> filterOptions(String clientId, DashboardFilter filter);
}
