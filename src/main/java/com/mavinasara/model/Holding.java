package com.mavinasara.model;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class Holding {

	@Id
	private String symbol;

	private String exchange;

	private Integer quantity;

	private BigDecimal avergeBuyPrice;

	private BigDecimal lastTransactionPrice;

	private BigDecimal buyValue;

	private BigDecimal presentValue;

	private BigDecimal pnl;

	private Double pnlInPercent;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "accountNumber")
	private Account account;

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
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

	public BigDecimal getAvergeBuyPrice() {
		return avergeBuyPrice;
	}

	public void setAvergeBuyPrice(BigDecimal avergeBuyPrice) {
		this.avergeBuyPrice = avergeBuyPrice;
	}

	public BigDecimal getLastTransactionPrice() {
		return lastTransactionPrice;
	}

	public void setLastTransactionPrice(BigDecimal lastTransactionPrice) {
		this.lastTransactionPrice = lastTransactionPrice;
	}

	public BigDecimal getBuyValue() {
		return buyValue;
	}

	public void setBuyValue(BigDecimal buyValue) {
		this.buyValue = buyValue;
	}

	public BigDecimal getPresentValue() {
		return presentValue;
	}

	public void setPresentValue(BigDecimal presentValue) {
		this.presentValue = presentValue;
	}

	public BigDecimal getPnl() {
		return pnl;
	}

	public void setPnl(BigDecimal pnl) {
		this.pnl = pnl;
	}

	public Double getPnlInPercent() {
		return pnlInPercent;
	}

	public void setPnlInPercent(Double pnlInPercent) {
		this.pnlInPercent = pnlInPercent;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

}
