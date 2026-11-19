package com.amillioncodelines.streamstories.activity.application;

import com.amillioncodelines.streamstories.catalog.ContentKind;
import java.time.Instant;
import java.util.UUID;

public record ProgressView(
        ContentKind kind,
        UUID contentId,
        int positionSeconds,
        int durationSeconds,
        boolean completed,
        Instant lastWatchedAt) {}
