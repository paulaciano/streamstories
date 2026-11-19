package com.amillioncodelines.streamstories.catalog;

import java.util.List;
import java.util.UUID;

/**
 * A title with its genres, as the recommendation engine reads the catalog.
 *
 * @param type {@link ContentKind#MOVIE} or {@link ContentKind#SERIES}
 */
public record CatalogTitle(
        UUID id,
        ContentKind type,
        String title,
        String coverImageUrl,
        int releaseYear,
        boolean available,
        List<GenreRef> genres) {

    public record GenreRef(UUID id, String name) {}
}
