package com.mavinasara.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.html5.LocalStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.common.collect.Lists;
import com.mavinasara.model.Account;
import com.mavinasara.model.Holding;
import com.mavinasara.model.StockInfo;
import com.mavinasara.model.Transaction;
import com.mavinasara.model.angel.StockInformation;
import com.mavinasara.model.zerodha.HoldingData;
import com.mavinasara.model.zerodha.HoldingResponse;
import com.mavinasara.model.zerodha.TradebookResponse;
import com.mavinasara.model.zerodha.TradebookResult;
import com.mavinasara.repository.AccountRepository;
import com.mavinasara.repository.HoldingRepository;
import com.mavinasara.repository.StockInfoRepository;
import com.mavinasara.repository.TransactionRepository;

import dev.turingcomplete.kotlinonetimepassword.GoogleAuthenticator;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.Interval;

@Service
public class StockService {

	@Value("${api.list.stocks.url:https://margincalculator.angelbroking.com/OpenAPI_File/files/OpenAPIScripMaster.json}")
	private String apiListStocksUrl;

	@Value("${zerodha.console.url:https://console.zerodha.com/dashboard}")
	private String zerodhaConsoleUrl;

	@Value("${zerodha.kite.url:https://kite.zerodha.com/dashboard}")
	private String zerodhaKiteUrl;

	@Value("${zerodha.api.tradebook.url:https://console.zerodha.com/api/reports/tradebook?segment=EQ}")
	private String zerodhaApiTradebookUrl;

	@Value("${zerodha.api.holding.url:https://kite.zerodha.com/oms/portfolio/holdings}")
	private String zerodhaApiHoldingUrl;

	@Autowired
	private StockInfoRepository stockInfoRepository;

	@Autowired
	private HoldingRepository holdingRepository;

	@Autowired
	private TransactionRepository transactionRepository;

	@Autowired
	private AccountRepository accountRepository;

	public void fetchAndUpdateStocks() {
		RestTemplate restTemplate = new RestTemplate();
		StockInformation[] stockList = restTemplate.getForObject(apiListStocksUrl, StockInformation[].class);
		Set<StockInformation> allStocks = new HashSet<StockInformation>(
				Arrays.stream(stockList).filter(s -> s.getExpiry().trim().isBlank()).collect((Collectors.toSet())));
		stockList = null;
		for (StockInformation stockInformation : allStocks) {
			String suffix = "NSE".equalsIgnoreCase(stockInformation.getExch_seg()) ? ".NS" : ".BO";
			Stock stock = getStockInfo(stockInformation.getName() + suffix, false, null, null, null);

			if (stock == null && !stockInformation.getName().equalsIgnoreCase(stockInformation.getSymbol())) {
				stock = getStockInfo(stockInformation.getSymbol() + suffix, false, null, null, null);
			}

			if (stock != null && StringUtils.isNotBlank(stock.getName())
					&& !stock.getName().equalsIgnoreCase(stock.getSymbol().substring(stock.getSymbol().indexOf(".")))) {
				StockInfo stockInfo = convertStockDetails(stock);
				stockInfoRepository.saveAndFlush(stockInfo);
			}
		}
	}

	public void updateStockDetails() {
		List<StockInfo> stocks = stockInfoRepository.findAll();
		for (StockInfo stockInfo : stocks) {
			Stock stock = getStockInfo(stockInfo.getSymbol(), false, null, null, null);
			if (stock == null) {
				stockInfoRepository.delete(stockInfo);
			} else {
				StockInfo updatedStockInfo = convertStockDetails(stock);
				stockInfoRepository.saveAndFlush(updatedStockInfo);
			}
		}
	}

	public void updateHolding(String clientId) throws InterruptedException {
		Account account = accountRepository.getReferenceById(clientId);
		HttpHeaders headers = getZerodhaHeader(account, true);

		HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
		RestTemplate restTemplate = new RestTemplate();

		ResponseEntity<HoldingResponse> response = restTemplate.exchange(
				"https://kite.zerodha.com/oms/portfolio/holdings", HttpMethod.GET, requestEntity,
				HoldingResponse.class);

		HoldingResponse holdingResponse = response.getBody();
		if (holdingResponse.getData() != null && !holdingResponse.getData().isEmpty()) {
			List<HoldingData> holdings = holdingResponse.getData();
			for (HoldingData holdingData : holdings) {
				String symbol = holdingData.getTradingsymbol().replaceAll("\\*", "");
				Holding holding = new Holding();
				holding.setAccount(account);
				holding.setSymbol(symbol + ".NS");
				holding.setExchange(holdingData.getExchange());
				holding.setQuantity(holdingData.getQuantity());
				holding.setAvergeBuyPrice(BigDecimal.valueOf(holdingData.getAverage_price()));
				holding.setBuyValue(BigDecimal.valueOf(holdingData.getAverage_price())
						.multiply(BigDecimal.valueOf(holdingData.getQuantity())));
				holding.setLastTransactionPrice(BigDecimal.valueOf(holdingData.getLast_price()));
				holding.setPresentValue(BigDecimal.valueOf(holdingData.getLast_price())
						.multiply(BigDecimal.valueOf(holdingData.getQuantity())));
				holding.setPnl(BigDecimal.valueOf(holdingData.getPnl()));
				holding.setPnlInPercent(BigDecimal.valueOf(holdingData.getPnl()).divide(holding.getBuyValue())
						.multiply(BigDecimal.valueOf(100)).doubleValue());
				holdingRepository.save(holding);
			}
		}
	}

	private StockInfo convertStockDetails(Stock stock) {
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

	public Map<String, Stock> getHistory(List<String> symbolList, Date fromDate, Date toDate, Interval interval) {
		return getStockInfo(symbolList, false, fromDate, toDate, interval);
	}

	private Map<String, Stock> getStockInfo(List<String> symbolList, boolean includeHistorical, Date fromDate,
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

	private Stock getStockInfo(String symbol, boolean includeHistorical, Date fromDate, Date toDate,
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

	public List<Transaction> getTransactions(Account account) throws Exception {
		TradebookResponse response = getTransactionsFromZerodha(account);

		List<String> symbols = Lists.newArrayList();
		response.getData().getResult().forEach(r -> {
			symbols.add(r.getTradingsymbol() + ".NS");

		});
		System.out.println(YahooFinance.get(symbols.toArray(new String[symbols.size()]), false));
		return null;
	}

	private TradebookResponse getTransactionsFromZerodha(Account account) throws InterruptedException {

		byte[] secretKey = "ZSIVWGE4ITWC7LQ5T52NGAZARXBHPQRU".getBytes();
		String totp = new GoogleAuthenticator(secretKey).generate(new Date(System.currentTimeMillis()));

		ChromeOptions options = new ChromeOptions();
		options.setHeadless(true);
		WebDriver driver = new ChromeDriver(options);

		driver.get(zerodhaConsoleUrl);

		WebElement username = driver.findElement(By.id("userid"));
		WebElement password = driver.findElement(By.id("password"));
		WebElement login = driver.findElement(By.xpath("//button[text()='Login ']"));
		username.sendKeys(account.getAccountNumber());
		password.sendKeys(account.getPassword());
		login.click();

		Thread.sleep(5000);

		// WebElement pin = driver.findElement(By.id("pin"));
		// pin.sendKeys(account.getPin());
		WebElement totpElement = driver.findElement(By.id("totp"));
		totpElement.sendKeys(totp);
		WebElement pinSubmit = driver.findElement(By.xpath("//button[text()='Continue ']"));
		pinSubmit.click();

		Thread.sleep(5000);

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.add("Cookie", driver.manage().getCookieNamed("session").toString());
		headers.add("x-csrftoken", driver.manage().getCookieNamed("public_token").toString());
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

		HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

		Calendar previousDate = Calendar.getInstance();
		previousDate.add(Calendar.DATE, -1);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String yesterday = sdf.format(previousDate.getTime());

		StringBuilder builder = new StringBuilder(zerodhaApiTradebookUrl);
		builder.append("&from_date=").append(yesterday).append("&to_date=").append(yesterday);

		ResponseEntity<TradebookResponse> response = restTemplate.exchange(builder.toString(), HttpMethod.GET,
				requestEntity, TradebookResponse.class);

		TradebookResponse tradebookResponse = response.getBody();

		if (tradebookResponse.getData() != null && tradebookResponse.getData().getResult() != null
				&& !tradebookResponse.getData().getResult().isEmpty()) {
			List<TradebookResult> results = tradebookResponse.getData().getResult();
			for (TradebookResult result : results) {
				result.getExchange();
			}
		}

		return tradebookResponse;
	}

	private HttpHeaders getZerodhaHeader(Account account, boolean isKite) throws InterruptedException {

		String os = System.getProperty("os.name");
		if (os.contains("Windows")) {
			System.setProperty("webdriver.chrome.driver",
					"D://Workspace/Projects/personal-investment-tracker-stock-api/src/main/resources/chromedriver/windows.exe");
		}

		ChromeOptions options = new ChromeOptions();
		options.setHeadless(true);
		ChromeDriver driver = new ChromeDriver(options);

		if (isKite) {
			driver.get(zerodhaKiteUrl);
		} else {
			driver.get(zerodhaConsoleUrl);
		}
		WebElement username = driver.findElement(By.id("userid"));
		WebElement password = driver.findElement(By.id("password"));
		WebElement login = driver.findElement(By.xpath("//button[text()='Login ']"));
		username.sendKeys(account.getAccountNumber());
		password.sendKeys(account.getPassword());
		login.click();

		Thread.sleep(5000);

		if (StringUtils.isBlank(account.getKey())) {
			WebElement pin = driver.findElement(By.id("pin"));
			pin.sendKeys(account.getPin());
		} else {
			String totp = new GoogleAuthenticator(account.getKey().getBytes())
					.generate(new Date(System.currentTimeMillis()));

			WebElement totpElement = driver.findElement(By.id("totp"));
			totpElement.sendKeys(totp);
		}

		WebElement pinSubmit = driver.findElement(By.xpath("//button[text()='Continue ']"));
		pinSubmit.click();

		Thread.sleep(5000);

		HttpHeaders headers = new HttpHeaders();

		if (isKite) {
			LocalStorage localStorage = driver.getLocalStorage();

			if (driver.manage().getCookies() != null) {
				headers.add("Cookie", driver.manage().getCookieNamed("kf_session").toString());
			}

			String enctoken = "enctoken " + localStorage.getItem("__storejs_kite_enctoken").substring(1,
					localStorage.getItem("__storejs_kite_enctoken").length() - 1);
			headers.add("authorization", enctoken);

			String uuid = localStorage.getItem("__storejs_kite_app_uuid").substring(1,
					localStorage.getItem("__storejs_kite_app_uuid").length() - 1);
			headers.add("x-kite-app-uuid", uuid);

			String userId = localStorage.getItem("__storejs_kite_user_id").substring(1,
					localStorage.getItem("__storejs_kite_user_id").length() - 1);
			headers.add("x-kite-userid", userId);

			String publicToken = localStorage.getItem("__storejs_kite_public_token").substring(1,
					localStorage.getItem("__storejs_kite_public_token").length() - 1);
			headers.add("x-public_token", publicToken);

		} else {
			headers.add("Cookie", driver.manage().getCookieNamed("session").toString());
			headers.add("x-csrftoken", driver.manage().getCookieNamed("public_token").toString());
			headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		}

		return headers;

	}

}
