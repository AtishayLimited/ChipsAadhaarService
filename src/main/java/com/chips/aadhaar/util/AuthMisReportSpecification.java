package com.chips.aadhaar.util;



import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import org.springframework.data.jpa.domain.Specification;

import com.chips.aadhaar.entity.AuthMisReportReqEntity;
import com.chips.aadhaar.entity.AuthMisReportResEntity;


/**
 * 
 * Single consolidated specification class for AuthMIS reports.
 *
 * 
 * 
 * IMPORTANT:
 * 
 * - No @Query / JPQL is used.
 * 
 * - All filtering is handled through Spring Data JPA Specification.
 * 
 * - If no date Specification is added by the service,
 * 
 * ALL available records are returned.
 * 
 * - No default/current-date filtering is applied here.
 * 
 * - JPA property names match the AuthMIS entity fields.
 * 
 */

public final class AuthMisReportSpecification {

	private AuthMisReportSpecification() {

	}

	// ============================================================

	// AUTH REQUEST (AuthMisReportReqEntity)

	// ============================================================

	/**
	 * 
	 * Request records between from and to.
	 *
	 * 
	 * 
	 * from = inclusive
	 * 
	 * to = exclusive
	 * 
	 */

	public static Specification<AuthMisReportReqEntity> reqDateBetween(

			LocalDateTime from,

			LocalDateTime to) {

		return (root, query, cb) -> cb.and(

				cb.greaterThanOrEqualTo(

						root.get("reqTs"),

						from

				),

				cb.lessThan(

						root.get("reqTs"),

						to

				)

		);

	}

	/**
	 * 
	 * Request records from the given date/time onwards.
	 *
	 * 
	 * 
	 * Used when only fromDate is provided.
	 * 
	 */

	public static Specification<AuthMisReportReqEntity> reqDateFrom(

			LocalDateTime from) {

		return (root, query, cb) ->

		cb.greaterThanOrEqualTo(

				root.get("reqTs"),

				from

		);

	}

	/**
	 * 
	 * Request records before the given date/time.
	 *
	 * 
	 * 
	 * Used when only toDate is provided.
	 *
	 * 
	 * 
	 * to is exclusive.
	 * 
	 */

	public static Specification<AuthMisReportReqEntity> reqDateTo(

			LocalDateTime to) {

		return (root, query, cb) ->

		cb.lessThan(

				root.get("reqTs"),

				to

		);

	}

	/**
	 * Request date filter. null + null = all records. fromDate is inclusive and
	 * toDate is inclusive for the whole day.
	 */
	public static Specification<AuthMisReportReqEntity> reqDateFilter(LocalDate fromDate, LocalDate toDate) {

		if (fromDate == null && toDate == null) {
			return (root, query, cb) -> cb.conjunction();
		}

		if (fromDate != null && toDate != null) {
			return reqDateBetween(fromDate.atStartOfDay(), toDate.plusDays(1).atStartOfDay());
		}

		if (fromDate != null) {
			return reqDateFrom(fromDate.atStartOfDay());
		}

		return reqDateTo(toDate.plusDays(1).atStartOfDay());
	}

	public static Specification<AuthMisReportReqEntity> reqClientId(

			String clientId) {

		return (root, query, cb) ->

		cb.equal(

				cb.upper(root.get("clientId")),

				clientId.toUpperCase()

		);

	}

	/**
	 * 
	 * Original AuthReqSpecification.authType behaviour:
	 * 
	 * case-insensitive.
	 * 
	 */

	/**
	 * Request records for transaction IDs.
	 */
	public static Specification<AuthMisReportReqEntity> reqTransactionIds(Collection<String> transactionIds) {

		if (transactionIds == null || transactionIds.isEmpty()) {
			return (root, query, cb) -> cb.conjunction();
		}

		return (root, query, cb) -> root.get("txn").in(transactionIds);
	}

	public static Specification<AuthMisReportReqEntity> reqAuthType(

			String authType) {

		return (root, query, cb) ->

		cb.equal(

				cb.upper(root.get("authMode")),

				authType.toUpperCase()

		);

	}

	/**
	 * 
	 * Original Odd/Suspected authMode behaviour:
	 * 
	 * exact value, blank means no filter.
	 * 
	 */

	public static Specification<AuthMisReportReqEntity> reqAuthMode(

			String authMode) {

		return (root, query, cb) -> {

			if (authMode == null || authMode.isBlank()) {

				return cb.conjunction();

			}

			return cb.equal(

					root.get("authMode"),

					authMode

			);

		};

	}

	public static Specification<AuthMisReportReqEntity> reqSubAua(

			String subAua) {

		return (root, query, cb) -> {

			if (subAua == null || subAua.isBlank()) {

				return cb.conjunction();

			}

			return cb.equal(

					root.get("sa"),

					subAua

			);

		};

	}

	public static Specification<AuthMisReportReqEntity> reqAc(

			String ac) {

		return (root, query, cb) -> {

			if (ac == null || ac.isBlank()) {

				return cb.conjunction();

			}

			return cb.equal(

					root.get("ac"),

					ac

			);

		};

	}

	public static Specification<AuthMisReportReqEntity> reqSa(

			String sa) {

		return (root, query, cb) -> {

			if (sa == null || sa.isBlank()) {

				return cb.conjunction();

			}

			return cb.equal(

					root.get("sa"),

					sa

			);

		};

	}

	public static Specification<AuthMisReportReqEntity> reqIpAddress(

			String ipAddress) {

		return (root, query, cb) -> {

			if (ipAddress == null || ipAddress.isBlank()) {

				return cb.conjunction();

			}

			return cb.equal(

					root.get("ipAddr"),

					ipAddress

			);

		};

	}

	// ============================================================

	// AUTH RESPONSE (AuthMisReportResEntity)

	// ============================================================

	/**
	 * 
	 * Response records between from and to.
	 *
	 * 
	 * 
	 * from = inclusive
	 * 
	 * to = exclusive
	 * 
	 */

	public static Specification<AuthMisReportResEntity> resDateBetween(

			LocalDateTime from,

			LocalDateTime to) {

		return (root, query, cb) -> cb.and(

				cb.greaterThanOrEqualTo(

						root.get("creationDate"),

						from

				),

				cb.lessThan(

						root.get("creationDate"),

						to

				)

		);

	}

	/**
	 * 
	 * Response records from the given date/time onwards.
	 *
	 * 
	 * 
	 * Used when only fromDate is provided.
	 * 
	 */

	public static Specification<AuthMisReportResEntity> resDateFrom(

			LocalDateTime from) {

		return (root, query, cb) ->

		cb.greaterThanOrEqualTo(

				root.get("creationDate"),

				from

		);

	}

	/**
	 * 
	 * Response records before the given date/time.
	 *
	 * 
	 * 
	 * Used when only toDate is provided.
	 *
	 * 
	 * 
	 * to is exclusive.
	 * 
	 */

	public static Specification<AuthMisReportResEntity> resDateTo(

			LocalDateTime to) {

		return (root, query, cb) ->

		cb.lessThan(

				root.get("creationDate"),

				to

		);

	}

	/**
	 * Response date filter. null + null = all records. fromDate is inclusive and
	 * toDate is inclusive for the whole day.
	 */
	public static Specification<AuthMisReportResEntity> resDateFilter(LocalDate fromDate, LocalDate toDate) {

		if (fromDate == null && toDate == null) {
			return (root, query, cb) -> cb.conjunction();
		}

		if (fromDate != null && toDate != null) {
			return resDateBetween(fromDate.atStartOfDay(), toDate.plusDays(1).atStartOfDay());
		}

		if (fromDate != null) {
			return resDateFrom(fromDate.atStartOfDay());
		}

		return resDateTo(toDate.plusDays(1).atStartOfDay());
	}

	public static Specification<AuthMisReportResEntity> resClientId(

			String clientId) {

		return (root, query, cb) ->

		cb.equal(

				cb.upper(root.get("clientId")),

				clientId.toUpperCase()

		);

	}

	public static Specification<AuthMisReportResEntity> resAuthType(

			String authType) {

		return (root, query, cb) ->

		cb.equal(

				cb.upper(root.get("authMode")),

				authType.toUpperCase()

		);

	}

	public static Specification<AuthMisReportResEntity> resAsaGateway(

			String asaGateway) {

		return (root, query, cb) ->

		cb.equal(

				cb.upper(root.get("asaGateway")),

				asaGateway.toUpperCase()

		);

	}

	/**
	 * 
	 * Successful transaction:
	 * 
	 * ret = Y
	 * 
	 */

	public static Specification<AuthMisReportResEntity> resSuccess() {

		return (root, query, cb) ->

		cb.equal(

				cb.upper(root.get("ret")),

				"Y"

		);

	}

	/**
	 * 
	 * Error transaction:
	 * 
	 * err exists and is not blank.
	 *
	 * 
	 * 
	 * Entity property:
	 * 
	 * err
	 * 
	 */

	public static Specification<AuthMisReportResEntity> resErrorTransactions() {

		return (root, query, cb) -> cb.and(

				cb.isNotNull(root.get("err")),

				cb.notEqual(root.get("err"), "")

		);

	}

	/**
	 * 
	 * Error code filter.
	 *
	 * 
	 * 
	 * DB/entity property:
	 * 
	 * err
	 * 
	 */

	public static Specification<AuthMisReportResEntity> resErrorCode(

			String errorCode) {

		return (root, query, cb) -> {

			if (errorCode == null || errorCode.isBlank()) {

				return cb.conjunction();

			}

			return cb.equal(

					root.get("err"),

					errorCode

			);

		};

	}

	/**
	 * 
	 * Timeout transaction.
	 *
	 * 
	 * 
	 * Current business rule:
	 * 
	 * err = 502
	 * 
	 */

	public static Specification<AuthMisReportResEntity> resTimeout() {

		return (root, query, cb) ->

		cb.equal(

				root.get("err"),

				"502"

		);

	}

	/**
	 * 
	 * Failed transactions excluding timeout.
	 * 
	 */

	public static Specification<AuthMisReportResEntity> resFailure() {

		return (root, query, cb) -> cb.and(

				cb.or(

						cb.isNull(root.get("ret")),

						cb.notEqual(

								cb.upper(root.get("ret")),

								"Y"

						)

				),

				cb.or(

						cb.isNull(root.get("err")),

						cb.notEqual(

								root.get("err"),

								"502"

						)

				)

		);

	}

	/**
	 * 
	 * Filter response records by transaction IDs.
	 * 
	 */

	public static Specification<AuthMisReportResEntity> resTransactionIds(

			Collection<String> transactionIds) {

		return (root, query, cb) ->

		root.get("txn").in(transactionIds);

	}

	/**
	 * 
	 * Search transaction ID or client ID.
	 * 
	 */

	public static Specification<AuthMisReportResEntity> resSearch(

			String search) {

		return (root, query, cb) -> {

			if (search == null || search.isBlank()) {

				return cb.conjunction();

			}

			String value =

					"%" + search.toUpperCase() + "%";

			return cb.or(

					cb.like(

							cb.upper(root.get("txn")),

							value

					),

					cb.like(

							cb.upper(root.get("clientId")),

							value

					)

			);

		};

	}

	// ============================================================

	// OPTIONAL / NO FILTER

	// ============================================================

	/**
	 * 
	 * Returns a Specification that does not add any restriction.
	 *
	 * 
	 * 
	 * This is useful when the service starts with:
	 *
	 * 
	 * 
	 * Specification<AuthMisReportResEntity> spec =
	 * 
	 * AuthMisReportSpecification.noResponseFilter();
	 *
	 * 
	 * 
	 * and then conditionally adds filters.
	 *
	 * 
	 * 
	 * IMPORTANT:
	 * 
	 * This does NOT filter by today's date.
	 * 
	 */

	public static Specification<AuthMisReportResEntity> noResponseFilter() {

		return (root, query, cb) ->

		cb.conjunction();

	}

	/**
	 * 
	 * No restriction for Auth Request.
	 * 
	 */

	public static Specification<AuthMisReportReqEntity> noRequestFilter() {

		return (root, query, cb) ->

		cb.conjunction();

	}

}