package com.music.musicstore.api;

import com.music.musicstore.dto.*;
import com.music.musicstore.services.PlaylistService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/playlists")
@CrossOrigin(origins = "*")
public class PlaylistApiController {

    private static final Logger logger = LoggerFactory.getLogger(PlaylistApiController.class);

    private final PlaylistService playlistService;

    @Autowired
    public PlaylistApiController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    /**
     * Create a new playlist
     * POST /api/playlists
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createPlaylist(
            @Valid @RequestBody CreatePlaylistRequest request,
            Authentication authentication) {

        logger.info("POST /api/playlists - Creating playlist for user: {}", authentication.getName());

        Map<String, Object> response = new HashMap<>();

        try {
            PlaylistDto playlist = playlistService.createPlaylist(authentication.getName(), request);

            response.put("success", true);
            response.put("message", "Playlist created successfully");
            response.put("data", playlist);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            logger.error("Error creating playlist: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "Failed to create playlist: " + e.getMessage());

            // Return appropriate HTTP status based on exception type
            if (e instanceof com.music.musicstore.exceptions.BusinessRuleException) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            } else if (e instanceof com.music.musicstore.exceptions.ResourceNotFoundException) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            } else if (e instanceof com.music.musicstore.exceptions.ValidationException) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get all playlists for the authenticated customer
     * GET /api/playlists
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getCustomerPlaylists(Authentication authentication) {

        logger.info("GET /api/playlists - Fetching playlists for user: {}", authentication.getName());

        Map<String, Object> response = new HashMap<>();

        try {
            List<PlaylistDto> playlists = playlistService.getCustomerPlaylists(authentication.getName());

            response.put("success", true);
            response.put("message", "Playlists retrieved successfully");
            response.put("data", playlists);
            response.put("count", playlists.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching playlists: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "Failed to fetch playlists: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get a specific playlist with its music tracks
     * GET /api/playlists/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getPlaylistById(
            @PathVariable Long id,
            Authentication authentication) {

        logger.info("GET /api/playlists/{} - Fetching playlist for user: {}", id, authentication.getName());

        Map<String, Object> response = new HashMap<>();

        try {
            PlaylistWithMusicDto playlist = playlistService.getPlaylistWithMusic(authentication.getName(), id);

            response.put("success", true);
            response.put("message", "Playlist retrieved successfully");
            response.put("data", playlist);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching playlist {}: {}", id, e.getMessage());
            response.put("success", false);
            response.put("message", "Failed to fetch playlist: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * Update playlist name
     * PUT /api/playlists/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updatePlaylist(
            @PathVariable Long id,
            @Valid @RequestBody CreatePlaylistRequest request,
            Authentication authentication) {

        logger.info("PUT /api/playlists/{} - Updating playlist for user: {}", id, authentication.getName());

        Map<String, Object> response = new HashMap<>();

        try {
            PlaylistDto playlist = playlistService.updatePlaylist(authentication.getName(), id, request);

            response.put("success", true);
            response.put("message", "Playlist updated successfully");
            response.put("data", playlist);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error updating playlist {}: {}", id, e.getMessage());
            response.put("success", false);
            response.put("message", "Failed to update playlist: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Delete a playlist
     * DELETE /api/playlists/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deletePlaylist(
            @PathVariable Long id,
            Authentication authentication) {

        logger.info("DELETE /api/playlists/{} - Deleting playlist for user: {}", id, authentication.getName());

        Map<String, Object> response = new HashMap<>();

        try {
            playlistService.deletePlaylist(authentication.getName(), id);

            response.put("success", true);
            response.put("message", "Playlist deleted successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error deleting playlist {}: {}", id, e.getMessage());
            response.put("success", false);
            response.put("message", "Failed to delete playlist: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Add music to playlist
     * POST /api/playlists/{id}/music/{musicId}
     */
    @PostMapping("/{id}/music/{musicId}")
    public ResponseEntity<Map<String, Object>> addMusicToPlaylist(
            @PathVariable Long id,
            @PathVariable Long musicId,
            Authentication authentication) {

        logger.info("POST /api/playlists/{}/music/{} - Adding music to playlist for user: {}",
                   id, musicId, authentication.getName());

        Map<String, Object> response = new HashMap<>();

        try {
            PlaylistWithMusicDto playlist = playlistService.addMusicToPlaylist(authentication.getName(), id, musicId);

            response.put("success", true);
            response.put("message", "Music added to playlist successfully");
            response.put("data", playlist);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error adding music {} to playlist {}: {}", musicId, id, e.getMessage());
            response.put("success", false);
            response.put("message", "Failed to add music to playlist: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Remove music from playlist
     * DELETE /api/playlists/{id}/music/{musicId}
     */
    @DeleteMapping("/{id}/music/{musicId}")
    public ResponseEntity<Map<String, Object>> removeMusicFromPlaylist(
            @PathVariable Long id,
            @PathVariable Long musicId,
            Authentication authentication) {

        logger.info("DELETE /api/playlists/{}/music/{} - Removing music from playlist for user: {}",
                   id, musicId, authentication.getName());

        Map<String, Object> response = new HashMap<>();

        try {
            PlaylistWithMusicDto playlist = playlistService.removeMusicFromPlaylist(authentication.getName(), id, musicId);

            response.put("success", true);
            response.put("message", "Music removed from playlist successfully");
            response.put("data", playlist);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error removing music {} from playlist {}: {}", musicId, id, e.getMessage());
            response.put("success", false);
            response.put("message", "Failed to remove music from playlist: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}
