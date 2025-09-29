package com.music.musicstore.api;

import com.music.musicstore.configs.JwtUtil;
import com.music.musicstore.dto.LoginRequest;
import com.music.musicstore.dto.RegisterRequest;
import com.music.musicstore.dto.UnifiedRegisterRequest;
import com.music.musicstore.dto.AuthResponse;
import com.music.musicstore.dto.UserDto;
import com.music.musicstore.services.UnifiedUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthApiController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final UnifiedUserService unifiedUserService;

    @Autowired
    public AuthApiController(AuthenticationManager authenticationManager,
                           @Qualifier("combinedUserDetailsService") UserDetailsService userDetailsService,
                           JwtUtil jwtUtil,
                           UnifiedUserService unifiedUserService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.unifiedUserService = unifiedUserService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        System.out.println("Login request received: " + request.getUsername());
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
            String token = jwtUtil.generateToken(userDetails);

            UserDto userDto = unifiedUserService.getUserInfo(userDetails);

            return ResponseEntity.ok(new AuthResponse(token, userDto));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Invalid username or password"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UnifiedRegisterRequest request) {
        try {
            // Validate role
            if (!isValidRole(request.getRole())) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Invalid role. Allowed roles: CUSTOMER, ARTIST"));
            }

            // Only allow CUSTOMER and ARTIST registration through public endpoint
            // STAFF and ADMIN should be created by admins only
            if (!request.getRole().equalsIgnoreCase("CUSTOMER") &&
                !request.getRole().equalsIgnoreCase("ARTIST")) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Only CUSTOMER and ARTIST registration is allowed"));
            }

            UserDto userDto = unifiedUserService.createUser(
                request.getUsername(),
                request.getPassword(),
                request.getEmail(),
                request.getRole(),
                request.getFirstName(),
                request.getLastName(),
                request.getArtistName(),
                request.getCover()
            );

            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
            String token = jwtUtil.generateToken(userDetails);

            return ResponseEntity.ok(new AuthResponse(token, userDto));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Registration failed: " + e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(401)
                    .body(new ErrorResponse("Not authenticated"));
            }

            UserDto userDto = unifiedUserService.getUserInfo(userDetails);
            return ResponseEntity.ok(userDto);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(new ErrorResponse("Failed to get user info"));
        }
    }

    @PostMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Invalid token format"));
            }

            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);
            String role = jwtUtil.extractRole(token);

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtUtil.validateToken(token, userDetails)) {
                UserDto userDto = unifiedUserService.getUserInfo(userDetails);
                return ResponseEntity.ok(userDto);
            } else {
                return ResponseEntity.status(401)
                    .body(new ErrorResponse("Invalid or expired token"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(401)
                .body(new ErrorResponse("Token validation failed"));
        }
    }

    private boolean isValidRole(String role) {
        return role != null && (
            role.equalsIgnoreCase("CUSTOMER") ||
            role.equalsIgnoreCase("ARTIST") ||
            role.equalsIgnoreCase("STAFF") ||
            role.equalsIgnoreCase("ADMIN")
        );
    }

    // Inner class for error responses
    public static class ErrorResponse {
        private String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
