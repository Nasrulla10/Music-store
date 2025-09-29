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