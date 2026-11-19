package com.amillioncodelines.streamstories.catalog;

import java.util.UUID;

public record MovieSummary(UUID id, String title, String coverImageUrl, int durationSeconds, boolean playable) {}
