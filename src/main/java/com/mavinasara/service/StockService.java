package com.mavinasara.service;

import java.io.IOException;
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
import com.mavinasara.model.StockInfo;
import com.mavinasara.model.Transaction;
import com.mavinasara.model.angel.StockInformation;
import com.mavinasara.model.zerodha.Result;
import com.mavinasara.model.zerodha.TradebookResponse;
import com.mavinasara.repository.ShareRepository;

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

	@Value("${zerodha.api.base.url:https://console.zerodha.com/api/reports/tradebook?segment=EQ}")
	private String zerodhaApiTradebookBaseUrl;

	@Autowired
	private ShareRepository shareRepository;

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
				shareRepository.saveAndFlush(stockInfo);
			}
		}
	}

	public void updateStockDetails() {
		List<StockInfo> stocks = shareRepository.findAll();
		for (StockInfo stockInfo : stocks) {
			Stock stock = getStockInfo(stockInfo.getSymbol(), false, null, null, null);
			if (stock == null) {
				shareRepository.delete(stockInfo);
			} else {
				StockInfo updatedStockInfo = convertStockDetails(stock);
				shareRepository.saveAndFlush(updatedStockInfo);
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

		StringBuilder builder = new StringBuilder(zerodhaApiTradebookBaseUrl);
		builder.append("&from_date=").append(yesterday).append("&to_date=").append(yesterday);

		ResponseEntity<TradebookResponse> response = restTemplate.exchange(builder.toString(), HttpMethod.GET,
				requestEntity, TradebookResponse.class);

		TradebookResponse tradebookResponse = response.getBody();

		if (tradebookResponse.getData() != null && tradebookResponse.getData().getResult() != null
				&& !tradebookResponse.getData().getResult().isEmpty()) {
			List<Result> results = tradebookResponse.getData().getResult();
			for (Result result : results) {
				result.getExchange();
			}
		}

		return tradebookResponse;
	}

	public static void addTransaction(List<Transaction> transactions) throws IOException, InterruptedException {
		byte[] secretKey = "ZSIVWGE4ITWC7LQ5T52NGAZARXBHPQRU".getBytes();
		String totp = new GoogleAuthenticator(secretKey).generate(new Date(System.currentTimeMillis()));

		ChromeOptions options = new ChromeOptions();
		options.setHeadless(true);
		WebDriver driver = new ChromeDriver(options);

		driver.get("https://console.zerodha.com/dashboard");

//		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(50000));

		WebElement username = driver.findElement(By.id("userid"));
		WebElement password = driver.findElement(By.id("password"));
		WebElement login = driver.findElement(By.xpath("//button[text()='Login ']"));
		username.sendKeys("ZM3272");
		password.sendKeys("Adithy@1985");
		login.click();

		Thread.sleep(5000);

		// WebElement pin = driver.findElement(By.id("pin")); // for id
		WebElement totpElement = driver.findElement(By.id("totp")); // for id
		WebElement pinSubmit = driver.findElement(By.xpath("//button[text()='Continue ']"));
//		wait.until(ExpectedConditions.visibilityOf(pin));
		totpElement.sendKeys(totp);
		pinSubmit.click();

		Thread.sleep(5000);

//		WebElement reportMenu = driver.findElement(By.xpath("//*[@id=\"app\"]/div[1]/div/div/div[1]/a[3]"));
//		wait.until(ExpectedConditions.visibilityOf(reportMenu));

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.add("Cookie", driver.manage().getCookieNamed("session").toString());
		headers.add("x-csrftoken", driver.manage().getCookieNamed("public_token").toString());
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

		HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

		Calendar previousDate = Calendar.getInstance();
		previousDate.add(Calendar.DATE, -2);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String yesterday = sdf.format(previousDate.getTime());

		ResponseEntity<TradebookResponse> response = restTemplate
				.exchange("https://console.zerodha.com/api/reports/tradebook?segment=EQ&from_date=" + yesterday
						+ "&to_date=" + yesterday, HttpMethod.GET, requestEntity, TradebookResponse.class);

		System.out.println(response.getBody());

		TradebookResponse body = response.getBody();
		List<String> symbols = Lists.newArrayList();
		body.getData().getResult().forEach(r -> {
			symbols.add(r.getTradingsymbol() + ".NS");

		});
		System.out.println(YahooFinance.get(symbols.toArray(new String[symbols.size()]), false));

	}

}
