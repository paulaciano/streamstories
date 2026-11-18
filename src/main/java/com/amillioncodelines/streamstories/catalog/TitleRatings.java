package com.amillioncodelines.streamstories.catalog;

import java.util.UUID;

/**
 * Gives the catalog the ratings of a title, to show them in its detail.
 *
 * <p>Like {@link ContentActivity}, the catalog defines what it needs and the module that records ratings implements
 * it, so the catalog does not depend on it. Without an implementation, titles have no ratings.
 */
public interface TitleRatings {

    /**
     * @param viewerId the user looking at the title, whose own score is included
     */
    RatingSummary summarize(UUID titleId, UUID viewerId);
}
