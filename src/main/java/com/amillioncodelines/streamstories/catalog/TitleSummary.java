package com.amillioncodelines.streamstories.catalog;

import java.util.UUID;

public record TitleSummary(UUID id, ContentKind type, String title, String coverImageUrl, boolean available) {}
