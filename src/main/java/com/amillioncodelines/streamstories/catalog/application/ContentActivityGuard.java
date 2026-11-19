package com.amillioncodelines.streamstories.catalog.application;

import com.amillioncodelines.streamstories.catalog.ContentActivity;
import com.amillioncodelines.streamstories.catalog.ContentKind;
import com.amillioncodelines.streamstories.shared.error.ConflictException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Rule C11: content with user activity cannot be deleted, only retired. */
@Component
class ContentActivityGuard {

    private final List<ContentActivity> activities;

    ContentActivityGuard(List<ContentActivity> activities) {
        this.activities = activities;
    }

    void ensureNoActivity(ContentKind kind, UUID contentId) {
        ensureNoActivity(kind, List.of(contentId));
    }

    void ensureNoActivity(ContentKind kind, Collection<UUID> contentIds) {
        if (contentIds.isEmpty()) {
            return;
        }
        boolean inUse = activities.stream().anyMatch(activity -> activity.existsForAny(kind, contentIds));
        if (inUse) {
            throw new ConflictException("The content has user activity: retire it instead of deleting it.");
        }
    }
}
