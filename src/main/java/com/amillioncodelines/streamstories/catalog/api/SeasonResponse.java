package com.amillioncodelines.streamstories.catalog.api;

import com.amillioncodelines.streamstories.catalog.application.CatalogViews.SeasonView;
import com.amillioncodelines.streamstories.catalog.domain.Availability;
import java.util.List;
import java.util.UUID;

/** A season with the episodes visible to the caller. */
record SeasonResponse(UUID seriesId, int number, String title, List<EpisodeSummary> episodes) {

    record EpisodeSummary(UUID id, int number, String title, int durationSeconds, Availability availability) {}

    static SeasonResponse from(SeasonView season) {
        List<EpisodeSummary> episodes = season.episodes().stream()
                .map(episode -> new EpisodeSummary(
                        episode.id(),
                        episode.number(),
                        episode.title(),
                        episode.durationSeconds(),
                        episode.availability()))
                .toList();
        return new SeasonResponse(season.seriesId(), season.number(), season.title(), episodes);
    }
}
