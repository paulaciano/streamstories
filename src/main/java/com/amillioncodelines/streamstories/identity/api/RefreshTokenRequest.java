package com.amillioncodelines.streamstories.identity.api;

import jakarta.validation.constraints.NotBlank;

record RefreshTokenRequest(@NotBlank String refreshToken) {}
