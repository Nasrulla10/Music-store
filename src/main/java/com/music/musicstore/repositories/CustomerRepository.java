package com.music.musicstore.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.music.musicstore.models.users.Customer;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByUsername(String username);
    Optional<Customer> findByEmail(String email);
    List<Customer> findAllByRole(String role);
    List<Customer> findAllByUsernameContainingIgnoreCase(String username);
    List<Customer> findAllByEmailContainingIgnoreCase(String email);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByRole(String role);

    void deleteByUsername(String username);
    void deleteByEmail(String email);

    @Query("SELECT CASE WHEN COUNT(pm) > 0 THEN true ELSE false END FROM Customer c JOIN c.purchasedMusic pm WHERE c.id = :customerId AND pm.id = :musicId")
    boolean hasPurchasedMusic(@Param("customerId") Long customerId, @Param("musicId") Long musicId);

    @Query("SELECT c FROM Customer c LEFT JOIN FETCH c.purchasedMusic WHERE c.id = :customerId")
    Optional<Customer> findByIdWithPurchasedMusic(@Param("customerId") Long customerId);
}
