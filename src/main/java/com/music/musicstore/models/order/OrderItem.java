package com.music.musicstore.models.order;


import com.music.musicstore.models.music.Music;
import com.music.musicstore.models.cart.CartItem;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnoreProperties({"orderItems"})
    private Order order;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties({"artist", "reviews", "orderItems", "cartItems"})
    private Music music;


    @NotNull(message = "Unit price is required")
    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;

    // Snapshot of music details at time of purchase
    @Column(name = "music_title")
    private String musicTitle;

    @Column(name = "artist_name")
    private String artistName;

    // Default constructor required by JPA
    public OrderItem() {
    }

    public OrderItem(Music music) {
        this.music = music;
        this.unitPrice = music.getPrice();
    }

    public OrderItem(CartItem cartItem) {
        this.music = cartItem.getMusic();
        this.unitPrice = cartItem.getUnitPrice();
        this.musicTitle = cartItem.getMusic().getName();
        this.artistName = cartItem.getMusic().getArtistUsername();
    }

    // Calculate subtotal
    public BigDecimal getSubtotal() {
        return unitPrice;
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

    public Music getProduct() {
        return music;
    }

    public void setProduct(Music music) {
        this.music = music;
    }

    public void setMusicTitle(String musicTitle) {
        this.musicTitle = musicTitle;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getMusicTitle() {
        return musicTitle;
    }

    public String getArtistName() {
        return artistName;
    }
}
