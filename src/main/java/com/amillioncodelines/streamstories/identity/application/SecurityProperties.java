package com.amillioncodelines.streamstories.identity.application;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

/**
 * Token settings. The JWT secret has no default: it must be provided through the
 * {@code STREAMSTORIES_SECURITY_JWT_SECRET} environment variable outside the {@code dev} and {@code test} profiles.
 */
@Validated
@ConfigurationProperties("streamstories.security")
public record SecurityProperties(
        @Valid @NotNull Jwt jwt,
        @NotNull @DefaultValue("7d") Duration refreshTokenTtl) {

    /**
     * @param secret HMAC-SHA256 signing key; at least 32 characters (256 bits)
     * @param issuer value of the {@code iss} claim
     * @param accessTokenTtl lifetime of access tokens
     */
    public record Jwt(
            @NotBlank @Size(min = 32) String secret,
            @NotBlank @DefaultValue("streamstories") String issuer,
            @NotNull @DefaultValue("15m") Duration accessTokenTtl) {}
}
