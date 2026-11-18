-- "Continue watching" and the history read a user's progress, most recently watched first.
CREATE INDEX ix_watch_progress_user_last_watched ON watch_progress (user_id, last_watched_at DESC);
