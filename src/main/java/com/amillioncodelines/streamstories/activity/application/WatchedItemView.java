package com.amillioncodelines.streamstories.activity.application;

import com.amillioncodelines.streamstories.catalog.ContentKind;
import java.time.Instant;
import java.util.UUID;

/**
 * A movie or an episode that the user watched, shown with its title: the movie itself, or the episode's series.
 *
 * @param type {@link ContentKind#MOVIE} or {@link ContentKind#SERIES}
 * @param episode only for series
 * @param positionSeconds capped at the current duration, which may have changed since it was reported (rule A2)
 * @param playable whether it can be played now
 */
public record WatchedItemView(
        ContentKind type,
        UUID titleId,
        String title,
        String coverImageUrl,
        EpisodeRef episode,
        int positionSeconds,
        int durationSeconds,
        boolean completed,
        boolean playable,
        Instant lastWatchedAt) {

    public record EpisodeRef(UUID id, int seasonNumber, int number, String title) {}
}
