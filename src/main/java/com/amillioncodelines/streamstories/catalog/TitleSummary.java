package com.amillioncodelines.streamstories.catalog;

import java.util.UUID;

/**
 * A title (movie or series), as other modules show it next to user activity.
 *
 * @param type {@link ContentKind#MOVIE} or {@link ContentKind#SERIES}
 */
public record TitleSummary(UUID id, ContentKind type, String title, String coverImageUrl, boolean available) {}
