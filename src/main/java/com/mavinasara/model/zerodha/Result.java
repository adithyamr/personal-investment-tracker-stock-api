package com.mavinasara.model.zerodha;

public class Result {

	private String trade_date;
	private String trade_type;
	private String quantity;
	private String price;
	private String tradingsymbol;

	public String getTrade_date() {
		return trade_date;
	}

	public void setTrade_date(String trade_date) {
		this.trade_date = trade_date;
	}

	public String getTrade_type() {
		return trade_type;
	}

	public void setTrade_type(String trade_type) {
		this.trade_type = trade_type;
	}

	public String getQuantity() {
		return quantity;
	}

	public void setQuantity(String quantity) {
		this.quantity = quantity;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getTradingsymbol() {
		return tradingsymbol;
	}

	public void setTradingsymbol(String tradingsymbol) {
		this.tradingsymbol = tradingsymbol;
	}

	@Override
	public String toString() {
		return "Result [trade_date=" + trade_date + ", trade_type=" + trade_type + ", quantity=" + quantity + ", price="
				+ price + ", tradingsymbol=" + tradingsymbol + "]";
	}

}
