package com.amillioncodelines.streamstories.catalog.api;

import com.amillioncodelines.streamstories.catalog.RatingSummary;

record RatingsResponse(Double average, long count, Integer mine) {

    static RatingsResponse from(RatingSummary summary) {
        return new RatingsResponse(summary.average(), summary.count(), summary.viewerScore());
    }
}
