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
import org.openqa.selenium.html5.SessionStorage;
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
import com.mavinasara.model.zerodha.HoldingEquityData;
import com.mavinasara.model.zerodha.HoldingRes;
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

	@Value("${zerodha.api.base.url:https://console.zerodha.com/api/reports/tradebook?segment=EQ}")
	private String zerodhaApiTradebookBaseUrl;

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
		HttpHeaders headers = getZerodhaHeader(account);

		HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

		Calendar date = Calendar.getInstance();

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String dateStr = sdf.format(date.getTime());

		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<HoldingResponse> holdingResponse = restTemplate.exchange(
				"https://console.zerodha.com/api/reports/holdings/portfolio?date=" + dateStr, HttpMethod.GET,
				requestEntity, HoldingResponse.class);
		if (holdingResponse.getBody() != null && holdingResponse.getBody().getData() != null
				&& holdingResponse.getBody().getData().getResult() != null
				&& holdingResponse.getBody().getData().getResult().getEq() != null
				&& !holdingResponse.getBody().getData().getResult().getEq().isEmpty()) {
			List<HoldingEquityData> holdings = holdingResponse.getBody().getData().getResult().getEq();
			for (HoldingEquityData holdingEquityData : holdings) {
				String symbol = holdingEquityData.getTradingsymbol().replaceAll("*", "");
				Holding holding = new Holding();
				holding.setAccount(account);
				holding.setSymbol(symbol + ".NS");
				holding.setExchange("NSE");
				holding.setQuantity(holdingEquityData.getTotal_quantity());
				holding.setAvergeBuyPrice(BigDecimal.valueOf(holdingEquityData.getBuy_average()));
				holding.setBuyValue(BigDecimal.valueOf(holdingEquityData.getHoldings_buy_value()));
				holding.setLastTransactionPrice(BigDecimal.valueOf(holdingEquityData.getLtp()));
				holding.setPresentValue(BigDecimal.valueOf(holdingEquityData.getClosing_value()));
				holding.setPnl(BigDecimal.valueOf(holdingEquityData.getUnrealized_profit()));
				holding.setPnlInPercent(holdingEquityData.getUnrealized_profit_percentage());
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

		StringBuilder builder = new StringBuilder(zerodhaApiTradebookBaseUrl);
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

	public static void main(String[] arg) throws IOException, InterruptedException {

		String os = System.getProperty("os.name");
		if (os.contains("Windows")) {
			System.setProperty("webdriver.chrome.driver",
					"D://Workspace/Projects/personal-investment-tracker-stock-api/src/main/resources/chromedriver/windows.exe");
		}

		byte[] secretKey = "ZSIVWGE4ITWC7LQ5T52NGAZARXBHPQRU".getBytes();
		String totp = new GoogleAuthenticator(secretKey).generate(new Date(System.currentTimeMillis()));

		ChromeOptions options = new ChromeOptions();
		options.setHeadless(true);
		ChromeDriver driver = new ChromeDriver(options);

		driver.get("https://kite.zerodha.com/dashboard");

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

		LocalStorage localStorage = driver.getLocalStorage();
		Set<String> keySet = localStorage.keySet();
		for (String key : keySet) {
			System.out.println(localStorage.getItem(key));
		}

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();

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

		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

		HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

		ResponseEntity<HoldingRes> response = restTemplate.exchange("https://kite.zerodha.com/oms/portfolio/holdings",
				HttpMethod.GET, requestEntity, HoldingRes.class);

		HoldingRes body = response.getBody();
		System.out.println(body);
	}

	private HttpHeaders getZerodhaHeader(Account account) throws InterruptedException {

		String os = System.getProperty("os.name");
		if (os.contains("Windows")) {
			System.setProperty("webdriver.chrome.driver",
					"D://Workspace/Projects/personal-investment-tracker-stock-api/src/main/resources/chromedriver/windows.exe");
		}

		String totp = new GoogleAuthenticator(account.getKey().getBytes())
				.generate(new Date(System.currentTimeMillis()));

		ChromeOptions options = new ChromeOptions();
		options.setHeadless(true);
		ChromeDriver driver = new ChromeDriver(options);

		driver.get("https://kite.zerodha.com/dashboard");

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

		HttpHeaders headers = new HttpHeaders();
//		headers.add("Cookie", driver.manage().getCookieNamed("session").toString());
//		headers.add("x-csrftoken", driver.manage().getCookieNamed("public_token").toString());
//		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

		LocalStorage localStorage = driver.getLocalStorage();
		Set<String> keySet = localStorage.keySet();
		for (String key : keySet) {
			System.out.println(localStorage.getItem(key));
		}

		SessionStorage sessionStorage = driver.getSessionStorage();
		Set<String> keySet2 = sessionStorage.keySet();
		for (String string : keySet2) {
			System.out.println(sessionStorage.getItem(string));
		}
		return headers;

	}

}
