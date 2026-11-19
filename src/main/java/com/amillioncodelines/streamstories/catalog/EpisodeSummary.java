package com.amillioncodelines.streamstories.catalog;

import java.util.UUID;

public record EpisodeSummary(
        UUID id,
        int seasonNumber,
        int number,
        String title,
        int durationSeconds,
        boolean playable,
        UUID seriesId,
        String seriesTitle,
        String seriesCoverImageUrl,
        boolean seriesAvailable,
        boolean followedByPlayableEpisode) {}
