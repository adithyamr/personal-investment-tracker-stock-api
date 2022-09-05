package com.mavinasara.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.mavinasara.model.StockInfo;

@Repository
public interface ShareRepository extends MongoRepository<StockInfo, String> {

}