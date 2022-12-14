package com.mavinasara.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity
public class Account {

	@Id
	private String accountNumber;

	private String password;

	private String pin;

	private String key;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "pan")
	private UserInfo userInfo;

	@OneToMany
	@JoinColumn(name = "accountNumber")
	private List<Holding> holdings;

	@OneToMany
	@JoinColumn(name = "accountNumber")
	private List<Transaction> transactions;

	private Double investedValue;

	private Double currentValue;

	private Double pnl;

	private Double pnlPercentage;

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPin() {
		return pin;
	}

	public void setPin(String pin) {
		this.pin = pin;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public UserInfo getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(UserInfo userInfo) {
		this.userInfo = userInfo;
	}

	public List<Holding> getHoldings() {
		return holdings;
	}

	public void setHoldings(List<Holding> holdings) {
		this.holdings = holdings;
	}

	public List<Transaction> getTransactions() {
		return transactions;
	}

	public void setTransactions(List<Transaction> transactions) {
		this.transactions = transactions;
	}

	public Double getInvestedValue() {
		return investedValue;
	}

	public void setInvestedValue(Double investedValue) {
		this.investedValue = investedValue;
	}

	public Double getCurrentValue() {
		return currentValue;
	}

	public void setCurrentValue(Double currentValue) {
		this.currentValue = currentValue;
	}

	public Double getPnl() {
		return pnl;
	}

	public void setPnl(Double pnl) {
		this.pnl = pnl;
	}

	public Double getPnlPercentage() {
		return pnlPercentage;
	}

	public void setPnlPercentage(Double pnlPercentage) {
		this.pnlPercentage = pnlPercentage;
	}

}
