package com.amillioncodelines.streamstories.activity.api;

import com.amillioncodelines.streamstories.activity.application.ProgressView;
import com.amillioncodelines.streamstories.catalog.ContentKind;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
record ProgressResponse(
        UUID movieId,
        UUID episodeId,
        int positionSeconds,
        int durationSeconds,
        boolean completed,
        Instant lastWatchedAt) {

    static ProgressResponse from(ProgressView view) {
        boolean movie = view.kind() == ContentKind.MOVIE;
        return new ProgressResponse(
                movie ? view.contentId() : null,
                movie ? null : view.contentId(),
                view.positionSeconds(),
                view.durationSeconds(),
                view.completed(),
                view.lastWatchedAt());
    }
}
