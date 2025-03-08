package com.thg.zohodesk.controller.api;

import com.thg.zohodesk.model.Customer;
import com.thg.zohodesk.model.OAuthToken;
import com.thg.zohodesk.service.CustomerService;
import com.thg.zohodesk.service.OAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class ApiAuthController {

    private final OAuthService oAuthService;
    private final CustomerService customerService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials, HttpSession session) {
        String email = credentials.get("email");
        Map<String, Object> response = new HashMap<>();

        if (email == null || email.isEmpty()) {
            response.put("success", false);
            response.put("message", "Email is required");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            Customer customer = customerService.getCustomerByEmail(email);

            if (customer == null) {
                // Create a new customer
                customer = new Customer();
                customer.setEmail(email);
                customer = customerService.createCustomer(customer);
            }

            // Store customer ID in session
            session.setAttribute("customerId", customer.getId().toString());

            // Check if customer already has a token
            OAuthToken token = oAuthService.getTokenByCustomerId(customer.getId().toString());

            if (token != null && oAuthService.isTokenValid(token)) {
                // Token exists and is valid
                response.put("success", true);
                response.put("authenticated", true);
                response.put("user", Map.of("id", customer.getId(), "email", customer.getEmail()));
                session.setAttribute("authenticated", true);
                session.setAttribute("userId", customer.getId().toString());
                return ResponseEntity.ok(response);
            } else {
                // Redirect to Zoho OAuth authorization
                String authUrl = oAuthService.getAuthorizationUrl();
                response.put("success", true);
                response.put("authenticated", false);
                response.put("redirectUrl", authUrl);
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            log.error("Login error: ", e);
            response.put("success", false);
            response.put("message", "Authentication failed. Please try again.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        String customerId = (String) session.getAttribute("customerId");

        if (customerId == null) {
            response.put("authenticated", false);
            return ResponseEntity.ok(response);
        }

        try {
            Customer customer = customerService.getCustomerById(Long.valueOf(customerId));
            OAuthToken token = oAuthService.getTokenByCustomerId(customerId);

            if (customer != null && token != null && oAuthService.isTokenValid(token)) {
                response.put("authenticated", true);
                response.put("user", Map.of("id", customer.getId(), "email", customer.getEmail()));
            } else {
                response.put("authenticated", false);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error checking authentication status: ", e);
            response.put("authenticated", false);
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpSession session) {
        session.invalidate();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/callback")
    public ResponseEntity<Map<String, Object>> handleOAuthCallback(@RequestParam String code, HttpSession session) {
        String customerId = (String) session.getAttribute("customerId");
        Map<String, Object> response = new HashMap<>();

        if (customerId == null) {
            response.put("success", false);
            response.put("message", "Session expired. Please login again.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
            OAuthToken token = oAuthService.exchangeCodeForToken(code, customerId);
            oAuthService.saveToken(token);

            // Get customer for response
            Customer customer = customerService.getCustomerById(Long.valueOf(customerId));

            response.put("success", true);
            response.put("authenticated", true);
            response.put("message", "Authentication successful");
            response.put("user", Map.of("id", customer.getId(), "email", customer.getEmail()));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error during OAuth callback: ", e);
            response.put("success", false);
            response.put("message", "Authentication failed. Please try again.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}