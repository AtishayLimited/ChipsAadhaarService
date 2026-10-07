package com.chips.aadhaar.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chips.aadhaar.entity.ClientMasterSubAua;



public interface ClientMasterSubAuaRepository
        extends JpaRepository<ClientMasterSubAua, Integer> {

    List<ClientMasterSubAua> findAllByOrderBySnoDesc();
}