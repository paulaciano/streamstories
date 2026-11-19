package com.amillioncodelines.streamstories.activity.application;

import com.amillioncodelines.streamstories.catalog.ContentKind;
import java.time.Instant;
import java.util.UUID;

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
