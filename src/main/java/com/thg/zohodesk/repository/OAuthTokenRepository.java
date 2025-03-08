package com.thg.zohodesk.repository;

import com.thg.zohodesk.model.OAuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OAuthTokenRepository extends JpaRepository<OAuthToken, Long> {
    Optional<OAuthToken> findByCustomerId(String customerId);
    Optional<OAuthToken> findByZohoUserId(String zohoUserId);
}