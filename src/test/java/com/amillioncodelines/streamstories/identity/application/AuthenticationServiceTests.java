package com.amillioncodelines.streamstories.identity.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amillioncodelines.streamstories.identity.domain.RefreshToken;
import com.amillioncodelines.streamstories.identity.domain.RefreshTokenRepository;
import com.amillioncodelines.streamstories.identity.domain.RevocationReason;
import com.amillioncodelines.streamstories.identity.domain.User;
import com.amillioncodelines.streamstories.shared.error.UnauthorizedException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/** Refresh token rotation: only the reuse of a rotated token is treated as theft. */
class AuthenticationServiceTests {

    private static final String TOKEN = "raw-refresh-token";
    private static final Instant NOW = Instant.parse("2026-11-17T10:00:00Z");

    private final RefreshTokenRepository refreshTokens = mock();
    private final TokenIssuer tokenIssuer = mock();
    private final AuthenticationService service = new AuthenticationService(
            mock(), refreshTokens, mock(), mock(), mock(), tokenIssuer, Clock.fixed(NOW, ZoneOffset.UTC));

    private final User user = withId(User.register("viewer@example.com", "{noop}x", "Viewer"));

    @Test
    void shouldRevokeTheWholeFamilyWhenARotatedTokenIsReused() {
        stored(token(NOW.plusSeconds(3600), RevocationReason.ROTATED));

        assertThatThrownBy(() -> service.refresh(TOKEN)).isInstanceOf(UnauthorizedException.class);

        verify(refreshTokens).revokeAllForUser(user.getId(), NOW, RevocationReason.REUSE_DETECTED);
    }

    @Test
    void shouldOnlyRejectATokenRevokedByLogout() {
        stored(token(NOW.plusSeconds(3600), RevocationReason.LOGOUT));

        assertThatThrownBy(() -> service.refresh(TOKEN)).isInstanceOf(UnauthorizedException.class);

        verify(refreshTokens, never()).revokeAllForUser(any(), any(), any());
    }

    @Test
    void shouldRejectAnExpiredTokenWithoutRevokingAnything() {
        stored(token(NOW.minusSeconds(1), null));

        assertThatThrownBy(() -> service.refresh(TOKEN)).isInstanceOf(UnauthorizedException.class);
        assertThatThrownBy(() -> service.refresh(TOKEN)).isInstanceOf(UnauthorizedException.class);

        verify(refreshTokens, never()).revokeIfActive(any(), any(), any());
        verify(refreshTokens, never()).revokeAllForUser(any(), any(), any());
    }

    @Test
    void shouldTreatAConcurrentRotationAsReuse() {
        RefreshToken token = stored(token(NOW.plusSeconds(3600), null));
        when(refreshTokens.revokeIfActive(token.getId(), NOW, RevocationReason.ROTATED))
                .thenReturn(0);

        assertThatThrownBy(() -> service.refresh(TOKEN)).isInstanceOf(UnauthorizedException.class);

        verify(refreshTokens).revokeAllForUser(user.getId(), NOW, RevocationReason.REUSE_DETECTED);
        verify(tokenIssuer, never()).issueFor(any());
    }

    @Test
    void shouldRotateAValidToken() {
        RefreshToken token = stored(token(NOW.plusSeconds(3600), null));
        when(refreshTokens.revokeIfActive(token.getId(), NOW, RevocationReason.ROTATED))
                .thenReturn(1);

        service.refresh(TOKEN);

        verify(tokenIssuer).issueFor(user);
    }

    private RefreshToken token(Instant expiresAt, RevocationReason revocation) {
        RefreshToken token = withId(new RefreshToken(user, TokenIssuer.hash(TOKEN), expiresAt));
        if (revocation != null) {
            ReflectionTestUtils.setField(token, "revokedAt", NOW.minusSeconds(60));
            ReflectionTestUtils.setField(token, "revocationReason", revocation);
        }
        return token;
    }

    private RefreshToken stored(RefreshToken token) {
        when(refreshTokens.findByTokenHash(TokenIssuer.hash(TOKEN))).thenReturn(Optional.of(token));
        return token;
    }

    private static <T> T withId(T entity) {
        ReflectionTestUtils.setField(entity, "id", UUID.randomUUID());
        return entity;
    }
}
