@Repository
public interface MusicRepository extends JpaRepository<Music, Long> {

    List<Music> findByCategory(String category);

    List<Music> findByArtistUsername(String artistUsername);

    List<Music> findByGenre(String genre);

    List<Music> findByReleaseYear(Integer releaseYear);

    Page<Music> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Music> findByArtistUsernameContainingIgnoreCase(String artistUsername, Pageable pageable);

    Page<Music> findByGenreContainingIgnoreCase(String genre, Pageable pageable);

    Page<Music> findByCategoryContainingIgnoreCase(String category, Pageable pageable);

    // Combined search
    Page<Music> findByNameContainingIgnoreCaseOrArtistUsernameContainingIgnoreCaseOrGenreContainingIgnoreCase(
            String name, String artistUsername, String genre, Pageable pageable);

    // Search by title or artist (for CustomerApiController search)
    Page<Music> findByNameContainingIgnoreCaseOrArtistUsernameContainingIgnoreCase(
            String name, String artistUsername, Pageable pageable);


    Optional<Music> findByName(String name);
    Optional<Music> findByNameContainingIgnoreCase(String name);

    // Paginated version for better performance
    Page<Music> findByArtistUsername(String artistUsername, Pageable pageable);

    // Find by genre with pagination
    Page<Music> findByGenre(String genre, Pageable pageable);

    // Missing methods for AdminApiController functionality

    // Count methods for analytics
    long countByArtistUsername(String artistUsername);

    // Flagged music methods
    Page<Music> findByIsFlaggedTrue(Pageable pageable);
    long countByIsFlaggedTrue();

    // Analytics methods
    @Query("SELECT AVG(m.averageRating) FROM Music m WHERE m.averageRating IS NOT NULL")
    Double getAverageRating();

    @Query("SELECT m.genre, COUNT(m) FROM Music m WHERE m.genre IS NOT NULL GROUP BY m.genre")
    List<Object[]> countByGenreGroupBy();

    @Query("SELECT m.category, COUNT(m) FROM Music m WHERE m.category IS NOT NULL GROUP BY m.category")
    List<Object[]> countByCategoryGroupBy();

    // Top-rated music for "top selling" placeholder
    Page<Music> findAllByOrderByAverageRatingDesc(Pageable pageable);

    // Artist performance analytics
    @Query("SELECT m.artistUsername, COUNT(m), AVG(m.averageRating), SUM(m.totalReviews) " +
            "FROM Music m WHERE m.artistUsername IS NOT NULL " +
            "GROUP BY m.artistUsername")
    List<Object[]> getArtistPerformanceStats();
}