package com.amillioncodelines.streamstories.catalog.application;

import com.amillioncodelines.streamstories.catalog.domain.Availability;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record FranchiseView(
        UUID id, String name, List<FranchiseMovieView> movies, Instant createdAt, Instant updatedAt) {

    public record FranchiseMovieView(
            UUID id, int order, String title, int releaseYear, String coverImageUrl, Availability availability) {}

    public record FranchiseSummaryView(UUID id, String name) {}
}
