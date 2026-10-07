package com.chips.aadhaar.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "client_credentials")
public class ClientCredentialSubAua {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "allowed_roles", nullable = false)
    private String allowedRoles;

    @Column(name = "client_id", nullable = false, length = 10)
    private String clientId;

    @Column(name = "client_secret_hash", nullable = false, length = 100)
    private String clientSecretHash;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "sub_aua_id", nullable = false, length = 10)
    private String subAuaId;

    protected ClientCredentialSubAua() {
        // JPA के लिए
    }

    public ClientCredentialSubAua(
            String clientId,
            String clientSecretHash) {

        this.active = true;
        this.allowedRoles = "SUB_AUA";
        this.clientId = clientId;
        this.clientSecretHash = clientSecretHash;
        this.createdAt = LocalDateTime.now();
        this.subAuaId = clientId;
    }
}