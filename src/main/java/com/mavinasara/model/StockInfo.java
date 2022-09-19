package com.mavinasara.model;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "stock_info")
public class StockInfo {

	@Id
	private String symbol;

	private String name;

	private String exchange;

	private BigDecimal marketCap;

	private BigDecimal price;

	private BigDecimal yearLow;

	private BigDecimal yearHigh;

	private BigDecimal priceAvg50;

	private BigDecimal priceAvg200;

	private Long avgVolume;

	private Calendar dividendExDate;

	private BigDecimal annualYield;

	private BigDecimal annualYieldPercent;

	@UpdateTimestamp
	private Date lastUpdated;

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getExchange() {
		return exchange;
	}

	public void setExchange(String exchange) {
		this.exchange = exchange;
	}

	public BigDecimal getMarketCap() {
		return marketCap;
	}

	public void setMarketCap(BigDecimal marketCap) {
		this.marketCap = marketCap;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public BigDecimal getYearLow() {
		return yearLow;
	}

	public void setYearLow(BigDecimal yearLow) {
		this.yearLow = yearLow;
	}

	public BigDecimal getYearHigh() {
		return yearHigh;
	}

	public void setYearHigh(BigDecimal yearHigh) {
		this.yearHigh = yearHigh;
	}

	public BigDecimal getPriceAvg50() {
		return priceAvg50;
	}

	public void setPriceAvg50(BigDecimal priceAvg50) {
		this.priceAvg50 = priceAvg50;
	}

	public BigDecimal getPriceAvg200() {
		return priceAvg200;
	}

	public void setPriceAvg200(BigDecimal priceAvg200) {
		this.priceAvg200 = priceAvg200;
	}

	public Long getAvgVolume() {
		return avgVolume;
	}

	public void setAvgVolume(Long avgVolume) {
		this.avgVolume = avgVolume;
	}

	public Calendar getDividendExDate() {
		return dividendExDate;
	}

	public void setDividendExDate(Calendar dividendExDate) {
		this.dividendExDate = dividendExDate;
	}

	public BigDecimal getAnnualYield() {
		return annualYield;
	}

	public void setAnnualYield(BigDecimal annualYield) {
		this.annualYield = annualYield;
	}

	public BigDecimal getAnnualYieldPercent() {
		return annualYieldPercent;
	}

	public void setAnnualYieldPercent(BigDecimal annualYieldPercent) {
		this.annualYieldPercent = annualYieldPercent;
	}

	public Date getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

}