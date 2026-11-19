package com.amillioncodelines.streamstories.catalog.api;

import com.amillioncodelines.streamstories.catalog.application.CatalogViews.CatalogItemView;
import com.amillioncodelines.streamstories.catalog.domain.Availability;
import com.amillioncodelines.streamstories.catalog.domain.MediaType;
import java.util.List;
import java.util.UUID;

record CatalogItemResponse(
        UUID id,
        MediaType type,
        String title,
        int releaseYear,
        String coverImageUrl,
        Availability availability,
        List<GenreResponse> genres) {

    static CatalogItemResponse from(CatalogItemView item) {
        return new CatalogItemResponse(
                item.id(),
                item.type(),
                item.title(),
                item.releaseYear(),
                item.coverImageUrl(),
                item.availability(),
                GenreResponse.fromAll(item.genres()));
    }
}
