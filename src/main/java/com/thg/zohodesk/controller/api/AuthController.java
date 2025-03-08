package com.thg.zohodesk.controller.api;

import com.thg.zohodesk.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> getUserInfo(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return ResponseEntity.ok(authService.getUserInfo(authentication));
        }
        return ResponseEntity.ok(Map.of("authenticated", false));
    }
    
    @GetMapping("/zoho-url")
    public ResponseEntity<Map<String, String>> getZohoAuthUrl(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String authUrl = authService.generateZohoAuthUrl(authentication);
            return ResponseEntity.ok(Map.of("url", authUrl));
        }
        return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
    }
}