package com.mavinasara.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mavinasara.model.StockInfo;

@Repository
public interface ShareRepository extends JpaRepository<StockInfo, String> {

}
