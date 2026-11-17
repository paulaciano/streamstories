package com.amillioncodelines.streamstories.identity.api;

import com.amillioncodelines.streamstories.identity.domain.AccountStatus;
import com.amillioncodelines.streamstories.identity.domain.Role;
import jakarta.validation.constraints.AssertTrue;

/** Partial update: only the fields that are present change. */
record UpdateUserRequest(AccountStatus status, Role role) {

    @AssertTrue(message = "se debe indicar status o role")
    boolean isAnyFieldPresent() {
        return status != null || role != null;
    }
}
