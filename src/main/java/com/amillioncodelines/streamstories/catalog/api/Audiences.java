package com.amillioncodelines.streamstories.catalog.api;

import com.amillioncodelines.streamstories.catalog.application.Audience;
import org.springframework.security.core.Authentication;

/** Derives the catalog audience from the authenticated user's role. */
final class Audiences {

    private static final String ADMIN_AUTHORITY = "ROLE_ADMIN";

    private Audiences() {}

    static Audience of(Authentication authentication) {
        boolean admin = authentication != null
                && authentication.getAuthorities().stream()
                        .anyMatch(authority -> ADMIN_AUTHORITY.equals(authority.getAuthority()));
        return admin ? Audience.ADMIN : Audience.VIEWER;
    }
}
