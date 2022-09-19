package com.mavinasara.model.zerodha;

import java.util.List;

public class HoldingRes {

	private String status;

	private List<Data> data;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<Data> getData() {
		return data;
	}

	public void setData(List<Data> data) {
		this.data = data;
	}

}
