package com.mavinasara.model.zerodha;

import java.util.List;

public class HoldingResponse {

	private String status;

	private List<HoldingData> data;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<HoldingData> getData() {
		return data;
	}

	public void setData(List<HoldingData> data) {
		this.data = data;
	}

}
