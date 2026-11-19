package com.amillioncodelines.streamstories.activity;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
