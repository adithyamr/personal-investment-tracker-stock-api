package com.mavinasara.model.zerodha;

public class TradebookResponse {

	private String status;

	private TradebookData data;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public TradebookData getData() {
		return data;
	}

	public void setData(TradebookData data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "TradebookResponse [status=" + status + ", data=" + data + "]";
	}

}
