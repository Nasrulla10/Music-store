package com.music.musicstore.services;

import com.music.musicstore.models.music.Album;
import com.music.musicstore.repositories.AlbumRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AlbumService {

    private final AlbumRepository albumRepository;

    @Autowired
    public AlbumService(AlbumRepository albumRepository) {
        this.albumRepository = albumRepository;
    }

    // Create new album
    public Album saveAlbum(Album album) {
        return albumRepository.save(album);
    }

    // Get all albums with pagination
    public Page<Album> getAllAlbumsPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return albumRepository.findAll(pageable);
    }

    // Get all albums with pagination and sorting
    public Page<Album> getAllAlbumsPaginated(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return albumRepository.findAll(pageable);
    }

    // Get album by ID
    public Optional<Album> getAlbumById(Long id) {
        return albumRepository.findById(id);
    }

    // Get album by ID with tracks loaded
    public Album getAlbumByIdWithTracks(Long id) {
        return albumRepository.findByIdWithTracks(id);
    }

    // Get all albums
    public List<Album> getAllAlbums() {
        return albumRepository.findAll();
    }

    // Search albums by title
    public Page<Album> searchAlbumsByTitle(String title, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return albumRepository.findByTitleContainingIgnoreCase(title, pageable);
    }

    // Get albums by genre
    public Page<Album> getAlbumsByGenre(String genre, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return albumRepository.findByGenreContainingIgnoreCase(genre, pageable);
    }

    // Get albums by artist username
    public Page<Album> getAlbumsByArtistUsername(String artistUsername, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return albumRepository.findByArtistUsername(artistUsername, pageable);
    }

    // Count albums by artist username
    public long countAlbumsByArtist(String artistUsername) {
        return albumRepository.countByArtistUsername(artistUsername);
    }

    // Get albums by artist (using username)
    public List<Album> getAlbumsByArtist(String artistUsername) {
        return albumRepository.findByArtistUsername(artistUsername);
    }

    // Get albums by genre (non-paginated)
    public List<Album> getAlbumsByGenre(String genre) {
        return albumRepository.findByGenre(genre);
    }

    // Update album
    public Album updateAlbum(Long id, Album albumDetails) {
        Optional<Album> optionalAlbum = albumRepository.findById(id);
        if (optionalAlbum.isPresent()) {
            Album album = optionalAlbum.get();
            album.setTitle(albumDetails.getTitle());
            album.setDescription(albumDetails.getDescription());
            album.setGenre(albumDetails.getGenre());
            album.setPrice(albumDetails.getPrice());
            album.setCoverImageUrl(albumDetails.getCoverImageUrl());
            album.setCover(albumDetails.getCover()); // Update cover field
            album.setReleaseDate(albumDetails.getReleaseDate());
            if (albumDetails.getArtistUsername() != null) {
                album.setArtistUsername(albumDetails.getArtistUsername());
            }
            return albumRepository.save(album);
        }
        return null;
    }

    // Delete album
    public boolean deleteAlbum(Long id) {
        if (albumRepository.existsById(id)) {
            albumRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Check if album exists
    public boolean albumExists(Long id) {
        return albumRepository.existsById(id);
    }

    // Get albums by artist username and genre
    public List<Album> getAlbumsByArtistAndGenre(String artistUsername, String genre) {
        return albumRepository.findByArtistUsernameAndGenre(artistUsername, genre);
    }
}
