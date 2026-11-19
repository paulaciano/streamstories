package com.amillioncodelines.streamstories.activity.application;

import com.amillioncodelines.streamstories.activity.application.WatchedItemView.EpisodeRef;
import com.amillioncodelines.streamstories.activity.domain.WatchProgress;
import com.amillioncodelines.streamstories.activity.domain.WatchProgressRepository;
import com.amillioncodelines.streamstories.catalog.CatalogQueries;
import com.amillioncodelines.streamstories.catalog.ContentKind;
import com.amillioncodelines.streamstories.catalog.EpisodeSummary;
import com.amillioncodelines.streamstories.catalog.MovieSummary;
import com.amillioncodelines.streamstories.shared.web.Pageables;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WatchHistoryService {

    private final WatchProgressRepository progress;
    private final CatalogQueries catalog;

    WatchHistoryService(WatchProgressRepository progress, CatalogQueries catalog) {
        this.progress = progress;
        this.catalog = catalog;
    }

    /** Which candidates stay depends on the catalog, so the page is cut in memory. */
    @Transactional(readOnly = true)
    public Page<WatchedItemView> continueWatching(UUID userId, Pageable pageable) {
        List<WatchProgress> candidates = progress.findContinueWatchingCandidates(userId);
        Catalog content = lookUp(candidates);
        List<WatchedItemView> entries = new ArrayList<>();
        for (WatchProgress saved : candidates) {
            if (saved.getMovieId() != null) {
                MovieSummary movie = content.movies().get(saved.getMovieId());
                if (movie != null && movie.playable()) {
                    entries.add(view(saved, movie));
                }
            } else {
                EpisodeSummary episode = content.episodes().get(saved.getEpisodeId());
                if (episode != null && resumable(saved, episode)) {
                    entries.add(view(saved, episode));
                }
            }
        }
        return Pageables.slice(entries, pageable);
    }

    @Transactional(readOnly = true)
    public Page<WatchedItemView> history(UUID userId, Pageable pageable) {
        Page<WatchProgress> saved = progress.findByUserId(userId, pageable);
        Catalog content = lookUp(saved.getContent());
        return saved.map(row -> row.getMovieId() != null
                ? view(row, content.movies().get(row.getMovieId()))
                : view(row, content.episodes().get(row.getEpisodeId())));
    }

    private static boolean resumable(WatchProgress saved, EpisodeSummary episode) {
        return episode.seriesAvailable() && (!saved.isCompleted() || episode.followedByPlayableEpisode());
    }

    private Catalog lookUp(List<WatchProgress> rows) {
        List<UUID> movieIds = rows.stream()
                .map(WatchProgress::getMovieId)
                .filter(Objects::nonNull)
                .toList();
        List<UUID> episodeIds = rows.stream()
                .map(WatchProgress::getEpisodeId)
                .filter(Objects::nonNull)
                .toList();
        return new Catalog(
                movieIds.isEmpty() ? Map.of() : catalog.findMovies(movieIds),
                episodeIds.isEmpty() ? Map.of() : catalog.findEpisodes(episodeIds));
    }

    private static WatchedItemView view(WatchProgress saved, MovieSummary movie) {
        return new WatchedItemView(
                ContentKind.MOVIE,
                movie.id(),
                movie.title(),
                movie.coverImageUrl(),
                null,
                Math.min(saved.getPositionSeconds(), movie.durationSeconds()),
                movie.durationSeconds(),
                saved.isCompleted(),
                movie.playable(),
                saved.getLastWatchedAt());
    }

    private static WatchedItemView view(WatchProgress saved, EpisodeSummary episode) {
        return new WatchedItemView(
                ContentKind.SERIES,
                episode.seriesId(),
                episode.seriesTitle(),
                episode.seriesCoverImageUrl(),
                new EpisodeRef(episode.id(), episode.seasonNumber(), episode.number(), episode.title()),
                Math.min(saved.getPositionSeconds(), episode.durationSeconds()),
                episode.durationSeconds(),
                saved.isCompleted(),
                episode.playable(),
                saved.getLastWatchedAt());
    }

    /** Content cannot be deleted while it has progress (rule C11), so every saved row is found in the catalog. */
    private record Catalog(Map<UUID, MovieSummary> movies, Map<UUID, EpisodeSummary> episodes) {}
}
