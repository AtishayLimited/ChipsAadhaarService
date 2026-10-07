package com.chips.aadhaar.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "auth_req")
public class AuthMisReportReqEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sno")
    private Integer sno;

    @Column(name = "client_id", nullable = false, length = 10)
    private String clientId;

    @Column(name = "rc", length = 1)
    private String rc;

    @Column(name = "auth_mode", nullable = false, length = 10)
    private String authMode;

    @Column(name = "txn", nullable = false, length = 60)
    private String txn;

    @Column(name = "ac", nullable = false, length = 10)
    private String ac;

    @Column(name = "sa", nullable = false, length = 10)
    private String sa;

    @Column(name = "ver", nullable = false, length = 4)
    private String ver;

    @Column(name = "pi", length = 1)
    private String pi;

    @Column(name = "pa", length = 1)
    private String pa;

    @Column(name = "pfa", length = 1)
    private String pfa;

    @Column(name = "bio", length = 1)
    private String bio;

    @Column(name = "bt", length = 10)
    private String bt;

    @Column(name = "pin", length = 1)
    private String pin;

    @Column(name = "otp", length = 1)
    private String otp;

    @Column(name = "ip_addr", length = 20)
    private String ipAddr;

    @Column(name = "req_ts")
    private LocalDateTime reqTs;

    @Column(name = "tid", length = 30)
    private String tid;

    @Column(name = "dc", length = 50)
    private String dc;

    @Column(name = "de", length = 1)
    private String de;

    @Column(name = "dpid", length = 255)
    private String dpid;

    @Column(name = "mi", length = 255)
    private String mi;

    @Column(name = "rdsid", length = 255)
    private String rdsid;

    @Column(name = "rdsver", length = 255)
    private String rdsver;

    @Column(name = "asa_gateway", length = 255)
    private String asaGateway;
    
    

	public Integer getSno() {
        return sno;
    }

    public String getClientId() {
        return clientId;
    }

    public String getRc() {
        return rc;
    }

    public String getAuthMode() {
        return authMode;
    }

    public String getTxn() {
        return txn;
    }

    public String getAc() {
        return ac;
    }

    public String getSa() {
        return sa;
    }

    public String getVer() {
        return ver;
    }

    public String getPi() {
        return pi;
    }

    public String getPa() {
        return pa;
    }

    public String getPfa() {
        return pfa;
    }

    public String getBio() {
        return bio;
    }

    public String getBt() {
        return bt;
    }

    public String getPin() {
        return pin;
    }

    public String getOtp() {
        return otp;
    }

    public String getIpAddr() {
        return ipAddr;
    }

    public LocalDateTime getReqTs() {
        return reqTs;
    }

    public String getTid() {
        return tid;
    }

    public String getDc() {
        return dc;
    }

    public String getDe() {
        return de;
    }

    public String getDpid() {
        return dpid;
    }

    public String getMi() {
        return mi;
    }

    public String getRdsid() {
        return rdsid;
    }

    public String getRdsver() {
        return rdsver;
    }

    public String getAsaGateway() {
        return asaGateway;
    }
}