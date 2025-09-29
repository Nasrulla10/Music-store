package musicstore.api;

import com.music.musicstore.dto.AlbumDto;
import com.music.musicstore.models.music.Album;
import com.music.musicstore.repositories.ArtistRepository;
import com.music.musicstore.services.AlbumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/albums")
@CrossOrigin(origins = "http://localhost:5173")

public class AlbumApiController {

    private final AlbumService albumService;
    private final ArtistRepository artistRepository;

    @Autowired
    public AlbumApiController(AlbumService albumService, ArtistRepository artistRepository) {
        this.albumService = albumService;
        this.artistRepository = artistRepository;
    }

    // GET /api/albums - Get all albums with pagination and filtering
    @GetMapping
    public ResponseEntity<Page<AlbumDto>> getAllAlbums(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String artist,
            @RequestParam(required = false) String search) {

        Page<Album> albumPage;

        if (search != null && !search.trim().isEmpty()) {
            albumPage = albumService.searchAlbumsByTitle(search, page, size);
        } else if (genre != null && !genre.trim().isEmpty()) {
            albumPage = albumService.getAlbumsByGenre(genre, page, size);
        } else if (artist != null && !artist.trim().isEmpty()) {
            albumPage = albumService.getAlbumsByArtistUsername(artist, page, size);
        } else {
            albumPage = albumService.getAllAlbumsPaginated(page, size, sortBy, sortDir);
        }

        Page<AlbumDto> albumDtoPage = albumPage.map(this::convertToDto);
        return ResponseEntity.ok(albumDtoPage);
    }

    // GET /api/albums/{id} - Get album by ID
    @GetMapping("/{id}")
    public ResponseEntity<AlbumDto> getAlbumById(@PathVariable Long id) {
        Optional<Album> album = albumService.getAlbumById(id);
        if (album.isPresent()) {
            return ResponseEntity.ok(convertToDto(album.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // GET /api/albums/{id}/with-tracks - Get album by ID with tracks
    @GetMapping("/{id}/with-tracks")
    public ResponseEntity<AlbumDto> getAlbumByIdWithTracks(@PathVariable Long id) {
        try {
            Album album = albumService.getAlbumByIdWithTracks(id);
            if (album != null) {
                return ResponseEntity.ok(convertToDto(album));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // POST /api/albums - Create new album
    @PostMapping
    public ResponseEntity<AlbumDto> createAlbum(@RequestBody CreateAlbumRequest request) {
        try {
            // Create new album
            Album album = new Album();
            album.setTitle(request.getTitle());
            album.setDescription(request.getDescription());
            album.setArtistUsername(request.getArtistUsername());
            album.setGenre(request.getGenre());
            album.setPrice(request.getPrice());
            album.setCoverImageUrl(request.getCoverImageUrl());
            album.setCover(request.getCoverImageUrl()); // Set cover field
            album.setReleaseDate(request.getReleaseDate());

            Album savedAlbum = albumService.saveAlbum(album);
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToDto(savedAlbum));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // PUT /api/albums/{id} - Update album
    @PutMapping("/{id}")
    public ResponseEntity<AlbumDto> updateAlbum(@PathVariable Long id, @RequestBody UpdateAlbumRequest request) {
        try {
            Optional<Album> existingAlbum = albumService.getAlbumById(id);
            if (existingAlbum.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Album albumDetails = new Album();
            albumDetails.setTitle(request.getTitle());
            albumDetails.setDescription(request.getDescription());
            albumDetails.setGenre(request.getGenre());
            albumDetails.setPrice(request.getPrice());
            albumDetails.setCoverImageUrl(request.getCoverImageUrl());
            albumDetails.setCover(request.getCoverImageUrl()); // Set cover field

            // If artist is being updated
            if (request.getArtistUsername() != null) {
                albumDetails.setArtistUsername(request.getArtistUsername());
            }

            Album updatedAlbum = albumService.updateAlbum(id, albumDetails);
            if (updatedAlbum != null) {
                return ResponseEntity.ok(convertToDto(updatedAlbum));
            } else {
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // DELETE /api/albums/{id} - Delete album
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteAlbum(@PathVariable Long id) {
        try {
            boolean deleted = albumService.deleteAlbum(id);
            if (deleted) {
                return ResponseEntity.ok(Map.of("message", "Album deleted successfully"));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to delete album"));
        }
    }

    // GET /api/albums/by-artist/{artistUsername} - Get albums by artist
    @GetMapping("/by-artist/{artistUsername}")
    public ResponseEntity<Page<AlbumDto>> getAlbumsByArtist(
            @PathVariable String artistUsername,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        try {
            Page<Album> albumPage = albumService.getAlbumsByArtistUsername(artistUsername, page, size);
            Page<AlbumDto> albumDtoPage = albumPage.map(this::convertToDto);
            return ResponseEntity.ok(albumDtoPage);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // GET /api/albums/artists/{artistUsername}/count - Get count of albums by artist
    @GetMapping("/artists/{artistUsername}/count")
    public ResponseEntity<Map<String, Object>> getArtistAlbumCount(@PathVariable String artistUsername) {
        try {
            long count = albumService.countAlbumsByArtist(artistUsername);
            return ResponseEntity.ok(Map.of("count", count, "artist", artistUsername));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // GET /api/albums/genres - Get all unique genres
    @GetMapping("/genres")
    public ResponseEntity<List<String>> getAllGenres() {
        List<String> genres = List.of("Pop", "Rock", "Jazz", "Classical", "Electronic", "Hip-Hop", "Country", "Blues", "Alternative", "R&B");
        return ResponseEntity.ok(genres);
    }

    // GET /api/albums/search - Search albums
    @GetMapping("/search")
    public ResponseEntity<Page<AlbumDto>> searchAlbums(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        try {
            Page<Album> albumPage = albumService.searchAlbumsByTitle(query, page, size);
            Page<AlbumDto> albumDtoPage = albumPage.map(this::convertToDto);
            return ResponseEntity.ok(albumDtoPage);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Convert Album entity to DTO
    private AlbumDto convertToDto(Album album) {
        return new AlbumDto(
                album.getId(),
                album.getTitle(),
                album.getDescription(),
                album.getArtistUsername() != null ? album.getArtistUsername() : "Unknown Artist",
                album.getGenre(),
                album.getPrice(),
                album.getCoverImageUrl(),
                album.getReleaseDate(),
                album.getCreatedAt(),
                album.getUpdatedAt(),
                0  // Track count removed as tracks relationship was removed
        );
    }

    // Request DTOs for API
    public static class CreateAlbumRequest {
        private String title;
        private String description;
        private String artistUsername;
        private String genre;
        private BigDecimal price;
        private String coverImageUrl;
        private LocalDateTime releaseDate;

        // Getters and setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getArtistUsername() { return artistUsername; }
        public void setArtistUsername(String artistUsername) { this.artistUsername = artistUsername; }

        public String getGenre() { return genre; }
        public void setGenre(String genre) { this.genre = genre; }

        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }

        public String getCoverImageUrl() { return coverImageUrl; }
        public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }

        public LocalDateTime getReleaseDate() { return releaseDate; }
        public void setReleaseDate(LocalDateTime releaseDate) { this.releaseDate = releaseDate; }
    }

    public static class UpdateAlbumRequest {
        private String title;
        private String description;
        private String artistUsername;
        private String genre;
        private BigDecimal price;
        private String coverImageUrl;
        private LocalDateTime releaseDate;

        // Getters and setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getArtistUsername() { return artistUsername; }
        public void setArtistUsername(String artistUsername) { this.artistUsername = artistUsername; }

        public String getGenre() { return genre; }
        public void setGenre(String genre) { this.genre = genre; }

        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }

        public String getCoverImageUrl() { return coverImageUrl; }
        public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }

        public LocalDateTime getReleaseDate() { return releaseDate; }
        public void setReleaseDate(LocalDateTime releaseDate) { this.releaseDate = releaseDate; }
    }
}
