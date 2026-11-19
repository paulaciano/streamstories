package com.amillioncodelines.streamstories.catalog.api;

import com.amillioncodelines.streamstories.catalog.application.SeriesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/episodes")
@Tag(name = "Series", description = "Series, seasons and episodes")
class EpisodeController {

    private final SeriesService series;

    EpisodeController(SeriesService series) {
        this.series = series;
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Episode detail",
            description = "`playable` tells whether the episode's season and series are available.")
    EpisodeResponse get(@PathVariable UUID id, Authentication authentication) {
        return EpisodeResponse.from(series.getEpisode(id, Audiences.of(authentication)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Replaces an episode's data (ADMIN)")
    EpisodeResponse replace(@PathVariable UUID id, @Valid @RequestBody EpisodeRequest request) {
        return EpisodeResponse.from(series.replaceEpisode(id, request.toCommand()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deletes an episode without user activity (ADMIN)")
    void delete(@PathVariable UUID id) {
        series.deleteEpisode(id);
    }
}
