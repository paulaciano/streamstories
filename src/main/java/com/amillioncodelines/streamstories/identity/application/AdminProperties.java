package com.amillioncodelines.streamstories.identity.application;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Credentials of the initial administrator, read from {@code STREAMSTORIES_ADMIN_EMAIL} and
 * {@code STREAMSTORIES_ADMIN_PASSWORD}. There are no default credentials.
 */
@ConfigurationProperties("streamstories.admin")
public record AdminProperties(
        String email,
        String password,
        @DefaultValue("Administrador") String displayName) {

    boolean isConfigured() {
        return email != null && !email.isBlank() && password != null && !password.isBlank();
    }
}
