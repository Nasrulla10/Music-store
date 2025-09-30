package com.music.musicstore.repositories;

import com.music.musicstore.models.cart.Cart;
import com.music.musicstore.models.users.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByCustomer(Customer customer);

    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items i LEFT JOIN FETCH i.music WHERE c.customer = :customer")
    Optional<Cart> findByCustomerWithItems(@Param("customer") Customer customer);
}
