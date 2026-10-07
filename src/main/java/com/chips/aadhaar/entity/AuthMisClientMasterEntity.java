package com.chips.aadhaar.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "client_master")
public class AuthMisClientMasterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sno")
    private Integer sno;

    @Column(name = "client_id", nullable = false, length = 10)
    private String clientId;

    @Column(name = "client_name", nullable = false, length = 30)
    private String clientName;

    @Column(name = "auth_token", nullable = false, length = 60)
    private String authToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "client_type", nullable = false)
    private ClientType clientType;

    @Column(name = "sa", nullable = false, length = 12)
    private String sa;

    @Enumerated(EnumType.STRING)
    @Column(name = "kyc_flag", nullable = false)
    private YesNo kycFlag;

    @Column(name = "aua_lk", length = 100)
    private String auaLk;

    @Column(name = "kua_lk", length = 100)
    private String kuaLk;

    @Enumerated(EnumType.STRING)
    @Column(name = "lk_flag", nullable = false)
    private YesNo lkFlag;

    @Enumerated(EnumType.STRING)
    @Column(name = "web_flag", nullable = false)
    private YesNo webFlag;

    @Column(name = "redirect_uri", nullable = false, length = 150)
    private String redirectUri;

    @Enumerated(EnumType.STRING)
    @Column(name = "active_status", nullable = false)
    private ActiveStatus activeStatus;

    @Column(name = "valid_upto")
    private LocalDateTime validUpto;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name = "ip_addr", nullable = false, length = 20)
    private String ipAddr;

    @Column(name = "ekyc_packet_storage_flag", length = 1)
    private String ekycPacketStorageFlag;

    public enum ClientType {
        I, E
    }

    public enum YesNo {
        Y, N
    }

    public enum ActiveStatus {
        A, I, D
    }

    public Integer getSno() {
        return sno;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public String getAuthToken() {
        return authToken;
    }

    public ClientType getClientType() {
        return clientType;
    }

    public String getSa() {
        return sa;
    }

    public YesNo getKycFlag() {
        return kycFlag;
    }

    public String getAuaLk() {
        return auaLk;
    }

    public String getKuaLk() {
        return kuaLk;
    }

    public YesNo getLkFlag() {
        return lkFlag;
    }

    public YesNo getWebFlag() {
        return webFlag;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public ActiveStatus getActiveStatus() {
        return activeStatus;
    }

    public LocalDateTime getValidUpto() {
        return validUpto;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public String getIpAddr() {
        return ipAddr;
    }

    public String getEkycPacketStorageFlag() {
        return ekycPacketStorageFlag;
    }
}