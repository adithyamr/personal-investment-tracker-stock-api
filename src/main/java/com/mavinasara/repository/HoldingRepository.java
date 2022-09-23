package com.mavinasara.repository;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.mavinasara.model.Holding;

@Repository
public interface HoldingRepository extends JpaRepository<Holding, String> {

	@Query(value = "SELECT sum(buyValue) FROM Holding")
	public BigDecimal investedValue();

	@Query(value = "SELECT sum(presentValue) FROM Holding")
	public BigDecimal presentValue();

}
