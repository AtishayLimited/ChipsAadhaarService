package com.chips.aadhaar.dto;
import java.util.*;
public record DashboardResponse(String clientId, String clientName, String status,
    DashboardFilter appliedFilters, Map<String,Object> summary,
    List<Map<String,Object>> gatewaySegregation,
    List<Map<String,Object>> dailyTrend, List<Map<String,Object>> hourlyTransactions,
    List<Map<String,Object>> responseCodeDistribution,
    List<Map<String,Object>> ekycVaultTrend,
    Map<String,String> filterScopes, String responseMatchingPolicy) {}
