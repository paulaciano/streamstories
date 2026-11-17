-- Identity: users, refresh tokens and reactivation requests.

CREATE TABLE app_user (
    id            uuid         PRIMARY KEY,
    email         varchar(254) NOT NULL,
    password_hash varchar(100) NOT NULL,
    display_name  varchar(100) NOT NULL,
    role          varchar(10)  NOT NULL,
    status        varchar(10)  NOT NULL,
    created_at    timestamptz  NOT NULL,
    updated_at    timestamptz  NOT NULL,
    CONSTRAINT uk_app_user_email UNIQUE (email),
    CONSTRAINT ck_app_user_email_normalized CHECK (email = lower(btrim(email))),
    CONSTRAINT ck_app_user_role CHECK (role IN ('USER', 'ADMIN')),
    CONSTRAINT ck_app_user_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE INDEX ix_app_user_role_status ON app_user (role, status);

CREATE TABLE refresh_token (
    id         uuid        PRIMARY KEY,
    user_id    uuid        NOT NULL REFERENCES app_user (id),
    token_hash varchar(64) NOT NULL,
    expires_at timestamptz NOT NULL,
    revoked_at timestamptz,
    CONSTRAINT uk_refresh_token_hash UNIQUE (token_hash)
);

-- Revoking all the tokens of a user only touches the ones still valid.
CREATE INDEX ix_refresh_token_user_valid ON refresh_token (user_id) WHERE revoked_at IS NULL;

CREATE TABLE reactivation_request (
    id           uuid        PRIMARY KEY,
    user_id      uuid        NOT NULL REFERENCES app_user (id),
    status       varchar(10) NOT NULL,
    requested_at timestamptz NOT NULL,
    resolved_at  timestamptz,
    resolved_by  uuid        REFERENCES app_user (id),
    CONSTRAINT ck_reactivation_request_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    CONSTRAINT ck_reactivation_request_resolution CHECK (
        (status = 'PENDING' AND resolved_at IS NULL AND resolved_by IS NULL)
        OR (status <> 'PENDING' AND resolved_at IS NOT NULL AND resolved_by IS NOT NULL)
    )
);

-- At most one pending request per user (rule I6).
CREATE UNIQUE INDEX ux_reactivation_request_pending ON reactivation_request (user_id) WHERE status = 'PENDING';

CREATE INDEX ix_reactivation_request_status ON reactivation_request (status, requested_at);
