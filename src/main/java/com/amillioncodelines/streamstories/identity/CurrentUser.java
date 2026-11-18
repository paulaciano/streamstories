package com.amillioncodelines.streamstories.identity;

import java.util.UUID;
import org.springframework.security.oauth2.jwt.Jwt;

/** The current user is always taken from the token, never from a request parameter. */
public final class CurrentUser {

    private CurrentUser() {}

    public static UUID id(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
