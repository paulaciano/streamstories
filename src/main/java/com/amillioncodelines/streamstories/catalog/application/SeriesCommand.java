package com.amillioncodelines.streamstories.catalog.application;

import java.util.Set;
import java.util.UUID;

/** Full data of a series, used to create it or replace it. Seasons are managed separately. */
public record SeriesCommand(String title, String synopsis, int releaseYear, String coverImageUrl, Set<UUID> genreIds) {}
