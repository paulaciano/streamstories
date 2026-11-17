package com.amillioncodelines.streamstories.catalog.api;

import com.amillioncodelines.streamstories.catalog.application.CatalogViews.SeriesView;
import com.amillioncodelines.streamstories.catalog.domain.Availability;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

record SeriesResponse(
        UUID id,
        String title,
        String synopsis,
        int releaseYear,
        String coverImageUrl,
        Availability availability,
        List<GenreResponse> genres,
        List<SeasonSummary> seasons,
        Instant createdAt,
        Instant updatedAt) {

    /**
     * @param episodeCount episodes visible to the caller: only available ones for a viewer
     */
    record SeasonSummary(int number, String title, int episodeCount) {}

    static SeriesResponse from(SeriesView series) {
        List<SeasonSummary> seasons = series.seasons().stream()
                .map(season -> new SeasonSummary(season.number(), season.title(), season.episodeCount()))
                .toList();
        return new SeriesResponse(
                series.id(),
                series.title(),
                series.synopsis(),
                series.releaseYear(),
                series.coverImageUrl(),
                series.availability(),
                GenreResponse.fromAll(series.genres()),
                seasons,
                series.createdAt(),
                series.updatedAt());
    }
}
