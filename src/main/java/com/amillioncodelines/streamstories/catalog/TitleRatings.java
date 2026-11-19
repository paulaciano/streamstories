package com.amillioncodelines.streamstories.catalog;

import java.util.UUID;

/** Implemented by the module that records ratings, so the catalog does not depend on it. */
public interface TitleRatings {

    RatingSummary summarize(UUID titleId, UUID viewerId);
}
