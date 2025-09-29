package com.music.musicstore.dto;

import java.time.LocalDateTime;

public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String role;
    private String firstName;
    private String lastName;
    private String artistName; // For artists
    private String cover; // For artists
    private boolean enabled; // User account status
    private LocalDateTime createdAt; // Account creation timestamp

    public UserDto() {}

    public UserDto(Long id, String username, String email, String role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
    }

    public UserDto(Long id, String username, String email, String role, String firstName, String lastName) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // Constructor with enabled and createdAt fields
    public UserDto(Long id, String username, String email, String role, String firstName, String lastName, boolean enabled, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.firstName = firstName;
        this.lastName = lastName;
        this.enabled = enabled;
        this.createdAt = createdAt;
    }

    // Constructor for artists
//    public UserDto(Long id, String username, String email, String role, String artistName, String cover) {
//        this.id = id;
//        this.username = username;
//        this.email = email;
//        this.role = role;
//        this.artistName = artistName;
//        this.cover = cover;
//    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getArtistName() { return artistName; }
    public void setArtistName(String artistName) { this.artistName = artistName; }

    public String getCover() { return cover; }
    public void setCover(String cover) { this.cover = cover; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
