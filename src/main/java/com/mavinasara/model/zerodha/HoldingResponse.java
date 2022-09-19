package com.mavinasara.model.zerodha;

public class HoldingResponse {

	private String status;

	private HoldingData data;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public HoldingData getData() {
		return data;
	}

	public void setData(HoldingData data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "TradebookResponse [status=" + status + ", data=" + data + "]";
	}

}
