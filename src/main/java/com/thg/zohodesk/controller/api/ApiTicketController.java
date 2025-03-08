package com.thg.zohodesk.controller.api;

import com.thg.zohodesk.model.Ticket;
import com.thg.zohodesk.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@Slf4j
public class ApiTicketController {

    private final TicketService ticketService;

    @GetMapping
    public ResponseEntity<List<Ticket>> getAllTickets(HttpSession session) {
        String customerId = (String) session.getAttribute("customerId");
        
        if (customerId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        List<Ticket> tickets = ticketService.getAllTickets(customerId);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ticket> getTicket(@PathVariable String id, HttpSession session) {
        String customerId = (String) session.getAttribute("customerId");
        
        if (customerId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Ticket ticket = ticketService.getTicketById(id, customerId);
        
        if (ticket == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(ticket);
    }

    @PostMapping
    public ResponseEntity<Ticket> createTicket(@RequestBody Ticket ticket, HttpSession session) {
        String customerId = (String) session.getAttribute("customerId");
        
        if (customerId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Ticket createdTicket = ticketService.createTicket(ticket, customerId);
        
        if (createdTicket == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTicket);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Ticket> updateTicket(
            @PathVariable String id,
            @RequestBody Ticket ticket,
            HttpSession session) {
        
        String customerId = (String) session.getAttribute("customerId");
        
        if (customerId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Ticket updatedTicket = ticketService.updateTicket(id, ticket, customerId);
        
        if (updatedTicket == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(updatedTicket);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> deleteTicket(@PathVariable String id, HttpSession session) {
        String customerId = (String) session.getAttribute("customerId");
        
        if (customerId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        boolean deleted = ticketService.deleteTicket(id, customerId);
        
        if (!deleted) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        
        return ResponseEntity.ok(Map.of("deleted", true));
    }
}