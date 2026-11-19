package com.amillioncodelines.streamstories.activity.application;

import com.amillioncodelines.streamstories.activity.ActivityQueries;
import com.amillioncodelines.streamstories.activity.CommunityRating;
import com.amillioncodelines.streamstories.activity.UserActivity;
import com.amillioncodelines.streamstories.activity.domain.Rating;
import com.amillioncodelines.streamstories.activity.domain.RatingRepository;
import com.amillioncodelines.streamstories.activity.domain.RatingRepository.TitleStats;
import com.amillioncodelines.streamstories.activity.domain.WatchProgressRepository;
import com.amillioncodelines.streamstories.activity.domain.WatchProgressRepository.StartedTitle;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class ActivityQueryService implements ActivityQueries {

    private final WatchProgressRepository progress;
    private final RatingRepository ratings;

    ActivityQueryService(WatchProgressRepository progress, RatingRepository ratings) {
        this.progress = progress;
        this.ratings = ratings;
    }

    @Override
    @Transactional(readOnly = true)
    public UserActivity activityOf(UUID userId) {
        Map<UUID, Integer> scores = ratings.findByUserId(userId).stream()
                .collect(Collectors.toMap(Rating::getMediaItemId, Rating::getScore));
        List<StartedTitle> started = progress.findStartedTitles(userId);
        Set<UUID> startedIds = started.stream().map(StartedTitle::getTitleId).collect(Collectors.toSet());
        Set<UUID> completedIds = started.stream()
                .filter(StartedTitle::getCompleted)
                .map(StartedTitle::getTitleId)
                .collect(Collectors.toSet());
        return new UserActivity(scores, startedIds, completedIds);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, CommunityRating> communityRatings() {
        return ratings.statsByTitle().stream()
                .collect(Collectors.toMap(
                        TitleStats::getMediaItemId,
                        stats -> new CommunityRating(stats.getAverage(), stats.getCount())));
    }
}
