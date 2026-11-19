package com.amillioncodelines.streamstories.activity.api;

import com.amillioncodelines.streamstories.activity.application.WatchlistItemView;
import com.amillioncodelines.streamstories.catalog.ContentKind;
import java.time.Instant;
import java.util.UUID;

record WatchlistItemResponse(
        UUID titleId, ContentKind type, String title, String coverImageUrl, boolean available, Instant addedAt) {

    static WatchlistItemResponse from(WatchlistItemView view) {
        return new WatchlistItemResponse(
                view.titleId(), view.type(), view.title(), view.coverImageUrl(), view.available(), view.addedAt());
    }
}
