package com.amillioncodelines.streamstories.catalog;

/**
 * The ratings of a title.
 *
 * @param average rounded to one decimal, or {@code null} without ratings
 * @param viewerScore the score of the user looking at the title, or {@code null} if they did not rate it
 */
public record RatingSummary(Double average, long count, Integer viewerScore) {

    public static final RatingSummary NONE = new RatingSummary(null, 0, null);
}
