package com.amillioncodelines.streamstories.catalog.api;

import com.amillioncodelines.streamstories.catalog.RatingSummary;

/**
 * Ratings in the detail of a title.
 *
 * @param average one decimal, {@code null} without ratings
 * @param mine the current user's score, {@code null} if they did not rate the title
 */
record RatingsResponse(Double average, long count, Integer mine) {

    static RatingsResponse from(RatingSummary summary) {
        return new RatingsResponse(summary.average(), summary.count(), summary.viewerScore());
    }
}
