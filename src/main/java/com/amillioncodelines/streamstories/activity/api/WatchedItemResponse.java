package com.amillioncodelines.streamstories.activity.api;

import com.amillioncodelines.streamstories.activity.application.WatchedItemView;
import com.amillioncodelines.streamstories.activity.application.WatchedItemView.EpisodeRef;
import com.amillioncodelines.streamstories.catalog.ContentKind;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
record WatchedItemResponse(
        ContentKind type,
        UUID titleId,
        String title,
        String coverImageUrl,
        EpisodeResponse episode,
        int positionSeconds,
        int durationSeconds,
        boolean completed,
        boolean playable,
        Instant lastWatchedAt) {

    record EpisodeResponse(UUID id, int seasonNumber, int number, String title) {

        static EpisodeResponse from(EpisodeRef episode) {
            return episode == null
                    ? null
                    : new EpisodeResponse(episode.id(), episode.seasonNumber(), episode.number(), episode.title());
        }
    }

    static WatchedItemResponse from(WatchedItemView view) {
        return new WatchedItemResponse(
                view.type(),
                view.titleId(),
                view.title(),
                view.coverImageUrl(),
                EpisodeResponse.from(view.episode()),
                view.positionSeconds(),
                view.durationSeconds(),
                view.completed(),
                view.playable(),
                view.lastWatchedAt());
    }
}
