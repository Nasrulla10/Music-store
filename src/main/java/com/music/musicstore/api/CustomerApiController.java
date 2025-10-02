package com.music.musicstore.api;

import com.music.musicstore.dto.CreateReviewRequest;
import com.music.musicstore.dto.ErrorResponse;
import com.music.musicstore.models.music.Music;
import com.music.musicstore.models.users.Customer;
import com.music.musicstore.models.support.Ticket;
import com.music.musicstore.services.CartService;
import com.music.musicstore.services.OrderService;
import com.music.musicstore.services.MusicService;
import com.music.musicstore.services.ReviewService;
import com.music.musicstore.services.TicketService;
import com.music.musicstore.repositories.CustomerRepository;
import com.music.musicstore.dto.TicketMessageDto;
import com.music.musicstore.dto.TicketMessageMapper;
import com.music.musicstore.dto.TicketDto;
import com.music.musicstore.dto.TicketMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletResponse;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.Map;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("/api/customer")
@PreAuthorize("hasRole('CUSTOMER')")
@CrossOrigin(origins = "http://localhost:5173")
public class CustomerApiController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerApiController.class);

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private MusicService musicService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TicketMessageMapper ticketMessageMapper;

    @Autowired
    private TicketMapper ticketMapper;

    @GetMapping("/purchased")
    public ResponseEntity<Set<Music>> getPurchasedMusic(@AuthenticationPrincipal Customer customer) {
        // Fetch customer with purchased music eagerly loaded to avoid LazyInitializationException
        Customer customerWithPurchasedMusic = customerRepository.findByIdWithPurchasedMusic(customer.getId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        return ResponseEntity.ok(customerWithPurchasedMusic.getPurchasedMusic());
    }




    // Cart Management
    @GetMapping("/cart")
    public ResponseEntity<?> getCart(@AuthenticationPrincipal Customer customer) {
        try {
            return ResponseEntity.ok(cartService.getCartByUsername(customer.getUsername()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch cart: " + e.getMessage()));
        }
    }

    @PostMapping("/cart/add/{musicId}")
    public ResponseEntity<?> addToCart(
            @PathVariable Long musicId,
            @AuthenticationPrincipal Customer customer) {
        try {
            cartService.addToCart(customer, musicId);
            return ResponseEntity.ok(new SuccessResponse("Music added to cart successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to add to cart: " + e.getMessage()));
        }
    }

    @DeleteMapping("/cart/remove/{musicId}")
    public ResponseEntity<?> removeFromCart(
            @PathVariable Long musicId,
            @AuthenticationPrincipal Customer customer) {
        try {
            cartService.removeFromCart(customer.getUsername(), musicId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to remove from cart: " + e.getMessage()));
        }
    }

    @PostMapping("/cart/checkout")
    public ResponseEntity<?> checkout(@AuthenticationPrincipal Customer customer) {
        try {
            return ResponseEntity.ok(orderService.checkout(customer.getUsername()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to checkout: " + e.getMessage()));
        }
    }

    // Purchase Management
    @PostMapping("/purchase/{musicId}")
    public ResponseEntity<?> purchaseMusic(
            @PathVariable Long musicId,
            @AuthenticationPrincipal Customer customer) {
        try {
            return ResponseEntity.ok(orderService.purchaseMusic(customer.getUsername(), musicId));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to purchase music: " + e.getMessage()));
        }
    }

    // Order Management
    @GetMapping("/orders")
    public ResponseEntity<?> getOrderHistory(@AuthenticationPrincipal Customer customer,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int size) {
        try {
            return ResponseEntity.ok(orderService.getOrdersByUsername(customer.getUsername(), page, size));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch order history: " + e.getMessage()));
        }
    }

    // Review Management
    @PostMapping("/review")
    public ResponseEntity<?> submitReview(
            @Valid @RequestBody CreateReviewRequest request,
            @AuthenticationPrincipal Customer customer) {
        try {
            return ResponseEntity.ok(reviewService.createReview(
                request.getMusicId(),
                customer,
                request.getRating(),
                request.getComment()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to submit review: " + e.getMessage()));
        }
    }

    // Support Ticket Management
    @PostMapping("/support/ticket")
    public ResponseEntity<?> createTicket(
            @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal Customer customer) {
        try {
            String subject = payload.get("subject");
            String description = payload.get("description");
            Ticket ticket = ticketService.createTicket(customer, subject, description);
            TicketDto dto = ticketMapper.toDto(ticket);
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to create ticket: " + e.getMessage()));
        }
    }

    @GetMapping("/support/tickets")
    public ResponseEntity<?> getTickets(@AuthenticationPrincipal Customer customer) {
        try {
            var tickets = ticketService.getTicketsByUsername(customer.getUsername());
            List<TicketDto> dtoList = ticketMapper.toDtoList(tickets);
            return ResponseEntity.ok(dtoList);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch tickets: " + e.getMessage()));
        }
    }

    @PostMapping("/support/ticket/{ticketId}/message")
    public ResponseEntity<?> addMessageToTicket(
            @PathVariable Long ticketId,
            @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal Customer customer) {
        try {
            String content = payload.get("content");
            var message = ticketService.addCustomerMessage(ticketId, content, customer);
            TicketMessageDto dto = ticketMessageMapper.toDto(message);
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to add message: " + e.getMessage()));
        }
    }

    @GetMapping("/support/ticket/{ticketId}/messages")
    public ResponseEntity<?> getTicketMessages(@PathVariable Long ticketId,
                                               @AuthenticationPrincipal Customer customer) {
        try {
            // Verify customer owns this ticket
            var ticket = ticketService.getTicketById(ticketId);
            if (ticket.isPresent() && ticket.get().getCustomer().getId().equals(customer.getId())) {
                var messages = ticketService.getTicketMessages(ticketId);
                List<TicketMessageDto> dtos = messages.stream().map(ticketMessageMapper::toDto).toList();
                return ResponseEntity.ok(dtos);
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("Ticket not found or access denied"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch messages: " + e.getMessage()));
        }
    }

    // Profile Management
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal Customer customer) {
        return ResponseEntity.ok(customer);
    }

    // Download Management
    @GetMapping("/download/{musicId}")
    public ResponseEntity<?> downloadMusic(@PathVariable Long musicId,
                                           @AuthenticationPrincipal Customer customer,
                                           HttpServletResponse servletResponse) {
        try {
            if (musicId == null) {
                return ResponseEntity.badRequest().body(new ErrorResponse("musicId is required"));
            }

            // Verify customer purchased this music
            boolean hasPurchased = customerRepository.hasPurchasedMusic(customer.getId(), musicId);
            if (!hasPurchased) {
                return ResponseEntity.status(403)
                        .body(new ErrorResponse("Access denied: you must purchase this track to download it"));
            }

            // Load music metadata
            var optMusic = musicService.getMusicById(musicId);
            if (optMusic.isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Music not found"));
            }
            Music music = optMusic.get();

            String audioPathStr = music.getAudioFilePath();
            String originalFileName = music.getOriginalFileName();
            if (originalFileName == null || originalFileName.isBlank()) {
                // fallback to a sanitized music name
                originalFileName = music.getName().replaceAll("[^a-zA-Z0-9._-]", "_") + ".mp3";
            }

            Path filePath = null;
            // Try a few likely locations
            if (audioPathStr != null && !audioPathStr.isBlank()) {
                Path p = Paths.get(audioPathStr);
                if (Files.exists(p) && Files.isRegularFile(p)) {
                    filePath = p;
                }
            }

            // Normalize audio filename for relative lookups (handle leading slashes)
            String audioFileName = null;
            if (audioPathStr != null && !audioPathStr.isBlank()) {
                try {
                    Path ap = Paths.get(audioPathStr);
                    // prefer the file name portion for resolving into resource folders
                    audioFileName = ap.getFileName() != null ? ap.getFileName().toString() : audioPathStr;
                } catch (Exception ex) {
                    audioFileName = audioPathStr;
                }
            }

            if (filePath == null) {
                // check resource folder (source)
                Path p2 = Paths.get("src/main/resources/static/uploads/music").resolve(audioFileName != null ? audioFileName : originalFileName);
                if (Files.exists(p2) && Files.isRegularFile(p2)) {
                    filePath = p2;
                }
            }

            if (filePath == null) {
                // check built classes resources
                Path p3 = Paths.get("target/classes/static/uploads/music").resolve(audioFileName != null ? audioFileName : originalFileName);
                if (Files.exists(p3) && Files.isRegularFile(p3)) {
                    filePath = p3;
                }
            }

            if (filePath == null) {
                // check uploads folder configured at runtime
                Path p4 = Paths.get("./uploads/music").resolve(audioFileName != null ? audioFileName : originalFileName);
                if (Files.exists(p4) && Files.isRegularFile(p4)) {
                    filePath = p4;
                }
            }

            if (filePath == null) {
                logger.warn("Audio file not found for music id {} (audioFilePath='{}')", musicId, audioPathStr);
                // Attempt to load from classpath (inside jar) at static/uploads/music/<filename>
                if (audioFileName != null) {
                    String classpathLocation = "/static/uploads/music/" + audioFileName;
                    try (InputStream resourceStream = CustomerApiController.class.getResourceAsStream(classpathLocation)) {
                        if (resourceStream != null) {
                            // Stream classpath resource directly to servlet response
                            servletResponse.setContentType("audio/mpeg");
                            servletResponse.setHeader("Content-Disposition", "attachment; filename=\"" + (originalFileName != null ? originalFileName : audioFileName) + "\"");
                            try (InputStream in = CustomerApiController.class.getResourceAsStream(classpathLocation); OutputStream out = servletResponse.getOutputStream()) {
                                byte[] buffer = new byte[8192];
                                int bytesRead;
                                while ((bytesRead = in.read(buffer)) != -1) {
                                    out.write(buffer, 0, bytesRead);
                                }
                                out.flush();
                            }
                            return ResponseEntity.ok().build();
                        }
                    }
                }
                return ResponseEntity.badRequest().body(new ErrorResponse("Audio file not found"));
            }

            // Ensure it's an mp3
            String fileNameLower = filePath.getFileName().toString().toLowerCase();
            String contentType = URLConnection.guessContentTypeFromName(fileNameLower);
            if (contentType == null) {
                contentType = Files.probeContentType(filePath);
            }
            if (contentType == null) {
                // fallback
                contentType = "application/octet-stream";
            }

            if (!fileNameLower.endsWith(".mp3") && !"audio/mpeg".equalsIgnoreCase(contentType)) {
                logger.warn("Blocked download - file is not MP3: {} (detected contentType={})", filePath, contentType);
                return ResponseEntity.badRequest().body(new ErrorResponse("Requested file is not an MP3"));
            }

            // Stream file directly to servlet response to avoid converter issues
            servletResponse.setContentType("audio/mpeg");
            servletResponse.setHeader("Content-Disposition", "attachment; filename=\"" + originalFileName + "\"");
            servletResponse.setContentLengthLong(Files.size(filePath));

            try (InputStream in = Files.newInputStream(filePath); OutputStream out = servletResponse.getOutputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                out.flush();
            }

            // Response already committed with the streamed bytes
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            logger.error("Error while preparing download for musicId {}: {}", musicId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Failed to prepare download: " + e.getMessage()));
        }
    }

    @RequestMapping(value = "/download/{musicId}", method = RequestMethod.HEAD)
    public ResponseEntity<?> downloadMusicHead(@PathVariable Long musicId,
                                               @AuthenticationPrincipal Customer customer) {
        try {
            if (musicId == null) {
                return ResponseEntity.badRequest().body(new ErrorResponse("musicId is required"));
            }

            // Verify customer purchased this music
            boolean hasPurchased = customerRepository.hasPurchasedMusic(customer.getId(), musicId);
            if (!hasPurchased) {
                return ResponseEntity.status(403)
                        .body(new ErrorResponse("Access denied: you must purchase this track to download it"));
            }

            var optMusic = musicService.getMusicById(musicId);
            if (optMusic.isEmpty()) {
                return ResponseEntity.status(404).body(new ErrorResponse("Music not found"));
            }
            Music music = optMusic.get();

            String audioPathStr = music.getAudioFilePath();
            String originalFileName = music.getOriginalFileName();
            if (originalFileName == null || originalFileName.isBlank()) {
                originalFileName = music.getName().replaceAll("[^a-zA-Z0-9._-]", "_") + ".mp3";
            }

            Path filePath = null;
            if (audioPathStr != null && !audioPathStr.isBlank()) {
                Path p = Paths.get(audioPathStr);
                if (Files.exists(p) && Files.isRegularFile(p)) {
                    filePath = p;
                }
            }

            String audioFileName = null;
            if (audioPathStr != null && !audioPathStr.isBlank()) {
                try {
                    Path ap = Paths.get(audioPathStr);
                    audioFileName = ap.getFileName() != null ? ap.getFileName().toString() : audioPathStr;
                } catch (Exception ex) {
                    audioFileName = audioPathStr;
                }
            }

            if (filePath == null) {
                Path p2 = Paths.get("src/main/resources/static/uploads/music").resolve(audioFileName != null ? audioFileName : originalFileName);
                if (Files.exists(p2) && Files.isRegularFile(p2)) {
                    filePath = p2;
                }
            }

            if (filePath == null) {
                Path p3 = Paths.get("target/classes/static/uploads/music").resolve(audioFileName != null ? audioFileName : originalFileName);
                if (Files.exists(p3) && Files.isRegularFile(p3)) {
                    filePath = p3;
                }
            }

            if (filePath == null) {
                Path p4 = Paths.get("./uploads/music").resolve(audioFileName != null ? audioFileName : originalFileName);
                if (Files.exists(p4) && Files.isRegularFile(p4)) {
                    filePath = p4;
                }
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("audio/mpeg"));

            if (filePath != null) {
                headers.setContentLength(Files.size(filePath));
                headers.setContentDispositionFormData("attachment", originalFileName);
                return ResponseEntity.ok().headers(headers).build();
            }

            // Try classpath
            if (audioFileName != null) {
                String classpathLocation = "/static/uploads/music/" + audioFileName;
                var resourceStream = CustomerApiController.class.getResourceAsStream(classpathLocation);
                if (resourceStream != null) {
                    headers.setContentDispositionFormData("attachment", originalFileName);
                    return ResponseEntity.ok().headers(headers).build();
                }
            }

            return ResponseEntity.status(404).body(new ErrorResponse("Audio file not found"));
        } catch (Exception e) {
            logger.error("Error during HEAD for download {}: {}", musicId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new ErrorResponse("Failed to check download: " + e.getMessage()));
        }
    }

    // Define SuccessResponse for consistent JSON responses
    private static class SuccessResponse {
        public String message;
        public SuccessResponse(String message) { this.message = message; }
    }
}
