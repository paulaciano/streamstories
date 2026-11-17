package com.amillioncodelines.streamstories.catalog.application;

/** Full data of an episode, used to create it or replace it. */
public record EpisodeCommand(int number, String title, String synopsis, int durationSeconds, String videoUrl) {}
