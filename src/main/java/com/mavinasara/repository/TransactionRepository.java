package com.mavinasara.repository;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.mavinasara.model.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

	@Query(value = "SELECT max(lastUpdated) FROM Transaction")
	public Date lastUpdated();

}
