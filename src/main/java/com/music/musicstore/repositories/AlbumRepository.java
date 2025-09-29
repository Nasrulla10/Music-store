package com.music.musicstore.repositories;

import com.music.musicstore.models.music.Album;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {

    Optional<Album> findByTitle(String title);
    // Find albums by genre
    List<Album> findByGenre(String genre);

    // Find albums by title containing text (case insensitive)
    Page<Album> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    // Find albums by artist username
    Page<Album> findByArtistUsername(String artistUsername, Pageable pageable);

    // Count albums by artist username
    long countByArtistUsername(String artistUsername);

    // Find albums by artist username (non-paginated)
    List<Album> findByArtistUsername(String artistUsername);

    // Find albums by genre (paginated)
    Page<Album> findByGenreContainingIgnoreCase(String genre, Pageable pageable);

    // Find albums by artist username and genre
    List<Album> findByArtistUsernameAndGenre(String artistUsername, String genre);

    // Find album by ID (tracks relationship removed)
    @Query("SELECT a FROM Album a WHERE a.id = :id")
    Album findByIdWithTracks(@Param("id") Long id);
}
