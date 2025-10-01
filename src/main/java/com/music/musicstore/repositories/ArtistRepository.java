package com.music.musicstore.repositories;

import com.music.musicstore.models.users.Artist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, Long> {
    Optional<Artist> findByUserName(String name);

    Optional<Artist> findAllByUserNameContainingIgnoreCase(String userName);

    default void deleteByUserName(String userName) {

    }

    Optional<Artist> findById(Long id);
}
