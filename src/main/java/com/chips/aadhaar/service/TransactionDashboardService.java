package com.chips.aadhaar.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.stereotype.Service;

import com.chips.aadhaar.dto.response.TransactionDashboardResponse;
import com.chips.aadhaar.repository.TransactionAuthReqRepository;


@Service
public class TransactionDashboardService {

	private final TransactionAuthReqRepository authReqRepository;

	private static final ZoneId ZONE = ZoneId.of("Asia/Kolkata");

	public TransactionDashboardService(TransactionAuthReqRepository authReqRepository) {

		this.authReqRepository = authReqRepository;
	}

	public TransactionDashboardResponse getTransactionDashboard() {

		LocalDate today = LocalDate.now(ZONE);

		// Today
		LocalDateTime todayStart = today.atStartOfDay();

		LocalDateTime tomorrowStart = today.plusDays(1).atStartOfDay();

		// Current Month
		LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay() ;

		LocalDateTime nextMonthStart = today.plusMonths(1).withDayOfMonth(1).atStartOfDay();

		// Current Year
		LocalDateTime yearStart = today.withDayOfYear(1).atStartOfDay();

		LocalDateTime nextYearStart = today.plusYears(1).withDayOfYear(1).atStartOfDay();

		long todayCount = authReqRepository.countByReqTsGreaterThanEqualAndReqTsLessThan(todayStart, tomorrowStart);

		long monthCount = authReqRepository.countByReqTsGreaterThanEqualAndReqTsLessThan(monthStart, nextMonthStart);

		long yearCount = authReqRepository.countByReqTsGreaterThanEqualAndReqTsLessThan(yearStart, nextYearStart);

		return new TransactionDashboardResponse(todayCount, monthCount, yearCount);
	}
}
