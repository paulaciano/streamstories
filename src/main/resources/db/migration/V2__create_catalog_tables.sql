-- Catalog: genres, titles (movies and series with JOINED inheritance), seasons and episodes.
-- No ON DELETE CASCADE: retiring content never deletes related data (rule C10), and physical deletion goes through
-- the application, which checks for activity first (rule C11).

CREATE TABLE genre (
    id   uuid        PRIMARY KEY,
    name varchar(50) NOT NULL,
    slug varchar(60) NOT NULL,
    CONSTRAINT uk_genre_slug UNIQUE (slug),
    CONSTRAINT ck_genre_slug_format CHECK (slug ~ '^[a-z0-9]+(-[a-z0-9]+)*$')
);

-- Rule C3: genre names are unique ignoring case.
CREATE UNIQUE INDEX ux_genre_name ON genre (lower(name));

CREATE TABLE media_item (
    id              uuid          PRIMARY KEY,
    type            varchar(10)   NOT NULL,
    title           varchar(200)  NOT NULL,
    synopsis        text,
    release_year    smallint      NOT NULL,
    cover_image_url varchar(2048),
    availability    varchar(10)   NOT NULL,
    created_at      timestamptz   NOT NULL,
    updated_at      timestamptz   NOT NULL,
    CONSTRAINT ck_media_item_type CHECK (type IN ('MOVIE', 'SERIES')),
    CONSTRAINT ck_media_item_title CHECK (btrim(title) <> ''),
    CONSTRAINT ck_media_item_release_year CHECK (release_year BETWEEN 1888 AND 2100),
    CONSTRAINT ck_media_item_availability CHECK (availability IN ('AVAILABLE', 'RETIRED'))
);

CREATE INDEX ix_media_item_availability_title ON media_item (availability, title);

-- Rule C2: a title has each genre at most once.
CREATE TABLE media_item_genre (
    media_item_id uuid NOT NULL REFERENCES media_item (id),
    genre_id      uuid NOT NULL REFERENCES genre (id),
    PRIMARY KEY (media_item_id, genre_id)
);

CREATE INDEX ix_media_item_genre_genre ON media_item_genre (genre_id);

CREATE TABLE movie (
    id               uuid          PRIMARY KEY REFERENCES media_item (id),
    duration_seconds integer       NOT NULL,
    video_url        varchar(2048) NOT NULL,
    CONSTRAINT ck_movie_duration CHECK (duration_seconds > 0)
);

CREATE TABLE series (
    id uuid PRIMARY KEY REFERENCES media_item (id)
);

CREATE TABLE season (
    id        uuid         PRIMARY KEY,
    series_id uuid         NOT NULL REFERENCES series (id),
    number    smallint     NOT NULL,
    title     varchar(200),
    CONSTRAINT uk_season_number UNIQUE (series_id, number),
    CONSTRAINT ck_season_number CHECK (number >= 1)
);

CREATE TABLE episode (
    id               uuid          PRIMARY KEY,
    season_id        uuid          NOT NULL REFERENCES season (id),
    number           smallint      NOT NULL,
    title            varchar(200)  NOT NULL,
    synopsis         text,
    duration_seconds integer       NOT NULL,
    video_url        varchar(2048) NOT NULL,
    availability     varchar(10)   NOT NULL,
    CONSTRAINT uk_episode_number UNIQUE (season_id, number),
    CONSTRAINT ck_episode_number CHECK (number >= 1),
    CONSTRAINT ck_episode_title CHECK (btrim(title) <> ''),
    CONSTRAINT ck_episode_duration CHECK (duration_seconds > 0),
    CONSTRAINT ck_episode_availability CHECK (availability IN ('AVAILABLE', 'RETIRED'))
);
