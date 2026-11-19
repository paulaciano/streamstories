package com.amillioncodelines.streamstories.activity.api;

import com.amillioncodelines.streamstories.activity.application.WatchlistService;
import com.amillioncodelines.streamstories.identity.CurrentUser;
import com.amillioncodelines.streamstories.shared.web.PageResponse;
import com.amillioncodelines.streamstories.shared.web.Pageables;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me/watchlist")
@Tag(name = "Watchlist", description = "Movies and series the current user saved to watch later")
class WatchlistController {

    private static final Sort MOST_RECENT_FIRST = Sort.by(Sort.Direction.DESC, "addedAt");
    private static final Sort BY_TITLE_ID = Sort.by("key.mediaItemId");

    private final WatchlistService watchlist;

    WatchlistController(WatchlistService watchlist) {
        this.watchlist = watchlist;
    }

    @GetMapping
    @Operation(
            summary = "The current user's watchlist",
            description = "Most recently added first. Retired titles have available set to false.")
    PageResponse<WatchlistItemResponse> list(@AuthenticationPrincipal Jwt jwt, @ParameterObject Pageable pageable) {
        var page = watchlist.list(CurrentUser.id(jwt), Pageables.fixedOrder(pageable, MOST_RECENT_FIRST, BY_TITLE_ID));
        return PageResponse.of(page, WatchlistItemResponse::from);
    }

    @PutMapping("/{titleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Adds a movie or a series to the watchlist",
            description = "Adding a title that is already there changes nothing. Answers 409 if the title is retired.")
    void add(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID titleId) {
        watchlist.add(CurrentUser.id(jwt), titleId);
    }

    @DeleteMapping("/{titleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Removes a title from the watchlist",
            description = "Allowed even if the title is retired. Without the title in the list, nothing changes.")
    void remove(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID titleId) {
        watchlist.remove(CurrentUser.id(jwt), titleId);
    }
}
