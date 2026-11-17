package com.amillioncodelines.streamstories.identity.api;

import com.amillioncodelines.streamstories.identity.application.IssuedTokens;

/**
 * @param expiresIn lifetime of the access token, in seconds
 */
record TokenResponse(String accessToken, String refreshToken, String tokenType, long expiresIn) {

    static TokenResponse from(IssuedTokens tokens) {
        return new TokenResponse(
                tokens.accessToken(),
                tokens.refreshToken(),
                "Bearer",
                tokens.accessTokenTtl().toSeconds());
    }
}
