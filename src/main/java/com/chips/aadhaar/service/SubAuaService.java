package com.chips.aadhaar.service;

import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.chips.aadhaar.dto.request.RegistrationRequest;
import com.chips.aadhaar.dto.response.ClientListResponse;
import com.chips.aadhaar.dto.response.RegistrationResponse;
import com.chips.aadhaar.entity.ClientCredentialSubAua;
import com.chips.aadhaar.entity.ClientMasterSubAua;
import com.chips.aadhaar.repository.ClientCredentialSubAuaRepository;
import com.chips.aadhaar.repository.ClientMasterSubAuaRepository;


@Service
public class SubAuaService {

    private final ClientMasterSubAuaRepository masterRepository;
    private final ClientCredentialSubAuaRepository credentialRepository;

    private final SecureRandom secureRandom = new SecureRandom();
    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    public SubAuaService(
    		ClientMasterSubAuaRepository masterRepository,
    		ClientCredentialSubAuaRepository credentialRepository) {

        this.masterRepository = masterRepository;
        this.credentialRepository = credentialRepository;
    }

    @Transactional
    public RegistrationResponse register(RegistrationRequest request) {

        validate(request);

        // client_id NOT NULL है; sno मिलने तक अस्थायी 10-character ID
        String temporaryId = "T" + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 9);

        String authToken = randomHex(16);
        String clientSecret = randomHex(32);

        ClientMasterSubAua client = new ClientMasterSubAua(
                temporaryId,
                request.clientName().trim(),
                authToken,
                request.clientType(),
                request.sa().trim(),
                request.kycFlag(),
                request.auaLk(),
                request.kuaLk(),
                request.lkFlag(),
                request.webFlag(),
                request.redirectUri().trim(),
                request.validUpto(),
                request.ipAddress().trim(),
                request.ekycPacketStorageFlag()
        );

        // INSERT होता है; database से generated sno मिलता है
        client = masterRepository.saveAndFlush(client);

        if (client.getSno() > 999) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "AUA-CHP999 ke baad ID format aur column length badalni hogi");
        }

        String clientId = "AUA-CHP"
                + String.format("%03d", client.getSno());

        // इसी transaction में temporary ID को final ID से बदलें
        client.setClientId(clientId);
        masterRepository.saveAndFlush(client);

        ClientCredentialSubAua credential = new ClientCredentialSubAua(
                clientId,
                passwordEncoder.encode(clientSecret)
        );

        credentialRepository.save(credential);

        return new RegistrationResponse(
                clientId,
                client.getClientName(),
                client.getActiveStatus(),
                authToken,
                clientSecret
        );
    }

    @Transactional(readOnly = true)
    public List<ClientListResponse> getAll() {
        return masterRepository.findAllByOrderBySnoDesc()
                .stream()
                .map(client -> new ClientListResponse(
                        client.getClientId(),
                        client.getClientName(),
                        client.getSa(),
                        client.getClientType(),
                        client.getActiveStatus(),
                        client.getValidUpto()
                ))
                .toList();
    }

    private void validate(RegistrationRequest r) {

        if (r == null
                || blank(r.clientName()) || r.clientName().length() > 30
                || blank(r.sa()) || r.sa().length() > 12
                || r.validUpto() == null
                || blank(r.redirectUri()) || r.redirectUri().length() > 150
                || blank(r.ipAddress()) || r.ipAddress().length() > 20) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Client name, SA, valid upto, redirect URI aur IP address check karein");
        }

        if (!List.of("I", "E").contains(r.clientType())
                || !yesNo(r.kycFlag())
                || !yesNo(r.lkFlag())
                || !yesNo(r.webFlag())
                || !yesNo(r.ekycPacketStorageFlag())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Client type I/E aur flags Y/N hone chahiye");
        }
    }

    private boolean yesNo(String value) {
        return "Y".equals(value) || "N".equals(value);
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private String randomHex(int byteCount) {
        byte[] bytes = new byte[byteCount];
        secureRandom.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}