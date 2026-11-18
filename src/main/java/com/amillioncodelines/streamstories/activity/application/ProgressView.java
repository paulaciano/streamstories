package com.amillioncodelines.streamstories.activity.application;

import com.amillioncodelines.streamstories.catalog.ContentKind;
import java.time.Instant;
import java.util.UUID;

/**
 * The progress of the current user on a movie or an episode.
 *
 * @param positionSeconds capped at the current duration, which may have changed since it was reported
 */
public record ProgressView(
        ContentKind kind,
        UUID contentId,
        int positionSeconds,
        int durationSeconds,
        boolean completed,
        Instant lastWatchedAt) {}
