package com.music.musicstore.services;

import com.music.musicstore.models.users.Customer;
import com.music.musicstore.models.users.Staff;
import com.music.musicstore.models.support.Ticket;
import com.music.musicstore.models.support.TicketMessage;
import com.music.musicstore.models.order.Order;
import com.music.musicstore.repositories.TicketRepository;
import com.music.musicstore.repositories.TicketMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TicketService {
    private final TicketRepository ticketRepository;
    private final TicketMessageRepository ticketMessageRepository;
    private final CustomerService customerService;
    @Autowired
    public TicketService(TicketRepository ticketRepository,
                                 TicketMessageRepository ticketMessageRepository,
                                     CustomerService customerService) {
        this.ticketMessageRepository = ticketMessageRepository;
        this.customerService = customerService;

                this.ticketRepository = ticketRepository;
    }
    // Create ticket with Customer object
    public Ticket createTicket(Customer customer, String subject, String initialMessage) {
        Ticket ticket = new Ticket(subject, customer, initialMessage);
        ticket = ticketRepository.save(ticket); // Save ticket first

        // Now add the initial message to the new message system
        TicketMessage initialMsg = new TicketMessage(ticket, initialMessage, customer);
        ticketMessageRepository.save(initialMsg);
        ticket.addMessage(initialMsg);

        return ticket;
    }

    // Create ticket with Customer object and priority
    public Ticket createTicket(Customer customer, String subject, String initialMessage, String priority) {
        Ticket ticket = new Ticket(subject, customer, initialMessage);

        if ("HIGH".equalsIgnoreCase(priority)) {
            ticket.setStatus("URGENT");
        } else if ("MEDIUM".equalsIgnoreCase(priority)) {
            ticket.setStatus("OPEN");
        } else {
            ticket.setStatus("OPEN"); // Default to OPEN for low priority
        }

        ticket = ticketRepository.save(ticket); // Save ticket first

        // Now add the initial message to the new message system
        TicketMessage initialMsg = new TicketMessage(ticket, initialMessage, customer);
        ticketMessageRepository.save(initialMsg);
        ticket.addMessage(initialMsg);

        return ticket;
    }

    // Create ticket with username
    public Ticket createTicket(String username, String subject, String initialMessage) {
        Customer customer = customerService.findByUsername(username);
        return createTicket(customer, subject, initialMessage);
    }

    // Create ticket with username and priority
    public Ticket createTicket(String username, String subject, String initialMessage, String priority) {
        Customer customer = customerService.findByUsername(username);
        return createTicket(customer, subject, initialMessage, priority);
    }

    // Create ticket with Order reference
    public Ticket createTicket(Customer customer, Order order, String subject, String initialMessage) {
        Ticket ticket = new Ticket(order, subject, customer, initialMessage);
        ticket = ticketRepository.save(ticket); // Save ticket first

        // Now add the initial message to the new message system
        TicketMessage initialMsg = new TicketMessage(ticket, initialMessage, customer);
        ticketMessageRepository.save(initialMsg);
        ticket.addMessage(initialMsg);

        return ticket;
    }

    // Add message to existing ticket (from customer)
    public TicketMessage addCustomerMessage(Long ticketId, String content, Customer customer) {
        Optional<Ticket> ticketOpt = ticketRepository.findById(ticketId);
        if (ticketOpt.isPresent()) {
            Ticket ticket = ticketOpt.get();
            TicketMessage message = new TicketMessage(ticket, content, customer);
            ticket.addMessage(message);
            ticketMessageRepository.save(message);
            ticketRepository.save(ticket);
            return message;
        }
        throw new RuntimeException("Ticket not found with ID: " + ticketId);
    }

    // Add message to existing ticket (from staff)
    public TicketMessage addStaffReply(Long ticketId, String content, Staff staff) {
        Optional<Ticket> ticketOpt = ticketRepository.findById(ticketId);
        if (ticketOpt.isPresent()) {
            Ticket ticket = ticketOpt.get();
            TicketMessage message = new TicketMessage(ticket, content, staff);
            ticket.addMessage(message);
            ticketMessageRepository.save(message);
            ticketRepository.save(ticket);
            return message;
        }
        throw new RuntimeException("Ticket not found with ID: " + ticketId);
    }

    // Get all messages for a ticket
    public List<TicketMessage> getTicketMessages(Long ticketId) {
        List<TicketMessage> messages = ticketMessageRepository.findByTicket_IdOrderByTimestampAsc(ticketId);
        messages.forEach(this::populateMessageTransientFields);
        return messages;
    }

    // Get all tickets
    public List<Ticket> getAllTickets() {
        List<Ticket> tickets = ticketRepository.findAll();
        tickets.forEach(this::populateTransientFields);
        return tickets;
    }

    // Get ticket by ID
    public Optional<Ticket> getTicketById(Long id) {
        Optional<Ticket> ticket = ticketRepository.findById(id);
        ticket.ifPresent(this::populateTransientFields);
        return ticket;
    }

    // Get tickets by username
    public List<Ticket> getTicketsByUsername(String username) {
        Customer customer = customerService.findByUsername(username);
        List<Ticket> tickets = ticketRepository.findByCustomer(customer);
        tickets.forEach(this::populateTransientFields);
        return tickets;
    }

    // Get tickets by status
    public List<Ticket> getTicketsByStatus(String status) {
        List<Ticket> tickets = ticketRepository.findByStatus(status);
        tickets.forEach(this::populateTransientFields);
        return tickets;
    }

    // Get unassigned tickets
    public List<Ticket> getUnassignedTickets() {
        List<Ticket> tickets = ticketRepository.findByAssignedStaffIsNull();
        tickets.forEach(this::populateTransientFields);
        return tickets;
    }

    // Get urgent tickets
    public List<Ticket> getUrgentTickets() {
        List<Ticket> tickets = ticketRepository.findUrgentTickets();
        tickets.forEach(this::populateTransientFields);
        return tickets;
    }

    // Get tickets needing attention
    public List<Ticket> getTicketsNeedingAttention() {
        List<Ticket> tickets = ticketRepository.findTicketsNeedingAttention();
        tickets.forEach(this::populateTransientFields);
        return tickets;
    }

    // Search tickets
    public List<Ticket> searchTickets(String searchTerm) {
        List<Ticket> tickets = ticketRepository.findBySubjectOrMessageContentContaining(searchTerm);
        tickets.forEach(this::populateTransientFields);
        return tickets;
    }

    // Update ticket status
    public Ticket updateTicketStatus(Long ticketId, String status) {
        Optional<Ticket> ticketOpt = ticketRepository.findById(ticketId);
        if (ticketOpt.isPresent()) {
            Ticket ticket = ticketOpt.get();
            ticket.setStatus(status);
            Ticket savedTicket = ticketRepository.save(ticket);
            populateTransientFields(savedTicket);
            return savedTicket;
        }
        throw new RuntimeException("Ticket not found with ID: " + ticketId);
    }

    // Assign ticket to staff
    public Ticket assignTicket(Long ticketId, Staff staff) {
        Optional<Ticket> ticketOpt = ticketRepository.findById(ticketId);
        if (ticketOpt.isPresent()) {
            Ticket ticket = ticketOpt.get();
            ticket.setAssignedStaff(staff);
            if ("OPEN".equals(ticket.getStatus())) {
                ticket.setStatus("IN_PROGRESS");
            }
            Ticket savedTicket = ticketRepository.save(ticket);
            populateTransientFields(savedTicket);
            return savedTicket;
        }
        throw new RuntimeException("Ticket not found with ID: " + ticketId);
    }


    // Close ticket
    public Ticket closeTicket(Long ticketId) {
        return updateTicketStatus(ticketId, "CLOSED");
    }

    // Reopen ticket
    public Ticket reopenTicket(Long ticketId) {
        return updateTicketStatus(ticketId, "OPEN");
    }

    // Get ticket statistics
    public List<Object[]> getStatusDistribution() {
        return ticketRepository.getStatusDistribution();
    }

    // NEW: Missing method for admin analytics
    public String getAverageResolutionTime() {
        try {
            // This would need proper implementation to calculate average resolution time
            // For now, return a placeholder value
            return "2.5 days"; // Placeholder
        } catch (Exception e) {
            return "N/A";
        }
    }

    // Helper method to populate transient fields for safe JSON serialization
    private void populateTransientFields(Ticket ticket) {
        if (ticket.getCustomer() != null) {
            ticket.setCustomerName(ticket.getCustomer().getUsername());
        }
        if (ticket.getAssignedStaff() != null) {
            ticket.setAssignedStaffName(ticket.getAssignedStaff().getUsername());
        }
    }

    // Helper method to populate transient fields for messages
    private void populateMessageTransientFields(TicketMessage message) {
        if (message.getTicket() != null) {
            message.setTicketId(message.getTicket().getId());
        }
        if (message.getCustomer() != null) {
            message.setCustomerName(message.getCustomer().getUsername());
        }
        if (message.getStaff() != null) {
            message.setStaffName(message.getStaff().getUsername());
        }
    }

    // Count tickets by status
    public long countTicketsByStatus(String status) {
        return ticketRepository.countByStatus(status);
    }

    // Count tickets for customer
    public long countTicketsForCustomer(Customer customer) {
        return ticketRepository.countByCustomer(customer);
    }

    // Delete ticket (admin only)
    public void deleteTicket(Long ticketId) {
        ticketRepository.deleteById(ticketId);
    }
}
