package com.mavinasara.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mavinasara.model.UserInfo;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, String> {

}
