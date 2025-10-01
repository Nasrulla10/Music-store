package com.music.musicstore.api;

import com.music.musicstore.dto.CartDto;
import com.music.musicstore.dto.CartItemDto;
import com.music.musicstore.dto.MusicDto;
import com.music.musicstore.models.cart.Cart;
import com.music.musicstore.models.cart.CartItem;
import com.music.musicstore.models.music.Music;
import com.music.musicstore.models.order.Order;
import com.music.musicstore.models.users.Customer;
import com.music.musicstore.services.CartService;
import com.music.musicstore.services.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "http://localhost:5173")
public class CartApiController {

    private final CartService cartService;
    private final OrderService orderService;

    public CartApiController(CartService cartService, OrderService orderService) {
        this.cartService = cartService;
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<CartDto> getCart(@AuthenticationPrincipal Customer customer) {
        if (customer == null) {
            return ResponseEntity.status(401).build();
        }

        Cart cart = cartService.getOrCreateCart(customer);
        return ResponseEntity.ok(convertToDto(cart));
    }

    @PostMapping("/add/{musicId}")
    public ResponseEntity<?> addToCart(@PathVariable Long musicId,
                                           @AuthenticationPrincipal Customer customer) {
        if (customer == null) {
            return ResponseEntity.status(401).body("Authentication required");
        }

        try {
            cartService.addToCart(customer, musicId);
            Cart cart = cartService.getOrCreateCart(customer);
            return ResponseEntity.ok(convertToDto(cart));
        } catch (com.music.musicstore.exceptions.ResourceNotFoundException e) {
            return ResponseEntity.status(404).body("Music not found");
        } catch (com.music.musicstore.exceptions.BusinessRuleException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to add item to cart");
        }
    }

    @DeleteMapping("/remove/{itemId}")
    public ResponseEntity<?> removeFromCart(@PathVariable Long itemId,
                                                @AuthenticationPrincipal Customer customer) {
        if (customer == null) {
            return ResponseEntity.status(401).body("Authentication required");
        }

        try {
            cartService.removeItem(customer, itemId);
            Cart cart = cartService.getOrCreateCart(customer);
            return ResponseEntity.ok(convertToDto(cart));
        } catch (com.music.musicstore.exceptions.ResourceNotFoundException e) {
            return ResponseEntity.status(404).body("Cart item not found");
        } catch (com.music.musicstore.exceptions.UnauthorizedException e) {
            return ResponseEntity.status(403).body("Unauthorized to remove this item");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to remove item from cart");
        }
    }

    @PostMapping("/clear")
    public ResponseEntity<?> clearCart(@AuthenticationPrincipal Customer customer) {
        if (customer == null) {
            return ResponseEntity.status(401).body("Authentication required");
        }

        try {
            cartService.clearCart(customer);
            Cart cart = cartService.getOrCreateCart(customer);
            return ResponseEntity.ok(convertToDto(cart));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to clear cart");
        }
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@AuthenticationPrincipal Customer customer) {
        if (customer == null) {
            return ResponseEntity.status(401).body("Authentication required");
        }

        try {
            Order order = orderService.checkout(customer.getUsername());
            return ResponseEntity.ok().body(Map.of(
                "message", "Checkout successful",
                "orderId", order.getId(),
                "orderDate", order.getOrderDate(),
                "totalAmount", order.getTotalAmount(),
                "status", order.getStatus()
            ));
        } catch (com.music.musicstore.exceptions.BusinessRuleException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to process checkout");
        }
    }

    private CartDto convertToDto(Cart cart) {
        if (cart == null) {
            return new CartDto();
        }

        CartDto cartDto = new CartDto();
        cartDto.setId(cart.getId());
        cartDto.setCustomerUsername(cart.getCustomer() != null ? cart.getCustomer().getUsername() : null);

        // Convert cart items to DTOs using stream
        List<CartItemDto> itemDtos = cart.getItems().stream()
            .map(this::convertCartItemToDto)
            .collect(java.util.stream.Collectors.toList());
        cartDto.setItems(itemDtos);

        // Calculate total from items
        BigDecimal total = cart.getTotal() != null ? cart.getTotal() : BigDecimal.ZERO;
        cartDto.setTotal(total);

        return cartDto;
    }

    private CartItemDto convertCartItemToDto(CartItem cartItem) {
        CartItemDto itemDto = new CartItemDto();
        itemDto.setId(cartItem.getId());
        itemDto.setUnitPrice(cartItem.getUnitPrice());
        itemDto.setTotalPrice(cartItem.getTotalPrice());

        // Convert Music entity to MusicDto
        if (cartItem.getMusic() != null) {
            itemDto.setMusic(convertMusicToDto(cartItem.getMusic()));
        }

        return itemDto;
    }

    private MusicDto convertMusicToDto(com.music.musicstore.models.music.Music music) {
        MusicDto musicDto = new MusicDto();
        musicDto.setId(music.getId());
        musicDto.setName(music.getName());
        musicDto.setDescription(music.getDescription());
        musicDto.setPrice(music.getPrice());
        musicDto.setImageUrl(music.getImageUrl());
        musicDto.setAudioFilePath(music.getAudioFilePath());
        musicDto.setCategory(music.getCategory());
        musicDto.setArtist(music.getArtistUsername());
        musicDto.setAlbum(music.getAlbumName());
        musicDto.setGenre(music.getGenre());
        musicDto.setReleaseYear(music.getReleaseYear());
        musicDto.setCreatedAt(music.getCreatedAt());
        musicDto.setAverageRating(music.getAverageRating() != null ? music.getAverageRating().doubleValue() : 0.0);
        musicDto.setTotalReviews(music.getTotalReviews());
        return musicDto;
    }
}
