package com.amillioncodelines.streamstories.catalog.application;

import java.util.Set;
import java.util.UUID;

/** Full data of a movie, used to create it or replace it. Input validation happens in the API layer. */
public record MovieCommand(
        String title,
        String synopsis,
        int releaseYear,
        int durationSeconds,
        String videoUrl,
        String coverImageUrl,
        Set<UUID> genreIds) {}
