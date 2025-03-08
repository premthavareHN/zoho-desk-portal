package com.thg.zohodesk.service;

import com.thg.zohodesk.model.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private OAuthService oauthService;
    
    @Autowired
    private CustomerService customerService;
    
    @Value("${app.zoho.auth-callback-url}")
    private String zohoAuthCallbackUrl;

    /**
     * Generate the Zoho authorization URL for a user authenticated with Okta
     */
    public String generateZohoAuthUrl(Authentication authentication) {
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2User oauth2User = ((OAuth2AuthenticationToken) authentication).getPrincipal();
            String email = oauth2User.getAttribute("email");
            
            // Find or create customer in our database
            Customer customer = findOrCreateCustomer(oauth2User);
            
            // Generate Zoho auth URL with state parameter containing customer ID
            return oauthService.generateAuthorizationUrl(customer.getId().toString());
        }
        
        throw new IllegalArgumentException("User not properly authenticated with Okta");
    }
    
    /**
     * Find or create a customer record based on Okta user information
     */
    private Customer findOrCreateCustomer(OAuth2User oauth2User) {
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        
        Optional<Customer> existingCustomer = customerService.findByEmail(email);
        
        if (existingCustomer.isPresent()) {
            return existingCustomer.get();
        } else {
            Customer newCustomer = new Customer();
            newCustomer.setEmail(email);
            newCustomer.setName(name);
            return customerService.save(newCustomer);
        }
    }
    
    /**
     * Get user information from the current authentication
     */
    public Map<String, Object> getUserInfo(Authentication authentication) {
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2User oauth2User = ((OAuth2AuthenticationToken) authentication).getPrincipal();
            
            String email = oauth2User.getAttribute("email");
            Optional<Customer> customer = customerService.findByEmail(email);
            
            Map<String, Object> userInfo = Map.of(
                "email", email,
                "name", oauth2User.getAttribute("name"),
                "authenticated", authentication.isAuthenticated()
            );
            
            if (customer.isPresent()) {
                Map<String, Object> additionalInfo = Map.of(
                    "customerId", customer.get().getId(),
                    "zohoConnected", customerService.hasZohoConnection(customer.get().getId())
                );
                
                // Combine the maps
                return new HashMap<>(userInfo) {{
                    putAll(additionalInfo);
                }};
            }
            
            return userInfo;
        }
        
        return Map.of("authenticated", authentication.isAuthenticated());
    }
}