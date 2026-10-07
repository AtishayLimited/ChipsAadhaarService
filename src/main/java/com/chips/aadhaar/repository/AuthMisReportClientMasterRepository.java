package com.chips.aadhaar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.chips.aadhaar.entity.AuthMisClientMasterEntity;

public interface AuthMisReportClientMasterRepository
        extends JpaRepository<AuthMisClientMasterEntity, Integer> {

}