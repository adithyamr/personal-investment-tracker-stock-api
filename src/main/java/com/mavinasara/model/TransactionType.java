package com.mavinasara.model;

public enum TransactionType {

	BUY("buy"),

	SELL("sell"),

	DIVIDEND("dividend");

	private String value;

	private TransactionType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}
