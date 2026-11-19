package com.amillioncodelines.streamstories.activity.api;

import com.amillioncodelines.streamstories.activity.application.RatingView;
import com.amillioncodelines.streamstories.catalog.ContentKind;
import java.time.Instant;
import java.util.UUID;

record RatingResponse(
        UUID titleId,
        ContentKind type,
        String title,
        String coverImageUrl,
        boolean available,
        int score,
        Instant ratedAt) {

    static RatingResponse from(RatingView view) {
        return new RatingResponse(
                view.titleId(),
                view.type(),
                view.title(),
                view.coverImageUrl(),
                view.available(),
                view.score(),
                view.ratedAt());
    }
}
