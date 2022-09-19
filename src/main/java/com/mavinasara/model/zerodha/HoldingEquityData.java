package com.mavinasara.model.zerodha;

public class HoldingEquityData {

	private String client_id;
	private String tradingsymbol;
	private Integer total_quantity;
	private Double buy_average;
	private Double holdings_buy_value;
	private Double ltp;
	private Double closing_value;
	private Double unrealized_profit;
	private Double unrealized_profit_percentage;

	public String getClient_id() {
		return client_id;
	}

	public void setClient_id(String client_id) {
		this.client_id = client_id;
	}

	public String getTradingsymbol() {
		return tradingsymbol;
	}

	public void setTradingsymbol(String tradingsymbol) {
		this.tradingsymbol = tradingsymbol;
	}

	public Integer getTotal_quantity() {
		return total_quantity;
	}

	public void setTotal_quantity(Integer total_quantity) {
		this.total_quantity = total_quantity;
	}

	public Double getBuy_average() {
		return buy_average;
	}

	public void setBuy_average(Double buy_average) {
		this.buy_average = buy_average;
	}

	public Double getHoldings_buy_value() {
		return holdings_buy_value;
	}

	public void setHoldings_buy_value(Double holdings_buy_value) {
		this.holdings_buy_value = holdings_buy_value;
	}

	public Double getLtp() {
		return ltp;
	}

	public void setLtp(Double ltp) {
		this.ltp = ltp;
	}

	public Double getClosing_value() {
		return closing_value;
	}

	public void setClosing_value(Double closing_value) {
		this.closing_value = closing_value;
	}

	public Double getUnrealized_profit() {
		return unrealized_profit;
	}

	public void setUnrealized_profit(Double unrealized_profit) {
		this.unrealized_profit = unrealized_profit;
	}

	public Double getUnrealized_profit_percentage() {
		return unrealized_profit_percentage;
	}

	public void setUnrealized_profit_percentage(Double unrealized_profit_percentage) {
		this.unrealized_profit_percentage = unrealized_profit_percentage;
	}

}
