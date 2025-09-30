package com.music.musicstore.models.music;

import com.music.musicstore.models.users.Customer;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "playlists")
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Playlist name is required")
    @Size(max = 100, message = "Playlist name cannot exceed 100 characters")
    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToMany
    @JoinTable(
            name = "playlist_music",
            joinColumns = @JoinColumn(name = "playlist_id"),
            inverseJoinColumns = @JoinColumn(name = "music_id")
    )
    private Set<Music> musics = new HashSet<>();

    @Column(name = "track_count", nullable = false)
    private Integer trackCount = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Default constructor required by JPA
    public Playlist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Playlist(String name, Customer customer) {
        this.name = name;
        this.customer = customer;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.trackCount = 0;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.updatedAt = LocalDateTime.now();
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Set<Music> getMusics() {
        return musics;
    }

    public void setMusics(Set<Music> musics) {
        this.musics = musics;
        this.trackCount = musics.size();
        this.updatedAt = LocalDateTime.now();
    }

    public Integer getTrackCount() {
        return trackCount;
    }

    public void setTrackCount(Integer trackCount) {
        this.trackCount = trackCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Utility methods for managing music tracks
    public void addMusic(Music music) {
        this.musics.add(music);
        this.trackCount = this.musics.size();
        this.updatedAt = LocalDateTime.now();
    }

    public void removeMusic(Music music) {
        this.musics.remove(music);
        this.trackCount = this.musics.size();
        this.updatedAt = LocalDateTime.now();
    }

    public boolean containsMusic(Music music) {
        return this.musics.contains(music);
    }
}
