package com.amillioncodelines.streamstories.catalog;

public record RatingSummary(Double average, long count, Integer viewerScore) {

    public static final RatingSummary NONE = new RatingSummary(null, 0, null);
}
