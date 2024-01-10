package com.api.calculator.stockprice.api.persistence.repository;

import java.util.List;
import java.util.UUID;

import com.api.calculator.stockprice.api.persistence.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, UUID> {
    
    @Query("select u from user u where u.email = ?1")
    List<User> findByEmail(String email);

    List<User> findByGoogleUserId(String googleUserId);
}
