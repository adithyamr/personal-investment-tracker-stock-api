package com.mavinasara.model.zerodha;

public class HoldingData {

	private String state;

	private HoldingResult result;

	private Pagination pagination;

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public HoldingResult getResult() {
		return result;
	}

	public void setResult(HoldingResult result) {
		this.result = result;
	}

	public Pagination getPagination() {
		return pagination;
	}

	public void setPagination(Pagination pagination) {
		this.pagination = pagination;
	}

	@Override
	public String toString() {
		return "Data [state=" + state + ", result=" + result + ", pagination=" + pagination + "]";
	}

}
