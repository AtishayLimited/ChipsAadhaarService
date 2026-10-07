package com.chips.aadhaar.util;

import java.time.LocalDateTime;
import java.util.Collection;

import org.springframework.data.jpa.domain.Specification;

import com.chips.aadhaar.entity.AuthMisReportReqEntity;
import com.chips.aadhaar.entity.AuthMisReportResEntity;


/**
 * Single consolidated specification class for AuthMIS reports.
 *
 * IMPORTANT:
 * - No overloaded methods differ only by return type.
 * - Request and response specifications have explicit prefixes.
 * - The existing service logic can use this class directly.
 * - JPA property names match the AuthMIS entity fields used by the service.
 */
public final class AuthMisReportSpecification {

    private AuthMisReportSpecification() {
    }

    // ============================================================
    // AUTH REQUEST (AuthMisReportReqEntity)
    // ============================================================

    public static Specification<AuthMisReportReqEntity> reqDateBetween(
            LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> cb.and(
                cb.greaterThanOrEqualTo(root.get("reqTs"), from),
                cb.lessThan(root.get("reqTs"), to));
    }

    public static Specification<AuthMisReportReqEntity> reqClientId(String clientId) {
        return (root, query, cb) -> cb.equal(root.get("clientId"), clientId);
    }

    /** Original AuthReqSpecification.authType behaviour: case-insensitive. */
    public static Specification<AuthMisReportReqEntity> reqAuthType(String authType) {
        return (root, query, cb) -> cb.equal(
                cb.upper(root.get("authMode")),
                authType.toUpperCase());
    }

    /** Original Odd/Suspected authMode behaviour: exact value, blank means no filter. */
    public static Specification<AuthMisReportReqEntity> reqAuthMode(String authMode) {
        return (root, query, cb) -> {
            if (authMode == null || authMode.isBlank()) {
                return cb.conjunction();
            }
            return cb.equal(root.get("authMode"), authMode);
        };
    }

    public static Specification<AuthMisReportReqEntity> reqSubAua(String subAua) {
        return (root, query, cb) -> {
            if (subAua == null || subAua.isBlank()) {
                return cb.conjunction();
            }
            return cb.equal(root.get("sa"), subAua);
        };
    }

    public static Specification<AuthMisReportReqEntity> reqAc(String ac) {
        return (root, query, cb) -> {
            if (ac == null || ac.isBlank()) {
                return cb.conjunction();
            }
            return cb.equal(root.get("ac"), ac);
        };
    }

    public static Specification<AuthMisReportReqEntity> reqSa(String sa) {
        return (root, query, cb) -> {
            if (sa == null || sa.isBlank()) {
                return cb.conjunction();
            }
            return cb.equal(root.get("sa"), sa);
        };
    }

    public static Specification<AuthMisReportReqEntity> reqIpAddress(String ipAddress) {
        return (root, query, cb) -> {
            if (ipAddress == null || ipAddress.isBlank()) {
                return cb.conjunction();
            }
            return cb.equal(root.get("ipAddr"), ipAddress);
        };
    }

    // ============================================================
    // AUTH RESPONSE (AuthMisReportResEntity)
    // ============================================================

    public static Specification<AuthMisReportResEntity> resDateBetween(
            LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> cb.and(
                cb.greaterThanOrEqualTo(root.get("creationDate"), from),
                cb.lessThan(root.get("creationDate"), to));
    }

    public static Specification<AuthMisReportResEntity> resClientId(String clientId) {
        return (root, query, cb) -> cb.equal(
                cb.upper(root.get("clientId")),
                clientId.toUpperCase());
    }

    public static Specification<AuthMisReportResEntity> resAuthType(String authType) {
        return (root, query, cb) -> cb.equal(
                cb.upper(root.get("authMode")),
                authType.toUpperCase());
    }

    public static Specification<AuthMisReportResEntity> resAsaGateway(String asaGateway) {
        return (root, query, cb) -> cb.equal(
                cb.upper(root.get("asaGateway")),
                asaGateway.toUpperCase());
    }

    /** Successful transaction: ret = Y. */
    public static Specification<AuthMisReportResEntity> resSuccess() {
        return (root, query, cb) -> cb.equal(
                cb.upper(root.get("ret")), "Y");
    }

    /**
     * Error transaction: err exists and is not blank.
     * This uses the persisted entity property 'err'.
     */
    public static Specification<AuthMisReportResEntity> resErrorTransactions() {
        return (root, query, cb) -> cb.and(
                cb.isNotNull(root.get("err")),
                cb.notEqual(root.get("err"), ""));
    }

    public static Specification<AuthMisReportResEntity> resErrorCode(String errorCode) {
        return (root, query, cb) -> {
            if (errorCode == null || errorCode.isBlank()) {
                return cb.conjunction();
            }
            return cb.equal(root.get("err"), errorCode);
        };
    }

    public static Specification<AuthMisReportResEntity> resTimeout() {
        return (root, query, cb) -> cb.equal(root.get("err"), "502");
    }

    public static Specification<AuthMisReportResEntity> resFailure() {
        return (root, query, cb) -> cb.and(
                cb.or(
                        cb.isNull(root.get("ret")),
                        cb.notEqual(cb.upper(root.get("ret")), "Y")),
                cb.or(
                        cb.isNull(root.get("err")),
                        cb.notEqual(root.get("err"), "502")));
    }

    public static Specification<AuthMisReportResEntity> resTransactionIds(
            Collection<String> transactionIds) {
        return (root, query, cb) -> root.get("txn").in(transactionIds);
    }

    public static Specification<AuthMisReportResEntity> resSearch(String search) {
        return (root, query, cb) -> {
            String value = "%" + search.toUpperCase() + "%";
            return cb.or(
                    cb.like(cb.upper(root.get("txn")), value),
                    cb.like(cb.upper(root.get("clientId")), value));
        };
    }
}
