package com.amillioncodelines.streamstories.catalog;

import java.util.List;
import java.util.UUID;

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
