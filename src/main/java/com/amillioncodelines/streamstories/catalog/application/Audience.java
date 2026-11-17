package com.amillioncodelines.streamstories.catalog.application;

/**
 * Who is looking at the catalog. Viewers only see available content in listings, while administrators see everything
 * (rule C9).
 */
public enum Audience {
    VIEWER,
    ADMIN;

    boolean seesRetiredContent() {
        return this == ADMIN;
    }
}
