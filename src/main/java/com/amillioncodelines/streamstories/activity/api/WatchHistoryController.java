package com.amillioncodelines.streamstories.activity.api;

import com.amillioncodelines.streamstories.activity.application.WatchHistoryService;
import com.amillioncodelines.streamstories.identity.CurrentUser;
import com.amillioncodelines.streamstories.shared.web.PageResponse;
import com.amillioncodelines.streamstories.shared.web.Pageables;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me")
@Tag(name = "Activity", description = "Watch progress, continue watching and history of the current user")
class WatchHistoryController {

    /** Both lists go from the most recently watched content: the order is fixed and a {@code sort} is ignored. */
    private static final Sort MOST_RECENT_FIRST = Sort.by(Sort.Direction.DESC, "lastWatchedAt");

    private final WatchHistoryService history;

    WatchHistoryController(WatchHistoryService history) {
        this.history = history;
    }

    @GetMapping("/continue-watching")
    @Operation(
            summary = "What can be resumed",
            description = "Unfinished movies and one entry per series with its last watched episode, most recent"
                    + " first. A series leaves once that episode is completed and no playable episode follows it.")
    PageResponse<WatchedItemResponse> continueWatching(
            @AuthenticationPrincipal Jwt jwt, @ParameterObject Pageable pageable) {
        var page = history.continueWatching(CurrentUser.id(jwt), mostRecentFirst(pageable));
        return PageResponse.of(page, WatchedItemResponse::from);
    }

    @GetMapping("/history")
    @Operation(
            summary = "Watch history",
            description = "Every movie and episode watched, with the last watched date, most recent first."
                    + " Retired content has playable set to false.")
    PageResponse<WatchedItemResponse> history(@AuthenticationPrincipal Jwt jwt, @ParameterObject Pageable pageable) {
        var page = history.history(CurrentUser.id(jwt), mostRecentFirst(pageable));
        return PageResponse.of(page, WatchedItemResponse::from);
    }

    private static Pageable mostRecentFirst(Pageable pageable) {
        return Pageables.fixedOrder(pageable, MOST_RECENT_FIRST);
    }
}
