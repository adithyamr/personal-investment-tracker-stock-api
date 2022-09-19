package com.mavinasara.model.zerodha;

import java.util.List;

public class TradebookData {

	private String state;

	private List<TradebookResult> result;

	private Pagination pagination;

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public List<TradebookResult> getResult() {
		return result;
	}

	public void setResult(List<TradebookResult> result) {
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
