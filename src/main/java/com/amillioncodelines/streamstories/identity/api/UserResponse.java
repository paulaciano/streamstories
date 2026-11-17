package com.amillioncodelines.streamstories.identity.api;

import com.amillioncodelines.streamstories.identity.application.UserView;
import com.amillioncodelines.streamstories.identity.domain.AccountStatus;
import com.amillioncodelines.streamstories.identity.domain.Role;
import java.time.Instant;
import java.util.UUID;

record UserResponse(
        UUID id,
        String email,
        String displayName,
        Role role,
        AccountStatus status,
        Instant createdAt,
        Instant updatedAt) {

    static UserResponse from(UserView user) {
        return new UserResponse(
                user.id(),
                user.email(),
                user.displayName(),
                user.role(),
                user.status(),
                user.createdAt(),
                user.updatedAt());
    }
}
