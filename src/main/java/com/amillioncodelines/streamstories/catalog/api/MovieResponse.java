package com.amillioncodelines.streamstories.catalog.api;

import com.amillioncodelines.streamstories.catalog.RatingSummary;
import com.amillioncodelines.streamstories.catalog.application.CatalogViews.MovieView;
import com.amillioncodelines.streamstories.catalog.domain.Availability;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * @param videoUrl {@code null} for a viewer when the movie is retired
 */
record MovieResponse(
        UUID id,
        String title,
        String synopsis,
        int releaseYear,
        int durationSeconds,
        String videoUrl,
        String coverImageUrl,
        Availability availability,
        List<GenreResponse> genres,
        Instant createdAt,
        Instant updatedAt,
        @JsonInclude(JsonInclude.Include.NON_NULL) RatingsResponse ratings) {

    static MovieResponse from(MovieView movie) {
        return from(movie, null);
    }

    /** The detail of a title also includes its ratings. */
    static MovieResponse from(MovieView movie, RatingSummary ratings) {
        return new MovieResponse(
                movie.id(),
                movie.title(),
                movie.synopsis(),
                movie.releaseYear(),
                movie.durationSeconds(),
                movie.videoUrl(),
                movie.coverImageUrl(),
                movie.availability(),
                GenreResponse.fromAll(movie.genres()),
                movie.createdAt(),
                movie.updatedAt(),
                ratings == null ? null : RatingsResponse.from(ratings));
    }
}
