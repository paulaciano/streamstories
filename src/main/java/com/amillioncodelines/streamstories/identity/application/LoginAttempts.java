package com.amillioncodelines.streamstories.identity.application;

import com.amillioncodelines.streamstories.identity.domain.LoginAttempt;
import com.amillioncodelines.streamstories.identity.domain.LoginAttemptRepository;
import com.amillioncodelines.streamstories.identity.domain.User;
import com.amillioncodelines.streamstories.shared.error.TooManyRequestsException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class LoginAttempts {

    private final LoginAttemptRepository attempts;
    private final Clock clock;
    private final int maxFailures;
    private final Duration window;
    private final SecretKeySpec hashKey;

    LoginAttempts(LoginAttemptRepository attempts, Clock clock, SecurityProperties properties) {
        this.attempts = attempts;
        this.clock = clock;
        this.maxFailures = properties.login().maxFailures();
        this.window = properties.login().window();
        // Keyed, so the hashes cannot be matched against a list of known emails.
        this.hashKey = new SecretKeySpec(properties.jwt().secret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    @Transactional(readOnly = true)
    public void ensureNotBlocked(String email) {
        Instant now = clock.instant();
        attempts.findById(hash(email))
                .filter(attempt -> attempt.getFailures() >= maxFailures)
                .map(LoginAttempt::getWindowStartedAt)
                .map(started -> started.plus(window))
                .filter(end -> end.isAfter(now))
                .ifPresent(end -> {
                    Duration retryAfter = Duration.between(now, end);
                    throw new TooManyRequestsException(
                            "Too many failed attempts. Try again in %d seconds."
                                    .formatted(Math.max(1, retryAfter.toSeconds())),
                            retryAfter);
                });
    }

    @Transactional
    public void recordFailure(String email) {
        Instant now = clock.instant();
        attempts.recordFailure(hash(email), now, now.minus(window));
    }

    @Transactional
    public void reset(String email) {
        attempts.deleteById(hash(email));
    }

    @Scheduled(cron = "${streamstories.security.cleanup-cron:0 30 4 * * *}")
    @Transactional
    public int deleteExpired() {
        return attempts.deleteWindowsStartedBefore(clock.instant().minus(window));
    }

    private String hash(String email) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(hashKey);
            return HexFormat.of()
                    .formatHex(mac.doFinal(User.normalizeEmail(email).getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
