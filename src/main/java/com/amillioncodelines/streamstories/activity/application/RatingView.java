package com.amillioncodelines.streamstories.activity.application;

import com.amillioncodelines.streamstories.catalog.ContentKind;
import java.time.Instant;
import java.util.UUID;

/**
 * A rating of the current user, with its title.
 *
 * @param type {@link ContentKind#MOVIE} or {@link ContentKind#SERIES}
 * @param available whether the title is available; a retired one keeps its ratings (rule A12)
 */
public record RatingView(
        UUID titleId,
        ContentKind type,
        String title,
        String coverImageUrl,
        boolean available,
        int score,
        Instant ratedAt) {}
