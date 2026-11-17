package com.amillioncodelines.streamstories.catalog;

import java.util.UUID;

/**
 * Tells the catalog whether a piece of content has user activity (watch progress, ratings, watchlist entries).
 *
 * <p>The catalog does not know about users or their activity. Modules that record activity implement this interface,
 * and the catalog asks every implementation before physically deleting content (rule C11). Without implementations,
 * no content has activity.
 */
public interface ContentActivity {

    boolean exists(ContentKind kind, UUID contentId);
}
