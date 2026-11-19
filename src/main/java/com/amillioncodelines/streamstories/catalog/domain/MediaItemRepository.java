package com.amillioncodelines.streamstories.catalog.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface MediaItemRepository extends JpaRepository<MediaItem, UUID>, JpaSpecificationExecutor<MediaItem> {

    @Query("select count(m) > 0 from MediaItem m join m.genres g where g.id = :genreId")
    boolean existsWithGenre(UUID genreId);

    /** Plain rows (id, type, title, cover, year, availability, genre id, genre name), not entities (ADR-0012). */
    @Query(nativeQuery = true, value = """
            SELECT m.id, m.type, m.title, m.cover_image_url, m.release_year, m.availability, g.id, g.name
            FROM media_item m
            LEFT JOIN media_item_genre mg ON mg.media_item_id = m.id
            LEFT JOIN genre g ON g.id = mg.genre_id
            ORDER BY m.id, g.name
            """)
    List<Object[]> findAllWithGenres();
}
