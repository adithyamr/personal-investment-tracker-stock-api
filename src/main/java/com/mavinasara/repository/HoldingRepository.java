package com.mavinasara.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mavinasara.model.Holding;

@Repository
public interface HoldingRepository extends JpaRepository<Holding, String> {

}
