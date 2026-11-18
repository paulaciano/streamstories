-- Activity: scores from 1 to 5 that users give to titles (movies and series, never episodes: rule A10).
-- No ON DELETE CASCADE: a title with ratings cannot be deleted, only retired (rule C11).

CREATE TABLE rating (
    id            uuid        PRIMARY KEY,
    user_id       uuid        NOT NULL REFERENCES app_user (id),
    media_item_id uuid        NOT NULL REFERENCES media_item (id),
    score         smallint    NOT NULL,
    created_at    timestamptz NOT NULL,
    updated_at    timestamptz NOT NULL,
    -- Rule A9: one rating per user and title.
    CONSTRAINT uk_rating_user_media_item UNIQUE (user_id, media_item_id),
    CONSTRAINT ck_rating_score CHECK (score BETWEEN 1 AND 5)
);

-- The average of a title (rule A11) and finding out whether it has ratings before deleting it (rule C11).
CREATE INDEX ix_rating_media_item ON rating (media_item_id);

-- The ratings of a user, most recently rated first.
CREATE INDEX ix_rating_user_updated ON rating (user_id, updated_at DESC);
