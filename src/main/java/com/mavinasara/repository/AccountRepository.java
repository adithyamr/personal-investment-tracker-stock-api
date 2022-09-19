package com.mavinasara.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mavinasara.model.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {

}
