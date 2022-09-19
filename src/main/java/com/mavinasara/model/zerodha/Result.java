package com.mavinasara.model.zerodha;

public class Result {

	private String trade_id;
	private String trade_date;
	private String order_execution_time;
	private String trade_type;
	private Integer quantity;
	private Double price;
	private String tradingsymbol;
	private String exchange;

	public String getTrade_id() {
		return trade_id;
	}

	public void setTrade_id(String trade_id) {
		this.trade_id = trade_id;
	}

	public String getTrade_date() {
		return trade_date;
	}

	public void setTrade_date(String trade_date) {
		this.trade_date = trade_date;
	}

	public String getOrder_execution_time() {
		return order_execution_time;
	}

	public void setOrder_execution_time(String order_execution_time) {
		this.order_execution_time = order_execution_time;
	}

	public String getTrade_type() {
		return trade_type;
	}

	public void setTrade_type(String trade_type) {
		this.trade_type = trade_type;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

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

//	{"trade_date":"2022-08-22","trade_type":"buy","quantity":10,"price":55.21,"strike":0,"order_id":"1200000006018899","trade_id":"51210866",
//		"series":"EQ","exchange":"NSE","segment":"EQ","order_execution_time":"2022-08-22T10:10:55","isin":"INF204KC1402","instrument_id":"96fb97918f",
//		"instrument_type":"","tradingsymbol":"SILVERBEES","expiry_date":"","external_trade_type":"","tag_ids":null}

}
