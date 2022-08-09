package com.mavinasara.model.zerodha;

public class TradebookResponse {

	private String status;

	private Data data;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Data getData() {
		return data;
	}

	public void setData(Data data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "TradebookResponse [status=" + status + ", data=" + data + "]";
	}

}
