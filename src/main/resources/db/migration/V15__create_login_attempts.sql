-- Keyed by an HMAC of the email, so it stores no email and also counts emails without an account.
CREATE TABLE login_attempt (
    email_hash        varchar(64) PRIMARY KEY,
    failures          integer     NOT NULL,
    window_started_at timestamptz NOT NULL
);

CREATE INDEX ix_login_attempt_window ON login_attempt (window_started_at);
