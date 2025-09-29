package musicstore.api;

import com.music.musicstore.dto.MusicDto;
import com.music.musicstore.models.music.Music;
import com.music.musicstore.services.MusicService;
import com.music.musicstore.services.ReviewService;
import com.music.musicstore.exceptions.ValidationException;
import com.music.musicstore.exceptions.ResourceNotFoundException;
import com.music.musicstore.exceptions.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/artist")
@PreAuthorize("hasRole('ARTIST')")
@CrossOrigin(origins = "http://localhost:5173")
public class ArtistApiController {

    private static final Logger logger = LoggerFactory.getLogger(ArtistApiController.class);

    @Autowired
    private MusicService musicService;

    @Autowired
    private ReviewService reviewService;

    @PostMapping("/music/upload")
    public ResponseEntity<?> uploadMusic(
            @RequestParam String title,
            @RequestParam String genre,
            @RequestParam Double price,
            @RequestParam(required = false) String description,
            @RequestParam MultipartFile musicFile,
            @RequestParam MultipartFile coverImage,
            @AuthenticationPrincipal UserDetails userDetails) {

        logger.info("Music upload request from artist: {}", userDetails.getUsername());

        try {
            // Additional validation
            if (title == null || title.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Title is required", null));
            }

            if (musicFile == null || musicFile.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Music file is required", null));
            }

            if (coverImage == null || coverImage.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Cover image is required", null));
            }

            // Validate file types
            String musicContentType = musicFile.getContentType();
            String imageContentType = coverImage.getContentType();

            if (musicContentType == null || !musicContentType.startsWith("audio/")) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Invalid music file format. Please upload an audio file.", null));
            }

            if (imageContentType == null || !imageContentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Invalid image file format. Please upload an image file.", null));
            }

            Music music = musicService.uploadMusic(
                    title, genre, price, description, musicFile, coverImage, userDetails.getUsername()
            );

            MusicDto musicDto = convertToDto(music);
            logger.info("Successfully uploaded music: {} by artist: {}", title, userDetails.getUsername());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "Music uploaded successfully", musicDto));

        } catch (ValidationException e) {
            logger.warn("Validation error during music upload: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Error uploading music by artist: {}", userDetails.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Failed to upload music: " + e.getMessage(), null));
        }
    }
    @GetMapping("/music/my-music")
    public ResponseEntity<?> getMyMusic(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {

        logger.debug("Fetching music for artist: {}, page: {}, size: {}", userDetails.getUsername(), page, size);

        try {
            if (page == 0 && size == 10) {
                // Return all music if default pagination
                List<Music> musicList = musicService.getMusicByArtist(userDetails.getUsername());
                List<MusicDto> musicDtoList = musicList.stream()
                        .map(this::convertToDto)
                        .toList();

                Map<String, Object> response = new HashMap<>();
                response.put("music", musicDtoList);
                response.put("totalElements", musicDtoList.size());
                response.put("totalPages", 1);
                response.put("currentPage", 0);
                response.put("pageSize", musicDtoList.size());

                return ResponseEntity.ok(new ApiResponse(true, "Music retrieved successfully", response));
            } else {
                // Return paginated results
                Page<Music> musicPage = musicService.getMusicByArtistPaginated(userDetails.getUsername(), page, size);
                Page<MusicDto> musicDtoPage = musicPage.map(this::convertToDto);

                Map<String, Object> response = new HashMap<>();
                response.put("music", musicDtoPage.getContent());
                response.put("totalElements", musicDtoPage.getTotalElements());
                response.put("totalPages", musicDtoPage.getTotalPages());
                response.put("currentPage", musicDtoPage.getNumber());
                response.put("pageSize", musicDtoPage.getSize());
                response.put("hasNext", musicDtoPage.hasNext());
                response.put("hasPrevious", musicDtoPage.hasPrevious());

                return ResponseEntity.ok(new ApiResponse(true, "Music retrieved successfully", response));
            }
        } catch (Exception e) {
            logger.error("Error fetching music for artist: {}", userDetails.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Failed to fetch music: " + e.getMessage(), null));
        }
    }

    @GetMapping("/music/count")
    public ResponseEntity<?> getMyMusicCount(@AuthenticationPrincipal UserDetails userDetails) {
        logger.debug("Fetching music count for artist: {}", userDetails.getUsername());

        try {
            long count = musicService.countMusicByArtist(userDetails.getUsername());

            Map<String, Object> data = new HashMap<>();
            data.put("count", count);

            return ResponseEntity.ok(new ApiResponse(true, "Music count retrieved successfully", data));
        } catch (Exception e) {
            logger.error("Error fetching music count for artist: {}", userDetails.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Failed to fetch music count: " + e.getMessage(), null));
        }
    }

    @PutMapping("/music/{musicId}")
    public ResponseEntity<?> updateMusic(
            @PathVariable Long musicId,
            @RequestBody MusicDto musicDto,
            @AuthenticationPrincipal UserDetails userDetails) {

        logger.info("Update request for music ID: {} by artist: {}", musicId, userDetails.getUsername());

        try {
            Music updatedMusic = musicService.updateMusic(musicId, musicDto, userDetails.getUsername());
            MusicDto updatedMusicDto = convertToDto(updatedMusic);

            logger.info("Successfully updated music ID: {} by artist: {}", musicId, userDetails.getUsername());
            return ResponseEntity.ok(new ApiResponse(true, "Music updated successfully", updatedMusicDto));

        } catch (ResourceNotFoundException e) {
            logger.warn("Music not found for update: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage(), null));
        } catch (UnauthorizedException e) {
            logger.warn("Unauthorized update attempt: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(false, e.getMessage(), null));
        } catch (ValidationException e) {
            logger.warn("Validation error during music update: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Error updating music ID: {} by artist: {}", musicId, userDetails.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Failed to update music: " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/music/{musicId}")
    public ResponseEntity<?> deleteMusic(
            @PathVariable Long musicId,
            @AuthenticationPrincipal UserDetails userDetails) {

        logger.info("Delete request for music ID: {} by artist: {}", musicId, userDetails.getUsername());

        try {
            musicService.deleteMusic(musicId, userDetails.getUsername());

            logger.info("Successfully deleted music ID: {} by artist: {}", musicId, userDetails.getUsername());
            return ResponseEntity.ok(new ApiResponse(true, "Music deleted successfully", null));

        } catch (ResourceNotFoundException e) {
            logger.warn("Music not found for deletion: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage(), null));
        } catch (UnauthorizedException e) {
            logger.warn("Unauthorized deletion attempt: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Error deleting music ID: {} by artist: {}", musicId, userDetails.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Failed to delete music: " + e.getMessage(), null));
        }
    }

    @GetMapping("/music/{musicId}/reviews")
    public ResponseEntity<?> getMusicReviews(@PathVariable Long musicId) {
        logger.debug("Fetching reviews for music ID: {}", musicId);

        try {
            if (musicId == null) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Music ID is required", null));
            }

            var reviews = reviewService.getReviewsByMusicId(musicId);
            return ResponseEntity.ok(new ApiResponse(true, "Reviews retrieved successfully", reviews));

        } catch (ResourceNotFoundException e) {
            logger.warn("Music not found for reviews: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Error fetching reviews for music ID: {}", musicId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Failed to fetch reviews: " + e.getMessage(), null));
        }
    }

