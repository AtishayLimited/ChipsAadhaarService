package com.chips.aadhaar.dto.response;

public class TransactionDashboardResponse {

	private long today;
	private long thisMonth;
	private long thisYear;

	public TransactionDashboardResponse() {
	}

	public TransactionDashboardResponse(long today, long thisMonth, long thisYear) {

		this.today = today;
		this.thisMonth = thisMonth;
		this.thisYear = thisYear;
	}

	public long getToday() {
		return today;
	}

	public void setToday(long today) {
		this.today = today;
	}

	public long getThisMonth() {
		return thisMonth;
	}

	public void setThisMonth(long thisMonth) {
		this.thisMonth = thisMonth;
	}

	public long getThisYear() {
		return thisYear;
	}

	public void setThisYear(long thisYear) {
		this.thisYear = thisYear;
	}
}
