package com.music.musicstore.models.support;

import com.music.musicstore.models.order.Order;
import com.music.musicstore.models.users.Customer;
import com.music.musicstore.models.users.Staff;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tickets")
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = true)
    @JsonIgnore  // Prevent potential circular reference with Order
    private Order order;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnore  // Prevent circular reference with Customer
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "assigned_staff_id", nullable = true)
    @JsonIgnore  // Prevent circular reference with Staff
    private Staff assignedStaff;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false)
    private String status = "OPEN";

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = true)
    private LocalDateTime closedAt;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderBy("timestamp ASC")
    @JsonIgnore  // Always ignore messages in ticket serialization
    private List<TicketMessage> messages = new ArrayList<>();

    // Add these fields for safe JSON serialization
    @Transient
    private String customerName;

    @Transient
    private String assignedStaffName;

    // Constructors
    public Ticket() {}

    public Ticket(Order order, String subject, Customer customer, String initialMessage) {
        this.order = order;
        this.subject = subject;
        this.customer = customer;
        if (customer != null) {
            this.customerName = customer.getUsername();
        }
    }

    public Ticket(String subject, Customer customer, String initialMessage) {
        this.subject = subject;
        this.customer = customer;
        if (customer != null) {
            this.customerName = customer.getUsername();
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        if ("CLOSED".equals(status) && closedAt == null) {
            this.closedAt = LocalDateTime.now();
        }
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Staff getAssignedStaff() {
        return assignedStaff;
    }

    public void setAssignedStaff(Staff assignedStaff) {
        this.assignedStaff = assignedStaff;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }

    public List<TicketMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<TicketMessage> messages) {
        this.messages = messages;
    }

    // Helper methods
    public void addMessage(TicketMessage message) {
        this.messages.add(message);
        message.setTicket(this);
    }

    // Remove the problematic getLastMessage() method completely
    // Add safe methods for JSON serialization
    public String getCustomerName() {
        if (customerName == null && customer != null) {
            customerName = customer.getUsername();
        }
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getAssignedStaffName() {
        if (assignedStaffName == null && assignedStaff != null) {
            assignedStaffName = assignedStaff.getUsername();
        }
        return assignedStaffName;
    }

    public void setAssignedStaffName(String assignedStaffName) {
        this.assignedStaffName = assignedStaffName;
    }

    @JsonIgnore
    public String getInitialMessage() {
        if (!messages.isEmpty()) {
            return messages.get(0).getContent();
        }
        return "";
    }

    public boolean isAssigned() {
        return assignedStaff != null;
    }

    public boolean isClosed() {
        return "CLOSED".equals(status);
    }
}
