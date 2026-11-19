CREATE TABLE franchise (
    id         uuid                         PRIMARY KEY,
    name       varchar(100) COLLATE "es-x-icu" NOT NULL,
    created_at timestamptz                  NOT NULL,
    updated_at timestamptz                  NOT NULL,
    CONSTRAINT ck_franchise_name CHECK (btrim(name) <> '')
);

CREATE UNIQUE INDEX ux_franchise_name ON franchise (lower(name));

ALTER TABLE movie
    ADD COLUMN franchise_id uuid REFERENCES franchise (id),
    ADD COLUMN franchise_order smallint;

ALTER TABLE movie ADD CONSTRAINT ck_movie_franchise CHECK ((franchise_id IS NULL) = (franchise_order IS NULL));

-- Deferred, so reordering a franchise can swap positions within one transaction.
ALTER TABLE movie
    ADD CONSTRAINT uk_movie_franchise_order UNIQUE (franchise_id, franchise_order) DEFERRABLE INITIALLY DEFERRED;
