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
@Tag(name = "Autenticación", description = "Registro, inicio de sesión, tokens y reactivación de cuentas")
class AuthController {

    private final AuthenticationService authentication;
    private final ReactivationService reactivation;

    AuthController(AuthenticationService authentication, ReactivationService reactivation) {
        this.authentication = authentication;
        this.reactivation = reactivation;
    }

    @PostMapping("/register")
    @SecurityRequirements
    @Operation(summary = "Crea una cuenta USER activa")
    ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        var user = authentication.register(request.email(), request.password(), request.displayName());
        return ResponseEntity.created(URI.create("/api/v1/me")).body(UserResponse.from(user));
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Inicia sesión y devuelve un access token y un refresh token")
    TokenResponse login(@Valid @RequestBody CredentialsRequest request) {
        return TokenResponse.from(authentication.login(request.email(), request.password()));
    }

    @PostMapping("/refresh")
    @SecurityRequirements
    @Operation(summary = "Cambia un refresh token por un par nuevo (con rotación)")
    TokenResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return TokenResponse.from(authentication.refresh(request.refreshToken()));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Revoca el refresh token enviado")
    void logout(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody RefreshTokenRequest request) {
        authentication.logout(CurrentUser.id(jwt), request.refreshToken());
    }

    @PostMapping("/reactivation-requests")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @SecurityRequirements
    @Operation(
            summary = "Solicita la reactivación de una cuenta inactiva",
            description = "Crea una solicitud pendiente o devuelve la que ya existe. Un administrador la resuelve.")
    ReactivationRequestResponse requestReactivation(@Valid @RequestBody CredentialsRequest request) {
        return ReactivationRequestResponse.from(reactivation.request(request.email(), request.password()));
    }
}
