package com.amillioncodelines.streamstories.catalog.api;

import com.amillioncodelines.streamstories.catalog.application.CatalogViews.MovieView;
import com.amillioncodelines.streamstories.catalog.domain.Availability;
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
        Instant updatedAt) {

    static MovieResponse from(MovieView movie) {
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
                movie.updatedAt());
    }
}
