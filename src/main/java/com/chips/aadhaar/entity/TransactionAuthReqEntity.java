package com.chips.aadhaar.entity;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "auth_req")
public class TransactionAuthReqEntity {

    @Id
    @Column(name = "sno")
    private Long sno;

    @Column(name = "client_id")
    private String clientId;

    @Column(name = "auth_mode")
    private String authMode;

    @Column(name = "txn")
    private String txn;

    @Column(name = "req_ts")
    private LocalDateTime reqTs;

    @Column(name = "asa_gateway")
    private String asaGateway;

    public Long getSno() {
        return sno;
    }

    public void setSno(Long sno) {
        this.sno = sno;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getAuthMode() {
        return authMode;
    }

    public void setAuthMode(String authMode) {
        this.authMode = authMode;
    }

    public String getTxn() {
        return txn;
    }

    public void setTxn(String txn) {
        this.txn = txn;
    }

    public LocalDateTime getReqTs() {
        return reqTs;
    }

    public void setReqTs(LocalDateTime reqTs) {
        this.reqTs = reqTs;
    }

    public String getAsaGateway() {
        return asaGateway;
    }

    public void setAsaGateway(String asaGateway) {
        this.asaGateway = asaGateway;
    }
}