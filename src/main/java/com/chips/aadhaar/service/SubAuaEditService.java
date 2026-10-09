package com.chips.aadhaar.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.chips.aadhaar.dto.EditRequest;
import com.chips.aadhaar.dto.EditResponse;
import com.chips.aadhaar.entity.ClientMasterSubAua;
import com.chips.aadhaar.repository.ClientMasterSubAuaRepository;

@Service
public class SubAuaEditService {
	private final ClientMasterSubAuaRepository repository;

	public SubAuaEditService(ClientMasterSubAuaRepository repository) {
		this.repository = repository;
	}

	public ClientMasterSubAua requireClient(String clientId) {
		var matches = repository.findByClientId(clientId);
		if (matches.isEmpty())
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sub-AUA not found");
		if (matches.size() != 1)
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"Duplicate client_id in client_master; resolve duplicates first");
		return matches.get(0);
	}

	@Transactional(readOnly = true)
	public EditResponse get(String clientId) {
		return response(requireClient(clientId));
	}

	@Transactional
	public EditResponse update(String clientId, EditRequest r) {
		var c = requireClient(clientId);
		c.updateDetails(r.clientName().trim(), r.sa().trim(), r.validUpto(), r.auaLk(), r.kuaLk(),
				r.redirectUri().trim(), r.clientType(), r.kycFlag(), r.ekycPacketStorageFlag(), r.lkFlag(),
				r.webFlag());
		repository.saveAndFlush(c);
		return response(c);
	}

	private EditResponse response(ClientMasterSubAua c) {
		return new EditResponse(c.getClientId(), c.getClientName(), c.getSa(), c.getValidUpto(), c.getAuaLk(),
				c.getKuaLk(), c.getRedirectUri(), c.getClientType(), c.getKycFlag(), c.getEkycPacketStorageFlag(),
				c.getLkFlag(), c.getWebFlag(), c.getActiveStatus(), null, null, null);
	}
}
