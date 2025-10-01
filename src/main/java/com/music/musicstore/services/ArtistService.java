package com.music.musicstore.services;

import com.music.musicstore.models.users.Artist;
import com.music.musicstore.repositories.ArtistRepository;
import com.music.musicstore.exceptions.ResourceNotFoundException;
import com.music.musicstore.exceptions.ValidationException;
import com.music.musicstore.exceptions.BusinessRuleException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Service
public class ArtistService {
    private static final Logger logger = LoggerFactory.getLogger(ArtistService.class);

    private final ArtistRepository artistRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public ArtistService(ArtistRepository artistRepository, PasswordEncoder passwordEncoder) {
        this.artistRepository = artistRepository;
        this.passwordEncoder = passwordEncoder;
        logger.info("ArtistService initialized successfully");
    }

    public UserDetails loadUserByUsername(String username) {
        logger.debug("Loading artist by username: {}", username);

        if (username == null || username.trim().isEmpty()) {
            logger.error("Username is null or empty");
            throw new ValidationException("Username cannot be null or empty");
        }

        Optional<Artist> artistOpt = artistRepository.findByUserName(username);
        if (artistOpt.isEmpty()) {
            // Don't log this as error since CombinedUserDetailsService expects this to fail for non-artist users
            logger.debug("Artist not found with username: {}", username);
            throw new UsernameNotFoundException("Artist not found with username: " + username);
        }

        Artist artist = artistOpt.get();
        logger.info("Successfully loaded artist: {}", username);
        return artist;
    }

    public void registerArtist(String name, String rawPassword) {
        logger.debug("Registering new artist: {}", name);

        if (name == null || name.trim().isEmpty()) {
            logger.error("Artist name is null or empty");
            throw new ValidationException("Artist name cannot be null or empty");
        }

        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            logger.error("Artist password is null or empty");
            throw new ValidationException("Artist password cannot be null or empty");
        }

        try {
            if (artistRepository.findByUserName(name).isPresent()) {
                logger.error("Artist already exists with name: {}", name);
                throw new BusinessRuleException("Artist already exists with name: " + name);
            }

            String encodedPassword = passwordEncoder.encode(rawPassword);
            Artist artist = new Artist();
            artist.setUserName(name);
            artist.setPassword(encodedPassword);
            Artist savedArtist = artistRepository.save(artist);

            logger.info("Successfully registered artist: {} (ID: {})", name, savedArtist.getId());
        } catch (Exception e) {
            logger.error("Error registering artist: {}", name, e);
            throw e;
        }
    }

    public void deleteArtistByName(String name) {
        logger.debug("Deleting artist by name: {}", name);

        if (name == null || name.trim().isEmpty()) {
            logger.error("Artist name is null or empty");
            throw new ValidationException("Artist name cannot be null or empty");
        }

        try {
            Optional<Artist> artist = artistRepository.findByUserName(name);
            if (artist.isEmpty()) {
                logger.error("Artist not found for deletion with name: {}", name);
                throw new ResourceNotFoundException("Artist", name);
            }

            artistRepository.deleteByUserName(name);
            logger.info("Successfully deleted artist: {}", name);
        } catch (Exception e) {
            logger.error("Error deleting artist by name: {}", name, e);
            throw new RuntimeException("Failed to delete artist", e);
        }
    }

    public void updateArtist(Artist artist) {
        logger.debug("Updating artist: {}", artist != null ? artist.getUserName() : "null");

        if (artist == null) {
            logger.error("Artist object is null");
            throw new ValidationException("Artist cannot be null");
        }

        if (artist.getId() == null) {
            logger.error("Artist ID is null for update");
            throw new ValidationException("Artist ID cannot be null for update");
        }

        try {
            Artist existingArtist = artistRepository.findById(artist.getId())
                    .orElseThrow(() -> {
                        logger.error("Artist not found for update with ID: {}", artist.getId());
                        return new ResourceNotFoundException("Artist", artist.getId().toString());
                    });

            if (artist.getPassword() != null && !artist.getPassword().isEmpty()) {
                artist.setPassword(passwordEncoder.encode(artist.getPassword()));
            }
            Artist updatedArtist = artistRepository.save(artist);
            logger.info("Successfully updated artist: {} (ID: {})", updatedArtist.getUserName(), updatedArtist.getId());
        } catch (Exception e) {
            logger.error("Error updating artist: {}", artist.getUserName(), e);
            throw e;
        }
    }

    public void updateArtistUsername(Long id, String newName) {
        logger.debug("Updating artist username for ID: {} to: {}", id, newName);

        if (id == null) {
            logger.error("Artist ID is null");
            throw new ValidationException("Artist ID cannot be null");
        }

        if (newName == null || newName.trim().isEmpty()) {
            logger.error("New artist name is null or empty");
            throw new ValidationException("New artist name cannot be null or empty");
        }

        try {
            Artist artist = artistRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.error("Artist not found with ID: {}", id);
                        return new ResourceNotFoundException("Artist", id.toString());
                    });

            // Check if new username already exists
            Optional<Artist> existingArtist = artistRepository.findByUserName(newName);
            if (existingArtist.isPresent() && !existingArtist.get().getId().equals(id)) {
                logger.error("Username already exists: {}", newName);
                throw new BusinessRuleException("Username already exists: " + newName);
            }

            String oldName = artist.getUserName();
            artist.setUserName(newName);
            artistRepository.save(artist);

            logger.info("Successfully updated artist username from '{}' to '{}' (ID: {})", oldName, newName, id);
        } catch (Exception e) {
            logger.error("Error updating artist username for ID: {}", id, e);
            throw e;
        }
    }

    public Artist findByUsername(String username) {
        logger.debug("Finding artist by username: {}", username);

        if (username == null || username.trim().isEmpty()) {
            logger.error("Username is null or empty");
            throw new ValidationException("Username cannot be null or empty");
        }

        try {
            Artist artist = artistRepository.findByUserName(username)
                    .orElseThrow(() -> {
                        logger.error("Artist not found with username: {}", username);
                        return new ResourceNotFoundException("Artist", username);
                    });

            logger.info("Successfully found artist: {}", username);
            return artist;
        } catch (Exception e) {
            logger.error("Error finding artist by username: {}", username, e);
            throw e;
        }
    }

    public Artist findById(Long id) {
        logger.debug("Finding artist by ID: {}", id);

        if (id == null) {
            logger.error("Artist ID is null");
            throw new ValidationException("Artist ID cannot be null");
        }

