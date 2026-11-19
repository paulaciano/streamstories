-- "Continue watching" needs the most recent episode of each series. Keeping the series next to the episode lets the
-- activity module find it in its own table, without loading every episode the user ever watched.
ALTER TABLE watch_progress ADD COLUMN series_id uuid REFERENCES series (id);

UPDATE watch_progress p
SET series_id = s.series_id
FROM episode e
JOIN season s ON s.id = e.season_id
WHERE p.episode_id = e.id;

-- Exactly the episodes have a series (episodes never move to another season).
ALTER TABLE watch_progress
    ADD CONSTRAINT ck_watch_progress_series CHECK ((episode_id IS NOT NULL) = (series_id IS NOT NULL));

CREATE INDEX ix_watch_progress_user_series_last_watched
    ON watch_progress (user_id, series_id, last_watched_at DESC)
    WHERE series_id IS NOT NULL;
