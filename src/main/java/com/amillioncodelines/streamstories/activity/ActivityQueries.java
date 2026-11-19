package com.amillioncodelines.streamstories.activity;

import java.util.Map;
import java.util.UUID;

public interface ActivityQueries {

    UserActivity activityOf(UUID userId);

    Map<UUID, CommunityRating> communityRatings();
}
