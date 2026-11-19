package com.amillioncodelines.streamstories.catalog.domain;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface MediaItemRepository extends JpaRepository<MediaItem, UUID>, JpaSpecificationExecutor<MediaItem> {

    @Query("select count(m) > 0 from MediaItem m join m.genres g where g.id = :genreId")
    boolean existsWithGenre(UUID genreId);
}
