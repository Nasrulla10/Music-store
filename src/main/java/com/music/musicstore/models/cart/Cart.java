package com.music.musicstore.models.cart;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.music.musicstore.models.users.Customer;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts")
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "customer_id")
    @JsonIgnore  // Always ignore customer reference to prevent circular reference
    private Customer customer;

    @Column
    private BigDecimal totalAmount;

    @OneToMany(mappedBy = "cart")
    @JsonIgnore  // Ignore cart items in basic cart serialization
    private List<CartItem> items = new ArrayList<>();

    // Add transient field for safe serialization
    @Transient
    private String customerName;

    public Cart() {}

    public Cart(Customer customer) {
        this.customer = customer;
        if (customer != null) {
            this.customerName = customer.getUsername();
        }
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Long getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public StringBuilder getItemList() {
        StringBuilder list = new StringBuilder();
        for (CartItem item : items) {
            list.append(item.getMusic().getName()).append(", ");
        }
        return list;
    }

    public void addItem(CartItem item) {
        items.add(item);
        item.setCart(this);
    }

    public void removeItem(CartItem item) {
        items.remove(item);
        item.setCart(null);
    }

    @Transient
    public BigDecimal getTotal() {
        return items.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void calculateTotalAmount() {
        this.totalAmount = getTotal();
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
}
