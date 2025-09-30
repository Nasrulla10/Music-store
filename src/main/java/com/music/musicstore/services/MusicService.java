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
