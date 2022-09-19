package com.mavinasara.model.angel;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StockInformation {

	@JsonProperty("symbol")
	private String symbol;

	@JsonProperty("name")
	private String name;

	@JsonProperty("exch_seg")
	private String exch_seg;

	@JsonProperty("expiry")
	private String expiry;

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

	public String getExch_seg() {
		return exch_seg;
	}

	public void setExch_seg(String exch_seg) {
		this.exch_seg = exch_seg;
	}

	public String getExpiry() {
		return expiry;
	}

	public void setExpiry(String expiry) {
		this.expiry = expiry;
	}

	@Override
	public int hashCode() {
		return Objects.hash(exch_seg, name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StockInformation other = (StockInformation) obj;
		return Objects.equals(exch_seg, other.exch_seg) && Objects.equals(name, other.name);
	}

}
