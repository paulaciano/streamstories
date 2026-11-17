package com.amillioncodelines.streamstories.catalog.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EpisodeRepository extends JpaRepository<Episode, UUID> {

    /** Loads the episode with its season and series, which every episode view needs. */
    @EntityGraph(attributePaths = {"season", "season.series"})
    Optional<Episode> findWithSeasonAndSeriesById(UUID id);
}
