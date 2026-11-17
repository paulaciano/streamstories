package com.amillioncodelines.streamstories.catalog.domain;

/** Descriptive data shared by movies and series. */
public record TitleDetails(String title, String synopsis, int releaseYear, String coverImageUrl) {}
