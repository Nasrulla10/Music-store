package com.music.musicstore.repositories;

import com.music.musicstore.models.music.Playlist;
import com.music.musicstore.models.users.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    /**
     * Find all playlists owned by a specific customer
     */
    List<Playlist> findByCustomer(Customer customer);

    /**
     * Find all playlists owned by a specific customer ID
     */
    List<Playlist> findByCustomerId(Long customerId);

    /**
     * Find a playlist by ID and customer (for security - ensuring customer owns the playlist)
     */
    Optional<Playlist> findByIdAndCustomer(Long id, Customer customer);

    /**
     * Find a playlist by ID and customer ID
     */
    Optional<Playlist> findByIdAndCustomerId(Long id, Long customerId);

    /**
     * Find playlists by name containing a search term for a specific customer
     */
    List<Playlist> findByCustomerAndNameContainingIgnoreCase(Customer customer, String name);

    /**
     * Count playlists owned by a customer
     */
    long countByCustomer(Customer customer);

    /**
     * Find playlists that contain a specific music track
     */
    @Query("SELECT p FROM Playlist p JOIN p.musics m WHERE m.id = :musicId")
    List<Playlist> findPlaylistsContainingMusic(@Param("musicId") Long musicId);

    /**
     * Check if a playlist name already exists for a customer
     */
    boolean existsByCustomerAndNameIgnoreCase(Customer customer, String name);
}
