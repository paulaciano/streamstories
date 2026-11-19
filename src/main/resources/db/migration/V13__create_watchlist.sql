CREATE TABLE watchlist_entry (
    user_id       uuid        NOT NULL REFERENCES app_user (id),
    media_item_id uuid        NOT NULL REFERENCES media_item (id),
    added_at      timestamptz NOT NULL,
    PRIMARY KEY (user_id, media_item_id)
);

CREATE INDEX ix_watchlist_entry_user_added ON watchlist_entry (user_id, added_at DESC);
CREATE INDEX ix_watchlist_entry_media_item ON watchlist_entry (media_item_id);
