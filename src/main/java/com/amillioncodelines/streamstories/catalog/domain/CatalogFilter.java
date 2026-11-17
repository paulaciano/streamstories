package com.amillioncodelines.streamstories.catalog.domain;

/**
 * Optional criteria to browse titles. A {@code null} field does not filter.
 *
 * @param genreSlug only titles classified in this genre
 * @param type only movies or only series
 * @param text case-insensitive fragment of the title
 * @param availability only titles with this availability
 */
public record CatalogFilter(String genreSlug, MediaType type, String text, Availability availability) {}
