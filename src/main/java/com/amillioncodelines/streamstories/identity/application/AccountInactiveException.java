package com.amillioncodelines.streamstories.identity.application;

import com.amillioncodelines.streamstories.shared.error.ProblemException;
import com.amillioncodelines.streamstories.shared.error.ProblemType;
import java.util.Map;

public class AccountInactiveException extends ProblemException {

    public AccountInactiveException(boolean reactivationRequestPending) {
        super(
                ProblemType.ACCOUNT_INACTIVE,
                "The account is inactive. Only an administrator can reactivate it.",
                Map.of("reactivationRequestPending", reactivationRequestPending));
    }
}
