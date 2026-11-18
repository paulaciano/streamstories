package com.amillioncodelines.streamstories.activity.api;

import com.amillioncodelines.streamstories.activity.application.WatchHistoryService;
import com.amillioncodelines.streamstories.identity.CurrentUser;
import com.amillioncodelines.streamstories.shared.web.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me")
@Tag(name = "Actividad", description = "Progreso de reproducción, Seguir viendo e historial del usuario actual")
class WatchHistoryController {

    /** Both lists go from the most recently watched content: the order is fixed and a {@code sort} is ignored. */
    private static final Sort MOST_RECENT_FIRST = Sort.by(Sort.Direction.DESC, "lastWatchedAt", "id");

    private final WatchHistoryService history;

    WatchHistoryController(WatchHistoryService history) {
        this.history = history;
    }

    @GetMapping("/continue-watching")
    @Operation(
            summary = "Lo que se puede retomar",
            description =
                    "Películas sin terminar y una entrada por serie con el último episodio visto, de lo más reciente a lo"
                            + " más antiguo. Una serie deja de aparecer cuando ese episodio está visto y no hay otro reproducible"
                            + " después.")
    PageResponse<WatchedItemResponse> continueWatching(
            @AuthenticationPrincipal Jwt jwt, @ParameterObject Pageable pageable) {
        var page = history.continueWatching(CurrentUser.id(jwt), mostRecentFirst(pageable));
        return PageResponse.of(page, WatchedItemResponse::from);
    }

    @GetMapping("/history")
    @Operation(
            summary = "Historial de reproducción",
            description = "Cada película o episodio visto, con la fecha de la última vez, de lo más reciente a lo más"
                    + " antiguo. El contenido retirado aparece con playable en false.")
    PageResponse<WatchedItemResponse> history(@AuthenticationPrincipal Jwt jwt, @ParameterObject Pageable pageable) {
        var page = history.history(CurrentUser.id(jwt), mostRecentFirst(pageable));
        return PageResponse.of(page, WatchedItemResponse::from);
    }

    private static Pageable mostRecentFirst(Pageable pageable) {
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), MOST_RECENT_FIRST);
    }
}
