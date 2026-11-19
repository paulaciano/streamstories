package com.amillioncodelines.streamstories.activity;

import java.util.Map;
import java.util.UUID;

/**
 * Read-only queries that other modules can make about user activity, without reaching its entities or repositories.
 *
 * <p>Implemented by the activity module itself.
 */
public interface ActivityQueries {

    /** What a user rated and watched, by title (movie or series). */
    UserActivity activityOf(UUID userId);

    /** Average and count of the ratings of every rated title, including those of inactive accounts (rule A11). */
    Map<UUID, CommunityRating> communityRatings();
}
