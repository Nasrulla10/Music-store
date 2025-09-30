package com.music.musicstore.services;

import com.music.musicstore.models.music.Music;
import com.music.musicstore.repositories.MusicRepository;
import com.music.musicstore.dto.MusicDto;
import com.music.musicstore.exceptions.ResourceNotFoundException;
import com.music.musicstore.exceptions.ValidationException;
import com.music.musicstore.exceptions.BusinessRuleException;
import com.music.musicstore.exceptions.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Service
public class MusicService {
    private static final Logger logger = LoggerFactory.getLogger(MusicService.class);

    private final MusicRepository musicRepository;

    @Autowired
    public MusicService(MusicRepository musicRepository) {
        this.musicRepository = musicRepository;
        logger.info("MusicService initialized successfully");
    }

    public Music saveMusic(Music music) {
        logger.debug("Saving music: {}", music != null ? music.getName() : "null");

        if (music == null) {
            logger.error("Music object is null");
            throw new ValidationException("Music cannot be null");
        }

        if (music.getName() == null || music.getName().trim().isEmpty()) {
            logger.error("Music name is null or empty");
            throw new ValidationException("Music name cannot be null or empty");
        }

        try {
            Music savedMusic = musicRepository.save(music);
            logger.info("Successfully saved music: {} (ID: {})", savedMusic.getName(), savedMusic.getId());
            return savedMusic;
        } catch (Exception e) {
            logger.error("Error saving music: {}", music.getName(), e);
            throw new RuntimeException("Failed to save music", e);
        }
    }

    public void deleteMusic(Long id) {
        logger.debug("Deleting music with ID: {}", id);

        if (id == null) {
            logger.error("Music ID is null");
            throw new ValidationException("Music ID cannot be null");
        }

        try {
            // Check if music exists before deletion
            Optional<Music> music = musicRepository.findById(id);
            if (music.isEmpty()) {
                logger.error("Music not found for deletion with ID: {}", id);
                throw new ResourceNotFoundException("Music", id.toString());
            }

            musicRepository.deleteById(id);
            logger.info("Successfully deleted music with ID: {}", id);
        } catch (Exception e) {
            logger.error("Error deleting music with ID: {}", id, e);
            throw new RuntimeException("Failed to delete music", e);
        }
    }

    public void updateMusic(Music music) {
        logger.debug("Updating music: {}", music != null ? music.getName() : "null");

        if (music == null) {
            logger.error("Music object is null");
            throw new ValidationException("Music cannot be null");
        }

        if (music.getId() == null) {
            logger.error("Music ID is null for update");
            throw new ValidationException("Music ID cannot be null for update");
        }
        try {
            // Check if music exists
            Optional<Music> existingMusic = musicRepository.findById(music.getId());
            if (existingMusic.isEmpty()) {
                logger.error("Music not found for update with ID: {}", music.getId());
                throw new ResourceNotFoundException("Music", music.getId().toString());
            }

            Music updatedMusic = musicRepository.save(music);
            logger.info("Successfully updated music: {} (ID: {})", updatedMusic.getName(), updatedMusic.getId());
        } catch (Exception e) {
            logger.error("Error updating music: {}", music.getName(), e);
            throw new RuntimeException("Failed to update music", e);
        }
    }

    public List<Music> getAllMusic() {
        logger.debug("Retrieving all music");

        try {
            List<Music> musicList = musicRepository.findAll();
            logger.info("Successfully retrieved {} music items", musicList.size());
            return musicList;
        } catch (Exception e) {
            logger.error("Error retrieving all music", e);
            throw new RuntimeException("Failed to retrieve music list", e);
        }
    }

    public Optional<Music> getMusicById(Long id) {
        logger.debug("Finding music by ID: {}", id);

        if (id == null) {
            logger.error("Music ID is null");
            throw new ValidationException("Music ID cannot be null");
        }

        try {
            Optional<Music> music = musicRepository.findById(id);
            if (music.isPresent()) {
                logger.info("Successfully found music by ID: {}", id);
            } else {
                logger.debug("Music not found by ID: {}", id);
            }
            return music;
        } catch (Exception e) {
            logger.error("Error finding music by ID: {}", id, e);
            throw new RuntimeException("Failed to find music by ID", e);
        }
    }

    public List<Music> getMusicByGenre(String genre) {
        logger.debug("Finding music by genre: {}", genre);

        if (genre == null || genre.trim().isEmpty()) {
            logger.error("Genre is null or empty");
            throw new ValidationException("Genre cannot be null or empty");
        }

        try {
            List<Music> musicList = musicRepository.findByGenre(genre);
            logger.info("Successfully retrieved {} music items for genre: {}", musicList.size(), genre);
            return musicList;
        } catch (Exception e) {
            logger.error("Error finding music by genre: {}", genre, e);
            throw new RuntimeException("Failed to find music by genre", e);
        }
    }

    public Page<Music> getAllMusicPaginated(int page, int size) {
        logger.debug("Retrieving paginated music: page={}, size={}", page, size);

        if (page < 0) {
            logger.error("Page number cannot be negative: {}", page);
            throw new ValidationException("Page number cannot be negative");
        }

        if (size <= 0) {
            logger.error("Page size must be positive: {}", size);
            throw new ValidationException("Page size must be positive");
        }

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Music> musicPage = musicRepository.findAll(pageable);
            logger.info("Successfully retrieved paginated music: {} items on page {}", musicPage.getNumberOfElements(), page);
            return musicPage;
        } catch (Exception e) {
            logger.error("Error retrieving paginated music: page={}, size={}", page, size, e);
            throw new RuntimeException("Failed to retrieve paginated music", e);
        }
    }

    public Page<Music> searchMusic(String query, int page, int size) {
        logger.debug("Searching music with query: '{}', page={}, size={}", query, page, size);

        if (query == null || query.trim().isEmpty()) {
            logger.error("Search query is null or empty");
            throw new ValidationException("Search query cannot be null or empty");
        }

        if (page < 0) {
            logger.error("Page number cannot be negative: {}", page);
            throw new ValidationException("Page number cannot be negative");
        }

        if (size <= 0) {
            logger.error("Page size must be positive: {}", size);
            throw new ValidationException("Page size must be positive");
        }

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Music> musicPage = musicRepository.findByNameContainingIgnoreCaseOrArtistUsernameContainingIgnoreCase(query, query, pageable);
            logger.info("Successfully searched music with query '{}': {} items found on page {}", query, musicPage.getNumberOfElements(), page);
            return musicPage;
        } catch (Exception e) {
            logger.error("Error searching music with query: {}", query, e);
            throw new RuntimeException("Failed to search music", e);
        }
    }

    public Optional<Music> getMusicByName(String query) {
        logger.debug("Finding music by name: {}", query);

        if (query == null || query.trim().isEmpty()) {
            logger.error("Search query is null or empty");
            throw new ValidationException("Search query cannot be null or empty");
        }

        try {
            return musicRepository.findByNameContainingIgnoreCase(query);
        } catch (Exception e) {
            logger.error("Error finding music by name: {}", query, e);
            throw new RuntimeException("Failed to find music by name", e);
        }
    }

    // Missing methods needed by CustomerApiController

    public List<Music> getDownloadableMusic(String username) {
        logger.debug("Getting downloadable music for user: {}", username);

        if (username == null || username.trim().isEmpty()) {
            logger.error("Username is null or empty");
            throw new ValidationException("Username cannot be null or empty");
        }

        try {
            // Implementation to get music that the customer has purchased and can download
            // This would need OrderService integration to check purchased items
            logger.warn("Downloadable music feature not yet implemented for user: {}", username);
            return List.of(); // Placeholder
        } catch (Exception e) {
            logger.error("Error getting downloadable music for user: {}", username, e);
            throw new RuntimeException("Failed to get downloadable music", e);
        }
    }

    public ResponseEntity<Resource> downloadMusic(Long musicId, String username) {
        logger.debug("Downloading music ID: {} for user: {}", musicId, username);

        if (musicId == null) {
            logger.error("Music ID is null");
            throw new ValidationException("Music ID cannot be null");
        }

        if (username == null || username.trim().isEmpty()) {
            logger.error("Username is null or empty");
            throw new ValidationException("Username cannot be null or empty");
        }

        try {
            // Implementation to provide music file for download
            // This would need to validate purchase and return the music file
            logger.error("Music download feature not yet implemented for music ID: {} and user: {}", musicId, username);
            throw new BusinessRuleException("Music download not yet implemented");
        } catch (Exception e) {
            logger.error("Error downloading music ID: {} for user: {}", musicId, username, e);
            throw e;
        }
    }

    public List<Object> getUserPlaylists(String username) {
        logger.debug("Getting playlists for user: {}", username);

        if (username == null || username.trim().isEmpty()) {
            logger.error("Username is null or empty");
            throw new ValidationException("Username cannot be null or empty");
        }

        try {
            // Implementation to get user playlists
            // This would need a Playlist entity and service
            logger.warn("Playlist feature not yet implemented for user: {}", username);
            return List.of(); // Placeholder
        } catch (Exception e) {
            logger.error("Error getting playlists for user: {}", username, e);
            throw new RuntimeException("Failed to get user playlists", e);
        }
    }

    public Object createPlaylist(String username, String name, String description) {
        logger.debug("Creating playlist '{}' for user: {}", name, username);

        if (username == null || username.trim().isEmpty()) {
            logger.error("Username is null or empty");
            throw new ValidationException("Username cannot be null or empty");
        }

        if (name == null || name.trim().isEmpty()) {
            logger.error("Playlist name is null or empty");
            throw new ValidationException("Playlist name cannot be null or empty");
        }

        try {
            // Implementation to create a new playlist
            // This would need a Playlist entity and service
            logger.error("Playlist creation feature not yet implemented for user: {} and playlist: {}", username, name);
            throw new BusinessRuleException("Playlist creation not yet implemented");
        } catch (Exception e) {
            logger.error("Error creating playlist '{}' for user: {}", name, username, e);
            throw e;
        }
    }

    public void addToPlaylist(Long playlistId, Long musicId, String username) {
        logger.debug("Adding music ID: {} to playlist ID: {} for user: {}", musicId, playlistId, username);

        if (playlistId == null) {
            logger.error("Playlist ID is null");
            throw new ValidationException("Playlist ID cannot be null");
        }

        if (musicId == null) {
            logger.error("Music ID is null");
            throw new ValidationException("Music ID cannot be null");
        }

        if (username == null || username.trim().isEmpty()) {
            logger.error("Username is null or empty");
            throw new ValidationException("Username cannot be null or empty");
        }

        try {
            // Implementation to add music to playlist
            // This would need a Playlist entity and service
            logger.error("Add to playlist feature not yet implemented");
            throw new BusinessRuleException("Add to playlist not yet implemented");
        } catch (Exception e) {
            logger.error("Error adding music ID: {} to playlist ID: {} for user: {}", musicId, playlistId, username, e);
            throw e;
        }
    }

    public void removeFromPlaylist(Long playlistId, Long musicId, String username) {
        logger.debug("Removing music ID: {} from playlist ID: {} for user: {}", musicId, playlistId, username);

        if (playlistId == null) {
            logger.error("Playlist ID is null");
            throw new ValidationException("Playlist ID cannot be null");
        }

        if (musicId == null) {
            logger.error("Music ID is null");
            throw new ValidationException("Music ID cannot be null");
        }

        if (username == null || username.trim().isEmpty()) {
            logger.error("Username is null or empty");
            throw new ValidationException("Username cannot be null or empty");
        }

        try {
            // Implementation to remove music from playlist
            // This would need a Playlist entity and service
            logger.error("Remove from playlist feature not yet implemented");
            throw new BusinessRuleException("Remove from playlist not yet implemented");
        } catch (Exception e) {
            logger.error("Error removing music ID: {} from playlist ID: {} for user: {}", musicId, playlistId, username, e);
            throw e;
        }
    }

    // Missing methods needed by StaffApiController analytics

    public Map<String, Object> getMusicPerformanceReport(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> report = new HashMap<>();

        // Get music performance metrics
        report.put("totalMusicTracks", musicRepository.count());
        report.put("mostPopularGenres", getMostPopularGenres());
        report.put("topSellingMusic", getTopSellingMusic(startDate, endDate));
        report.put("newMusicAdded", getNewMusicCount(startDate, endDate));
        report.put("musicByGenreDistribution", getMusicByGenreDistribution());
        report.put("averageRating", getAverageMusicRating());
        report.put("period", startDate + " to " + endDate);

        return report;
    }

