package com.amillioncodelines.streamstories.identity.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.amillioncodelines.streamstories.identity.domain.RefreshToken;
import com.amillioncodelines.streamstories.identity.domain.RefreshTokenRepository;
import com.amillioncodelines.streamstories.identity.domain.User;
import com.amillioncodelines.streamstories.identity.domain.UserRepository;
import com.amillioncodelines.streamstories.support.ApiClient;
import com.amillioncodelines.streamstories.support.IntegrationTest;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class RefreshTokenCleanupIntegrationTests {

    @Autowired
    RefreshTokenCleanup cleanup;

    @Autowired
    RefreshTokenRepository refreshTokens;

    @Autowired
    UserRepository users;

    @Autowired
    ApiClient api;

    @Test
    void shouldDeleteExpiredRefreshTokensAndKeepTheOthers() {
        String email = ApiClient.uniqueEmail();
        api.register(email, ApiClient.DEFAULT_PASSWORD);
        User user = users.findByEmail(email).orElseThrow();
        Instant now = Instant.now();
        RefreshToken expired = refreshTokens.save(
                new RefreshToken(user, "expired-" + UUID.randomUUID(), now.minus(Duration.ofDays(1))));
        RefreshToken valid =
                refreshTokens.save(new RefreshToken(user, "valid-" + UUID.randomUUID(), now.plus(Duration.ofDays(1))));

        assertThat(cleanup.deleteExpired()).isPositive();

        assertThat(refreshTokens.findById(expired.getId())).isEmpty();
        assertThat(refreshTokens.findById(valid.getId())).isPresent();
    }
}
