package com.amillioncodelines.streamstories.catalog.api;

import com.amillioncodelines.streamstories.catalog.application.CatalogViews.SeriesSummaryView;
import com.amillioncodelines.streamstories.catalog.domain.Availability;
import java.util.List;
import java.util.UUID;

record SeriesSummaryResponse(
        UUID id,
        String title,
        String synopsis,
        int releaseYear,
        String coverImageUrl,
        Availability availability,
        List<GenreResponse> genres) {

    static SeriesSummaryResponse from(SeriesSummaryView series) {
        return new SeriesSummaryResponse(
                series.id(),
                series.title(),
                series.synopsis(),
                series.releaseYear(),
                series.coverImageUrl(),
                series.availability(),
                GenreResponse.fromAll(series.genres()));
    }
}
