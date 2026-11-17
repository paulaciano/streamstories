package com.amillioncodelines.streamstories.identity.application;

import com.amillioncodelines.streamstories.shared.error.ProblemException;
import com.amillioncodelines.streamstories.shared.error.ProblemType;
import java.util.Map;

/**
 * Thrown when an inactive account tries to log in or use the API. The response tells whether a reactivation request
 * is already pending.
 */
public class AccountInactiveException extends ProblemException {

    public AccountInactiveException(boolean reactivationRequestPending) {
        super(
                ProblemType.ACCOUNT_INACTIVE,
                "La cuenta está inactiva. Solo un administrador puede reactivarla.",
                Map.of("reactivationRequestPending", reactivationRequestPending));
    }
}
