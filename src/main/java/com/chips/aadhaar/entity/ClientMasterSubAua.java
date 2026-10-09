package com.chips.aadhaar.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "client_master")
public class ClientMasterSubAua {

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

    @Column(name = "client_type", nullable = false)
    private String clientType;

    @Column(name = "sa", nullable = false, length = 12)
    private String sa;

    @Column(name = "kyc_flag", nullable = false)
    private String kycFlag;

    @Column(name = "aua_lk")
    private String auaLk;

    @Column(name = "kua_lk")
    private String kuaLk;

    @Column(name = "lk_flag", nullable = false)
    private String lkFlag;

    @Column(name = "web_flag", nullable = false)
    private String webFlag;

    @Column(name = "redirect_uri", nullable = false, length = 150)
    private String redirectUri;

    @Column(name = "active_status", nullable = false)
    private String activeStatus;

    @Column(name = "valid_upto", nullable = false)
    private LocalDateTime validUpto;

    @Column(name = "ip_addr", nullable = false, length = 20)
    private String ipAddr;

    @Column(name = "ekyc_packet_storage_flag", length = 1)
    private String ekycPacketStorageFlag;

    protected ClientMasterSubAua() {
        // JPA के लिए
    }

    public ClientMasterSubAua(
            String clientId,
            String clientName,
            String authToken,
            String clientType,
            String sa,
            String kycFlag,
            String auaLk,
            String kuaLk,
            String lkFlag,
            String webFlag,
            String redirectUri,
            LocalDateTime validUpto,
            String ipAddr,
            String ekycPacketStorageFlag) {

        this.clientId = clientId;
        this.clientName = clientName;
        this.authToken = authToken;
        this.clientType = clientType;
        this.sa = sa;
        this.kycFlag = kycFlag;
        this.auaLk = auaLk;
        this.kuaLk = kuaLk;
        this.lkFlag = lkFlag;
        this.webFlag = webFlag;
        this.redirectUri = redirectUri;
        this.activeStatus = "A";
        this.validUpto = validUpto;
        this.ipAddr = ipAddr;
        this.ekycPacketStorageFlag = ekycPacketStorageFlag;
    }

    public Integer getSno() {
        return sno;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public String getSa() {
        return sa;
    }

    public String getClientType() {
        return clientType;
    }

    public String getActiveStatus() {
        return activeStatus;
    }

    public LocalDateTime getValidUpto() {
        return validUpto;
    }
    public String getKycFlag() { return kycFlag; }
    public String getAuaLk() { return auaLk; }
    public String getKuaLk() { return kuaLk; }
    public String getLkFlag() { return lkFlag; }
    public String getWebFlag() { return webFlag; }
    public String getRedirectUri() { return redirectUri; }
    public String getEkycPacketStorageFlag() { return ekycPacketStorageFlag; }

    public void updateDetails(String clientName, String sa, LocalDateTime validUpto,
            String auaLk, String kuaLk, String redirectUri, String clientType,
            String kycFlag, String ekycPacketStorageFlag, String lkFlag, String webFlag) {
        this.clientName = clientName;
        this.sa = sa;
        this.validUpto = validUpto;
        this.auaLk = auaLk;
        this.kuaLk = kuaLk;
        this.redirectUri = redirectUri;
        this.clientType = clientType;
        this.kycFlag = kycFlag;
        this.ekycPacketStorageFlag = ekycPacketStorageFlag;
        this.lkFlag = lkFlag;
        this.webFlag = webFlag;
    }
}