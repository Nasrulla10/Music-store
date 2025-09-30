package com.music.musicstore.models.cart;

import com.music.musicstore.models.music.Music;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "cart_items")
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "music_id")
    private Music music;


    @Column(nullable = false)
    private BigDecimal unitPrice;

    @Column()
    private String added_at;


    public CartItem() {}

    public CartItem(Music music) {
        this.music = music;
        this.unitPrice = music.getPrice();
    }

    public Long getId() {
        return id;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public Music getMusic() {
        return music;
    }

    public void setMusic(Music music) {
        this.music = music;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    @Transient
    public BigDecimal getTotalPrice() {
        return unitPrice;
    }
}
