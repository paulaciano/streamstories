package com.amillioncodelines.streamstories.catalog;

import java.util.UUID;

public record PlayableContent(ContentKind kind, UUID id, int durationSeconds, boolean playable, UUID seriesId) {}
