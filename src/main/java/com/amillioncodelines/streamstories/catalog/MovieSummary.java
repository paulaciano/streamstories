package com.amillioncodelines.streamstories.catalog;

import java.util.UUID;

/** A movie, as other modules show it next to user activity. */
public record MovieSummary(UUID id, String title, String coverImageUrl, int durationSeconds, boolean playable) {}
