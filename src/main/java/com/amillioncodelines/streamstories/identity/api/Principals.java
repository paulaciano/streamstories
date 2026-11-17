package com.amillioncodelines.streamstories.identity.api;

import java.util.UUID;
import org.springframework.security.oauth2.jwt.Jwt;

/** The current user is always taken from the token, never from a request parameter. */
final class Principals {

    private Principals() {}

    static UUID userId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
