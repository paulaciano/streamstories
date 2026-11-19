package com.amillioncodelines.streamstories.activity.application;

import com.amillioncodelines.streamstories.activity.domain.PlaybackPosition;
import com.amillioncodelines.streamstories.activity.domain.WatchProgress;
import com.amillioncodelines.streamstories.activity.domain.WatchProgressRepository;
import com.amillioncodelines.streamstories.catalog.CatalogQueries;
import com.amillioncodelines.streamstories.catalog.ContentKind;
import com.amillioncodelines.streamstories.catalog.PlayableContent;
import com.amillioncodelines.streamstories.shared.error.ContentUnavailableException;
import com.amillioncodelines.streamstories.shared.error.ResourceNotFoundException;
import java.time.Clock;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProgressService {

    private final WatchProgressRepository progress;
    private final CatalogQueries catalog;
    private final Clock clock;

    ProgressService(WatchProgressRepository progress, CatalogQueries catalog, Clock clock) {
        this.progress = progress;
        this.catalog = catalog;
        this.clock = clock;
    }

    @Transactional
    public ProgressView reportMovie(UUID userId, UUID movieId, int positionSeconds) {
        PlayableContent movie = playable(ContentKind.MOVIE, movieId, "Movie %s does not exist.");
        PlaybackPosition position = PlaybackPosition.of(positionSeconds, movie.durationSeconds());
        progress.upsertMovieProgress(
                userId, movieId, position.positionSeconds(), position.completed(), clock.instant());
        return view(movie, progress.findByUserIdAndMovieId(userId, movieId).orElseThrow());
    }

    @Transactional
    public ProgressView reportEpisode(UUID userId, UUID episodeId, int positionSeconds) {
        PlayableContent episode = playable(ContentKind.EPISODE, episodeId, "Episode %s does not exist.");
        PlaybackPosition position = PlaybackPosition.of(positionSeconds, episode.durationSeconds());
        progress.upsertEpisodeProgress(
                userId,
                episodeId,
                episode.seriesId(),
                position.positionSeconds(),
                position.completed(),
                clock.instant());
        return view(
                episode, progress.findByUserIdAndEpisodeId(userId, episodeId).orElseThrow());
    }

    private PlayableContent playable(ContentKind kind, UUID id, String notFoundMessage) {
        PlayableContent content = catalog.findPlayable(kind, id)
                .orElseThrow(() -> new ResourceNotFoundException(notFoundMessage.formatted(id)));
        if (!content.playable()) {
            throw new ContentUnavailableException("Content %s is retired and cannot be played.".formatted(id));
        }
        return content;
    }

    private static ProgressView view(PlayableContent content, WatchProgress saved) {
        return new ProgressView(
                content.kind(),
                content.id(),
                Math.min(saved.getPositionSeconds(), content.durationSeconds()),
                content.durationSeconds(),
                saved.isCompleted(),
                saved.getLastWatchedAt());
    }
}
