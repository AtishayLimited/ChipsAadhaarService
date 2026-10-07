package com.chips.aadhaar.dto.request;

import java.time.LocalDateTime;

public record RegistrationRequest(
        String clientName,
        String sa,
        LocalDateTime validUpto,
        String auaLk,
        String kuaLk,
        String redirectUri,
        String clientType,
        String kycFlag,
        String ekycPacketStorageFlag,
        String lkFlag,
        String webFlag,
        String ipAddress
) {
}