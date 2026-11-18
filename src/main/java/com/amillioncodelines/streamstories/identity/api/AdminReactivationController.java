package com.amillioncodelines.streamstories.identity.api;

import com.amillioncodelines.streamstories.identity.CurrentUser;
import com.amillioncodelines.streamstories.identity.application.ReactivationService;
import com.amillioncodelines.streamstories.identity.domain.ReactivationStatus;
import com.amillioncodelines.streamstories.shared.web.PageResponse;
import com.amillioncodelines.streamstories.shared.web.Pageables;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Set;
import java.util.UUID;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/reactivation-requests")
@Tag(name = "Administración de usuarios")
class AdminReactivationController {

    private static final Set<String> SORTABLE = Set.of("requestedAt", "resolvedAt");
    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.ASC, "requestedAt");

    private final ReactivationService reactivation;

    AdminReactivationController(ReactivationService reactivation) {
        this.reactivation = reactivation;
    }

    @GetMapping
    @Operation(summary = "Lista las solicitudes de reactivación (por defecto, las pendientes)")
    PageResponse<AdminReactivationResponse> list(
            @RequestParam(defaultValue = "PENDING") ReactivationStatus status, @ParameterObject Pageable pageable) {
        var page = reactivation.list(status, Pageables.restrictSort(pageable, SORTABLE, DEFAULT_SORT));
        return PageResponse.of(page, AdminReactivationResponse::from);
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Aprueba la solicitud y reactiva la cuenta")
    AdminReactivationResponse approve(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return AdminReactivationResponse.from(reactivation.approve(id, CurrentUser.id(jwt)));
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Rechaza la solicitud; la cuenta sigue inactiva")
    AdminReactivationResponse reject(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return AdminReactivationResponse.from(reactivation.reject(id, CurrentUser.id(jwt)));
    }
}
