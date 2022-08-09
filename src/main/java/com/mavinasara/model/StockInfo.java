package com.mavinasara.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonProperty;

@Document(collection = "stock_info")
public class StockInfo {

	@Id
	@JsonProperty("symbol")
	private String symbol;

	@JsonProperty("name")
	private String name;

	@JsonProperty("exchange")
	private String exchange;

	@JsonProperty("currency")
	private String currency;

}
