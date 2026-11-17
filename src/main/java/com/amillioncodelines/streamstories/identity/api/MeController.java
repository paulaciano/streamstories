package com.amillioncodelines.streamstories.identity.api;

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
@Tag(name = "Cuenta", description = "Datos y estado de la cuenta del usuario actual")
class MeController {

    private final AccountService accounts;

    MeController(AccountService accounts) {
        this.accounts = accounts;
    }

    @GetMapping
    @Operation(summary = "Datos del usuario actual")
    UserResponse me(@AuthenticationPrincipal Jwt jwt) {
        return UserResponse.from(accounts.currentUser(Principals.userId(jwt)));
    }

    @PostMapping("/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Desactiva la propia cuenta",
            description = "Se pierde el acceso de inmediato y se revocan los tokens. La actividad se conserva.")
    void deactivate(@AuthenticationPrincipal Jwt jwt) {
        accounts.deactivateOwnAccount(Principals.userId(jwt));
    }
}
