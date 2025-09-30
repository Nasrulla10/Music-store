package com.music.musicstore.services;

import com.music.musicstore.dto.*;
import com.music.musicstore.exceptions.BusinessRuleException;
import com.music.musicstore.exceptions.ResourceNotFoundException;
import com.music.musicstore.exceptions.UnauthorizedException;
import com.music.musicstore.exceptions.ValidationException;
import com.music.musicstore.models.music.Music;
import com.music.musicstore.models.music.Playlist;
import com.music.musicstore.models.users.Customer;
import com.music.musicstore.repositories.CustomerRepository;
import com.music.musicstore.repositories.MusicRepository;
import com.music.musicstore.repositories.PlaylistRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PlaylistService {

    private static final Logger logger = LoggerFactory.getLogger(PlaylistService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final PlaylistRepository playlistRepository;
    private final CustomerRepository customerRepository;
    private final MusicRepository musicRepository;

    @Autowired
    public PlaylistService(PlaylistRepository playlistRepository,
                          CustomerRepository customerRepository,
                          MusicRepository musicRepository) {
        this.playlistRepository = playlistRepository;
        this.customerRepository = customerRepository;
        this.musicRepository = musicRepository;
    }

    /**
     * Create a new playlist for a customer
     */
    public PlaylistDto createPlaylist(String customerUsername, CreatePlaylistRequest request) {
        logger.info("Creating playlist '{}' for customer '{}'", request.getName(), customerUsername);

        try {
            Customer customer = customerRepository.findByUsername(customerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerUsername));

            // Check if playlist name already exists for this customer
            if (playlistRepository.existsByCustomerAndNameIgnoreCase(customer, request.getName())) {
                throw new BusinessRuleException("Playlist with name '" + request.getName() + "' already exists");
            }

            Playlist playlist = new Playlist(request.getName(), customer);
            Playlist savedPlaylist = playlistRepository.save(playlist);

            logger.info("Successfully created playlist with ID: {} for customer: {}", savedPlaylist.getId(), customerUsername);
            return convertToDto(savedPlaylist);

        } catch (Exception e) {
            logger.error("Error creating playlist for customer '{}': {}", customerUsername, e.getMessage());
            throw e;
        }
    }

    /**
     * Get all playlists for a customer
     */
    @Transactional(readOnly = true)
    public List<PlaylistDto> getCustomerPlaylists(String customerUsername) {
        logger.info("Fetching playlists for customer '{}'", customerUsername);

        try {
            Customer customer = customerRepository.findByUsername(customerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerUsername));

            List<Playlist> playlists = playlistRepository.findByCustomer(customer);
            logger.info("Found {} playlists for customer '{}'", playlists.size(), customerUsername);

            return playlists.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("Error fetching playlists for customer '{}': {}", customerUsername, e.getMessage());
            throw e;
        }
    }

    /**
     * Get a specific playlist with its music tracks
     */
    @Transactional(readOnly = true)
    public PlaylistWithMusicDto getPlaylistWithMusic(String customerUsername, Long playlistId) {
        logger.info("Fetching playlist {} with music for customer '{}'", playlistId, customerUsername);

        try {
            Customer customer = customerRepository.findByUsername(customerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerUsername));

            Playlist playlist = playlistRepository.findByIdAndCustomer(playlistId, customer)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist not found or not owned by customer"));

            logger.info("Successfully fetched playlist '{}' with {} tracks", playlist.getName(), playlist.getTrackCount());
            return convertToPlaylistWithMusicDto(playlist);

        } catch (Exception e) {
            logger.error("Error fetching playlist {} for customer '{}': {}", playlistId, customerUsername, e.getMessage());
            throw e;
        }
    }

    /**
     * Update playlist name
     */
    public PlaylistDto updatePlaylist(String customerUsername, Long playlistId, CreatePlaylistRequest request) {
        logger.info("Updating playlist {} for customer '{}'", playlistId, customerUsername);

        try {
            Customer customer = customerRepository.findByUsername(customerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerUsername));

            Playlist playlist = playlistRepository.findByIdAndCustomer(playlistId, customer)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist not found or not owned by customer"));

            // Check if new name conflicts with existing playlists (excluding current one)
            if (!playlist.getName().equalsIgnoreCase(request.getName()) &&
                playlistRepository.existsByCustomerAndNameIgnoreCase(customer, request.getName())) {
                throw new BusinessRuleException("Playlist with name '" + request.getName() + "' already exists");
            }

            playlist.setName(request.getName());
            Playlist updatedPlaylist = playlistRepository.save(playlist);

            logger.info("Successfully updated playlist {} name to '{}'", playlistId, request.getName());
            return convertToDto(updatedPlaylist);

        } catch (Exception e) {
            logger.error("Error updating playlist {} for customer '{}': {}", playlistId, customerUsername, e.getMessage());
            throw e;
        }
    }

    /**
     * Delete a playlist
     */
    public void deletePlaylist(String customerUsername, Long playlistId) {
        logger.info("Deleting playlist {} for customer '{}'", playlistId, customerUsername);

        try {
            Customer customer = customerRepository.findByUsername(customerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerUsername));

            Playlist playlist = playlistRepository.findByIdAndCustomer(playlistId, customer)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist not found or not owned by customer"));

            playlistRepository.delete(playlist);
            logger.info("Successfully deleted playlist {} for customer '{}'", playlistId, customerUsername);

        } catch (Exception e) {
            logger.error("Error deleting playlist {} for customer '{}': {}", playlistId, customerUsername, e.getMessage());
            throw e;
        }
    }

    /**
     * Add music to playlist (only if customer owns the music)
     */
    public PlaylistWithMusicDto addMusicToPlaylist(String customerUsername, Long playlistId, Long musicId) {
        logger.info("Adding music {} to playlist {} for customer '{}'", musicId, playlistId, customerUsername);

        try {
            Customer customer = customerRepository.findByUsername(customerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerUsername));

            Playlist playlist = playlistRepository.findByIdAndCustomer(playlistId, customer)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist not found or not owned by customer"));

            Music music = musicRepository.findById(musicId)
                .orElseThrow(() -> new ResourceNotFoundException("Music not found: " + musicId));

            // Check if customer has purchased this music
            if (!customer.getPurchasedMusic().contains(music)) {
                throw new BusinessRuleException("You can only add purchased music to your playlist");
            }

            // Check if music is already in playlist
            if (playlist.containsMusic(music)) {
                throw new BusinessRuleException("Music is already in the playlist");
            }

            playlist.addMusic(music);
            Playlist updatedPlaylist = playlistRepository.save(playlist);

            logger.info("Successfully added music {} to playlist {} for customer '{}'", musicId, playlistId, customerUsername);
            return convertToPlaylistWithMusicDto(updatedPlaylist);

        } catch (Exception e) {
            logger.error("Error adding music {} to playlist {} for customer '{}': {}", musicId, playlistId, customerUsername, e.getMessage());
            throw e;
        }
    }

    /**
     * Remove music from playlist
     */
    public PlaylistWithMusicDto removeMusicFromPlaylist(String customerUsername, Long playlistId, Long musicId) {
        logger.info("Removing music {} from playlist {} for customer '{}'", musicId, playlistId, customerUsername);

        try {
            Customer customer = customerRepository.findByUsername(customerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerUsername));

            Playlist playlist = playlistRepository.findByIdAndCustomer(playlistId, customer)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist not found or not owned by customer"));

            Music music = musicRepository.findById(musicId)
                .orElseThrow(() -> new ResourceNotFoundException("Music not found: " + musicId));

            if (!playlist.containsMusic(music)) {
                throw new BusinessRuleException("Music is not in the playlist");
            }

            playlist.removeMusic(music);
            Playlist updatedPlaylist = playlistRepository.save(playlist);

            logger.info("Successfully removed music {} from playlist {} for customer '{}'", musicId, playlistId, customerUsername);
            return convertToPlaylistWithMusicDto(updatedPlaylist);

        } catch (Exception e) {
            logger.error("Error removing music {} from playlist {} for customer '{}': {}", musicId, playlistId, customerUsername, e.getMessage());
            throw e;
        }
    }

    // Helper methods for DTO conversion
    private PlaylistDto convertToDto(Playlist playlist) {
        return new PlaylistDto(
            playlist.getId(),
            playlist.getName(),
            playlist.getCustomer().getId(),
            playlist.getCustomer().getUsername(),
            playlist.getTrackCount(),
            playlist.getCreatedAt().format(DATE_FORMATTER),
            playlist.getUpdatedAt().format(DATE_FORMATTER)
        );
    }

    private PlaylistWithMusicDto convertToPlaylistWithMusicDto(Playlist playlist) {
        List<MusicDto> musicDtos = playlist.getMusics().stream()
            .map(this::convertMusicToDto)
            .collect(Collectors.toList());

        return new PlaylistWithMusicDto(
            playlist.getId(),
            playlist.getName(),
            playlist.getCustomer().getId(),
            playlist.getCustomer().getUsername(),
            playlist.getTrackCount(),
            playlist.getCreatedAt().format(DATE_FORMATTER),
            playlist.getUpdatedAt().format(DATE_FORMATTER),
            musicDtos
        );
    }

    private MusicDto convertMusicToDto(Music music) {
        MusicDto dto = new MusicDto();
        dto.setId(music.getId());
        dto.setName(music.getName());
        dto.setDescription(music.getDescription());
        dto.setPrice(music.getPrice());
        dto.setImageUrl(music.getImageUrl());
        dto.setAudioFilePath(music.getAudioFilePath());
        dto.setCategory(music.getCategory());
        dto.setArtist(music.getArtistUsername()); // Map artistUsername to artist
        dto.setAlbum(music.getAlbumName()); // Map albumName to album
        dto.setGenre(music.getGenre());
        dto.setReleaseYear(music.getReleaseYear());
        dto.setCreatedAt(music.getCreatedAt());
        dto.setAverageRating(music.getAverageRating() != null ? music.getAverageRating().doubleValue() : 0.0);
        dto.setTotalReviews(music.getTotalReviews());
        return dto;
    }
}
