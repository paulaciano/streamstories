package com.amillioncodelines.streamstories.activity.application;

import com.amillioncodelines.streamstories.activity.application.WatchedItemView.EpisodeRef;
import com.amillioncodelines.streamstories.activity.domain.WatchProgress;
import com.amillioncodelines.streamstories.activity.domain.WatchProgressRepository;
import com.amillioncodelines.streamstories.catalog.CatalogQueries;
import com.amillioncodelines.streamstories.catalog.ContentKind;
import com.amillioncodelines.streamstories.catalog.EpisodeSummary;
import com.amillioncodelines.streamstories.catalog.MovieSummary;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** "Continue watching" and the history, built from the saved progress and the catalog (rules A5 to A8). */
@Service
public class WatchHistoryService {

    private final WatchProgressRepository progress;
    private final CatalogQueries catalog;

    WatchHistoryService(WatchProgressRepository progress, CatalogQueries catalog) {
        this.progress = progress;
        this.catalog = catalog;
    }

    /**
     * Movies not completed and one entry per series with its most recently watched episode. A series leaves once
     * that episode is completed and no playable episode comes after it. Retired movies and series are left out.
     *
     * <p>Built in memory from the user's own progress, which is small enough; the page is cut at the end.
     */
    @Transactional(readOnly = true)
    public Page<WatchedItemView> continueWatching(UUID userId, Pageable pageable) {
        List<WatchProgress> candidates = progress.findContinueWatchingCandidates(userId);
        Catalog content = lookUp(candidates);
        Set<UUID> seenSeries = new HashSet<>();
        List<WatchedItemView> entries = new ArrayList<>();
        for (WatchProgress saved : candidates) {
            if (saved.getMovieId() != null) {
                MovieSummary movie = content.movies().get(saved.getMovieId());
                if (movie != null && movie.playable()) {
                    entries.add(view(saved, movie));
                }
            } else {
                EpisodeSummary episode = content.episodes().get(saved.getEpisodeId());
                // Only the most recently watched episode of each series counts (rule A6).
                if (episode != null && seenSeries.add(episode.seriesId()) && resumable(saved, episode)) {
                    entries.add(view(saved, episode));
                }
            }
        }
        return page(entries, pageable);
    }

    /** Every movie and episode watched, including retired content, marked as not playable (rule A8). */
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

    private static <T> Page<T> page(List<T> all, Pageable pageable) {
        int from = (int) Math.min(pageable.getOffset(), all.size());
        int to = Math.min(from + pageable.getPageSize(), all.size());
        return new PageImpl<>(all.subList(from, to), pageable, all.size());
    }

    /** Content cannot be deleted while it has progress (rule C11), so every saved row is found in the catalog. */
    private record Catalog(Map<UUID, MovieSummary> movies, Map<UUID, EpisodeSummary> episodes) {}
}
