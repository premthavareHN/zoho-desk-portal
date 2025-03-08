package com.thg.zohodesk.service;

import com.thg.zohodesk.model.OAuthToken;

public interface OAuthService {
    String getAuthorizationUrl();
    OAuthToken exchangeCodeForToken(String code, String customerId);
    OAuthToken refreshAccessToken(String customerId);
    OAuthToken getTokenByCustomerId(String customerId);
    void saveToken(OAuthToken token);
    boolean isTokenValid(OAuthToken token);
}