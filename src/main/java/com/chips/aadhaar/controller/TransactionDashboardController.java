package com.chips.aadhaar.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chips.aadhaar.dto.response.TransactionDashboardResponse;
import com.chips.aadhaar.service.TransactionDashboardService;

@RestController
@RequestMapping("/api/dashboard")
public class TransactionDashboardController {

	private final TransactionDashboardService service;

	public TransactionDashboardController(TransactionDashboardService service) {

		this.service = service;
	}

	@GetMapping("/transactions")
	public ResponseEntity<TransactionDashboardResponse> getTransactionDashboard() {

		return ResponseEntity.ok(service.getTransactionDashboard());
	}
}
