package com.mavinasara.service;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.mavinasara.model.StockInfo;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.Interval;

public final class Helper {

	public static StockInfo convertStockDetails(Stock stock) {
		StockInfo stockInfo = new StockInfo();
		stockInfo.setSymbol(stock.getSymbol());
		stockInfo.setName(stock.getName());
		stockInfo.setExchange(stock.getStockExchange());

		if (stock.getDividend() != null) {
			stockInfo.setAnnualYield(stock.getDividend().getAnnualYield());
			stockInfo.setAnnualYieldPercent(stock.getDividend().getAnnualYieldPercent());
			stockInfo.setDividendExDate(stock.getDividend().getExDate());
		}
		if (stock.getStats() != null) {
			stockInfo.setMarketCap(stock.getStats().getMarketCap());
		}

		if (stock.getQuote() != null) {
			stockInfo.setAvgVolume(stock.getQuote().getAvgVolume());
			stockInfo.setPrice(stock.getQuote().getPrice());
			stockInfo.setPriceAvg200(stock.getQuote().getChangeFromAvg200());
			stockInfo.setPriceAvg50(stock.getQuote().getChangeFromAvg50());
			stockInfo.setYearHigh(stock.getQuote().getYearHigh());
			stockInfo.setYearLow(stock.getQuote().getYearLow());
		}
		return stockInfo;
	}

	public static Map<String, Stock> getStockInfo(List<String> symbolList, boolean includeHistorical, Date fromDate,
			Date toDate, Interval interval) {
		try {
			String[] symbols = symbolList.stream().toArray(String[]::new);
			if (fromDate == null) {
				return YahooFinance.get(symbols, includeHistorical);
			}

			Calendar from = Calendar.getInstance();
			from.setTime(fromDate);

			Calendar to = Calendar.getInstance();
			if (toDate != null) {
				to.setTime(toDate);
			}

			return YahooFinance.get(symbols, from, to, interval);
		} catch (IOException e) {
			return null;
		}
	}

	public static Stock getStockInfo(String symbol, boolean includeHistorical, Date fromDate, Date toDate,
			Interval interval) {
		try {
			if (fromDate == null) {
				return YahooFinance.get(symbol, includeHistorical);
			}

			Calendar from = Calendar.getInstance();
			from.setTime(fromDate);

			Calendar to = Calendar.getInstance();
			if (toDate != null) {
				to.setTime(toDate);
			}

			return YahooFinance.get(symbol, from, to, interval);
		} catch (IOException e) {
			return null;
		}
	}

	public static String getStockSymbol(String exchange, String symbol) {
		symbol = symbol.replaceAll("\\*", "");
		String suffix = "NSE".equalsIgnoreCase(exchange) ? ".NS" : ".BO";
		return symbol + suffix;
	}

}
