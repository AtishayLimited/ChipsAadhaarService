package com.chips.aadhaar.dto;
import java.util.*;
public record TransactionPage(int page, int size, long totalElements,
    long totalPages, List<Map<String,Object>> content) {}
