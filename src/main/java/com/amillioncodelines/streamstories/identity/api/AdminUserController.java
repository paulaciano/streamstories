package com.amillioncodelines.streamstories.identity.api;

import com.amillioncodelines.streamstories.identity.CurrentUser;
import com.amillioncodelines.streamstories.identity.application.AccountService;
import com.amillioncodelines.streamstories.identity.domain.AccountStatus;
import com.amillioncodelines.streamstories.identity.domain.Role;
import com.amillioncodelines.streamstories.shared.web.PageResponse;
import com.amillioncodelines.streamstories.shared.web.Pageables;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Set;
import java.util.UUID;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/users")
@Tag(name = "User administration")
class AdminUserController {

    private static final Set<String> SORTABLE = Set.of("email", "displayName", "createdAt");
    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "createdAt");

    private final AccountService accounts;

    AdminUserController(AccountService accounts) {
        this.accounts = accounts;
    }

    @GetMapping
    @Operation(summary = "Lists users, filterable by status and role")
    PageResponse<UserResponse> list(
            @RequestParam(required = false) AccountStatus status,
            @RequestParam(required = false) Role role,
            @ParameterObject Pageable pageable) {
        var page = accounts.listUsers(status, role, Pageables.restrictSort(pageable, SORTABLE, DEFAULT_SORT));
        return PageResponse.of(page, UserResponse::from);
    }

    @PatchMapping("/{id}")
    @Operation(
            summary = "Changes a user's status or role",
            description = "The last active administrator cannot be deactivated or demoted.")
    UserResponse update(
            @PathVariable UUID id, @Valid @RequestBody UpdateUserRequest request, @AuthenticationPrincipal Jwt jwt) {
        return UserResponse.from(accounts.updateUser(id, request.status(), request.role(), CurrentUser.id(jwt)));
    }
}
