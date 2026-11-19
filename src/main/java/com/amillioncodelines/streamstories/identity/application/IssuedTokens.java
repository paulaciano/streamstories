package com.amillioncodelines.streamstories.identity.application;

import java.time.Duration;

public record IssuedTokens(String accessToken, String refreshToken, Duration accessTokenTtl) {}
