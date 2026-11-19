package com.amillioncodelines.streamstories.catalog.api;

import com.amillioncodelines.streamstories.catalog.application.FranchiseView;
import com.amillioncodelines.streamstories.catalog.application.FranchiseView.FranchiseSummaryView;
import com.amillioncodelines.streamstories.catalog.domain.Availability;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

record FranchiseResponse(UUID id, String name, List<MovieEntry> movies, Instant createdAt, Instant updatedAt) {

    record MovieEntry(
            UUID id, int order, String title, int releaseYear, String coverImageUrl, Availability availability) {}

    record Summary(UUID id, String name) {

        static Summary from(FranchiseSummaryView view) {
            return new Summary(view.id(), view.name());
        }
    }

    static FranchiseResponse from(FranchiseView view) {
        List<MovieEntry> movies = view.movies().stream()
                .map(movie -> new MovieEntry(
                        movie.id(),
                        movie.order(),
                        movie.title(),
                        movie.releaseYear(),
                        movie.coverImageUrl(),
                        movie.availability()))
                .toList();
        return new FranchiseResponse(view.id(), view.name(), movies, view.createdAt(), view.updatedAt());
    }
}
