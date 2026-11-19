package com.amillioncodelines.streamstories.catalog.application;

import com.amillioncodelines.streamstories.catalog.CatalogQueries;
import com.amillioncodelines.streamstories.catalog.CatalogTitle;
import com.amillioncodelines.streamstories.catalog.CatalogTitle.GenreRef;
import com.amillioncodelines.streamstories.catalog.ContentKind;
import com.amillioncodelines.streamstories.catalog.EpisodeSummary;
import com.amillioncodelines.streamstories.catalog.MovieSummary;
import com.amillioncodelines.streamstories.catalog.PlayableContent;
import com.amillioncodelines.streamstories.catalog.TitleSummary;
import com.amillioncodelines.streamstories.catalog.domain.Episode;
import com.amillioncodelines.streamstories.catalog.domain.EpisodeRepository;
import com.amillioncodelines.streamstories.catalog.domain.MediaItem;
import com.amillioncodelines.streamstories.catalog.domain.MediaItemRepository;
import com.amillioncodelines.streamstories.catalog.domain.MediaType;
import com.amillioncodelines.streamstories.catalog.domain.MovieRepository;
import com.amillioncodelines.streamstories.catalog.domain.Series;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class CatalogQueryService implements CatalogQueries {

    private final MediaItemRepository titles;
    private final MovieRepository movies;
    private final EpisodeRepository episodes;

    CatalogQueryService(MediaItemRepository titles, MovieRepository movies, EpisodeRepository episodes) {
        this.titles = titles;
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
                                ContentKind.MOVIE,
                                movie.getId(),
                                movie.getDurationSeconds(),
                                movie.isPlayable(),
                                null));
            case EPISODE ->
                episodes.findWithSeasonAndSeriesById(id)
                        .map(episode -> new PlayableContent(
                                ContentKind.EPISODE,
                                episode.getId(),
                                episode.getDurationSeconds(),
                                episode.isPlayable(),
                                episode.getSeason().getSeries().getId()));
            case SERIES -> throw new IllegalArgumentException("A series is played through its episodes");
        };
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, MovieSummary> findMovies(Collection<UUID> ids) {
        return movies.findAllById(ids).stream()
                .map(movie -> new MovieSummary(
                        movie.getId(),
                        movie.getTitle(),
                        movie.getCoverImageUrl(),
                        movie.getDurationSeconds(),
                        movie.isPlayable()))
                .collect(Collectors.toMap(MovieSummary::id, Function.identity()));
    }

    /** Seasons and episodes of each series are loaded in batches (default_batch_fetch_size), not one by one. */
    @Override
    @Transactional(readOnly = true)
    public Map<UUID, EpisodeSummary> findEpisodes(Collection<UUID> ids) {
        return episodes.findWithSeasonAndSeriesByIdIn(ids).stream()
                .map(CatalogQueryService::summary)
                .collect(Collectors.toMap(EpisodeSummary::id, Function.identity()));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TitleSummary> findTitle(UUID id) {
        return titles.findById(id).map(CatalogQueryService::summary);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, TitleSummary> findTitles(Collection<UUID> ids) {
        return titles.findAllById(ids).stream()
                .map(CatalogQueryService::summary)
                .collect(Collectors.toMap(TitleSummary::id, Function.identity()));
    }

    /** Genres are loaded in batches (default_batch_fetch_size), not one query per title. */
    @Override
    @Transactional(readOnly = true)
    public List<CatalogTitle> findAllTitles() {
        return titles.findAll().stream().map(CatalogQueryService::catalogTitle).toList();
    }

    private static CatalogTitle catalogTitle(MediaItem title) {
        List<GenreRef> genres = title.getGenres().stream()
                .map(genre -> new GenreRef(genre.getId(), genre.getName()))
                .sorted(Comparator.comparing(GenreRef::name))
                .toList();
        return new CatalogTitle(
                title.getId(),
                kind(title),
                title.getTitle(),
                title.getCoverImageUrl(),
                title.getReleaseYear(),
                title.isAvailable(),
                genres);
    }

    private static ContentKind kind(MediaItem title) {
        return title.getType() == MediaType.MOVIE ? ContentKind.MOVIE : ContentKind.SERIES;
    }

    private static TitleSummary summary(MediaItem title) {
        return new TitleSummary(
                title.getId(), kind(title), title.getTitle(), title.getCoverImageUrl(), title.isAvailable());
    }

    private static EpisodeSummary summary(Episode episode) {
        Series series = episode.getSeason().getSeries();
        return new EpisodeSummary(
                episode.getId(),
                episode.getSeason().getNumber(),
                episode.getNumber(),
                episode.getTitle(),
                episode.getDurationSeconds(),
                episode.isPlayable(),
                series.getId(),
                series.getTitle(),
                series.getCoverImageUrl(),
                series.isAvailable(),
                series.hasPlayableEpisodeAfter(episode));
    }
}
