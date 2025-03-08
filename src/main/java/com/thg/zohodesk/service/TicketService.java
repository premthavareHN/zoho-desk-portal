package com.thg.zohodesk.service;

import com.thg.zohodesk.model.Ticket;

import java.util.List;

public interface TicketService {
    List<Ticket> getAllTickets(String customerId);
    Ticket getTicketById(String id, String customerId);
    Ticket createTicket(Ticket ticket, String customerId);
    Ticket updateTicket(String id, Ticket ticket, String customerId);
    boolean deleteTicket(String id, String customerId);
}