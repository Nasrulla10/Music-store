package com.music.musicstore.repositories;

import com.music.musicstore.models.cart.Cart;
import com.music.musicstore.models.cart.CartItem;
import com.music.musicstore.models.music.Music;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCart(Cart cart);
    Optional<CartItem> findByCartAndMusic(Cart cart, Music music);
}
