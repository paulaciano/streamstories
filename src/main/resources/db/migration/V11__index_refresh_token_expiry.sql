-- The daily cleanup deletes expired refresh tokens.
CREATE INDEX ix_refresh_token_expires_at ON refresh_token (expires_at);
