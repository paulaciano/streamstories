package com.amillioncodelines.streamstories.catalog.domain;

/** Descriptive and media data of an episode. */
public record EpisodeDetails(String title, String synopsis, int durationSeconds, String videoUrl) {}
