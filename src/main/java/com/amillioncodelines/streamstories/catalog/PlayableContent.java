package com.amillioncodelines.streamstories.catalog;

import java.util.UUID;

/**
 * A movie or an episode, as other modules see it.
 *
 * @param playable whether it can be played now: an available movie, or an episode whose season and series are
 *     available
 */
public record PlayableContent(ContentKind kind, UUID id, int durationSeconds, boolean playable) {}
