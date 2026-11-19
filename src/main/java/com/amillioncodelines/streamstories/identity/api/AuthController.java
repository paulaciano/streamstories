package com.amillioncodelines.streamstories.identity.api;

import com.amillioncodelines.streamstories.identity.CurrentUser;
import com.amillioncodelines.streamstories.identity.application.AuthenticationService;
import com.amillioncodelines.streamstories.identity.application.ReactivationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Sign-up, login, tokens and account reactivation")
class AuthController {

    private final AuthenticationService authentication;
    private final ReactivationService reactivation;

    AuthController(AuthenticationService authentication, ReactivationService reactivation) {
        this.authentication = authentication;
        this.reactivation = reactivation;
    }

    @PostMapping("/register")
    @SecurityRequirements
    @Operation(summary = "Creates an active USER account")
    ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        var user = authentication.register(request.email(), request.password(), request.displayName());
        return ResponseEntity.created(URI.create("/api/v1/me")).body(UserResponse.from(user));
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Logs in and returns an access token and a refresh token")
    TokenResponse login(@Valid @RequestBody CredentialsRequest request) {
        return TokenResponse.from(authentication.login(request.email(), request.password()));
    }

    @PostMapping("/refresh")
    @SecurityRequirements
    @Operation(summary = "Exchanges a refresh token for a new pair (with rotation)")
    TokenResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return TokenResponse.from(authentication.refresh(request.refreshToken()));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Revokes the given refresh token")
    void logout(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody RefreshTokenRequest request) {
        authentication.logout(CurrentUser.id(jwt), request.refreshToken());
    }

    @PostMapping("/reactivation-requests")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @SecurityRequirements
    @Operation(
            summary = "Requests the reactivation of an inactive account",
            description = "Creates a pending request or returns the existing one. An administrator resolves it.")
    ReactivationRequestResponse requestReactivation(@Valid @RequestBody CredentialsRequest request) {
        return ReactivationRequestResponse.from(reactivation.request(request.email(), request.password()));
    }
}
