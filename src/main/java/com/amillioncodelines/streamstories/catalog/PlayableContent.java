package com.amillioncodelines.streamstories.catalog;

import java.util.UUID;

/**
 * A movie or an episode, as other modules see it.
 *
 * @param playable whether it can be played now: an available movie, or an episode whose season and series are
 *     available
 * @param seriesId the series of an episode, {@code null} for a movie
 */
public record PlayableContent(ContentKind kind, UUID id, int durationSeconds, boolean playable, UUID seriesId) {}
