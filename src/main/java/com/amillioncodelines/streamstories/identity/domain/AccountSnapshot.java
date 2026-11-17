package com.amillioncodelines.streamstories.identity.domain;

import java.util.UUID;

/** The current role and status of an account, read on every authenticated request. */
public record AccountSnapshot(UUID id, Role role, AccountStatus status) {

    public boolean isActive() {
        return status == AccountStatus.ACTIVE;
    }
}
