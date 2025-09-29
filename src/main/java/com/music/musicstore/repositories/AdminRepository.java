package com.music.musicstore.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.music.musicstore.models.users.Admin;

import java.util.List;
import java.util.Optional;

public interface  AdminRepository  extends JpaRepository<Admin, Long>{
    Optional<Admin> findByUsername(String username);
    Optional<Admin> findByEmail(String email);
    List<Admin> findAllByUsernameContainingIgnoreCase(String username);
    List<Admin> findAllByEmailContainingIgnoreCase(String email);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
