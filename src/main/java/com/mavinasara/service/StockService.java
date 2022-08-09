package com.mavinasara.service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
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
import com.mavinasara.model.Transaction;
import com.mavinasara.model.zerodha.TradebookResponse;

import yahoofinance.YahooFinance;

@Service
public class StockService {

	@Value("${zerodha.console.url}")
	private String url = "https://console.zerodha.com/dashboard";

	@Value("${zerodha.api.base.url}")
	private String apiBaseUrl = "https://console.zerodha.com/api/reports/tradebook?segment=EQ";

	public static void main(String[] args) throws InterruptedException, IOException {

		addTransaction(null);
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
		ChromeOptions options = new ChromeOptions();
		options.setHeadless(true);
		WebDriver driver = new ChromeDriver(options);

		driver.get(url);

		WebElement username = driver.findElement(By.id("userid"));
		WebElement password = driver.findElement(By.id("password"));
		WebElement login = driver.findElement(By.xpath("//button[text()='Login ']"));
		username.sendKeys(account.getAccountNumber());
		password.sendKeys(account.getPassword());
		login.click();

		Thread.sleep(5000);

		WebElement pin = driver.findElement(By.id("pin"));
		WebElement pinSubmit = driver.findElement(By.xpath("//button[text()='Continue ']"));
		pin.sendKeys(account.getPin());
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

		StringBuilder builder = new StringBuilder(apiBaseUrl);
		builder.append("&from_date=").append(yesterday).append("&to_date=").append(yesterday);

		ResponseEntity<TradebookResponse> response = restTemplate.exchange(builder.toString(), HttpMethod.GET,
				requestEntity, TradebookResponse.class);

		TradebookResponse tradebookResponse = response.getBody();
		return tradebookResponse;
	}

	public static void addTransaction(List<Transaction> transactions) throws IOException, InterruptedException {
		ChromeOptions options = new ChromeOptions();
		options.setHeadless(true);
		WebDriver driver = new ChromeDriver(options);

		// And now use this to visit Google
		driver.get("https://console.zerodha.com/dashboard");

//		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(50000));

		WebElement username = driver.findElement(By.id("userid"));
		WebElement password = driver.findElement(By.id("password"));
		WebElement login = driver.findElement(By.xpath("//button[text()='Login ']"));
		username.sendKeys("ZM3272");
		password.sendKeys("Adithy@1985");
		login.click();

		Thread.sleep(5000);

		WebElement pin = driver.findElement(By.id("pin"));
		WebElement pinSubmit = driver.findElement(By.xpath("//button[text()='Continue ']"));
//		wait.until(ExpectedConditions.visibilityOf(pin));
		pin.sendKeys("271016");
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
		previousDate.add(Calendar.DATE, -1);

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
