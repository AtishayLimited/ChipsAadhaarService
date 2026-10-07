package com.chips.aadhaar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.chips.aadhaar.entity.AuthMisReportReqEntity;

public interface AuthMisReportReqRepository
        extends JpaRepository<AuthMisReportReqEntity, Integer>,
                JpaSpecificationExecutor<AuthMisReportReqEntity> {

}