package com.amillioncodelines.streamstories.identity.domain;

import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, String> {

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(nativeQuery = true, value = """
            INSERT INTO login_attempt (email_hash, failures, window_started_at)
            VALUES (:emailHash, 1, :now)
            ON CONFLICT (email_hash) DO UPDATE
            SET failures = CASE WHEN login_attempt.window_started_at <= :windowStart THEN 1
                                ELSE login_attempt.failures + 1 END,
                window_started_at = CASE WHEN login_attempt.window_started_at <= :windowStart THEN :now
                                         ELSE login_attempt.window_started_at END
            """)
    void recordFailure(
            @Param("emailHash") String emailHash, @Param("now") Instant now, @Param("windowStart") Instant windowStart);

    @Modifying
    @Query("delete from LoginAttempt a where a.windowStartedAt <= :windowStart")
    int deleteWindowsStartedBefore(@Param("windowStart") Instant windowStart);
}
