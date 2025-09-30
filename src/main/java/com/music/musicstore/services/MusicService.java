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