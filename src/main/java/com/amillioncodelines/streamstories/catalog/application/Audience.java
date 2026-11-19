package com.amillioncodelines.streamstories.catalog.application;

public enum Audience {
    VIEWER,
    ADMIN;

    boolean seesRetiredContent() {
        return this == ADMIN;
    }
}
