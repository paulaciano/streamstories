package com.amillioncodelines.streamstories.catalog.api;

import com.amillioncodelines.streamstories.catalog.application.CatalogViews.GenreView;
import java.util.List;
import java.util.UUID;

record GenreResponse(UUID id, String name, String slug) {

    static GenreResponse from(GenreView genre) {
        return new GenreResponse(genre.id(), genre.name(), genre.slug());
    }

    static List<GenreResponse> fromAll(List<GenreView> genres) {
        return genres.stream().map(GenreResponse::from).toList();
    }
}
