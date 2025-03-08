package com.thg.zohodesk.service;

import com.thg.zohodesk.model.OAuthToken;
import com.thg.zohodesk.model.Ticket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketServiceImpl implements TicketService {

    private final OAuthService oAuthService;
    private final RestTemplate restTemplate;
    
    @Value("${zoho.api.base.url}")
    private String apiBaseUrl;

    @Override
    public List<Ticket> getAllTickets(String customerId) {
        try {
            OAuthToken token = getValidToken(customerId);
            if (token == null) {
                return new ArrayList<>();
            }
            
            HttpHeaders headers = createAuthHeaders(token.getAccessToken());
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                    apiBaseUrl + "/tickets",
                    HttpMethod.GET,
                    entity,
                    String.class
            );
            
            return parseTicketList(response.getBody());
        } catch (Exception e) {
            log.error("Error fetching tickets: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public Ticket getTicketById(String id, String customerId) {
        try {
            OAuthToken token = getValidToken(customerId);
            if (token == null) {
                return null;
            }
            
            HttpHeaders headers = createAuthHeaders(token.getAccessToken());
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                    apiBaseUrl + "/tickets/" + id,
                    HttpMethod.GET,
                    entity,
                    String.class
            );
            
            return parseTicket(response.getBody());
        } catch (Exception e) {
            log.error("Error fetching ticket by id: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public Ticket createTicket(Ticket ticket, String customerId) {
        try {
            OAuthToken token = getValidToken(customerId);
            if (token == null) {
                return null;
            }
            
            HttpHeaders headers = createAuthHeaders(token.getAccessToken());
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            JSONObject requestBody = new JSONObject();
            requestBody.put("subject", ticket.getSubject());
            requestBody.put("description", ticket.getDescription());
            
            if (ticket.getDepartmentId() != null) {
                requestBody.put("departmentId", ticket.getDepartmentId());
            }
            
            if (ticket.getStatus() != null) {
                requestBody.put("status", ticket.getStatus());
            }
            
            if (ticket.getPriority() != null) {
                requestBody.put("priority", ticket.getPriority());
            }
            
            if (ticket.getCategory() != null) {
                requestBody.put("category", ticket.getCategory());
            }
            
            if (ticket.getSubCategory() != null) {
                requestBody.put("subCategory", ticket.getSubCategory());
            }
            
            HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                    apiBaseUrl + "/tickets",
                    HttpMethod.POST,
                    entity,
                    String.class
            );
            
            return parseTicket(response.getBody());
        } catch (Exception e) {
            log.error("Error creating ticket: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public Ticket updateTicket(String id, Ticket ticket, String customerId) {
        try {
            OAuthToken token = getValidToken(customerId);
            if (token == null) {
                return null;
            }
            
            HttpHeaders headers = createAuthHeaders(token.getAccessToken());
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            JSONObject requestBody = new JSONObject();
            
            if (ticket.getSubject() != null) {
                requestBody.put("subject", ticket.getSubject());
            }
            
            if (ticket.getDescription() != null) {
                requestBody.put("description", ticket.getDescription());
            }
            
            if (ticket.getDepartmentId() != null) {
                requestBody.put("departmentId", ticket.getDepartmentId());
            }
            
            if (ticket.getStatus() != null) {
                requestBody.put("status", ticket.getStatus());
            }
            
            if (ticket.getPriority() != null) {
                requestBody.put("priority", ticket.getPriority());
            }
            
            if (ticket.getCategory() != null) {
                requestBody.put("category", ticket.getCategory());
            }
            
            if (ticket.getSubCategory() != null) {
                requestBody.put("subCategory", ticket.getSubCategory());
            }
            
            HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                    apiBaseUrl + "/tickets/" + id,
                    HttpMethod.PATCH,
                    entity,
                    String.class
            );
            
            return parseTicket(response.getBody());
        } catch (Exception e) {
            log.error("Error updating ticket: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public boolean deleteTicket(String id, String customerId) {
        try {
            OAuthToken token = getValidToken(customerId);
            if (token == null) {
                return false;
            }
            
            HttpHeaders headers = createAuthHeaders(token.getAccessToken());
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                    apiBaseUrl + "/tickets/" + id,
                    HttpMethod.DELETE,
                    entity,
                    String.class
            );
            
            return response.getStatusCode() == HttpStatus.NO_CONTENT;
        } catch (Exception e) {
            log.error("Error deleting ticket: {}", e.getMessage(), e);
            return false;
        }
    }

    private OAuthToken getValidToken(String customerId) {
        OAuthToken token = oAuthService.getTokenByCustomerId(customerId);
        
        if (token == null) {
            return null;
        }
        
        if (!oAuthService.isTokenValid(token)) {
            token = oAuthService.refreshAccessToken(customerId);
        }
        
        return token;
    }

    private HttpHeaders createAuthHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("orgId", ""); // Set your org ID if needed
        return headers;
    }

    private List<Ticket> parseTicketList(String responseBody) {
        List<Ticket> tickets = new ArrayList<>();
        JSONObject jsonObject = new JSONObject(responseBody);
        JSONArray ticketsArray = jsonObject.getJSONArray("data");
        
        for (int i = 0; i < ticketsArray.length(); i++) {
            JSONObject ticketJson = ticketsArray.getJSONObject(i);
            Ticket ticket = new Ticket();
            
            ticket.setId(ticketJson.getString("id"));
            ticket.setSubject(ticketJson.optString("subject"));
            ticket.setDescription(ticketJson.optString("description"));
            ticket.setStatus(ticketJson.optString("status"));
            ticket.setDepartmentId(ticketJson.optString("departmentId"));
            ticket.setContactId(ticketJson.optString("contactId"));
            ticket.setPriority(ticketJson.optString("priority"));
            ticket.setCategory(ticketJson.optString("category"));
            ticket.setSubCategory(ticketJson.optString("subCategory"));
            ticket.setCreatedTime(ticketJson.optString("createdTime"));
            ticket.setModifiedTime(ticketJson.optString("modifiedTime"));
            
            tickets.add(ticket);
        }
        
        return tickets;
    }

    private Ticket parseTicket(String responseBody) {
        JSONObject jsonObject = new JSONObject(responseBody);
        JSONObject ticketJson = jsonObject.getJSONObject("data");
        
        Ticket ticket = new Ticket();
        ticket.setId(ticketJson.getString("id"));
        ticket.setSubject(ticketJson.optString("subject"));
        ticket.setDescription(ticketJson.optString("description"));
        ticket.setStatus(ticketJson.optString("status"));
        ticket.setDepartmentId(ticketJson.optString("departmentId"));
        ticket.setContactId(ticketJson.optString("contactId"));
        ticket.setPriority(ticketJson.optString("priority"));
        ticket.setCategory(ticketJson.optString("category"));
        ticket.setSubCategory(ticketJson.optString("subCategory"));
        ticket.setCreatedTime(ticketJson.optString("createdTime"));
        ticket.setModifiedTime(ticketJson.optString("modifiedTime"));
        
        return ticket;
    }
}