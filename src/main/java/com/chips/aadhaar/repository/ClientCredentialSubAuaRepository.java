package com.chips.aadhaar.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chips.aadhaar.entity.ClientCredentialSubAua;



public interface ClientCredentialSubAuaRepository
        extends JpaRepository<ClientCredentialSubAua, Long> {
}