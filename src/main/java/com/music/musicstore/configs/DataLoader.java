package com.music.musicstore.configs;

import com.music.musicstore.models.music.Album;
import com.music.musicstore.models.music.Music;
import com.music.musicstore.models.users.Artist;
import com.music.musicstore.repositories.AlbumRepository;
import com.music.musicstore.repositories.ArtistRepository;
import com.music.musicstore.repositories.MusicRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner seedArtistAndMusicData(
            MusicRepository musicRepository,
            ArtistRepository artistRepository,
            AlbumRepository albumRepository,
            PasswordEncoder encoder
    ) {
        return args -> {
            // Artists
            Artist edSheeran = createArtist("Ed Sheeran", "EdSheeran123", "EdSheeran321", "EdSheeran123@gmail.com", "static/uploads/covers/Ed shareen.jpg", artistRepository, encoder);
            Artist justinBieber = createArtist("Justin Bieber", "JustinBieber123", "JustinBieber321", "JustinBieber123@gmail.com", "static/uploads/covers/Justin Bieber.jpg", artistRepository, encoder);
            Artist ladyGaga = createArtist("Lady Gaga", "LadyGaga123", "LadyGaga321", "LadyGaga123@gmail.com", "static/uploads/covers/Lady Gaga.jpg", artistRepository, encoder);
            Artist oneDirection = createArtist("One Direction", "OneDirection123", "OneDirection321", "OneDirection123@gmail.com", "static/uploads/covers/One Direction.jpg", artistRepository, encoder);
            Artist sabrinaCarpenter = createArtist("Sabrina Carpenter", "SabrinaCarpenter123", "SabrinaCarpenter321", "SabrinaCarpenter123@gmail.com", "static/uploads/covers/Sabrina Carpenter.jpeg", artistRepository, encoder);
            Artist alexWarren = createArtist("Alex Warren", "AlexWarren123", "AlexWarren321", "AlexWarren123@gmail.com", "static/uploads/covers/Alex Warren.jpg", artistRepository, encoder);

            // Albums
            Album edSheeranAlbum = createAlbum("Ed Sheeran's Hits", "A collection of Ed Sheeran's greatest hits.", edSheeran.getUserName(), "Pop", new BigDecimal("9.99"), "static/uploads/covers/Ed shareen.jpg", albumRepository, artistRepository);
            Album justinBieberAlbum = createAlbum("Justin Bieber's Collection", "The best of Justin Bieber.", justinBieber.getUserName(), "Pop", new BigDecimal("12.99"), "static/uploads/covers/Justin Bieber.jpg", albumRepository, artistRepository);
            Album ladyGagaAlbum = createAlbum("Gaga's Anthems", "Iconic tracks from Lady Gaga.", ladyGaga.getUserName(), "Pop", new BigDecimal("11.99"), "static/uploads/covers/Lady Gaga.jpg", albumRepository, artistRepository);
            Album oneDirectionAlbum = createAlbum("One Direction: The Ultimate Album", "All the hits from One Direction.", oneDirection.getUserName(), "Pop", new BigDecimal("15.99"), "static/uploads/covers/One Direction.jpg", albumRepository, artistRepository);
            Album sabrinaCarpenterAlbum = createAlbum("Sabrina's Songs", "A collection of songs by Sabrina Carpenter.", sabrinaCarpenter.getUserName(), "Pop", new BigDecimal("8.99"), "static/uploads/covers/Sabrina Carpenter.jpeg", albumRepository, artistRepository);

            // Music
            createMusic("Ordinary", "Alex Warren - Ordinary (Official Video)", new BigDecimal("1.29"), "Pop", alexWarren.getUserName(), null, "Pop", 2023, "static/uploads/music/Alex Warren - Ordinary (Official Video).mp3", "static/uploads/covers/Alex Warren.jpg", musicRepository);
            createMusic("A Little More", "Ed Sheeran - A Little More (Official Music Video)", new BigDecimal("1.29"), "Pop", edSheeran.getUserName(), edSheeranAlbum.getTitle(), "Pop", 2017, "static/uploads/music/Ed Sheeran - A Little More (Official Music Video).mp3", "static/uploads/covers/Ed shareen.jpg", musicRepository);
            createMusic("Camera", "Ed Sheeran - Camera (Amazon Music Songline)", new BigDecimal("1.29"), "Pop", edSheeran.getUserName(), edSheeranAlbum.getTitle(), "Pop", 2017, "static/uploads/music/Ed Sheeran - Camera (Amazon Music Songline).mp3", "static/uploads/covers/Ed shareen.jpg", musicRepository);
            createMusic("Freedom", "Ed Sheeran - Freedom [Lyric Video]", new BigDecimal("1.29"), "Pop", edSheeran.getUserName(), edSheeranAlbum.getTitle(), "Pop", 2017, "static/uploads/music/Ed Sheeran - Freedom [Lyric Video].mp3", "static/uploads/covers/Ed shareen.jpg", musicRepository);
            createMusic("Perfect", "Ed Sheeran - Perfect (Official Music Video)", new BigDecimal("1.29"), "Pop", edSheeran.getUserName(), edSheeranAlbum.getTitle(), "Pop", 2017, "static/uploads/music/Ed Sheeran - Perfect (Official Music Video).mp3", "static/uploads/covers/Ed shareen.jpg", musicRepository);
            createMusic("Shape of You", "Ed Sheeran - Shape of You (Official Music Video)", new BigDecimal("1.29"), "Pop", edSheeran.getUserName(), edSheeranAlbum.getTitle(), "Pop", 2017, "static/uploads/music/Ed Sheeran - Shape of You (Official Music Video).mp3", "static/uploads/covers/Ed shareen.jpg", musicRepository);
            createMusic("Thinking Out Loud", "Ed Sheeran - Thinking Out Loud (Official Music Video)", new BigDecimal("1.29"), "Pop", edSheeran.getUserName(), edSheeranAlbum.getTitle(), "Pop", 2014, "static/uploads/music/Ed Sheeran - Thinking Out Loud (Official Music Video).mp3", "static/uploads/covers/Ed shareen.jpg", musicRepository);
            createMusic("Baby", "Justin Bieber - Baby ft. Ludacris", new BigDecimal("1.29"), "Pop", justinBieber.getUserName(), justinBieberAlbum.getTitle(), "Pop", 2010, "static/uploads/music/Justin Bieber - Baby ft. Ludacris.mp3", "static/uploads/covers/Justin Bieber.jpg", musicRepository);
            createMusic("Company", "Justin Bieber - Company", new BigDecimal("1.29"), "Pop", justinBieber.getUserName(), justinBieberAlbum.getTitle(), "Pop", 2015, "static/uploads/music/Justin Bieber - Company.mp3", "static/uploads/covers/Justin Bieber.jpg", musicRepository);
            createMusic("DAISIES", "Justin Bieber - DAISIES (Audio)", new BigDecimal("1.29"), "Pop", justinBieber.getUserName(), justinBieberAlbum.getTitle(), "Pop", 2020, "static/uploads/music/Justin Bieber - DAISIES (Audio).mp3", "static/uploads/covers/Justin Bieber.jpg", musicRepository);
            createMusic("Ghost", "Justin Bieber - Ghost", new BigDecimal("1.29"), "Pop", justinBieber.getUserName(), justinBieberAlbum.getTitle(), "Pop", 2021, "static/uploads/music/Justin Bieber - Ghost.mp3", "static/uploads/covers/Justin Bieber.jpg", musicRepository);
            createMusic("Never Say Never", "Justin Bieber - Never Say Never ft. Jaden", new BigDecimal("1.29"), "Pop", justinBieber.getUserName(), justinBieberAlbum.getTitle(), "Pop", 2010, "static/uploads/music/Justin Bieber - Never Say Never ft. Jaden.mp3", "static/uploads/covers/Justin Bieber.jpg", musicRepository);
            createMusic("Peaches", "Justin Bieber - Peaches ft. Daniel Caesar, Giveon", new BigDecimal("1.29"), "Pop", justinBieber.getUserName(), justinBieberAlbum.getTitle(), "Pop", 2021, "static/uploads/music/Justin Bieber - Peaches ft. Daniel Caesar, Giveon.mp3", "static/uploads/covers/Justin Bieber.jpg", musicRepository);
            createMusic("Sorry", "Justin Bieber - Sorry (Lyric Video)", new BigDecimal("1.29"), "Pop", justinBieber.getUserName(), justinBieberAlbum.getTitle(), "Pop", 2015, "static/uploads/music/Justin Bieber - Sorry (Lyric Video).mp3", "static/uploads/covers/Justin Bieber.jpg", musicRepository);
            createMusic("YUKON", "Justin Bieber - YUKON", new BigDecimal("1.29"), "Pop", justinBieber.getUserName(), justinBieberAlbum.getTitle(), "Pop", 2022, "static/uploads/music/Justin Bieber - YUKON.mp3", "static/uploads/covers/Justin Bieber.jpg", musicRepository);
            createMusic("STAY", "The Kid LAROI, Justin Bieber - STAY (Official Video)", new BigDecimal("1.29"), "Pop", justinBieber.getUserName(), justinBieberAlbum.getTitle(), "Pop", 2021, "static/uploads/music/The Kid LAROI, Justin Bieber - STAY (Official Video).mp3", "static/uploads/covers/Justin Bieber.jpg", musicRepository);
            createMusic("Abracadabra", "Lady Gaga - Abracadabra (Official Music Video)", new BigDecimal("1.29"), "Pop", ladyGaga.getUserName(), ladyGagaAlbum.getTitle(), "Pop", 2023, "static/uploads/music/Lady Gaga - Abracadabra (Official Music Video).mp3", "static/uploads/covers/Lady Gaga.jpg", musicRepository);
            createMusic("The Dead Dance", "Lady Gaga - The Dead Dance (Official Music Video)", new BigDecimal("1.29"), "Pop", ladyGaga.getUserName(), ladyGagaAlbum.getTitle(), "Pop", 2023, "static/uploads/music/Lady Gaga - The Dead Dance (Official Music Video).mp3", "static/uploads/covers/Lady Gaga.jpg", musicRepository);
            createMusic("Die With A Smile", "Lady Gaga, Bruno Mars - Die With A Smile (Official Music Video)", new BigDecimal("1.29"), "Pop", ladyGaga.getUserName(), ladyGagaAlbum.getTitle(), "Pop", 2023, "static/uploads/music/Lady Gaga, Bruno Mars - Die With A Smile (Official Music Video).mp3", "static/uploads/covers/Lady Gaga.jpg", musicRepository);
            createMusic("Drag Me Down", "One Direction - Drag Me Down (Official Video)", new BigDecimal("1.29"), "Pop", oneDirection.getUserName(), oneDirectionAlbum.getTitle(), "Pop", 2015, "static/uploads/music/One Direction - Drag Me Down (Official Video).mp3", "static/uploads/covers/One Direction.jpg", musicRepository);
            createMusic("Kiss You", "One Direction - Kiss You (Official)", new BigDecimal("1.29"), "Pop", oneDirection.getUserName(), oneDirectionAlbum.getTitle(), "Pop", 2013, "static/uploads/music/One Direction - Kiss You (Official).mp3", "static/uploads/covers/One Direction.jpg", musicRepository);
            createMusic("Live While We're Young", "One Direction - Live While We're Young", new BigDecimal("1.29"), "Pop", oneDirection.getUserName(), oneDirectionAlbum.getTitle(), "Pop", 2012, "static/uploads/music/One Direction - Live While We're Young.mp3", "static/uploads/covers/One Direction.jpg", musicRepository);
            createMusic("Night Changes", "One Direction - Night Changes", new BigDecimal("1.29"), "Pop", oneDirection.getUserName(), oneDirectionAlbum.getTitle(), "Pop", 2014, "static/uploads/music/One Direction - Night Changes.mp3", "static/uploads/covers/One Direction.jpg", musicRepository);
            createMusic("One Thing", "One Direction - One Thing", new BigDecimal("1.29"), "Pop", oneDirection.getUserName(), oneDirectionAlbum.getTitle(), "Pop", 2012, "static/uploads/music/One Direction - One Thing.mp3", "static/uploads/covers/One Direction.jpg", musicRepository);
            createMusic("Perfect", "One Direction - Perfect (Official Video)", new BigDecimal("1.29"), "Pop", oneDirection.getUserName(), oneDirectionAlbum.getTitle(), "Pop", 2015, "static/uploads/music/One Direction - Perfect (Official Video).mp3", "static/uploads/covers/One Direction.jpg", musicRepository);
            createMusic("Steal My Girl", "One Direction - Steal My Girl", new BigDecimal("1.29"), "Pop", oneDirection.getUserName(), oneDirectionAlbum.getTitle(), "Pop", 2014, "static/uploads/music/One Direction - Steal My Girl.mp3", "static/uploads/covers/One Direction.jpg", musicRepository);
            createMusic("Story of My Life", "One Direction - Story of My Life (Official 4K Video)", new BigDecimal("1.29"), "Pop", oneDirection.getUserName(), oneDirectionAlbum.getTitle(), "Pop", 2013, "static/uploads/music/One Direction - Story of My Life (Official 4K Video).mp3", "static/uploads/covers/One Direction.jpg", musicRepository);
            createMusic("What Makes You Beautiful", "One Direction - What Makes You Beautiful (Official Video)", new BigDecimal("1.29"), "Pop", oneDirection.getUserName(), oneDirectionAlbum.getTitle(), "Pop", 2011, "static/uploads/music/One Direction - What Makes You Beautiful (Official Video).mp3", "static/uploads/covers/One Direction.jpg", musicRepository);
            createMusic("You & I", "One Direction - You & I", new BigDecimal("1.29"), "Pop", oneDirection.getUserName(), oneDirectionAlbum.getTitle(), "Pop", 2014, "static/uploads/music/One Direction - You & I.mp3", "static/uploads/covers/One Direction.jpg", musicRepository);
            createMusic("Manchild", "Sabrina Carpenter - Manchild (Official Video)", new BigDecimal("1.29"), "Pop", sabrinaCarpenter.getUserName(), sabrinaCarpenterAlbum.getTitle(), "Pop", 2023, "static/uploads/music/Sabrina Carpenter - Manchild (Official Video).mp3", "static/uploads/covers/Sabrina Carpenter.jpeg", musicRepository);
            createMusic("Tears", "Sabrina Carpenter - Tears (Official Video)", new BigDecimal("1.29"), "Pop", sabrinaCarpenter.getUserName(), sabrinaCarpenterAlbum.getTitle(), "Pop", 2023, "static/uploads/music/Sabrina Carpenter - Tears (Official Video).mp3", "static/uploads/covers/Sabrina Carpenter.jpeg", musicRepository);
            createMusic("When Did You Get Hot?", "Sabrina Carpenter - When Did You Get Hot? (Official Lyric Video)", new BigDecimal("1.29"), "Pop", sabrinaCarpenter.getUserName(), sabrinaCarpenterAlbum.getTitle(), "Pop", 2023, "static/uploads/music/Sabrina Carpenter - When Did You Get Hot？ (Official Lyric Video).mp3", "static/uploads/covers/Sabrina Carpenter.jpeg", musicRepository);
        };
    }

    private Artist createArtist(String name, String username, String password, String email, String photoUrl, ArtistRepository artistRepository, PasswordEncoder encoder) {
        return artistRepository.findByUserName(username).orElseGet(() -> {
            Artist artist = new Artist();
            artist.setArtistName(name);
            artist.setUserName(username);
            artist.setPassword(encoder.encode(password)); // Encode the raw password here
            artist.setEmail(email);
            artist.setPhotoUrl(photoUrl);
            artist.setRole("ROLE_ARTIST");
            artist.setCreatedAt(LocalDateTime.now());
            return artistRepository.save(artist);
        });
    }

    private Album createAlbum(String title, String description, String artistUsername, String genre, BigDecimal price, String coverImageUrl, AlbumRepository albumRepository, ArtistRepository artistRepository) {
        return albumRepository.findByTitle(title).orElseGet(() -> {
            Album album = new Album();
            album.setTitle(title);
            album.setDescription(description);
            album.setArtistUsername(artistUsername);
            album.setGenre(genre);
            album.setPrice(price);
            album.setCoverImageUrl(coverImageUrl);
            album.setCover(coverImageUrl); // Set cover field to satisfy NOT NULL constraint
            album.setReleaseDate(LocalDateTime.now());
            return albumRepository.save(album);
        });
    }

    private void createMusic(String name, String desc, BigDecimal price, String category, String artistUsername, String albumTitle, String genre, int year, String audioPath, String imageUrl, MusicRepository musicRepository) {
        if (musicRepository.findByName(name).isEmpty()) {
            Music m = new Music();
            m.setName(name);
            m.setDescription(desc);
            m.setPrice(price);
            m.setCategory(category);
            m.setArtistUsername(artistUsername);
            m.setAlbumName(albumTitle);
            m.setGenre(genre);
            m.setReleaseYear(year);
            m.setImageUrl(imageUrl);
            m.setAudioFilePath(audioPath);
            m.setCreatedAt(LocalDateTime.now());
            m.setUpdatedAt(LocalDateTime.now());
            musicRepository.save(m);
        }
    }
}
