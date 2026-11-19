package com.amillioncodelines.streamstories.catalog.api;

import com.amillioncodelines.streamstories.catalog.application.Audience;
import java.util.UUID;
import org.springframework.security.core.Authentication;

final class Audiences {

    private static final String ADMIN_AUTHORITY = "ROLE_ADMIN";

    private Audiences() {}

    static Audience of(Authentication authentication) {
        boolean admin = authentication != null
                && authentication.getAuthorities().stream()
                        .anyMatch(authority -> ADMIN_AUTHORITY.equals(authority.getAuthority()));
        return admin ? Audience.ADMIN : Audience.VIEWER;
    }

    /** The subject of the token is the user id; the catalog only passes it along, to show their own score. */
    static UUID viewerId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}
