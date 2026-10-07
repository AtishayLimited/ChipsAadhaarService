package com.chips.aadhaar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import com.chips.aadhaar.entity.AuthMisReportResEntity;



public interface AuthMisReportResRepository
        extends JpaRepository<AuthMisReportResEntity, Integer>,
                JpaSpecificationExecutor<AuthMisReportResEntity> {

}