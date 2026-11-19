package com.amillioncodelines.streamstories.identity.application;

import com.amillioncodelines.streamstories.identity.domain.User;
import com.amillioncodelines.streamstories.identity.domain.UserRepository;
import com.amillioncodelines.streamstories.shared.error.UnauthorizedException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Checks an email and password pair without revealing which of the two was wrong.
 *
 * <p>When the email does not exist, the password is still compared against a dummy hash, so the response time does
 * not tell whether an account exists.
 */
@Component
class CredentialsVerifier {

    /** BCrypt only uses the first 72 bytes of a password; longer values are rejected instead of silently truncated. */
    static final int MAX_PASSWORD_BYTES = 72;

    private static final String INVALID_CREDENTIALS = "Invalid credentials.";

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final LoginAttempts attempts;
    private final String dummyHash;

    CredentialsVerifier(UserRepository users, PasswordEncoder passwordEncoder, LoginAttempts attempts) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.attempts = attempts;
        this.dummyHash = passwordEncoder.encode("dummy-password-for-timing-safety");
    }

    /** Blocked emails are rejected before the password is checked, whether the account exists or not. */
    User verify(String email, String password) {
        attempts.ensureNotBlocked(email);
        if (exceedsMaxLength(password)) {
            attempts.recordFailure(email);
            throw new UnauthorizedException(INVALID_CREDENTIALS);
        }
        Optional<User> user = users.findByEmail(User.normalizeEmail(email));
        String hash = user.map(User::getPasswordHash).orElse(dummyHash);
        boolean matches = passwordEncoder.matches(password, hash);
        if (user.isEmpty() || !matches) {
            attempts.recordFailure(email);
            throw new UnauthorizedException(INVALID_CREDENTIALS);
        }
        attempts.reset(email);
        return user.get();
    }

    static boolean exceedsMaxLength(String password) {
        return password.getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES;
    }
}
