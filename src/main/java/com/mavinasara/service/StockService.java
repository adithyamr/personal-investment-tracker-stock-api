package com.mavinasara.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.text.StrSubstitutor;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
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

import com.mavinasara.model.Account;
import com.mavinasara.model.Holding;
import com.mavinasara.model.StockInfo;
import com.mavinasara.model.Transaction;
import com.mavinasara.model.angel.StockInformation;
import com.mavinasara.model.zerodha.HoldingData;
import com.mavinasara.model.zerodha.HoldingResponse;
import com.mavinasara.model.zerodha.Pagination;
import com.mavinasara.model.zerodha.TradebookResponse;
import com.mavinasara.model.zerodha.TradebookResult;
import com.mavinasara.repository.AccountRepository;
import com.mavinasara.repository.HoldingRepository;
import com.mavinasara.repository.StockInfoRepository;
import com.mavinasara.repository.TransactionRepository;

import dev.turingcomplete.kotlinonetimepassword.GoogleAuthenticator;
import yahoofinance.Stock;
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

	public Map<String, Stock> getHistory(List<String> symbolList, Date fromDate, Date toDate, Interval interval) {
		return Helper.getStockInfo(symbolList, false, fromDate, toDate, interval);
	}

	public void fetchAndUpdateStocks() {
		RestTemplate restTemplate = new RestTemplate();
		StockInformation[] stockList = restTemplate.getForObject(apiListStocksUrl, StockInformation[].class);
		Set<StockInformation> allStocks = new HashSet<StockInformation>(
				Arrays.stream(stockList).filter(s -> s.getExpiry().trim().isBlank()).collect((Collectors.toSet())));
		stockList = null;
		for (StockInformation stockInformation : allStocks) {
			addStockInformation(stockInformation);
		}
	}

	private void addStockInformation(StockInformation stockInformation) {
		String symbol = Helper.getStockSymbol(stockInformation.getExch_seg(), stockInformation.getName());
		Stock stock = Helper.getStockInfo(symbol, false, null, null, null);

		if (stock == null && !stockInformation.getName().equalsIgnoreCase(stockInformation.getSymbol())) {
			symbol = Helper.getStockSymbol(stockInformation.getExch_seg(), stockInformation.getSymbol());
			stock = Helper.getStockInfo(symbol, false, null, null, null);
		}

		if (stock != null && StringUtils.isNotBlank(stock.getName())
				&& !stock.getName().equalsIgnoreCase(stock.getSymbol().substring(stock.getSymbol().indexOf(".")))) {
			StockInfo stockInfo = Helper.convertStockDetails(stock);
			stockInfoRepository.saveAndFlush(stockInfo);
		}
	}

	public void updateStockDetails() {
		List<StockInfo> stocks = stockInfoRepository.findAll();
		for (StockInfo stockInfo : stocks) {
			Stock stock = Helper.getStockInfo(stockInfo.getSymbol(), false, null, null, null);
			if (stock == null) {
				stockInfoRepository.delete(stockInfo);
			} else {
				StockInfo updatedStockInfo = Helper.convertStockDetails(stock);
				stockInfoRepository.saveAndFlush(updatedStockInfo);
			}
		}
	}

	public void updateHolding(String clientId) throws InterruptedException {
		Account account = accountRepository.getReferenceById(clientId);
		HttpHeaders headers = getZerodhaHeader(account, true);

		HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
		RestTemplate restTemplate = new RestTemplate();

		ResponseEntity<HoldingResponse> response = null;
		boolean isSuccessfulResponse = false;
		int retryCount = 0;
		while (!isSuccessfulResponse || retryCount++ >= 10) {
			response = restTemplate.exchange(zerodhaApiHoldingUrl, HttpMethod.GET, requestEntity,
					HoldingResponse.class);

			if (response != null && response.getStatusCode().is2xxSuccessful()
					&& "success".equalsIgnoreCase(response.getBody().getStatus())) {
				isSuccessfulResponse = true;
			}
		}

		if (response.getBody() != null && response.getBody().getData() != null
				&& !response.getBody().getData().isEmpty()) {
			holdingRepository.deleteAll();
			List<HoldingData> holdings = response.getBody().getData();
			for (HoldingData holdingData : holdings) {
				String symbol = Helper.getStockSymbol(holdingData.getExchange(), holdingData.getTradingsymbol());
				Holding holding = new Holding();
				holding.setAccount(account);
				holding.setSymbol(symbol);
				holding.setExchange(holdingData.getExchange());
				holding.setQuantity(holdingData.getQuantity());
				holding.setAvergeBuyPrice(BigDecimal.valueOf(holdingData.getAverage_price()));
				holding.setBuyValue(BigDecimal.valueOf(holdingData.getAverage_price())
						.multiply(BigDecimal.valueOf(holdingData.getQuantity())));
				holding.setLastTransactionPrice(BigDecimal.valueOf(holdingData.getLast_price()));
				holding.setPresentValue(BigDecimal.valueOf(holdingData.getLast_price())
						.multiply(BigDecimal.valueOf(holdingData.getQuantity())));
				holding.setPnl(BigDecimal.valueOf(holdingData.getPnl()));
				holding.setPnlInPercent(
						BigDecimal.valueOf(holdingData.getPnl()).divide(holding.getBuyValue(), 2, RoundingMode.HALF_UP)
								.multiply(BigDecimal.valueOf(100)).doubleValue());
				holdingRepository.save(holding);
			}
		}

		BigDecimal investedValue = holdingRepository.investedValue();
		BigDecimal presentValue = holdingRepository.presentValue();
		BigDecimal pnl = presentValue.subtract(investedValue);
		BigDecimal pnlPercentage = pnl.divide(investedValue, 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

		account.setInvestedValue(investedValue.doubleValue());
		account.setCurrentValue(presentValue.doubleValue());
		account.setPnl(pnl.doubleValue());
		account.setPnlPercentage(pnlPercentage.doubleValue());
		accountRepository.save(account);

	}

	public void updateTransaction(String clientId) throws ParseException, InterruptedException {
		Account account = accountRepository.getReferenceById(clientId);
		HttpHeaders headers = getZerodhaHeader(account, false);

		Date lastUpdated = transactionRepository.lastUpdated();

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar fromDate = Calendar.getInstance();
		if (lastUpdated == null) {
			int year = fromDate.get(Calendar.YEAR);
			String date = year + "-04-01";
			Date pastDate = sdf.parse(date);
			fromDate.setTime(pastDate);
		} else {
			fromDate.setTime(lastUpdated);
			fromDate.add(Calendar.DATE, 1);
		}

		Calendar toDate = Calendar.getInstance();
		toDate.add(Calendar.DATE, -1);

		String from = sdf.format(fromDate.getTime());
		String to = sdf.format(toDate.getTime());

		List<TradebookResult> tradeResultList = getTradeResult(headers, from, to);

		if (!tradeResultList.isEmpty()) {
			for (TradebookResult result : tradeResultList) {

				String symbol = Helper.getStockSymbol(result.getExchange(), result.getTradingsymbol());
				Optional<StockInfo> stockInfo = stockInfoRepository.findById(symbol);

				if (stockInfo.isEmpty()) {
					StockInformation stockInformation = new StockInformation();
					stockInformation.setSymbol(result.getTradingsymbol());
					stockInformation.setExch_seg(result.getExchange());
					stockInformation.setName(result.getTradingsymbol());
					addStockInformation(stockInformation);
					stockInfo = stockInfoRepository.findById(symbol);
				}

				if (stockInfo.isPresent()) {
					Transaction transaction = new Transaction();
					transaction.setTransactionId(result.getTrade_id());
					transaction.setAccount(account);
					transaction.setDate(sdf.parse(result.getTrade_date()));
					transaction.setPrice(BigDecimal.valueOf(result.getPrice()));
					transaction.setQuantity(result.getQuantity());
					transaction.setStockInfo(stockInfo.get());
					transaction.setTransactionType(result.getTrade_type());
					transactionRepository.saveAndFlush(transaction);
				}

			}
		}
	}

	private List<TradebookResult> getTradeResult(HttpHeaders headers, String fromDate, String toDate) {
		List<TradebookResult> tradeResultList = new ArrayList<>();
		int pageNumber = 1;
		Long totalPages = null;
		StringBuilder builder = new StringBuilder(zerodhaApiTradebookUrl);
		builder.append(
				"&from_date={FROMDATE}&to_date={TODATE}&page={PAGENUMBER}&sort_by=order_execution_time&sort_desc=false");

		HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
		RestTemplate restTemplate = new RestTemplate();

		ResponseEntity<TradebookResponse> response = null;

		boolean isFirstPage = true;

		while (totalPages == null || totalPages-- >= 0L) {

			boolean isSuccessfulResponse = false;
			int retryCount = 0;

			Map<String, String> replacementStrings = Map.of("PAGENUMBER", String.valueOf(pageNumber++), "FROMDATE",
					fromDate, "TODATE", toDate);
			StrSubstitutor sub = new StrSubstitutor(replacementStrings, "{", "}");

			String apiString = sub.replace(builder);

			while (!isSuccessfulResponse && retryCount++ <= 10) {
				response = restTemplate.exchange(apiString, HttpMethod.GET, requestEntity, TradebookResponse.class);

				if (response != null && response.getStatusCode().is2xxSuccessful()
						&& "success".equalsIgnoreCase(response.getBody().getData().getState())) {
					isSuccessfulResponse = true;
					if (isFirstPage) {
						isFirstPage = false;
						Pagination pagination = response.getBody().getData().getPagination();
						totalPages = pagination.getTotal_pages();
					}

					tradeResultList.addAll(response.getBody().getData().getResult());
				}
			}
		}
		return tradeResultList;
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
