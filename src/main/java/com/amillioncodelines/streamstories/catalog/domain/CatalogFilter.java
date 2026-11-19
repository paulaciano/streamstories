package com.amillioncodelines.streamstories.catalog.domain;

public record CatalogFilter(String genreSlug, MediaType type, String text, Availability availability) {}
