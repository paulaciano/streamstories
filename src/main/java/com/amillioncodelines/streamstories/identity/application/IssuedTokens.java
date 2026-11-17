package com.amillioncodelines.streamstories.identity.application;

import java.time.Duration;

/** A freshly issued access token and refresh token pair. */
public record IssuedTokens(String accessToken, String refreshToken, Duration accessTokenTtl) {}
