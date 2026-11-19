package com.amillioncodelines.streamstories.identity.domain;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, String> {

    /**
     * Counts the attempt before the password is checked, in one statement, so simultaneous attempts cannot all see
     * the count below the limit. Returns the updated count and window start.
     */
    @Query(nativeQuery = true, value = """
            INSERT INTO login_attempt (email_hash, failures, window_started_at)
            VALUES (:emailHash, 1, :now)
            ON CONFLICT (email_hash) DO UPDATE
            SET failures = CASE WHEN login_attempt.window_started_at <= :windowStart THEN 1
                                ELSE login_attempt.failures + 1 END,
                window_started_at = CASE WHEN login_attempt.window_started_at <= :windowStart THEN :now
                                         ELSE login_attempt.window_started_at END
            RETURNING failures, window_started_at
            """)
    List<Object[]> reserveAttempt(
            @Param("emailHash") String emailHash, @Param("now") Instant now, @Param("windowStart") Instant windowStart);

    @Modifying
    @Query("delete from LoginAttempt a where a.windowStartedAt <= :windowStart")
    int deleteWindowsStartedBefore(@Param("windowStart") Instant windowStart);
}
