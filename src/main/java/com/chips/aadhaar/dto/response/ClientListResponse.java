package com.chips.aadhaar.dto.response;

import java.time.LocalDateTime;

public record ClientListResponse(
        String clientId,
        String clientName,
        String sa,
        String clientType,
        String status,
        LocalDateTime validUpto
) {
}