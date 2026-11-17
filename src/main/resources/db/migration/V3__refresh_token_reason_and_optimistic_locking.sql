-- Record why each refresh token was revoked, so only the reuse of a rotated token is treated as theft.
ALTER TABLE refresh_token ADD COLUMN revocation_reason varchar(20);

-- Tokens revoked before this migration are assumed rotated, which keeps reuse detection on the safe side.
UPDATE refresh_token SET revocation_reason = 'ROTATED' WHERE revoked_at IS NOT NULL;

ALTER TABLE refresh_token
    ADD CONSTRAINT ck_refresh_token_revocation_reason
        CHECK (revocation_reason IN ('ROTATED', 'LOGOUT', 'ACCOUNT_DEACTIVATED', 'REUSE_DETECTED')),
    ADD CONSTRAINT ck_refresh_token_revocation
        CHECK ((revoked_at IS NULL) = (revocation_reason IS NULL));

-- Optimistic locking for titles: concurrent edits fail instead of silently overwriting each other, and a change that
-- only touches the genres still counts as a modification (and updates updated_at).
ALTER TABLE media_item ADD COLUMN version bigint NOT NULL DEFAULT 0;
