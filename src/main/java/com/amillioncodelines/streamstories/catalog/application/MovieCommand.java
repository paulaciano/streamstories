package com.amillioncodelines.streamstories.catalog.application;

import java.util.Set;
import java.util.UUID;

public record MovieCommand(
        String title,
        String synopsis,
        int releaseYear,
        int durationSeconds,
        String videoUrl,
        String coverImageUrl,
        Set<UUID> genreIds) {}
