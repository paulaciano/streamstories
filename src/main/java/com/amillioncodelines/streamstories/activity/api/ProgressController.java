package com.amillioncodelines.streamstories.activity.api;

import com.amillioncodelines.streamstories.activity.application.ProgressService;
import com.amillioncodelines.streamstories.identity.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me/progress")
@Tag(name = "Activity", description = "Watch progress, continue watching and history of the current user")
class ProgressController {

    private static final String DESCRIPTION =
            "Saves the exact position. A position beyond the duration is capped, and the"
                    + " content is completed at 90%, even if a shorter position is reported later."
                    + " Answers 409 if the content is retired.";

    private final ProgressService progress;

    ProgressController(ProgressService progress) {
        this.progress = progress;
    }

    @PutMapping("/movies/{movieId}")
    @Operation(summary = "Reports the playback position of a movie", description = DESCRIPTION)
    ProgressResponse reportMovie(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID movieId, @Valid @RequestBody ProgressRequest request) {
        return ProgressResponse.from(progress.reportMovie(CurrentUser.id(jwt), movieId, request.positionSeconds()));
    }

    @PutMapping("/episodes/{episodeId}")
    @Operation(summary = "Reports the playback position of an episode", description = DESCRIPTION)
    ProgressResponse reportEpisode(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID episodeId,
            @Valid @RequestBody ProgressRequest request) {
        return ProgressResponse.from(progress.reportEpisode(CurrentUser.id(jwt), episodeId, request.positionSeconds()));
    }
}
