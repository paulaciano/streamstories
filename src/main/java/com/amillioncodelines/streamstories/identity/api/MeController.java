package com.amillioncodelines.streamstories.identity.api;

import com.amillioncodelines.streamstories.identity.CurrentUser;
import com.amillioncodelines.streamstories.identity.application.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me")
@Tag(name = "Account", description = "Data and status of the current user's account")
class MeController {

    private final AccountService accounts;

    MeController(AccountService accounts) {
        this.accounts = accounts;
    }

    @GetMapping
    @Operation(summary = "Current user's data")
    UserResponse me(@AuthenticationPrincipal Jwt jwt) {
        return UserResponse.from(accounts.currentUser(CurrentUser.id(jwt)));
    }

    @PostMapping("/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Deactivates your own account",
            description = "Access is lost immediately and tokens are revoked. Activity is kept.")
    void deactivate(@AuthenticationPrincipal Jwt jwt) {
        accounts.deactivateOwnAccount(CurrentUser.id(jwt));
    }
}
