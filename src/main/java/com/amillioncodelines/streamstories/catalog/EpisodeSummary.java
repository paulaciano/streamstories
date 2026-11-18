package com.amillioncodelines.streamstories.catalog;

import java.util.UUID;

/**
 * An episode with its series, as other modules show it next to user activity.
 *
 * @param playable whether its season and its series are available
 * @param followedByPlayableEpisode whether some playable episode comes after it in the series
 */
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
