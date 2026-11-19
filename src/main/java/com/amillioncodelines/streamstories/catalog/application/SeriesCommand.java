package com.amillioncodelines.streamstories.catalog.application;

import java.util.Set;
import java.util.UUID;

public record SeriesCommand(String title, String synopsis, int releaseYear, String coverImageUrl, Set<UUID> genreIds) {}
