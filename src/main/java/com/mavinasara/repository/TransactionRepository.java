package com.mavinasara.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mavinasara.model.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

}
