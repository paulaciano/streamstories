package com.amillioncodelines.streamstories.catalog.api;

import com.amillioncodelines.streamstories.catalog.application.CatalogViews.EpisodeView;
import java.util.UUID;

/**
 * @param videoUrl {@code null} for a viewer when the episode cannot be played
 * @param playable whether the episode's season and series are both available
 */
record EpisodeResponse(
        UUID id,
        UUID seriesId,
        String seriesTitle,
        int seasonNumber,
        int number,
        String title,
        String synopsis,
        int durationSeconds,
        String videoUrl,
        boolean playable) {

    static EpisodeResponse from(EpisodeView episode) {
        return new EpisodeResponse(
                episode.id(),
                episode.seriesId(),
                episode.seriesTitle(),
                episode.seasonNumber(),
                episode.number(),
                episode.title(),
                episode.synopsis(),
                episode.durationSeconds(),
                episode.videoUrl(),
                episode.playable());
    }
}
