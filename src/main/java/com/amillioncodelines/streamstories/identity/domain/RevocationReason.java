package com.amillioncodelines.streamstories.identity.domain;

/** Why a refresh token stopped being valid. Only the reuse of a {@link #ROTATED} token is treated as theft. */
public enum RevocationReason {
    /** Exchanged for a new pair: presenting it again means someone else may have a copy. */
    ROTATED,
    /** The holder logged out with it. */
    LOGOUT,
    /** The account was deactivated. */
    ACCOUNT_DEACTIVATED,
    /** Reuse of a rotated token was detected, so the whole family was revoked. */
    REUSE_DETECTED
}
