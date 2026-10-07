package com.chips.aadhaar.service;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.chips.aadhaar.dto.AuthMisReportDto.*;
import com.chips.aadhaar.entity.AuthMisClientMasterEntity;
import com.chips.aadhaar.entity.AuthMisReportReqEntity;
import com.chips.aadhaar.entity.AuthMisReportResEntity;
import com.chips.aadhaar.repository.AuthMisReportClientMasterRepository;
import com.chips.aadhaar.repository.AuthMisReportReqRepository;
import com.chips.aadhaar.repository.AuthMisReportResRepository;
import com.chips.aadhaar.util.AuthMisReportSpecification;


/**
 * Consolidated AuthMIS business service.
 *
 * Each public method maps to one frontend AuthMIS menu item. Helper methods are
 * prefixed by report name to avoid duplicated logic and make deployment
 * maintenance easier.
 */
@Service
public class AuthMisReportServiceImpl implements AuthMisReportService {

	private final AuthMisReportReqRepository authReqRepository;
	private final AuthMisReportResRepository authResRepository;
	private final AuthMisReportClientMasterRepository clientMasterRepository;

	private static final int PAGE_SIZE = 5000;
	private static final int DB_PAGE_SIZE = 5000;

	// Odd-time report uses these formatters for input and display conversion.
	private static final DateTimeFormatter ODD_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final DateTimeFormatter ODD_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
	private static final DateTimeFormatter ODD_DISPLAY_DATE = DateTimeFormatter.ofPattern("dd-MM-yyyy");
	private static final DateTimeFormatter ODD_DISPLAY_TIME = DateTimeFormatter.ofPattern("HH:mm:ss");

	// Suspected Aadhaar report date conversion.
	private static final DateTimeFormatter SUSPECTED_INPUT_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final DateTimeFormatter SUSPECTED_OUTPUT_DATE = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

	public AuthMisReportServiceImpl(AuthMisReportReqRepository authReqRepository, AuthMisReportResRepository authResRepository,
			AuthMisReportClientMasterRepository clientMasterRepository) {
		this.authReqRepository = authReqRepository;
		this.authResRepository = authResRepository;
		this.clientMasterRepository = clientMasterRepository;
	}

	// ==================== DASHBOARD REPORT LOGIC ====================
	@Override
	public AuthMisDashboardResponse getDashboard(LocalDate fromDate, LocalDate toDate, String clientId, String authType,
			String asaGateway) {

		LocalDateTime from = fromDate.atStartOfDay();

		LocalDateTime to = toDate.plusDays(1).atStartOfDay();

		Specification<AuthMisReportResEntity> responseSpec = AuthMisReportSpecification.resDateBetween(from, to);

		if (dashboard_hasValue(clientId)) {
			responseSpec = responseSpec.and(AuthMisReportSpecification.resClientId(clientId));
		}

		if (dashboard_hasValue(authType)) {
			responseSpec = responseSpec.and(AuthMisReportSpecification.resAuthType(authType));
		}

		if (dashboard_hasValue(asaGateway)) {
			responseSpec = responseSpec.and(AuthMisReportSpecification.resAsaGateway(asaGateway));
		}

		List<AuthMisReportResEntity> responses = dashboard_loadResponses(responseSpec);

		/*
		 * Request data is required to identify:
		 *
		 * OTP Fingerprint Iris Face Demographic
		 */
		Specification<AuthMisReportReqEntity> requestSpec = AuthMisReportSpecification.reqDateBetween(from, to);

		if (dashboard_hasValue(clientId)) {
			requestSpec = requestSpec.and(AuthMisReportSpecification.reqClientId(clientId));
		}

		if (dashboard_hasValue(authType)) {
			requestSpec = requestSpec.and(AuthMisReportSpecification.reqAuthType(authType));
		}

		List<AuthMisReportReqEntity> requests = dashboard_loadRequests(requestSpec);

		Map<String, AuthMisReportReqEntity> requestByTxn = requests.stream().filter(r -> r.getTxn() != null)
				.collect(Collectors.toMap(AuthMisReportReqEntity::getTxn, Function.identity(), (oldValue, newValue) -> newValue));

		List<AuthMisClientMasterEntity> clients = clientMasterRepository.findAll();

		Map<String, AuthMisClientMasterEntity> clientMap = clients.stream().collect(
				Collectors.toMap(AuthMisClientMasterEntity::getClientId, Function.identity(), (oldValue, newValue) -> oldValue));

		DashboardSummaryDto summary = dashboard_buildSummary(responses);

		List<AuthenticationTrendDto> trend = dashboard_buildTrend(responses);

		List<AuthenticationTypeDto> authTypes = dashboard_buildAuthenticationTypes(responses, requestByTxn);

		List<TopErrorDto> topErrors = dashboard_buildTopErrors(responses);

		List<AsaPerformanceDto> asaPerformance = dashboard_buildAsaPerformance(responses);

		List<ClientPerformanceDto> clientPerformance = dashboard_buildClientPerformance(responses, requestByTxn,
				clientMap);

		return new AuthMisDashboardResponse(summary, trend, authTypes, topErrors, asaPerformance, clientPerformance);
	}

	private List<AuthMisReportResEntity> dashboard_loadResponses(Specification<AuthMisReportResEntity> specification) {

		List<AuthMisReportResEntity> result = new ArrayList<>();

		int pageNumber = 0;

		Page<AuthMisReportResEntity> page;

		do {

			Pageable pageable = PageRequest.of(pageNumber, PAGE_SIZE);

			page = authResRepository.findAll(specification, pageable);

			result.addAll(page.getContent());

			pageNumber++;

		} while (page.hasNext());

		return result;
	}

	private List<AuthMisReportReqEntity> dashboard_loadRequests(Specification<AuthMisReportReqEntity> specification) {

		List<AuthMisReportReqEntity> result = new ArrayList<>();

		int pageNumber = 0;

		Page<AuthMisReportReqEntity> page;

		do {

			Pageable pageable = PageRequest.of(pageNumber, PAGE_SIZE);

			page = authReqRepository.findAll(specification, pageable);

			result.addAll(page.getContent());

			pageNumber++;

		} while (page.hasNext());

		return result;
	}

	private DashboardSummaryDto dashboard_buildSummary(List<AuthMisReportResEntity> responses) {

		long total = responses.size();

		long successful = responses.stream().filter(this::dashboard_isSuccessful).count();

		long failed = responses.stream().filter(response -> !dashboard_isSuccessful(response)).count();

		/*
		 * Timeout rule is not defined in the SQL schema. So currently timeout = 0.
		 */
		long timeout = 0;

		return new DashboardSummaryDto(total, successful, failed, timeout, dashboard_percentage(successful, total),
				dashboard_percentage(failed, total), dashboard_percentage(timeout, total));
	}

	private List<AuthenticationTrendDto> dashboard_buildTrend(List<AuthMisReportResEntity> responses) {

		Map<LocalDate, List<AuthMisReportResEntity>> grouped = responses.stream().filter(r -> r.getCreationDate() != null)
				.collect(Collectors.groupingBy(r -> r.getCreationDate().toLocalDate()));

		return grouped.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(entry -> {

			List<AuthMisReportResEntity> list = entry.getValue();

			long success = list.stream().filter(this::dashboard_isSuccessful).count();

			long failed = list.size() - success;

			return new AuthenticationTrendDto(entry.getKey(), success, failed, list.size());
		}).toList();
	}

	private List<AuthenticationTypeDto> dashboard_buildAuthenticationTypes(List<AuthMisReportResEntity> responses,
			Map<String, AuthMisReportReqEntity> requestByTxn) {

		Map<String, List<AuthMisReportResEntity>> grouped = responses.stream().collect(Collectors.groupingBy(
				response -> dashboard_getAuthenticationType(response, requestByTxn.get(response.getTxn()))));

		return grouped.entrySet().stream().map(entry -> {

			List<AuthMisReportResEntity> list = entry.getValue();

			long total = list.size();

			long success = list.stream().filter(this::dashboard_isSuccessful).count();

			long failed = total - success;

			return new AuthenticationTypeDto(entry.getKey(), total, success, failed,
					dashboard_percentage(success, total));
		}).sorted(Comparator.comparingLong(AuthenticationTypeDto::total).reversed()).toList();
	}

	private String dashboard_getAuthenticationType(AuthMisReportResEntity response, AuthMisReportReqEntity request) {

		if (request == null) {

			if ("OTP".equalsIgnoreCase(response.getAuthMode())) {

				return "OTP";
			}

			return dashboard_normalizeAuthMode(response.getAuthMode());
		}

		if (dashboard_isYes(request.getOtp())) {
			return "OTP";
		}

		if (dashboard_isYes(request.getBio())) {

			String biometricType = request.getBt();

			if (biometricType == null) {
				return "Biometric";
			}

			String value = biometricType.toUpperCase();

			if (value.contains("FMR") || value.contains("FINGER")) {

				return "Fingerprint";
			}

			if (value.contains("FIR") || value.contains("IRIS")) {

				return "Iris";
			}

			if (value.contains("FACE")) {
				return "Face";
			}

			return "Biometric";
		}

		if (dashboard_isYes(request.getPi()) || dashboard_isYes(request.getPa()) || dashboard_isYes(request.getPfa())) {

			return "Demographic";
		}

		return dashboard_normalizeAuthMode(response.getAuthMode());
	}

	private List<TopErrorDto> dashboard_buildTopErrors(List<AuthMisReportResEntity> responses) {

		long totalFailed = responses.stream().filter(r -> !dashboard_isSuccessful(r)).count();

		Map<String, Long> errors = responses.stream().filter(r -> !dashboard_isSuccessful(r))
				.collect(Collectors.groupingBy(r -> {

					if (r.getErr() == null || r.getErr().isBlank()) {

						return "UNKNOWN";
					}

					return r.getErr();
				}, Collectors.counting()));

		return errors.entrySet().stream().sorted(Map.Entry.<String, Long>comparingByValue().reversed()).limit(10)
				.map(entry -> new TopErrorDto(entry.getKey(), entry.getValue(),
						dashboard_percentage(entry.getValue(), totalFailed)))
				.toList();
	}

	private List<AsaPerformanceDto> dashboard_buildAsaPerformance(List<AuthMisReportResEntity> responses) {

		Map<String, List<AuthMisReportResEntity>> grouped = responses.stream().collect(Collectors.groupingBy(response -> {

			if (response.getAsaGateway() == null || response.getAsaGateway().isBlank()) {

				return "UNKNOWN";
			}

			return response.getAsaGateway();
		}));

		return grouped.entrySet().stream().map(entry -> {

			List<AuthMisReportResEntity> list = entry.getValue();

			long successful = list.stream().filter(this::dashboard_isSuccessful).count();

			long failed = list.size() - successful;

			double averageResponseTime = list.stream().map(AuthMisReportResEntity::getTimeDif)
					.mapToDouble(this::dashboard_parseResponseTime).filter(value -> value >= 0).average().orElse(0.0);

			return new AsaPerformanceDto(entry.getKey(), list.size(), list.size(), successful, failed,
					dashboard_percentage(successful, list.size()), averageResponseTime);
		}).sorted(Comparator.comparingLong(AsaPerformanceDto::responses).reversed()).toList();
	}

	private List<ClientPerformanceDto> dashboard_buildClientPerformance(List<AuthMisReportResEntity> responses,
			Map<String, AuthMisReportReqEntity> requestByTxn, Map<String, AuthMisClientMasterEntity> clientMap) {

		Map<String, List<AuthMisReportResEntity>> grouped = responses.stream().collect(Collectors.groupingBy(AuthMisReportResEntity::getClientId));

		return grouped.entrySet().stream().map(entry -> {

			String clientId = entry.getKey();

			List<AuthMisReportResEntity> list = entry.getValue();

			AuthMisClientMasterEntity client = clientMap.get(clientId);

			long successful = list.stream().filter(this::dashboard_isSuccessful).count();

			long failed = list.size() - successful;

			long biometric = list.stream()
					.filter(response -> dashboard_isBiometric(requestByTxn.get(response.getTxn()))).count();

			long otp = list.stream().filter(response -> dashboard_isOtp(requestByTxn.get(response.getTxn()))).count();

			long ekyc = 0;

			String clientName = client == null ? "Unknown" : client.getClientName();

			return new ClientPerformanceDto(clientId, clientName, list.size(), successful, failed, 0,
					dashboard_percentage(successful, list.size()), biometric, otp, ekyc);
		}).sorted(Comparator.comparingLong(ClientPerformanceDto::totalTransactions).reversed()).toList();
	}

	private boolean dashboard_isSuccessful(AuthMisReportResEntity response) {

		return response.getRet() != null && response.getRet().equalsIgnoreCase("Y");
	}

	private boolean dashboard_isOtp(AuthMisReportReqEntity request) {

		return request != null && dashboard_isYes(request.getOtp());
	}

	private boolean dashboard_isBiometric(AuthMisReportReqEntity request) {

		return request != null && dashboard_isYes(request.getBio());
	}

	private boolean dashboard_isYes(String value) {

		return value != null && value.equalsIgnoreCase("Y");
	}

	private String dashboard_normalizeAuthMode(String value) {

		if (value == null || value.isBlank()) {

			return "UNKNOWN";
		}

		if (value.equalsIgnoreCase("Otp")) {
			return "OTP";
		}

		if (value.equalsIgnoreCase("Auth")) {
			return "AUTH";
		}

		return value.toUpperCase();
	}

	private double dashboard_parseResponseTime(String value) {

		if (value == null || value.isBlank()) {

			return -1;
		}

		try {
			return Double.parseDouble(value);

		} catch (NumberFormatException ignored) {
		}

		/*
		 * Supports formats such as:
		 *
		 * 0.45 1.2 00:00:01.250
		 */
		try {

			if (value.contains(":")) {

				String[] parts = value.split(":");

				if (parts.length == 3) {

					double hours = Double.parseDouble(parts[0]);

					double minutes = Double.parseDouble(parts[1]);

					double seconds = Double.parseDouble(parts[2]);

					return (hours * 3600) + (minutes * 60) + seconds;
				}
			}

		} catch (Exception ignored) {
		}

		return -1;
	}

	private double dashboard_percentage(long value, long total) {

		if (total == 0) {
			return 0.0;
		}

		return Math.round(((double) value / total) * 10000.0) / 100.0;
	}

	private boolean dashboard_hasValue(String value) {

		return value != null && !value.isBlank();
	}

	// ==================== AVGSUCCESS REPORT LOGIC ====================
	@Override
	public AvgResponseTimeResponse getReport(

			LocalDate fromDate, LocalDate toDate, String subAuaCode, String authMode, int page, int size) {

		if (page < 0) {
			page = 0;
		}

		if (size <= 0) {
			size = 10;
		}

		if (size > 100) {
			size = 100;
		}

		/*
		 * Date range
		 */
		LocalDateTime from = fromDate.atStartOfDay();

		LocalDateTime to = toDate.plusDays(1).atStartOfDay();

		/*
		 * Base specification
		 */
		Specification<AuthMisReportResEntity> specification = AuthMisReportSpecification.resDateBetween(from, to);

		/*
		 * IMPORTANT:
		 *
		 * This report is ONLY for successful transactions.
		 */
		specification = specification.and(AuthMisReportSpecification.resSuccess());

		/*
		 * Sub-AUA filter
		 *
		 * Current schema doesn't have a dedicated Sub-AUA field, therefore client_id is
		 * used as the available identifier.
		 */
		if (avgSuccess_hasValue(subAuaCode)) {

			specification = specification.and(AuthMisReportSpecification.resClientId(subAuaCode));
		}

		/*
		 * Auth mode filter.
		 *
		 * If the UI sends FMR / OTP / eKYC-BIO, actual service classification is done
		 * after loading auth_req.
		 */
		List<AuthMisReportResEntity> responses = avgSuccess_loadResponses(specification);

		/*
		 * Load request data.
		 */
		Map<String, AuthMisReportReqEntity> requestMap = avgSuccess_loadRequests(responses);

		/*
		 * Filter by actual authentication mode.
		 */
		List<avgSuccess_SuccessfulTransaction> transactions = new ArrayList<>();

		for (AuthMisReportResEntity response : responses) {

			AuthMisReportReqEntity request = requestMap.get(response.getTxn());

			String currentAuthMode = avgSuccess_determineAuthMode(response, request);

			/*
			 * Apply auth mode filter.
			 */
			if (avgSuccess_hasValue(authMode)) {

				if (!currentAuthMode.equalsIgnoreCase(avgSuccess_normalizeFilterAuthMode(authMode))) {

					continue;
				}
			}

			/*
			 * Response time
			 */
			double responseTime = avgSuccess_parseResponseTime(response.getTimeDif());

			/*
			 * Ignore records where response time is unavailable.
			 */
			if (responseTime < 0) {
				continue;
			}

			/*
			 * Date
			 */
			if (response.getCreationDate() == null) {

				continue;
			}

			/*
			 * Sub-AUA identifier.
			 */
			String currentSubAua = avgSuccess_determineSubAua(response, request);

			transactions.add(new avgSuccess_SuccessfulTransaction(

					response.getCreationDate(),

					currentSubAua,

					currentAuthMode,

					responseTime));
		}

		/*
		 * Group:
		 *
		 * DATE SUB-AUA AUTH MODE
		 */
		Map<avgSuccess_GroupKey, List<Double>> groups = new HashMap<>();

		for (avgSuccess_SuccessfulTransaction transaction : transactions) {

			LocalDate date = transaction.dateTime().toLocalDate();

			avgSuccess_GroupKey key = new avgSuccess_GroupKey(

					date,

					transaction.subAuaCode(),

					transaction.authMode());

			groups.computeIfAbsent(key, k -> new ArrayList<>()).add(transaction.responseTime());
		}

		/*
		 * Build rows.
		 */
		List<AvgResponseTimeRecordDto> allRecords = groups.entrySet()

				.stream()

				.map(entry -> avgSuccess_buildRecord(entry.getKey(), entry.getValue()))

				.sorted(Comparator.comparing(AvgResponseTimeRecordDto::date).reversed()

						.thenComparing(AvgResponseTimeRecordDto::subAuaCode)

						.thenComparing(AvgResponseTimeRecordDto::authMode))

				.toList();

		/*
		 * Overall summary.
		 */
		AvgResponseTimeSummaryDto summary = avgSuccess_buildSummary(transactions, allRecords);

		/*
		 * Pagination.
		 */
		int totalRecords = allRecords.size();

		int totalPages = totalRecords == 0 ? 0 : (int) Math.ceil((double) totalRecords / size);

		int start = page * size;

		int end = Math.min(start + size, totalRecords);

		List<AvgResponseTimeRecordDto> pageRecords;

		if (start >= totalRecords) {

			pageRecords = List.of();

		} else {

			pageRecords = allRecords.subList(start, end);
		}

		return new AvgResponseTimeResponse(

				summary,

				pageRecords,

				page,

				size,

				totalRecords,

				totalPages);
	}

	/*
	 * ========================================================= LOAD AUTH RESPONSE
	 * DATA =========================================================
	 */

	private List<AuthMisReportResEntity> avgSuccess_loadResponses(

			Specification<AuthMisReportResEntity> specification) {

		List<AuthMisReportResEntity> result = new ArrayList<>();

		int pageNumber = 0;

		Page<AuthMisReportResEntity> page;

		do {

			Pageable pageable = PageRequest.of(pageNumber, DB_PAGE_SIZE);

			page = authResRepository.findAll(specification, pageable);

			result.addAll(page.getContent());

			pageNumber++;

		} while (page.hasNext());

		return result;
	}

	/*
	 * ========================================================= LOAD REQUEST DATA
	 * =========================================================
	 */

	private Map<String, AuthMisReportReqEntity> avgSuccess_loadRequests(

			List<AuthMisReportResEntity> responses) {

		Map<String, AuthMisReportReqEntity> result = new HashMap<>();

		List<String> transactionIds = responses.stream()

				.map(AuthMisReportResEntity::getTxn)

				.filter(txn -> txn != null && !txn.isBlank())

				.toList();

		if (transactionIds.isEmpty()) {

			return result;
		}

		Specification<AuthMisReportReqEntity> specification = (root, query, cb) ->

		root.get("txn").in(transactionIds);

		List<AuthMisReportReqEntity> requests = authReqRepository.findAll(specification);

		for (AuthMisReportReqEntity request : requests) {

			result.put(request.getTxn(), request);
		}

		return result;
	}

	/*
	 * ========================================================= BUILD REPORT ROW
	 * =========================================================
	 */

	private AvgResponseTimeRecordDto avgSuccess_buildRecord(

			avgSuccess_GroupKey key,

			List<Double> responseTimes) {

		List<Double> sorted = responseTimes.stream().sorted().toList();

		long count = sorted.size();

		double sum = sorted.stream().mapToDouble(Double::doubleValue).sum();

		double average = count == 0 ? 0.0 : sum / count;

		double min = count == 0 ? 0.0 : sorted.get(0);

		double max = count == 0 ? 0.0 : sorted.get(sorted.size() - 1);

		double p99 = avgSuccess_calculateP99(sorted);

		return new AvgResponseTimeRecordDto(

				key.date().toString(),

				key.subAuaCode(),

				key.authMode(),

				count,

				avgSuccess_round(average),

				avgSuccess_round(min),

				avgSuccess_round(max),

				avgSuccess_round(p99));
	}

	/*
	 * ========================================================= SUMMARY
	 * =========================================================
	 */

	private AvgResponseTimeSummaryDto avgSuccess_buildSummary(

			List<avgSuccess_SuccessfulTransaction> transactions,

			List<AvgResponseTimeRecordDto> records) {

		long totalSuccessful = transactions.size();

		double sum = transactions.stream().mapToDouble(avgSuccess_SuccessfulTransaction::responseTime).sum();

		double weightedAverage = totalSuccessful == 0 ? 0.0 : sum / totalSuccessful;

		double maxP99 = records.stream().mapToDouble(AvgResponseTimeRecordDto::p99ResponseTime).max().orElse(0.0);

		return new AvgResponseTimeSummaryDto(

				totalSuccessful,

				avgSuccess_round(weightedAverage),

				avgSuccess_round(maxP99),

				records.size());
	}

	/*
	 * ========================================================= P99
	 * =========================================================
	 */

	private double avgSuccess_calculateP99(List<Double> sortedValues) {

		if (sortedValues.isEmpty()) {
			return 0.0;
		}

		int index = (int) Math.ceil(0.99 * sortedValues.size()) - 1;

		if (index < 0) {
			index = 0;
		}

		if (index >= sortedValues.size()) {

			index = sortedValues.size() - 1;
		}

		return sortedValues.get(index);
	}

	/*
	 * ========================================================= AUTH MODE
	 * =========================================================
	 */

	private String avgSuccess_determineAuthMode(

			AuthMisReportResEntity response,

			AuthMisReportReqEntity request) {

		if (request != null) {

			/*
			 * OTP
			 */
			if (avgSuccess_isYes(request.getOtp())) {

				return "OTP";
			}

			/*
			 * Biometric
			 */
			if (avgSuccess_isYes(request.getBio())) {

				String bt = request.getBt();

				if (bt == null || bt.isBlank()) {

					return "BIOMETRIC";
				}

				String value = bt.toUpperCase();

				if (value.contains("FMR") || value.contains("FINGER")) {

					return "FMR";
				}

				if (value.contains("FIR") || value.contains("IRIS")) {

					return "IRIS";
				}

				if (value.contains("FACE")) {

					return "FACE";
				}

				return "BIOMETRIC";
			}

			/*
			 * Demographic
			 */
			if (avgSuccess_isYes(request.getPi()) || avgSuccess_isYes(request.getPa())
					|| avgSuccess_isYes(request.getPfa())) {

				return "DEMOGRAPHIC";
			}
		}

		/*
		 * Fallback to auth_res.
		 */
		if (response.getAuthMode() != null) {

			String mode = response.getAuthMode().toUpperCase();

			if (mode.equals("KYC")) {

				return "EKYC";
			}

			if (mode.equals("OTP")) {

				return "OTP";
			}

			return mode;
		}

		return "UNKNOWN";
	}

	private String avgSuccess_normalizeFilterAuthMode(String value) {

		String mode = value.trim().toUpperCase();

		if ("FINGERPRINT".equals(mode)) {
			return "FMR";
		}

		if ("BIOMETRIC".equals(mode)) {
			return "FMR";
		}

		if ("E-KYC".equals(mode)) {
			return "EKYC";
		}

		if ("EKYC-BIO".equals(mode)) {
			return "BIOMETRIC";
		}

		return mode;
	}

	/*
	 * ========================================================= SUB-AUA
	 * =========================================================
	 */

	private String avgSuccess_determineSubAua(

			AuthMisReportResEntity response,

			AuthMisReportReqEntity request) {

		/*
		 * Current auth_req/auth_res schema does not contain a dedicated Sub-AUA code
		 * such as RYPCOLL01.
		 *
		 * Therefore use client_id as the currently available identifier.
		 */
		if (response.getClientId() != null && !response.getClientId().isBlank()) {

			return response.getClientId();
		}

		if (request != null && request.getClientId() != null) {

			return request.getClientId();
		}

		return "UNKNOWN";
	}

	/*
	 * ========================================================= RESPONSE TIME
	 * =========================================================
	 */

	private double avgSuccess_parseResponseTime(String value) {

		if (value == null || value.isBlank()) {

			return -1;
		}

		try {

			return Double.parseDouble(value);

		} catch (NumberFormatException e) {

			return -1;
		}
	}

	private boolean avgSuccess_isYes(String value) {

		return value != null && value.equalsIgnoreCase("Y");
	}

	private boolean avgSuccess_hasValue(String value) {

		return value != null && !value.isBlank();
	}

	private double avgSuccess_round(double value) {

		return Math.round(value * 100.0) / 100.0;
	}

	/*
	 * ========================================================= INTERNAL RECORDS
	 * =========================================================
	 */

	private record avgSuccess_SuccessfulTransaction(

			LocalDateTime dateTime,

			String subAuaCode,

			String authMode,

			double responseTime

	) {
	}

	private record avgSuccess_GroupKey(

			LocalDate date,

			String subAuaCode,

			String authMode

	) {
	}

	// ==================== AVGERROR REPORT LOGIC ====================
	@Override
	public AvgResponseTimeErrorResponse getReport(

			LocalDate fromDate, LocalDate toDate, String subAuaCode, String authMode, String errorCode, int page,
			int size) {

		if (page < 0) {
			page = 0;
		}

		if (size <= 0) {
			size = 10;
		}

		if (size > 100) {
			size = 100;
		}

		/*
		 * DATE RANGE
		 */
		LocalDateTime from = fromDate.atStartOfDay();

		LocalDateTime to = toDate.plusDays(1).atStartOfDay();

		/*
		 * BASE SPECIFICATION
		 */
		Specification<AuthMisReportResEntity> specification =

				AuthMisReportSpecification.resDateBetween(from, to);

		/*
		 * ONLY ERROR TRANSACTIONS
		 */
		specification =

				specification.and(

						AuthMisReportSpecification.resErrorTransactions());

		/*
		 * SUB-AUA FILTER
		 */
		if (avgError_hasValue(subAuaCode)) {

			specification =

					specification.and(

							AuthMisReportSpecification.resClientId(subAuaCode));
		}

		/*
		 * ERROR CODE FILTER
		 */
		if (avgError_hasValue(errorCode)) {

			specification =

					specification.and(

							AuthMisReportSpecification.resErrorCode(errorCode));
		}

		/*
		 * LOAD ERROR RESPONSES
		 */
		List<AuthMisReportResEntity> responses = avgError_loadResponses(specification);

		/*
		 * LOAD REQUEST DATA
		 */
		Map<String, AuthMisReportReqEntity> requestMap = avgError_loadRequests(responses);

		/*
		 * CONVERT DATABASE RECORDS INTO REPORT TRANSACTIONS
		 */
		List<avgError_ErrorTransaction> transactions = new ArrayList<>();

		for (AuthMisReportResEntity response : responses) {

			if (response.getCreationDate() == null) {

				continue;
			}

			/*
			 * AUTH MODE
			 */
			String currentAuthMode =

					avgError_determineAuthMode(

							response,

							requestMap.get(response.getTxn()));

			/*
			 * AUTH MODE FILTER
			 */
			if (avgError_hasValue(authMode)) {

				String filterMode =

						avgError_normalizeAuthMode(authMode);

				if (!currentAuthMode.equalsIgnoreCase(filterMode)) {

					continue;
				}
			}

			/*
			 * ERROR CODE
			 */
			String currentErrorCode =

					response.getErrCode();

			if (!avgError_hasValue(currentErrorCode)) {

				currentErrorCode = "UNKNOWN";
			}

			/*
			 * ERROR DESCRIPTION
			 */
			String description =

					response.getErrInfo();

			if (!avgError_hasValue(description)) {

				description = avgError_getErrorDescription(currentErrorCode);
			}

			/*
			 * RESPONSE TIME
			 */
			double responseTime =

					avgError_parseResponseTime(response.getTimeDif());

			/*
			 * If response time isn't available, don't use it in average.
			 */
			if (responseTime < 0) {

				responseTime = 0;
			}

			/*
			 * SUB-AUA
			 */
			String currentSubAua =

					avgError_determineSubAua(

							response,

							requestMap.get(response.getTxn()));

			transactions.add(

					new avgError_ErrorTransaction(

							response.getCreationDate(),

							currentSubAua,

							currentAuthMode,

							currentErrorCode,

							description,

							responseTime));
		}

		/*
		 * TOTAL ERROR TRANSACTIONS
		 */
		long totalErrors = transactions.size();

		/*
		 * GROUP DATA
		 *
		 * DATE SUB-AUA AUTH MODE ERROR CODE ERROR DESCRIPTION
		 */
		Map<avgError_ErrorGroupKey, List<Double>> groups = new HashMap<>();

		for (avgError_ErrorTransaction transaction : transactions) {

			LocalDate date =

					transaction.dateTime().toLocalDate();

			avgError_ErrorGroupKey key =

					new avgError_ErrorGroupKey(

							date,

							transaction.subAuaCode(),

							transaction.authMode(),

							transaction.errorCode(),

							transaction.errorDescription());

			groups.computeIfAbsent(key, k -> new ArrayList<>()).add(transaction.responseTime());
		}

		/*
		 * BUILD TABLE RECORDS
		 */
		List<AvgResponseTimeErrorRecordDto> allRecords =

				groups.entrySet()

						.stream()

						.map(entry ->

						avgError_buildRecord(

								entry.getKey(),

								entry.getValue(),

								totalErrors))

						.sorted(

								Comparator

										.comparing(AvgResponseTimeErrorRecordDto::date).reversed()

										.thenComparing(AvgResponseTimeErrorRecordDto::subAuaCode)

										.thenComparing(AvgResponseTimeErrorRecordDto::authMode)

										.thenComparing(AvgResponseTimeErrorRecordDto::errorCode))

						.toList();

		/*
		 * SUMMARY
		 */
		AvgResponseTimeErrorSummaryDto summary =

				avgError_buildSummary(

						transactions,

						allRecords);

		/*
		 * PAGINATION
		 */
		int totalRecords = allRecords.size();

		int totalPages =

				totalRecords == 0

						? 0

						: (int) Math.ceil(

								(double) totalRecords / size);

		int start = page * size;

		int end =

				Math.min(

						start + size,

						totalRecords);

		List<AvgResponseTimeErrorRecordDto> pageRecords;

		if (start >= totalRecords) {

			pageRecords = List.of();

		} else {

			pageRecords =

					allRecords.subList(start, end);
		}

		return new AvgResponseTimeErrorResponse(

				summary,

				pageRecords,

				page,

				size,

				totalRecords,

				totalPages);
	}

	/*
	 * ========================================================= LOAD RESPONSES
	 * =========================================================
	 */

	private List<AuthMisReportResEntity> avgError_loadResponses(

			Specification<AuthMisReportResEntity> specification) {

		List<AuthMisReportResEntity> result = new ArrayList<>();

		int pageNumber = 0;

		Page<AuthMisReportResEntity> dbPage;

		do {

			Pageable pageable =

					PageRequest.of(pageNumber, DB_PAGE_SIZE);

			dbPage =

					authResRepository.findAll(specification, pageable);

			result.addAll(dbPage.getContent());

			pageNumber++;

		} while (dbPage.hasNext());

		return result;
	}

	/*
	 * ========================================================= LOAD AUTH REQUESTS
	 * =========================================================
	 */

	private Map<String, AuthMisReportReqEntity> avgError_loadRequests(

			List<AuthMisReportResEntity> responses) {

		Map<String, AuthMisReportReqEntity> result = new HashMap<>();

		List<String> transactionIds =

				responses

						.stream()

						.map(AuthMisReportResEntity::getTxn)

						.filter(

								txn ->

								txn != null && !txn.isBlank())

						.toList();

		if (transactionIds.isEmpty()) {

			return result;
		}

		Specification<AuthMisReportReqEntity> specification =

				(root, query, cb) ->

				root.get("txn").in(transactionIds);

		List<AuthMisReportReqEntity> requests =

				authReqRepository.findAll(specification);

		for (AuthMisReportReqEntity request : requests) {

			result.put(

					request.getTxn(),

					request);
		}

		return result;
	}

	/*
	 * ========================================================= BUILD RECORD
	 * =========================================================
	 */

	private AvgResponseTimeErrorRecordDto avgError_buildRecord(

			avgError_ErrorGroupKey key,

			List<Double> responseTimes,

			long totalErrors) {

		long count = responseTimes.size();

		double sum =

				responseTimes

						.stream()

						.mapToDouble(Double::doubleValue)

						.sum();

		double average =

				count == 0

						? 0

						: sum / count;

		double percentage =

				totalErrors == 0

						? 0

						: ((double) count / totalErrors) * 100.0;

		return new AvgResponseTimeErrorRecordDto(

				key.date().toString(),

				key.subAuaCode(),

				key.authMode(),

				key.errorCode(),

				key.errorDescription(),

				count,

				avgError_round(average),

				avgError_round(percentage));
	}

	/*
	 * ========================================================= SUMMARY
	 * =========================================================
	 */

	private AvgResponseTimeErrorSummaryDto avgError_buildSummary(

			List<avgError_ErrorTransaction> transactions,

			List<AvgResponseTimeErrorRecordDto> records) {

		long totalErrors = transactions.size();

		double totalResponseTime =

				transactions

						.stream()

						.mapToDouble(avgError_ErrorTransaction::responseTime)

						.sum();

		double weightedAverage =

				totalErrors == 0

						? 0

						: totalResponseTime / totalErrors;

		/*
		 * TOP ERROR CODE
		 */
		Map<String, Long> errorCounts =

				transactions

						.stream()

						.collect(

								java.util.stream.Collectors.groupingBy(

										avgError_ErrorTransaction::errorCode,

										java.util.stream.Collectors.counting()));

		String topErrorCode =

				errorCounts

						.entrySet()

						.stream()

						.max(Map.Entry.comparingByValue())

						.map(Map.Entry::getKey)

						.orElse("");

		String topErrorDescription =

				transactions

						.stream()

						.filter(

								t -> t.errorCode().equals(topErrorCode))

						.map(avgError_ErrorTransaction::errorDescription)

						.filter(this::avgError_hasValue)

						.findFirst()

						.orElse(avgError_getErrorDescription(topErrorCode));

		return new AvgResponseTimeErrorSummaryDto(

				totalErrors,

				avgError_round(weightedAverage),

				topErrorCode,

				topErrorDescription,

				records.size());
	}

	/*
	 * ========================================================= AUTH MODE
	 * =========================================================
	 */

	private String avgError_determineAuthMode(

			AuthMisReportResEntity response,

			AuthMisReportReqEntity request) {

		if (request != null) {

			/*
			 * OTP
			 */
			if (avgError_isYes(request.getOtp())) {

				return "OTP";
			}

			/*
			 * BIOMETRIC
			 */
			if (avgError_isYes(request.getBio())) {

				String bt = request.getBt();

				if (bt == null || bt.isBlank()) {

					return "BIOMETRIC";
				}

				String mode = bt.toUpperCase();

				if (mode.contains("FMR") || mode.contains("FINGER")) {

					return "FMR";
				}

				if (mode.contains("FIR") || mode.contains("IRIS")) {

					return "IRIS";
				}

				if (mode.contains("FACE")) {

					return "FACE";
				}

				return "BIOMETRIC";
			}

			/*
			 * DEMOGRAPHIC
			 */
			if (avgError_isYes(request.getPi()) || avgError_isYes(request.getPa())
					|| avgError_isYes(request.getPfa())) {

				return "DEMOGRAPHIC";
			}
		}

		/*
		 * FALLBACK
		 */
		if (response.getAuthMode() != null) {

			String mode =

					response.getAuthMode().toUpperCase();

			if ("KYC".equals(mode)) {

				return "EKYC";
			}

			return mode;
		}

		return "UNKNOWN";
	}

	/*
	 * ========================================================= SUB-AUA
	 * =========================================================
	 */

	private String avgError_determineSubAua(

			AuthMisReportResEntity response,

			AuthMisReportReqEntity request) {

		if (response.getClientId() != null && !response.getClientId().isBlank()) {

			return response.getClientId();
		}

		if (request != null && request.getClientId() != null && !request.getClientId().isBlank()) {

			return request.getClientId();
		}

		return "UNKNOWN";
	}

	/*
	 * ========================================================= ERROR DESCRIPTION
	 * =========================================================
	 */

	private String avgError_getErrorDescription(

			String errorCode) {

		if (errorCode == null) {

			return "Unknown Error";
		}

		return switch (errorCode) {

		case "200" -> "Invalid / Missing Biometric data";

		case "300" -> "Invalid / Expired OTP";

		case "400" -> "Invalid demographic data";

		case "500" -> "Internal Server Error";

		case "580" -> "Invalid digital signature (DSC)";

		case "502" -> "Gateway timeout";

		default -> "UIDAI Error Code " + errorCode;
		};
	}

	/*
	 * ========================================================= AUTH MODE
	 * NORMALIZATION =========================================================
	 */

	private String avgError_normalizeAuthMode(

			String authMode) {

		String mode = authMode.trim().toUpperCase();

		if ("BIOMETRIC".equals(mode)) {

			return "FMR";
		}

		if ("FINGERPRINT".equals(mode)) {

			return "FMR";
		}

		if ("E-KYC".equals(mode)) {

			return "EKYC";
		}

		return mode;
	}

	/*
	 * ========================================================= RESPONSE TIME
	 * =========================================================
	 */

	private double avgError_parseResponseTime(

			String value) {

		if (!avgError_hasValue(value)) {

			return -1;
		}

		try {

			return Double.parseDouble(value);

		} catch (NumberFormatException e) {

			return -1;
		}
	}

	private boolean avgError_isYes(String value) {

		return value != null && value.equalsIgnoreCase("Y");
	}

	private boolean avgError_hasValue(String value) {

		return value != null && !value.isBlank();
	}

	private double avgError_round(double value) {

		return Math.round(value * 100.0) / 100.0;
	}

	/*
	 * ========================================================= INTERNAL RECORDS
	 * =========================================================
	 */

	private record avgError_ErrorTransaction(

			LocalDateTime dateTime,

			String subAuaCode,

			String authMode,

			String errorCode,

			String errorDescription,

			double responseTime

	) {
	}

	private record avgError_ErrorGroupKey(

			LocalDate date,

			String subAuaCode,

			String authMode,

			String errorCode,

			String errorDescription

	) {
	}

	// ==================== MINUTE REPORT LOGIC ====================
	@Override
	public MinuteWiseTransactionResponse getMinuteWiseTransactions(

			LocalDate date,

			LocalTime fromTime,

			LocalTime toTime,

			String service) {

		/*
		 * Default:
		 *
		 * From = 00:00 To = 24:00
		 */
		if (fromTime == null) {

			fromTime = LocalTime.MIN;
		}

		if (toTime == null) {

			toTime = LocalTime.MAX;
		}

		LocalDateTime fromDateTime = LocalDateTime.of(date, fromTime);

		LocalDateTime toDateTime = LocalDateTime.of(date, toTime);

		/*
		 * If same time is supplied, consider complete minute.
		 */
		if (!toDateTime.isAfter(fromDateTime)) {

			toDateTime = toDateTime.plusMinutes(1);
		}

		/*
		 * Base JPA Specification.
		 */
		Specification<AuthMisReportResEntity> specification =

				AuthMisReportSpecification.resDateBetween(fromDateTime, toDateTime);

		/*
		 * Load response records page by page.
		 */
		List<AuthMisReportResEntity> responses = minute_loadResponses(specification);

		/*
		 * Get transaction IDs.
		 */
		List<String> transactionIds = responses.stream()

				.map(AuthMisReportResEntity::getTxn)

				.filter(txn -> txn != null && !txn.isBlank())

				.toList();

		/*
		 * Load corresponding requests.
		 */
		Map<String, AuthMisReportReqEntity> requestMap = new HashMap<>();

		if (!transactionIds.isEmpty()) {

			Specification<AuthMisReportReqEntity> requestSpecification = (root, query, cb) ->

			root.get("txn").in(transactionIds);

			List<AuthMisReportReqEntity> requests = authReqRepository.findAll(requestSpecification);

			for (AuthMisReportReqEntity request : requests) {

				requestMap.put(request.getTxn(), request);
			}
		}

		/*
		 * Filter service.
		 */
		List<AuthMisReportResEntity> filteredResponses = responses.stream()

				.filter(response -> {

					if (!minute_hasValue(service)) {

						return true;
					}

					AuthMisReportReqEntity request = requestMap.get(response.getTxn());

					String currentService = minute_determineService(response, request);

					return currentService.equalsIgnoreCase(service);
				})

				.toList();

		/*
		 * Create minute-wise groups.
		 */
		Map<minute_MinuteKey, List<AuthMisReportResEntity>> grouped = new HashMap<>();

		for (AuthMisReportResEntity response : filteredResponses) {

			if (response.getCreationDate() == null) {

				continue;
			}

			LocalDateTime minute = response.getCreationDate().truncatedTo(ChronoUnit.MINUTES);

			AuthMisReportReqEntity request = requestMap.get(response.getTxn());

			String currentService = minute_determineService(response, request);

			String subAuaCode = minute_determineSubAuaCode(response, request);

			minute_MinuteKey key = new minute_MinuteKey(minute, currentService, subAuaCode);

			grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(response);
		}

		/*
		 * Convert groups to response DTO.
		 */
		List<MinuteWiseTransactionDto> records = grouped.entrySet()

				.stream()

				.sorted(Map.Entry.comparingByKey(Comparator.comparing(minute_MinuteKey::dateTime)))

				.map(entry ->

				minute_buildMinuteDto(entry.getKey(), entry.getValue())

				)

				.toList();

		/*
		 * Summary.
		 */
		MinuteWiseSummaryDto summary = minute_buildSummary(filteredResponses);

		return new MinuteWiseTransactionResponse(

				summary,

				records,

				records.size());
	}

	private List<AuthMisReportResEntity> minute_loadResponses(

			Specification<AuthMisReportResEntity> specification) {

		List<AuthMisReportResEntity> result = new ArrayList<>();

		int pageNumber = 0;

		Page<AuthMisReportResEntity> page;

		do {

			Pageable pageable = PageRequest.of(pageNumber, DB_PAGE_SIZE);

			page = authResRepository.findAll(specification, pageable);

			result.addAll(page.getContent());

			pageNumber++;

		} while (page.hasNext());

		return result;
	}

	private MinuteWiseTransactionDto minute_buildMinuteDto(

			minute_MinuteKey key,

			List<AuthMisReportResEntity> transactions) {

		long total = transactions.size();

		long success = transactions.stream()

				.filter(this::minute_isSuccessful)

				.count();

		long errors = transactions.stream()

				.filter(response -> !minute_isSuccessful(response))

				.count();

		double successRate = minute_percentage(success, total);

		double averageResponseTime = transactions.stream()

				.map(AuthMisReportResEntity::getTimeDif)

				.mapToDouble(this::minute_parseResponseTime)

				.filter(value -> value >= 0)

				.average()

				.orElse(0.0);

		return new MinuteWiseTransactionDto(

				key.dateTime().toLocalDate().toString(),

				String.format("%02d:%02d", key.dateTime().getHour(), key.dateTime().getMinute()),

				key.service(),

				key.subAuaCode(),

				total,

				success,

				errors,

				successRate,

				averageResponseTime);
	}

	private MinuteWiseSummaryDto minute_buildSummary(

			List<AuthMisReportResEntity> responses) {

		long total = responses.size();

		long success = responses.stream()

				.filter(this::minute_isSuccessful)

				.count();

		long errors = total - success;

		double averageResponseTime = responses.stream()

				.map(AuthMisReportResEntity::getTimeDif)

				.mapToDouble(this::minute_parseResponseTime)

				.filter(value -> value >= 0)

				.average()

				.orElse(0.0);

		return new MinuteWiseSummaryDto(

				total,

				success,

				errors,

				averageResponseTime);
	}

	/*
	 * Determine service from request.
	 */
	private String minute_determineService(

			AuthMisReportResEntity response,

			AuthMisReportReqEntity request) {

		if (request == null) {

			return minute_normalizeAuthMode(response.getAuthMode());
		}

		/*
		 * OTP
		 */
		if (minute_isYes(request.getOtp())) {

			return "OTP";
		}

		/*
		 * Biometric
		 */
		if (minute_isYes(request.getBio())) {

			String bt = request.getBt();

			if (bt == null || bt.isBlank()) {

				return "Biometric";
			}

			String value = bt.toUpperCase();

			if (value.contains("FMR") || value.contains("FINGER")) {

				return "FMR Auth";
			}

			if (value.contains("FIR") || value.contains("IRIS")) {

				return "Iris Auth";
			}

			if (value.contains("FACE")) {

				return "Face Auth";
			}

			return "Biometric";
		}

		/*
		 * Demographic.
		 */
		if (minute_isYes(request.getPi()) || minute_isYes(request.getPa()) || minute_isYes(request.getPfa())) {

			return "Demographic";
		}

		return minute_normalizeAuthMode(response.getAuthMode());
	}

	private String minute_determineSubAuaCode(

			AuthMisReportResEntity response,

			AuthMisReportReqEntity request) {

		/*
		 * Current SQL does not have a dedicated Sub-AUA code field.
		 *
		 * client_id is the closest available identifier.
		 */
		if (response.getClientId() != null) {

			return response.getClientId();
		}

		if (request != null && request.getClientId() != null) {

			return request.getClientId();
		}

		return "UNKNOWN";
	}

	private String minute_normalizeAuthMode(String authMode) {

		if (authMode == null || authMode.isBlank()) {

			return "UNKNOWN";
		}

		if ("Otp".equalsIgnoreCase(authMode)) {

			return "OTP";
		}

		if ("Auth".equalsIgnoreCase(authMode)) {

			return "AUTH";
		}

		if ("Kyc".equalsIgnoreCase(authMode)) {

			return "EKYC";
		}

		return authMode;
	}

	private boolean minute_isSuccessful(AuthMisReportResEntity response) {

		return response.getRet() != null

				&& response.getRet().equalsIgnoreCase("Y");
	}

	private boolean minute_isYes(String value) {

		return value != null && value.equalsIgnoreCase("Y");
	}

	private double minute_parseResponseTime(String value) {

		if (value == null || value.isBlank()) {

			return -1;
		}

		try {

			return Double.parseDouble(value);

		} catch (NumberFormatException ignored) {

		}

		return -1;
	}

	private double minute_percentage(

			long value,

			long total) {

		if (total == 0) {

			return 0.0;
		}

		return Math.round(

				((double) value / total) * 10000.0

		) / 100.0;
	}

	private boolean minute_hasValue(String value) {

		return value != null && !value.isBlank();
	}

	private record minute_MinuteKey(

			LocalDateTime dateTime, String service, String subAuaCode

	) {
	}

	// ==================== ODD REPORT LOGIC ====================
	@Override
	public OddTimeTransactionResponse getOddTimeTransactions(String fromDate, String toDate, String fromTime,
			String toTime, String authMode, String result, int page, int size) {

		LocalDate startDate = LocalDate.parse(fromDate, ODD_DATE_FORMAT);

		LocalDate endDate = LocalDate.parse(toDate, ODD_DATE_FORMAT);

		LocalDateTime from = startDate.atStartOfDay();

		LocalDateTime to = endDate.plusDays(1).atStartOfDay();

		/*
		 * -------------------------------------------------- GET REQUEST DATA
		 * --------------------------------------------------
		 */

		Specification<AuthMisReportReqEntity> specification = AuthMisReportSpecification.reqDateBetween(from, to)
				.and(AuthMisReportSpecification.reqAuthMode(authMode));

		List<AuthMisReportReqEntity> requests = authReqRepository.findAll(specification);

		/*
		 * -------------------------------------------------- GET RESPONSE DATA
		 * --------------------------------------------------
		 */

		Specification<AuthMisReportResEntity> responseSpecification = (root, query, cb) -> cb.and(
				cb.greaterThanOrEqualTo(root.get("creationDate"), from), cb.lessThan(root.get("creationDate"), to));

		List<AuthMisReportResEntity> responses = authResRepository.findAll(responseSpecification);

		/*
		 * -------------------------------------------------- MAP RESPONSE BY TXN
		 * --------------------------------------------------
		 */

		Map<String, AuthMisReportResEntity> responseMap = new HashMap<>();

		for (AuthMisReportResEntity response : responses) {

			if (response.getTxn() != null) {

				responseMap.put(response.getTxn(), response);
			}
		}

		/*
		 * -------------------------------------------------- TIME FILTER
		 * --------------------------------------------------
		 */

		LocalTime startTime = odd_parseTime(fromTime, LocalTime.of(23, 0));

		LocalTime endTime = odd_parseTime(toTime, LocalTime.of(5, 0));

		/*
		 * -------------------------------------------------- BUILD RECORDS
		 * --------------------------------------------------
		 */

		List<OddTimeTransactionRecordDto> allRecords = new ArrayList<>();

		long serialNo = 1;

		for (AuthMisReportReqEntity request : requests) {

			if (request.getReqTs() == null) {
				continue;
			}

			LocalTime transactionTime = request.getReqTs().toLocalTime();

			/*
			 * ---------------------------------------------- ODD TIME CHECK
			 *
			 * Default: 23:00 -> 05:00
			 *
			 * This crosses midnight. ----------------------------------------------
			 */

			if (!odd_isWithinOddTime(transactionTime, startTime, endTime)) {

				continue;
			}

			AuthMisReportResEntity response = responseMap.get(request.getTxn());

			/*
			 * ---------------------------------------------- RESULT
			 * ----------------------------------------------
			 */

			String transactionResult = odd_resolveResult(response);

			/*
			 * Result filter
			 */

			if (result != null && !result.isBlank() && !result.equalsIgnoreCase(transactionResult)) {

				continue;
			}

			/*
			 * ---------------------------------------------- AUTH MODE
			 * ----------------------------------------------
			 */

			String mode = odd_resolveAuthMode(request);

			/*
			 * ---------------------------------------------- DTO
			 * ----------------------------------------------
			 */

			OddTimeTransactionRecordDto dto = new OddTimeTransactionRecordDto();

			dto.setSno(serialNo++);

			dto.setTransactionId(request.getTxn());

			/*
			 * Actual Aadhaar is not available in AuthReq/AuthRes.
			 */
			dto.setMaskedAadhaar(null);

			dto.setSubAua(request.getSa());

			dto.setAuthMode(mode);

			dto.setDate(request.getReqTs().format(ODD_DISPLAY_DATE));

			dto.setTime(request.getReqTs().format(ODD_DISPLAY_TIME));

			dto.setResult(transactionResult);

			/*
			 * ---------------------------------------------- ERROR CODE
			 * ----------------------------------------------
			 */

			if (response != null) {

				dto.setErrorCode(odd_emptyToNull(response.getErr()));

			} else {

				dto.setErrorCode(null);
			}

			/*
			 * ---------------------------------------------- IP
			 * ----------------------------------------------
			 */

			dto.setIpAddress(request.getIpAddr());

			/*
			 * ---------------------------------------------- DEVICE ID
			 *
			 * Using TID as device identifier.
			 * ----------------------------------------------
			 */

			dto.setDeviceId(odd_firstAvailable(request.getTid(), request.getDpid(), request.getMi()));

			allRecords.add(dto);
		}

		/*
		 * -------------------------------------------------- SORT BY DATE/TIME
		 * --------------------------------------------------
		 */

		allRecords.sort((a, b) -> {

			String dateA = a.getDate() + " " + a.getTime();

			String dateB = b.getDate() + " " + b.getTime();

			return dateA.compareTo(dateB);
		});

		/*
		 * -------------------------------------------------- SUMMARY
		 * --------------------------------------------------
		 */

		long totalOddTime = allRecords.size();

		long failures = allRecords.stream().filter(x -> "n".equalsIgnoreCase(x.getResult())).count();

		/*
		 * Flagged:
		 *
		 * Failed odd-time transactions are considered flagged.
		 */

		long flagged = allRecords.stream().filter(x -> "n".equalsIgnoreCase(x.getResult())).count();

		long success = allRecords.stream().filter(x -> "y".equalsIgnoreCase(x.getResult())).count();

		OddTimeTransactionSummaryDto summary = new OddTimeTransactionSummaryDto();

		summary.setOddTimeTxns(totalOddTime);

		summary.setFailures(failures);

		summary.setFlagged(flagged);

		summary.setSuccessAtOddHours(success);

		/*
		 * -------------------------------------------------- PAGINATION
		 * --------------------------------------------------
		 */

		int totalRecords = allRecords.size();

		int safeSize = size <= 0 ? 10 : size;

		int safePage = Math.max(page, 0);

		int totalPages = totalRecords == 0 ? 0 : (int) Math.ceil((double) totalRecords / safeSize);

		int start = safePage * safeSize;

		int end = Math.min(start + safeSize, totalRecords);

		List<OddTimeTransactionRecordDto> pageRecords;

		if (start >= totalRecords) {

			pageRecords = new ArrayList<>();

		} else {

			pageRecords = allRecords.subList(start, end);
		}

		/*
		 * -------------------------------------------------- FINAL RESPONSE
		 * --------------------------------------------------
		 */

		OddTimeTransactionResponse finalResponse = new OddTimeTransactionResponse();

		finalResponse.setSummary(summary);

		finalResponse.setRecords(pageRecords);

		finalResponse.setPage(safePage);

		finalResponse.setSize(safeSize);

		finalResponse.setTotalRecords(totalRecords);

		finalResponse.setTotalPages(totalPages);

		return finalResponse;
	}

	/*
	 * ====================================================== ODD TIME
	 * ======================================================
	 */

	private boolean odd_isWithinOddTime(LocalTime transactionTime, LocalTime start, LocalTime end) {

		/*
		 * 23:00 -> 05:00
		 *
		 * Since start > end, range crosses midnight.
		 */

		if (start.isAfter(end)) {

			return !transactionTime.isBefore(start) || transactionTime.isBefore(end);
		}

		return !transactionTime.isBefore(start) && transactionTime.isBefore(end);
	}

	/*
	 * ====================================================== RESULT
	 * ======================================================
	 */

	private String odd_resolveResult(AuthMisReportResEntity response) {

		if (response == null) {
			return "n";
		}

		String ret = response.getRet();

		/*
		 * UIDAI style:
		 *
		 * ret = y => success ret = n => failure
		 */

		if ("y".equalsIgnoreCase(ret)) {
			return "y";
		}

		if ("n".equalsIgnoreCase(ret)) {
			return "n";
		}

		/*
		 * If err exists, consider failure.
		 */

		if (response.getErr() != null && !response.getErr().isBlank()) {

			return "n";
		}

		return "n";
	}

	/*
	 * ====================================================== AUTH MODE
	 * ======================================================
	 */

	private String odd_resolveAuthMode(AuthMisReportReqEntity request) {

		if (odd_hasValue(request.getBio())) {
			return "FMR";
		}

		if (odd_hasValue(request.getBt())) {
			return "FMR";
		}

		if (odd_hasValue(request.getOtp())) {
			return "OTP";
		}

		if (odd_hasValue(request.getPi())) {
			return "PI";
		}

		if (odd_hasValue(request.getPa())) {
			return "PA";
		}

		if (odd_hasValue(request.getPfa())) {
			return "PFA";
		}

		if (request.getAuthMode() != null && !request.getAuthMode().isBlank()) {

			return request.getAuthMode();
		}

		return "UNKNOWN";
	}

	private boolean odd_hasValue(String value) {

		return value != null && !value.isBlank() && !"N".equalsIgnoreCase(value) && !"0".equals(value);
	}

	/*
	 * ====================================================== TIME
	 * ======================================================
	 */

	private LocalTime odd_parseTime(String value, LocalTime defaultValue) {

		if (value == null || value.isBlank()) {
			return defaultValue;
		}

		try {

			return LocalTime.parse(value, ODD_TIME_FORMAT);

		} catch (Exception e) {

			return defaultValue;
		}
	}

	/*
	 * ====================================================== NULL / EMPTY
	 * ======================================================
	 */

	private String odd_emptyToNull(String value) {

		if (value == null || value.isBlank()) {
			return null;
		}

		return value;
	}

	/*
	 * ====================================================== DEVICE
	 * ======================================================
	 */

	private String odd_firstAvailable(String... values) {

		for (String value : values) {

			if (value != null && !value.isBlank()) {

				return value;
			}
		}

		return null;
	}

	// ==================== SUBAUA REPORT LOGIC ====================
	@Override
	public SubAuaTransactionResponse getSubAuaTransactions(

			LocalDate fromDate,

			LocalDate toDate,

			String clientId,

			int page,

			int size) {

		if (page < 0) {
			page = 0;
		}

		if (size <= 0) {
			size = 10;
		}

		if (size > 100) {
			size = 100;
		}

		LocalDateTime from = fromDate.atStartOfDay();

		LocalDateTime to = toDate.plusDays(1).atStartOfDay();

		Specification<AuthMisReportResEntity> specification = AuthMisReportSpecification.resDateBetween(from, to);

		if (subAua_hasValue(clientId)) {

			specification = specification.and(AuthMisReportSpecification.resClientId(clientId));
		}

		/*
		 * Client master data
		 */
		List<AuthMisClientMasterEntity> clients = clientMasterRepository.findAll();

		Map<String, AuthMisClientMasterEntity> clientMap = new HashMap<>();

		for (AuthMisClientMasterEntity client : clients) {

			clientMap.put(client.getClientId(), client);
		}

		/*
		 * Aggregate transaction data.
		 */
		Map<String, subAua_TransactionCounter> counters = new HashMap<>();

		int dbPage = 0;

		Page<AuthMisReportResEntity> responsePage;

		do {

			Pageable pageable = PageRequest.of(dbPage, DB_PAGE_SIZE);

			responsePage = authResRepository.findAll(specification, pageable);

			for (AuthMisReportResEntity response : responsePage.getContent()) {

				String client = response.getClientId();

				if (client == null || client.isBlank()) {

					client = "UNKNOWN";
				}

				subAua_TransactionCounter counter = counters.computeIfAbsent(client,
						key -> new subAua_TransactionCounter());

				counter.total++;

				if (subAua_isSuccessful(response)) {

					counter.successful++;

				} else {

					counter.failed++;
				}

				/*
				 * Timeout rule is not available in current database schema.
				 */
			}

			dbPage++;

		} while (responsePage.hasNext());

		/*
		 * Convert aggregation result into DTO.
		 */
		List<SubAuaTransactionDto> allRecords = new ArrayList<>();

		long serialNo = 1;

		for (Map.Entry<String, subAua_TransactionCounter> entry : counters.entrySet()) {

			String currentClientId = entry.getKey();

			subAua_TransactionCounter counter = entry.getValue();

			AuthMisClientMasterEntity client = clientMap.get(currentClientId);

			String clientName = client != null ? client.getClientName() : "Unknown";

			double successRate = subAua_calculatePercentage(counter.successful, counter.total);

			SubAuaTransactionDto dto = new SubAuaTransactionDto(

					serialNo++,

					currentClientId,

					clientName,

					null,

					null,

					null,

					counter.total,

					counter.successful,

					counter.failed,

					counter.timeout,

					successRate);

			allRecords.add(dto);
		}

		/*
		 * Highest transaction first.
		 */
		allRecords.sort(Comparator.comparingLong(SubAuaTransactionDto::totalTransactions).reversed());

		/*
		 * Application-level pagination.
		 */
		int totalRecords = allRecords.size();

		int totalPages = totalRecords == 0 ? 0 : (int) Math.ceil((double) totalRecords / size);

		int start = page * size;

		int end = Math.min(start + size, totalRecords);

		List<SubAuaTransactionDto> pageRecords;

		if (start >= totalRecords) {

			pageRecords = List.of();

		} else {

			pageRecords = allRecords.subList(start, end);
		}

		return new SubAuaTransactionResponse(

				pageRecords,

				page,

				size,

				totalRecords,

				totalPages);
	}

	private boolean subAua_isSuccessful(AuthMisReportResEntity response) {

		return response.getRet() != null

				&& response.getRet().equalsIgnoreCase("Y");
	}

	private double subAua_calculatePercentage(

			long value,

			long total) {

		if (total == 0) {
			return 0.0;
		}

		return Math.round(((double) value / total) * 10000) / 100.0;
	}

	private boolean subAua_hasValue(String value) {

		return value != null && !value.isBlank();
	}

	private static class subAua_TransactionCounter {

		private long total;
		private long successful;
		private long failed;
		private long timeout;
	}

	// ==================== SUSPECTED REPORT LOGIC ====================
	@Override
	public SuspectedAadhaarResponse getSuspectedAadhaarReport(String fromDate, String toDate, String risk,
			String status, String auaCode, String subAua, String authMode, int page, int size) {

		LocalDateTime from = LocalDate.parse(fromDate, SUSPECTED_INPUT_DATE).atStartOfDay();

		LocalDateTime to = LocalDate.parse(toDate, SUSPECTED_INPUT_DATE).plusDays(1).atStartOfDay();

		/*
		 * ---------------------------------------------------- AUTH REQUESTS
		 * ----------------------------------------------------
		 */

		Specification<AuthMisReportReqEntity> requestSpec = AuthMisReportSpecification.reqDateBetween(from, to)
				.and(AuthMisReportSpecification.reqAc(auaCode)).and(AuthMisReportSpecification.reqSa(subAua))
				.and(AuthMisReportSpecification.reqAuthMode(authMode));

		List<AuthMisReportReqEntity> requests = authReqRepository.findAll(requestSpec);

		/*
		 * ---------------------------------------------------- AUTH RESPONSES
		 * ----------------------------------------------------
		 */

		Specification<AuthMisReportResEntity> responseSpec = AuthMisReportSpecification.resDateBetween(from, to)
				.and(AuthMisReportSpecification.resErrorTransactions());

		List<AuthMisReportResEntity> errorResponses = authResRepository.findAll(responseSpec);

		/*
		 * ---------------------------------------------------- ERROR TXN MAP
		 * ----------------------------------------------------
		 */

		Set<String> errorTransactions = new HashSet<>();

		for (AuthMisReportResEntity response : errorResponses) {

			if (response.getTxn() != null) {
				errorTransactions.add(response.getTxn());
			}
		}

		/*
		 * ---------------------------------------------------- GROUP REQUESTS
		 *
		 * AUA + SUB-AUA + AUTH MODE
		 * ----------------------------------------------------
		 */

		Map<String, suspected_GroupData> groups = new HashMap<>();

		for (AuthMisReportReqEntity request : requests) {

			if (request.getAc() == null || request.getSa() == null) {
				continue;
			}

			String mode = suspected_resolveAuthMode(request);

			String key = suspected_safe(request.getAc()) + "|" + suspected_safe(request.getSa()) + "|"
					+ suspected_safe(mode);

			suspected_GroupData group = groups.computeIfAbsent(key,
					k -> new suspected_GroupData(request.getAc(), request.getSa(), mode));

			group.totalAttempts++;

			/*
			 * DISTINCT IP
			 */
			if (request.getIpAddr() != null && !request.getIpAddr().isBlank()) {

				group.ipAddresses.add(request.getIpAddr());
			}

			/*
			 * FIRST / LAST SEEN
			 */
			if (request.getReqTs() != null) {

				if (group.firstSeen == null || request.getReqTs().isBefore(group.firstSeen)) {

					group.firstSeen = request.getReqTs();
				}

				if (group.lastSeen == null || request.getReqTs().isAfter(group.lastSeen)) {

					group.lastSeen = request.getReqTs();
				}
			}

			/*
			 * FAILED TRANSACTION
			 */
			if (request.getTxn() != null && errorTransactions.contains(request.getTxn())) {

				group.failures++;
			}
		}

		/*
		 * ---------------------------------------------------- CONVERT TO DTO
		 * ----------------------------------------------------
		 */

		List<SuspectedAadhaarRecordDto> allRecords = new ArrayList<>();

		for (suspected_GroupData group : groups.values()) {

			/*
			 * Suspicious activity condition
			 *
			 * At least one: 1. failures >= 5 2. failure percentage >= 30% 3. distinct IP >=
			 * 3
			 */

			double failurePercentage = 0;

			if (group.totalAttempts > 0) {

				failurePercentage = (group.failures * 100.0) / group.totalAttempts;
			}

			boolean suspicious = group.failures >= 5 || failurePercentage >= 30 || group.ipAddresses.size() >= 3;

			if (!suspicious) {
				continue;
			}

			String calculatedRisk = suspected_calculateRisk(group.failures, group.totalAttempts,
					group.ipAddresses.size());

			/*
			 * Risk filter
			 */

			if (risk != null && !risk.isBlank() && !risk.equalsIgnoreCase(calculatedRisk)) {

				continue;
			}

			/*
			 * Currently no separate status table/entity exists in supplied entities.
			 */
			String calculatedStatus = "ACTIVE";

			/*
			 * Status filter
			 */

			if (status != null && !status.isBlank() && !status.equalsIgnoreCase(calculatedStatus)) {

				continue;
			}

			SuspectedAadhaarRecordDto dto = new SuspectedAadhaarRecordDto();

			dto.setAuaCode(group.auaCode);
			dto.setSubAua(group.subAua);
			dto.setAuthMode(group.authMode);

			dto.setFailures24h(group.failures);
			dto.setTotalAttempts24h(group.totalAttempts);

			dto.setDistinctIps(group.ipAddresses.size());

			if (group.firstSeen != null) {

				dto.setFirstSeen(group.firstSeen.format(SUSPECTED_OUTPUT_DATE));
			}

			if (group.lastSeen != null) {

				dto.setLastSeen(group.lastSeen.format(SUSPECTED_OUTPUT_DATE));
			}

			dto.setRisk(calculatedRisk);
			dto.setStatus(calculatedStatus);

			/*
			 * Since no block/status table was supplied, these are UI capabilities only.
			 */
			dto.setCanBlock("ACTIVE".equalsIgnoreCase(calculatedStatus));

			dto.setCanClear("BLOCKED".equalsIgnoreCase(calculatedStatus));

			allRecords.add(dto);
		}

		/*
		 * ---------------------------------------------------- SORT
		 *
		 * Highest risk first, then failures
		 * ----------------------------------------------------
		 */

		allRecords.sort((a, b) -> {

			int riskCompare = Integer.compare(suspected_riskWeight(b.getRisk()), suspected_riskWeight(a.getRisk()));

			if (riskCompare != 0) {
				return riskCompare;
			}

			return Long.compare(b.getFailures24h(), a.getFailures24h());
		});

		/*
		 * ---------------------------------------------------- SUMMARY
		 * ----------------------------------------------------
		 */

		SuspectedAadhaarSummaryDto summary = new SuspectedAadhaarSummaryDto();

		summary.setTotalSuspected(allRecords.size());

		summary.setHighRisk(allRecords.stream().filter(x -> "HIGH".equalsIgnoreCase(x.getRisk())).count());

		summary.setCurrentlyBlocked(allRecords.stream().filter(x -> "BLOCKED".equalsIgnoreCase(x.getStatus())).count());

		summary.setCleared(allRecords.stream().filter(x -> "CLEARED".equalsIgnoreCase(x.getStatus())).count());

		/*
		 * ---------------------------------------------------- PAGINATION
		 * ----------------------------------------------------
		 */

		int totalRecords = allRecords.size();

		int totalPages = size <= 0 ? 0 : (int) Math.ceil((double) totalRecords / size);

		int safePage = Math.max(page, 0);

		int start = safePage * size;

		int end = Math.min(start + size, totalRecords);

		List<SuspectedAadhaarRecordDto> pageRecords;

		if (size <= 0 || start >= totalRecords) {

			pageRecords = new ArrayList<>();

		} else {

			pageRecords = allRecords.subList(start, end);
		}

		/*
		 * ---------------------------------------------------- RESPONSE
		 * ----------------------------------------------------
		 */

		SuspectedAadhaarResponse response = new SuspectedAadhaarResponse();

		response.setSummary(summary);
		response.setRecords(pageRecords);

		response.setPage(safePage);
		response.setSize(size);

		response.setTotalRecords(totalRecords);
		response.setTotalPages(totalPages);

		return response;
	}

	/*
	 * -------------------------------------------------------- AUTH MODE
	 * --------------------------------------------------------
	 */

	private String suspected_resolveAuthMode(AuthMisReportReqEntity request) {

		if (suspected_hasValue(request.getBio())) {
			return "FMR";
		}

		if (suspected_hasValue(request.getOtp())) {
			return "OTP";
		}

		if (suspected_hasValue(request.getBt())) {
			return "FMR";
		}

		if (suspected_hasValue(request.getPi())) {
			return "PI";
		}

		if (suspected_hasValue(request.getPa())) {
			return "PA";
		}

		if (suspected_hasValue(request.getPfa())) {
			return "PFA";
		}

		if (suspected_hasValue(request.getAuthMode())) {
			return request.getAuthMode();
		}

		return "UNKNOWN";
	}

	private boolean suspected_hasValue(String value) {

		return value != null && !value.isBlank() && !"N".equalsIgnoreCase(value) && !"0".equals(value);
	}

	/*
	 * -------------------------------------------------------- RISK CALCULATION
	 * --------------------------------------------------------
	 */

	private String suspected_calculateRisk(long failures, long totalAttempts, long distinctIps) {

		double failurePercentage = 0;

		if (totalAttempts > 0) {

			failurePercentage = (failures * 100.0) / totalAttempts;
		}

		/*
		 * HIGH
		 */
		if (failures >= 20 || failurePercentage >= 60 || distinctIps >= 5) {

			return "HIGH";
		}

		/*
		 * MEDIUM
		 */
		if (failures >= 10 || failurePercentage >= 30 || distinctIps >= 3) {

			return "MEDIUM";
		}

		return "LOW";
	}

	private int suspected_riskWeight(String risk) {

		if ("HIGH".equalsIgnoreCase(risk)) {
			return 3;
		}

		if ("MEDIUM".equalsIgnoreCase(risk)) {
			return 2;
		}

		return 1;
	}

	private String suspected_safe(String value) {

		return value == null ? "" : value;
	}

	/*
	 * -------------------------------------------------------- INTERNAL GROUP
	 * --------------------------------------------------------
	 */

	private static class suspected_GroupData {

		private final String auaCode;
		private final String subAua;
		private final String authMode;

		private long totalAttempts;
		private long failures;

		private final Set<String> ipAddresses = new HashSet<>();

		private LocalDateTime firstSeen;
		private LocalDateTime lastSeen;

		suspected_GroupData(String auaCode, String subAua, String authMode) {

			this.auaCode = auaCode;
			this.subAua = subAua;
			this.authMode = authMode;
		}
	}

	// ==================== DETAIL REPORT LOGIC ====================
	@Override
	public TransactionDetailResponse getTransactionDetails(LocalDate fromDate, LocalDate toDate, String aua,
			String authType, String status, String search, int page, int size) {

		// if (page < 0) {
		// page = 0;
		// }
		//
		// if (size <= 0) {
		// size = 10;
		// }
		//
		// if (size > 100) {
		// size = 100;
		// }

		LocalDateTime from = fromDate.atStartOfDay();
		LocalDateTime to = toDate.plusDays(1).atStartOfDay();

		/*
		 * Base specification
		 */
		Specification<AuthMisReportResEntity> specification = AuthMisReportSpecification.resDateBetween(from, to);

		/*
		 * AUA filter
		 */
		if (detail_hasValue(aua)) {
			specification = specification.and(AuthMisReportSpecification.resClientId(aua));
		}

		/*
		 * Auth Type filter
		 */
		if (detail_hasValue(authType)) {
			specification = specification.and(AuthMisReportSpecification.resAuthType(authType));
		}

		/*
		 * Status filter
		 */
		if (detail_hasValue(status)) {
			switch (status.toUpperCase()) {
			case "SUCCESS":
				specification = specification.and(AuthMisReportSpecification.resSuccess());
				break;
			case "FAILURE":
				specification = specification.and(AuthMisReportSpecification.resFailure());
				break;
			case "TIMEOUT":
				specification = specification.and(AuthMisReportSpecification.resTimeout());
				break;
			default:
				break;
			}
		}

		/*
		 * Search filter
		 *
		 * Current database supports:
		 *
		 * Transaction ID AUA / Client ID
		 *
		 * Aadhaar / Resident Name are not stored in these tables.
		 */
		if (detail_hasValue(search)) {
			specification = specification.and(AuthMisReportSpecification.resSearch(search));
		}

		/*
		 * Summary
		 */
		long total = authResRepository.count(specification);
		long success = authResRepository.count(specification.and(AuthMisReportSpecification.resSuccess()));
		long timeout = authResRepository.count(specification.and(AuthMisReportSpecification.resTimeout()));
		long failure = total - success - timeout;
		TransactionDetailSummaryDto summary = new TransactionDetailSummaryDto(success, failure, timeout, total);

		/*
		 * Pagination
		 */
		Pageable pageable = PageRequest.of(page, size);
		Page<AuthMisReportResEntity> responsePage = authResRepository.findAll(specification, pageable);

		/*
		 * Get transaction IDs of current page.
		 */
		List<String> transactionIds = responsePage.getContent().stream().map(AuthMisReportResEntity::getTxn)
				.filter(txn -> txn != null && !txn.isBlank()).toList();

		/*
		 * Load request records using JPA Specification.
		 */
		Map<String, AuthMisReportReqEntity> requestMap = new HashMap<>();
		if (!transactionIds.isEmpty()) {
			Specification<AuthMisReportReqEntity> requestSpecification = (root, query, cb) -> root.get("txn").in(transactionIds);
			List<AuthMisReportReqEntity> requests = authReqRepository.findAll(requestSpecification);
			for (AuthMisReportReqEntity request : requests) {
				requestMap.put(request.getTxn(), request);
			}
		}

		/*
		 * Convert entities into DTOs.
		 */
		List<TransactionDetailDto> records = responsePage.getContent().stream().map(response -> {
			AuthMisReportReqEntity request = requestMap.get(response.getTxn());
			return detail_mapToDto(response, request, page, size, responsePage.getContent().indexOf(response));
		}).toList();
		return new TransactionDetailResponse(summary, records, responsePage.getNumber(), responsePage.getSize(),
				responsePage.getTotalElements(), responsePage.getTotalPages());
	}

	private TransactionDetailDto detail_mapToDto(AuthMisReportResEntity response, AuthMisReportReqEntity request, int page, int size, int index) {

		long serialNo = ((long) page * size) + index + 1;
		String status = detail_getStatus(response);
		String errorDescription = detail_getErrorDescription(response.getErr());
		String authType = detail_getAuthenticationType(response, request);

		/*
		 * Current DB does not contain:
		 *
		 * Resident Name Plain Aadhaar District Sub-AUA code
		 *
		 * Therefore return null instead of fake data.
		 */
		String residentName = null;
		String maskedAadhaar = null;
		String subAuaCode = null;
		String district = null;
		return new TransactionDetailDto(serialNo, response.getTxn(), response.getClientId(), subAuaCode, residentName,
				maskedAadhaar, authType, status, response.getErr(), errorDescription, district,
				response.getCreationDate(), response.getAsaGateway(), response.getTimeDif());
	}

	private String detail_getStatus(AuthMisReportResEntity response) {
		if (response.getRet() != null && response.getRet().equalsIgnoreCase("Y")) {
			return "SUCCESS";
		}
		if ("502".equals(response.getErr())) {
			return "TIMEOUT";
		}
		return "FAILURE";
	}

	private String detail_getAuthenticationType(AuthMisReportResEntity response, AuthMisReportReqEntity request) {
		if (request == null) {
			return detail_normalizeAuthMode(response.getAuthMode());
		}

		/*
		 * OTP
		 */
		if (detail_isYes(request.getOtp())) {
			return "OTP";
		}

		/*
		 * Biometric
		 */
		if (detail_isYes(request.getBio())) {
			String biometricType = request.getBt();
			if (biometricType == null) {
				return "BIOMETRIC";
			}

			String value = biometricType.toUpperCase();
			if (value.contains("FMR") || value.contains("FINGER")) {
				return "BIOMETRIC";
			}

			if (value.contains("FIR") || value.contains("IRIS")) {
				return "BIOMETRIC";
			}

			if (value.contains("FACE")) {
				return "BIOMETRIC";
			}

			return "BIOMETRIC";
		}

		/*
		 * Demographic
		 */
		if (detail_isYes(request.getPi()) || detail_isYes(request.getPa()) || detail_isYes(request.getPfa())) {
			return "DEMOGRAPHIC";
		}

		/*
		 * KYC / other
		 */
		if ("Kyc".equalsIgnoreCase(response.getAuthMode())) {
			return "EKYC";
		}

		return detail_normalizeAuthMode(response.getAuthMode());
	}

	private String detail_normalizeAuthMode(String authMode) {
		if (authMode == null || authMode.isBlank()) {
			return "UNKNOWN";
		}
		if ("Otp".equalsIgnoreCase(authMode)) {
			return "OTP";
		}
		if ("Auth".equalsIgnoreCase(authMode)) {
			return "AUTH";
		}
		if ("Kyc".equalsIgnoreCase(authMode)) {
			return "EKYC";
		}
		return authMode.toUpperCase();
	}

	private String detail_getErrorDescription(String errorCode) {
		if (errorCode == null || errorCode.isBlank()) {
			return null;
		}
		return switch (errorCode) {
		case "502" -> "Gateway timeout";
		case "320" -> "Biometric mismatch";
		case "565" -> "Invalid OTP";
		case "523" -> "Authentication failed";
		case "400" -> "Bad request";
		case "403" -> "Authentication failed";
		case "569" -> "Authentication error";
		default -> "Authentication error";
		};
	}

	private boolean detail_isYes(String value) {
		return value != null && value.equalsIgnoreCase("Y");
	}

	private boolean detail_hasValue(String value) {
		return value != null && !value.isBlank();
	}

}
