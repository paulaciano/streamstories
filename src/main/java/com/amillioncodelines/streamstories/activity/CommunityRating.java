package com.amillioncodelines.streamstories.activity;

/** The ratings of a title from every user: the exact average (not rounded) and how many there are. */
public record CommunityRating(double average, long count) {}
