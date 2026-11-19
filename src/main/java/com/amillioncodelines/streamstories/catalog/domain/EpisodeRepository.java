package com.amillioncodelines.streamstories.catalog.domain;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EpisodeRepository extends JpaRepository<Episode, UUID> {

    @EntityGraph(attributePaths = {"season", "season.series"})
    Optional<Episode> findWithSeasonAndSeriesById(UUID id);

    @EntityGraph(attributePaths = {"season", "season.series"})
    List<Episode> findWithSeasonAndSeriesByIdIn(Collection<UUID> ids);
}
