package com.amillioncodelines.streamstories.identity.application;

import com.amillioncodelines.streamstories.identity.domain.AccountStatus;
import com.amillioncodelines.streamstories.identity.domain.Role;
import com.amillioncodelines.streamstories.identity.domain.User;
import java.time.Instant;
import java.util.UUID;

/** Read-only view of a user, safe to return from the application layer. Never contains the password hash. */
public record UserView(
        UUID id,
        String email,
        String displayName,
        Role role,
        AccountStatus status,
        Instant createdAt,
        Instant updatedAt) {

    static UserView of(User user) {
        return new UserView(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }
}
