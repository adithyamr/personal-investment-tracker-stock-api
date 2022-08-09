package com.mavinasara.controller;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mavinasara.model.StockInfo;
import com.mavinasara.repository.ShareRepository;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.Interval;

@RefreshScope
@RestController
@RequestMapping("/stock")
public class StockController {

	@Value("${spring.data.mongodb.username}")
	private String user;

	@Value("${spring.data.mongodb.password}")
	private String password;

	@Autowired
	private ShareRepository shareRepository;

	@GetMapping
	public List<StockInfo> stock(@RequestParam(name = "symbol") String symbol) throws IOException {
		System.out.println(user + "----" + password);
		return shareRepository.findAll();
	}

	@GetMapping("/history")
	public Stock stockHistory(@RequestParam(name = "symbol") String symbol,
			@RequestParam(name = "fromDate", required = true) @DateTimeFormat(pattern = "dd-MM-yyyy") @Valid Date fromDate,
			@RequestParam(name = "toDate", required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") @Valid Date toDate,
			@RequestParam(name = "interval", required = false, defaultValue = "m") Interval interval)
			throws IOException {

		Calendar from = Calendar.getInstance();
		from.setTime(fromDate);

		Calendar to = Calendar.getInstance();
		if (toDate != null) {
			to.setTime(toDate);
		}
		Stock stock = YahooFinance.get(symbol, from, to, interval);
		return stock;
	}

	@PostMapping("/purchase")
	public List<StockInfo> purchaseStock(@RequestBody List<StockInfo> shares) {
		shareRepository.saveAll(shares);
		return shares;
	}

	@PostMapping("/sell")
	public List<StockInfo> sellStock(@RequestBody List<StockInfo> shares) {
		shareRepository.saveAll(shares);
		return shares;

	}
}
