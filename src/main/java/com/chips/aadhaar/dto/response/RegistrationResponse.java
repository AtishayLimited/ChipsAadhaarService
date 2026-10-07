package com.chips.aadhaar.dto.response;

public record RegistrationResponse(
        String clientId,
        String clientName,
        String status,
        String authToken,
        String clientSecret
) {
}