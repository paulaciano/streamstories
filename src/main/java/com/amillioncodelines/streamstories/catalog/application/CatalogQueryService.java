package com.amillioncodelines.streamstories.catalog.application;

import com.amillioncodelines.streamstories.catalog.CatalogQueries;
import com.amillioncodelines.streamstories.catalog.ContentKind;
import com.amillioncodelines.streamstories.catalog.PlayableContent;
import com.amillioncodelines.streamstories.catalog.domain.EpisodeRepository;
import com.amillioncodelines.streamstories.catalog.domain.MovieRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class CatalogQueryService implements CatalogQueries {

    private final MovieRepository movies;
    private final EpisodeRepository episodes;

    CatalogQueryService(MovieRepository movies, EpisodeRepository episodes) {
        this.movies = movies;
        this.episodes = episodes;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PlayableContent> findPlayable(ContentKind kind, UUID id) {
        return switch (kind) {
            case MOVIE ->
                movies.findById(id)
                        .map(movie -> new PlayableContent(
                                ContentKind.MOVIE, movie.getId(), movie.getDurationSeconds(), movie.isPlayable()));
            case EPISODE ->
                episodes.findWithSeasonAndSeriesById(id)
                        .map(episode -> new PlayableContent(
                                ContentKind.EPISODE,
                                episode.getId(),
                                episode.getDurationSeconds(),
                                episode.isPlayable()));
            case SERIES -> throw new IllegalArgumentException("A series is played through its episodes");
        };
    }
}
