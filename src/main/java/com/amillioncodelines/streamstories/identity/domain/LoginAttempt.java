package com.amillioncodelines.streamstories.identity.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.Instant;

@Entity
public class LoginAttempt {

    @Id
    private String emailHash;

    @Column(nullable = false)
    private int failures;

    @Column(nullable = false)
    private Instant windowStartedAt;

    protected LoginAttempt() {}

    public int getFailures() {
        return failures;
    }

    public Instant getWindowStartedAt() {
        return windowStartedAt;
    }
}
