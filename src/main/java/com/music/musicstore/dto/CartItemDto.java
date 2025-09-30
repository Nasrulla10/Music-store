package com.music.musicstore.dto;

import java.math.BigDecimal;

public class CartItemDto {
    private Long id;
    private MusicDto music;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;

    // Constructors
    public CartItemDto() {}

    public CartItemDto(Long id, MusicDto music, BigDecimal unitPrice, BigDecimal totalPrice) {
        this.id = id;
        this.music = music;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public MusicDto getMusic() { return music; }
    public void setMusic(MusicDto music) { this.music = music; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
}
