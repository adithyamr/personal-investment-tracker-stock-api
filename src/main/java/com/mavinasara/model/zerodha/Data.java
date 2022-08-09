package com.mavinasara.model.zerodha;

import java.util.List;

public class Data {

	private String state;

	private List<Result> result;

	private Pagination pagination;

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public List<Result> getResult() {
		return result;
	}

	public void setResult(List<Result> result) {
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
