package com.chips.aadhaar.repository;

import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

import com.chips.aadhaar.entity.TransactionAuthReqEntity;


public interface TransactionAuthReqRepository extends JpaRepository<TransactionAuthReqEntity, Long> {
	long countByReqTsGreaterThanEqualAndReqTsLessThan(LocalDateTime start, LocalDateTime end);
}