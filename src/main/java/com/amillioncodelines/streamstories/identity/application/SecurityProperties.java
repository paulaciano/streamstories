package com.amillioncodelines.streamstories.identity.application;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("streamstories.security")
public record SecurityProperties(
        @Valid @NotNull Jwt jwt,
        @NotNull @DefaultValue("7d") Duration refreshTokenTtl,
        @Valid @NotNull @DefaultValue Login login) {

    public record Jwt(
            @NotBlank @Size(min = 32) String secret,
            @NotBlank @DefaultValue("streamstories") String issuer,
            @NotNull @DefaultValue("15m") Duration accessTokenTtl) {}

    public record Login(
            @Positive @DefaultValue("5") int maxFailures,
            @NotNull @DefaultValue("15m") Duration window) {}
}
