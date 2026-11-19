package com.amillioncodelines.streamstories.activity.application;

import com.amillioncodelines.streamstories.catalog.ContentKind;
import java.time.Instant;
import java.util.UUID;

public record RatingView(
        UUID titleId,
        ContentKind type,
        String title,
        String coverImageUrl,
        boolean available,
        int score,
        Instant ratedAt) {}
