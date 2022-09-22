package com.mavinasara.model.zerodha;

public class HoldingData {

	private String tradingsymbol;
	private String exchange;
	private Integer quantity;
	private Double average_price;

	private Double last_price;
	private Double pnl;

	public String getTradingsymbol() {
		return tradingsymbol;
	}

	public void setTradingsymbol(String tradingsymbol) {
		this.tradingsymbol = tradingsymbol;
	}

	public String getExchange() {
		return exchange;
	}

	public void setExchange(String exchange) {
		this.exchange = exchange;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public Double getAverage_price() {
		return average_price;
	}

	public void setAverage_price(Double average_price) {
		this.average_price = average_price;
	}

	public Double getLast_price() {
		return last_price;
	}

	public void setLast_price(Double last_price) {
		this.last_price = last_price;
	}

	public Double getPnl() {
		return pnl;
	}

	public void setPnl(Double pnl) {
		this.pnl = pnl;
	}

}
