package com.chips.aadhaar.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chips.aadhaar.dto.request.RegistrationRequest;
import com.chips.aadhaar.dto.response.ClientListResponse;
import com.chips.aadhaar.dto.response.RegistrationResponse;
import com.chips.aadhaar.service.SubAuaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/sub-aua")
@Tag(name = "Sub-AUA Registration")
public class SubAuaController {

    private final SubAuaService service;

    public SubAuaController(SubAuaService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Register a new Sub-AUA")
    public ResponseEntity<RegistrationResponse> register(
            @RequestBody RegistrationRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.register(request));
    }

    @GetMapping
    @Operation(summary = "List registered Sub-AUA clients")
    public List<ClientListResponse> getAll() {
        return service.getAll();
    }
}