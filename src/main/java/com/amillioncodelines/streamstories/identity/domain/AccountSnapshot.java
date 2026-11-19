package com.amillioncodelines.streamstories.identity.domain;

import java.util.UUID;

public record AccountSnapshot(UUID id, Role role, AccountStatus status) {

    public boolean isActive() {
        return status == AccountStatus.ACTIVE;
    }
}
