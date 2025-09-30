package com.music.musicstore.models.support;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.music.musicstore.models.users.Customer;
import com.music.musicstore.models.users.Staff;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_messages")
public class TicketMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ticket_id", nullable = false)
    @JsonIgnore  // Always ignore ticket reference to prevent circular reference
    private Ticket ticket;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(nullable = false)
    private boolean isFromStaff = false;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = true)
    @JsonIgnore  // Ignore to prevent circular reference
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "staff_id", nullable = true)
    @JsonIgnore  // Ignore to prevent circular reference
    private Staff staff;

    // Add these transient fields for safe JSON serialization
    @Transient
    private Long ticketId;

    @Transient
    private String customerName;

    @Transient
    private String staffName;

    // Constructors
    public TicketMessage() {}

    public TicketMessage(Ticket ticket, String content, Customer customer) {
        this.ticket = ticket;
        this.content = content;
        this.customer = customer;
        this.isFromStaff = false;
        if (ticket != null) {
            this.ticketId = ticket.getId();
        }
        if (customer != null) {
            this.customerName = customer.getUsername();
        }
    }

    public TicketMessage(Ticket ticket, String content, Staff staff) {
        this.ticket = ticket;
        this.content = content;
        this.staff = staff;
        this.isFromStaff = true;
        if (ticket != null) {
            this.ticketId = ticket.getId();
        }
        if (staff != null) {
            this.staffName = staff.getUsername();
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isFromStaff() {
        return isFromStaff;
    }

    public void setFromStaff(boolean fromStaff) {
        isFromStaff = fromStaff;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Staff getStaff() {
        return staff;
    }

    public void setStaff(Staff staff) {
        this.staff = staff;
    }

    public Long getTicketId() {
        if (ticketId == null && ticket != null) {
            ticketId = ticket.getId();
        }
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public String getCustomerName() {
        if (customerName == null && customer != null) {
            customerName = customer.getUsername();
        }
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getStaffName() {
        if (staffName == null && staff != null) {
            staffName = staff.getUsername();
        }
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }
}
