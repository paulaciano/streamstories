-- Activity: watch progress of each user on movies and episodes.
-- Two exclusive foreign keys instead of a polymorphic reference, so the database keeps referential integrity
-- (ADR-0007). No ON DELETE CASCADE: content with progress cannot be deleted, only retired (rule C11).

CREATE TABLE watch_progress (
    id               uuid        PRIMARY KEY,
    user_id          uuid        NOT NULL REFERENCES app_user (id),
    movie_id         uuid        REFERENCES movie (id),
    episode_id       uuid        REFERENCES episode (id),
    position_seconds integer     NOT NULL,
    completed        boolean     NOT NULL,
    last_watched_at  timestamptz NOT NULL,
    CONSTRAINT ck_watch_progress_content CHECK ((movie_id IS NOT NULL) <> (episode_id IS NOT NULL)),
    CONSTRAINT ck_watch_progress_position CHECK (position_seconds >= 0)
);

-- Rule A1: one progress record per user and playable content.
CREATE UNIQUE INDEX ux_watch_progress_user_movie ON watch_progress (user_id, movie_id) WHERE movie_id IS NOT NULL;
CREATE UNIQUE INDEX ux_watch_progress_user_episode ON watch_progress (user_id, episode_id) WHERE episode_id IS NOT NULL;

-- Rule C11: finding out whether a movie or an episode has progress before deleting it.
CREATE INDEX ix_watch_progress_movie ON watch_progress (movie_id) WHERE movie_id IS NOT NULL;
CREATE INDEX ix_watch_progress_episode ON watch_progress (episode_id) WHERE episode_id IS NOT NULL;
