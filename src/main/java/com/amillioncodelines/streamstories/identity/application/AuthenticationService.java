package com.amillioncodelines.streamstories.identity.application;

import com.amillioncodelines.streamstories.identity.domain.ReactivationRequestRepository;
import com.amillioncodelines.streamstories.identity.domain.ReactivationStatus;
import com.amillioncodelines.streamstories.identity.domain.RefreshToken;
import com.amillioncodelines.streamstories.identity.domain.RefreshTokenRepository;
import com.amillioncodelines.streamstories.identity.domain.RevocationReason;
import com.amillioncodelines.streamstories.identity.domain.User;
import com.amillioncodelines.streamstories.identity.domain.UserRepository;
import com.amillioncodelines.streamstories.shared.error.ConflictException;
import com.amillioncodelines.streamstories.shared.error.InvalidRequestException;
import com.amillioncodelines.streamstories.shared.error.UnauthorizedException;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);
    private static final String INVALID_REFRESH_TOKEN = "The refresh token is invalid or expired.";

    private final UserRepository users;
    private final RefreshTokenRepository refreshTokens;
    private final ReactivationRequestRepository reactivationRequests;
    private final PasswordEncoder passwordEncoder;
    private final CredentialsVerifier credentials;
    private final TokenIssuer tokenIssuer;
    private final Clock clock;

    AuthenticationService(
            UserRepository users,
            RefreshTokenRepository refreshTokens,
            ReactivationRequestRepository reactivationRequests,
            PasswordEncoder passwordEncoder,
            CredentialsVerifier credentials,
            TokenIssuer tokenIssuer,
            Clock clock) {
        this.users = users;
        this.refreshTokens = refreshTokens;
        this.reactivationRequests = reactivationRequests;
        this.passwordEncoder = passwordEncoder;
        this.credentials = credentials;
        this.tokenIssuer = tokenIssuer;
        this.clock = clock;
    }

    /** Not transactional: hashing the password takes about 100 ms and must not hold a database connection. */
    public UserView register(String email, String password, String displayName) {
        if (CredentialsVerifier.exceedsMaxLength(password)) {
            throw new InvalidRequestException(
                    "The password cannot exceed %d bytes.".formatted(CredentialsVerifier.MAX_PASSWORD_BYTES));
        }
        String normalizedEmail = User.normalizeEmail(email);
        if (users.existsByEmail(normalizedEmail)) {
            throw new ConflictException("An account with that email already exists.");
        }
        String passwordHash = passwordEncoder.encode(password);
        User user = users.saveAndFlush(User.register(normalizedEmail, passwordHash, displayName));
        return UserView.of(user);
    }

    /** {@code account-inactive} is only returned once the password matched, so it never reveals that an email exists. */
    public IssuedTokens login(String email, String password) {
        User user = credentials.verify(email, password);
        if (!user.isActive()) {
            throw new AccountInactiveException(
                    reactivationRequests.existsByUserIdAndStatus(user.getId(), ReactivationStatus.PENDING));
        }
        return tokenIssuer.issueFor(user);
    }

    /** Presenting a rotated token again is treated as theft: every refresh token of the user is revoked. */
    @Transactional(noRollbackFor = UnauthorizedException.class)
    public IssuedTokens refresh(String refreshToken) {
        Instant now = clock.instant();
        RefreshToken token = refreshTokens
                .findByTokenHash(TokenIssuer.hash(refreshToken))
                .orElseThrow(() -> new UnauthorizedException(INVALID_REFRESH_TOKEN));
        User user = token.getUser();

        if (token.isRevoked()) {
            if (token.wasRotated()) {
                revokeStolenFamily(user, now);
            }
            throw new UnauthorizedException(INVALID_REFRESH_TOKEN);
        }
        if (token.isExpired(now) || !user.isActive()) {
            throw new UnauthorizedException(INVALID_REFRESH_TOKEN);
        }
        if (refreshTokens.revokeIfActive(token.getId(), now, RevocationReason.ROTATED) == 0) {
            // Another request revoked it between the read and the update: the same token was presented twice.
            revokeStolenFamily(user, now);
            throw new UnauthorizedException(INVALID_REFRESH_TOKEN);
        }
        return tokenIssuer.issueFor(user);
    }

    private void revokeStolenFamily(User user, Instant now) {
        int revoked = refreshTokens.revokeAllForUser(user.getId(), now, RevocationReason.REUSE_DETECTED);
        log.warn("Reuse of a rotated refresh token detected; revoked {} token(s) of user {}", revoked, user.getId());
    }

    /** Revokes the given refresh token if it belongs to the user. Unknown or foreign tokens are silently ignored. */
    @Transactional
    public void logout(UUID userId, String refreshToken) {
        refreshTokens
                .findByTokenHash(TokenIssuer.hash(refreshToken))
                .filter(token -> token.getUser().getId().equals(userId))
                .ifPresent(
                        token -> refreshTokens.revokeIfActive(token.getId(), clock.instant(), RevocationReason.LOGOUT));
    }
}
