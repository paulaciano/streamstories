package com.amillioncodelines.streamstories.identity.api;

import com.amillioncodelines.streamstories.identity.domain.AccountStatus;
import com.amillioncodelines.streamstories.identity.domain.Role;
import jakarta.validation.constraints.AssertTrue;

record UpdateUserRequest(AccountStatus status, Role role) {

    @AssertTrue(message = "status or role is required")
    boolean isAnyFieldPresent() {
        return status != null || role != null;
    }
}
