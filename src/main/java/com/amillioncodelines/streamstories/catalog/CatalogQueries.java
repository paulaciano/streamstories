package com.amillioncodelines.streamstories.catalog;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface CatalogQueries {

    Optional<PlayableContent> findPlayable(ContentKind kind, UUID id);

    Map<UUID, MovieSummary> findMovies(Collection<UUID> ids);

    Map<UUID, EpisodeSummary> findEpisodes(Collection<UUID> ids);

    Optional<TitleSummary> findTitle(UUID id);

    Map<UUID, TitleSummary> findTitles(Collection<UUID> ids);

    List<CatalogTitle> findAllTitles();
}
