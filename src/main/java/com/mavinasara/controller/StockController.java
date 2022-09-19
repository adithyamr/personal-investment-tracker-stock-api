package com.mavinasara.controller;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mavinasara.model.StockInfo;
import com.mavinasara.service.StockService;

import yahoofinance.Stock;
import yahoofinance.histquotes.Interval;

@RefreshScope
@RestController
@RequestMapping("/stock")
public class StockController {

	private final StockService stockService;

	public StockController(StockService stockService) {
		super();
		this.stockService = stockService;
	}

	@PutMapping("/updateStockInfo")
	public void updateStockInfo() throws IOException {
		stockService.updateStockDetails();
	}

	@PutMapping("/addAllIndianStocks")
	public void addAllIndianStocks() throws IOException {
		stockService.fetchAndUpdateStocks();
	}

	@GetMapping
	public List<StockInfo> stock(@RequestParam(name = "symbol") String symbol) throws IOException {
		return null;
	}

	@GetMapping("/history")
	public Map<String, Stock> stockHistory(@RequestParam(name = "symbols") List<String> symbols,
			@RequestParam(name = "fromDate", required = true) @DateTimeFormat(pattern = "dd-MM-yyyy") @Valid Date fromDate,
			@RequestParam(name = "toDate", required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") @Valid Date toDate,
			@RequestParam(name = "interval", required = false, defaultValue = "m") Interval interval)
			throws IOException {
		return stockService.getHistory(symbols, fromDate, toDate, interval);
	}

	@PostMapping("/purchase")
	public List<StockInfo> purchaseStock(@RequestBody List<StockInfo> shares) {
		return shares;
	}

	@PostMapping("/sell")
	public List<StockInfo> sellStock(@RequestBody List<StockInfo> shares) {
		return shares;

	}
}
