package com.amillioncodelines.streamstories.catalog.application;

public record EpisodeCommand(int number, String title, String synopsis, int durationSeconds, String videoUrl) {}
