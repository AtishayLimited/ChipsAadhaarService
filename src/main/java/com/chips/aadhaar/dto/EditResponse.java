package com.chips.aadhaar.dto;

import java.time.LocalDateTime;

public record EditResponse(String clientId, String clientName, String sa, LocalDateTime validUpto, String auaLk,
		String kuaLk, String redirectUri, String clientType, String kycFlag, String ekycPacketStorageFlag,
		String lkFlag, String webFlag, String status, String departmentAddress, Object technicalPoc,
		Object managementPoc) {
}
