package com.thg.zohodesk.service;

import com.thg.zohodesk.model.OAuthToken;
import com.thg.zohodesk.repository.OAuthTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuthServiceImpl implements OAuthService {

    private final OAuthTokenRepository oAuthTokenRepository;
    private final RestTemplate restTemplate;
    
    @Value("${zoho.client.id}")
    private String clientId;
    
    @Value("${zoho.client.secret}")
    private String clientSecret;
    
    @Value("${zoho.redirect.uri}")
    private String redirectUri;
    
    @Value("${zoho.auth.url}")
    private String authUrl;
    
    @Value("${zoho.token.url}")
    private String tokenUrl;
    
    @Override
    public String getAuthorizationUrl() {
        return authUrl + "?"
                + "scope=Desk.tickets.CREATE,Desk.tickets.READ,Desk.tickets.UPDATE,Desk.tickets.DELETE,Desk.basic.READ"
                + "&client_id=" + clientId
                + "&response_type=code"
                + "&access_type=offline"
                + "&redirect_uri=" + redirectUri;
    }
    
    @Override
    public OAuthToken exchangeCodeForToken(String code, String customerId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "authorization_code");
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("redirect_uri", redirectUri);
        map.add("code", code);
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, request, String.class);
            return parseTokenResponse(response.getBody(), customerId);
        } catch (Exception e) {
            log.error("Error exchanging code for token: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public OAuthToken refreshAccessToken(String customerId) {
        OAuthToken token = getTokenByCustomerId(customerId);
        
        if (token == null) {
            log.error("No token found for customer: {}", customerId);
            return null;
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "refresh_token");
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("refresh_token", token.getRefreshToken());
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, request, String.class);
            
            OAuthToken newToken = parseTokenResponse(response.getBody(), customerId);
            newToken.setId(token.getId());
            newToken.setZohoUserId(token.getZohoUserId());
            
            // If refresh token is not returned in the response, use the old one
            if (newToken.getRefreshToken() == null || newToken.getRefreshToken().isEmpty()) {
                newToken.setRefreshToken(token.getRefreshToken());
            }
            
            saveToken(newToken);
            return newToken;
        } catch (Exception e) {
            log.error("Error refreshing token: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public OAuthToken getTokenByCustomerId(String customerId) {
        Optional<OAuthToken> tokenOpt = oAuthTokenRepository.findByCustomerId(customerId);
        return tokenOpt.orElse(null);
    }
    
    @Override
    public void saveToken(OAuthToken token) {
        oAuthTokenRepository.save(token);
    }
    
    @Override
    public boolean isTokenValid(OAuthToken token) {
        if (token == null || token.getAccessToken() == null || token.getAccessTokenExpiry() == null) {
            return false;
        }
        
        // Check if token is expired (with 5 minute buffer)
        return token.getAccessTokenExpiry().isAfter(LocalDateTime.now().plusMinutes(5));
    }
    
    private OAuthToken parseTokenResponse(String responseBody, String customerId) {
        JSONObject jsonResponse = new JSONObject(responseBody);
        
        OAuthToken token = new OAuthToken();
        token.setCustomerId(customerId);
        token.setAccessToken(jsonResponse.getString("access_token"));
        
        // Set refresh token if available
        if (jsonResponse.has("refresh_token")) {
            token.setRefreshToken(jsonResponse.getString("refresh_token"));
        }
        
        // Calculate token expiry time
        int expiresIn = jsonResponse.getInt("expires_in");
        token.setAccessTokenExpiry(LocalDateTime.now().plusSeconds(expiresIn));
        
        return token;
    }
}    