package com.chips.aadhaar.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "auth_res")
public class AuthMisReportResEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sno")
    private Integer sno;

    @Column(name = "client_id", nullable = false, length = 10)
    private String clientId;

    @Column(name = "ret", length = 2)
    private String ret;

    @Column(name = "code", length = 50)
    private String code;

    @Column(name = "txn", nullable = false, length = 60)
    private String txn;

    @Column(name = "err", length = 5)
    private String err;

    @Column(name = "ts", length = 30)
    private String ts;

    @Column(name = "uid_token", length = 72)
    private String uidToken;

    @Column(name = "creation_date")
    private LocalDateTime creationDate;

    @Column(name = "time_dif", nullable = false, length = 20)
    private String timeDif;

    @Column(name = "auth_mode", length = 10)
    private String authMode;

    @Column(name = "asa_gateway", length = 255)
    private String asaGateway;

    public Integer getSno() {
        return sno;
    }

    public String getClientId() {
        return clientId;
    }

    public String getRet() {
        return ret;
    }

    public String getCode() {
        return code;
    }

    public String getTxn() {
        return txn;
    }

    public String getErr() {
        return err;
    }

    public String getTs() {
        return ts;
    }

    public String getUidToken() {
        return uidToken;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public String getTimeDif() {
        return timeDif;
    }

    public String getAuthMode() {
        return authMode;
    }

    public String getAsaGateway() {
        return asaGateway;
    }
    
    private String getErrCode;
    private String getErrInfo;
    
    public String getErrCode() {
    	    return getErrCode;
    }
    public String getErrInfo() {
	    return getErrInfo;
}
}