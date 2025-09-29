package com.music.musicstore.configs;

import com.music.musicstore.models.cart.Cart;
import com.music.musicstore.models.cart.CartItem;
import com.music.musicstore.models.users.Admin;
import com.music.musicstore.models.users.Customer;
import com.music.musicstore.repositories.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedAdminAndCustomerData(
            MusicRepository musicRepository,
            AdminRepository adminRepository,
            CustomerRepository customerRepository,
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            PasswordEncoder encoder
    ) {
        return args -> {
            // Seed admin if not present
            if (!adminRepository.existsByUsername("admin")) {
                Admin admin = new Admin();
                admin.setUsername("admin");
                admin.setEmail("admin@example.com");
                admin.setPassword(encoder.encode("admin123"));
                admin.setRole("ROLE_ADMIN");
                adminRepository.save(admin);
            }

            // Seed a demo customer
            Customer customer = customerRepository.findByUsername("jane").orElseGet(() -> {
                Customer c = new Customer();
                c.setUsername("jane");
                c.setFirstName("Jane");
                c.setLastName("Doe");
                c.setEmail("jane@example.com");
                c.setRole("ROLE_CUSTOMER");
                c.setPassword(encoder.encode("password"));
                return customerRepository.save(c);
            });

            // Ensure customer has a cart with one item for demo
            Cart cart = cartRepository.findByCustomer(customer).orElseGet(() -> cartRepository.save(new Cart(customer)));
            if (cartItemRepository.findByCart(cart).isEmpty()) {
                musicRepository.findAll().stream().findFirst().ifPresent(m -> {
                    CartItem item = new CartItem(m);
                    item.setCart(cart);
                    cartItemRepository.save(item);
                });
            }
        };
    }
}
