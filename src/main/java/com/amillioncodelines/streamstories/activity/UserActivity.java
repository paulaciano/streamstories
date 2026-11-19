package com.amillioncodelines.streamstories.activity;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * The activity of a user, by title. A series counts as started when any of its episodes has progress, and as
 * completed when at least one of its episodes is completed.
 *
 * @param scores the user's score of each rated title
 * @param started titles with any watch progress
 * @param completed titles completed: a completed movie, or a series with a completed episode
 */
public record UserActivity(Map<UUID, Integer> scores, Set<UUID> started, Set<UUID> completed) {

    public UserActivity {
        scores = Map.copyOf(scores);
        started = Set.copyOf(started);
        completed = Set.copyOf(completed);
    }

    public boolean isEmpty() {
        return scores.isEmpty() && started.isEmpty();
    }
}
