package com.amillioncodelines.streamstories.identity.application;

import com.amillioncodelines.streamstories.identity.domain.RefreshTokenRepository;
import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Deletes expired refresh tokens once a day, so the table does not grow forever. A token is useless once expired,
 * so nothing is lost: until then it is kept, even if revoked, because a rotated token is what detects reuse.
 */
@Component
class RefreshTokenCleanup {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenCleanup.class);

    private final RefreshTokenRepository refreshTokens;
    private final Clock clock;

    RefreshTokenCleanup(RefreshTokenRepository refreshTokens, Clock clock) {
        this.refreshTokens = refreshTokens;
        this.clock = clock;
    }

    @Scheduled(cron = "${streamstories.security.refresh-token-cleanup-cron:0 30 4 * * *}")
    @Transactional
    public int deleteExpired() {
        int deleted = refreshTokens.deleteExpiredBefore(clock.instant());
        log.info("Deleted {} expired refresh tokens", deleted);
        return deleted;
    }
}
