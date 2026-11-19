package com.amillioncodelines.streamstories.identity.application;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("streamstories.admin")
public record AdminProperties(
        String email,
        String password,
        @DefaultValue("Administrator") String displayName) {

    boolean isConfigured() {
        return email != null && !email.isBlank() && password != null && !password.isBlank();
    }
}
