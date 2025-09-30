package com.music.musicstore.dto;

import java.math.BigDecimal;
import java.util.List;

public class CartDto {
    private Long id;
    private List<CartItemDto> items;
    private BigDecimal total;
    private String customerUsername;

    // Constructors
    public CartDto() {}

    public CartDto(Long id, List<CartItemDto> items, BigDecimal total, String customerUsername) {
        this.id = id;
        this.items = items;
        this.total = total;
        this.customerUsername = customerUsername;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public List<CartItemDto> getItems() { return items; }
    public void setItems(List<CartItemDto> items) { this.items = items; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public String getCustomerUsername() { return customerUsername; }
    public void setCustomerUsername(String customerUsername) { this.customerUsername = customerUsername; }
}
