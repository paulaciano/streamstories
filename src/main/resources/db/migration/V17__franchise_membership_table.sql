-- The franchise owns the order of its movies, so joining or leaving a franchise does not change the movie row (and
-- its version, which clients use with If-Match).
CREATE TABLE franchise_movie (
    franchise_id uuid     NOT NULL REFERENCES franchise (id),
    position     integer  NOT NULL,
    movie_id     uuid     NOT NULL REFERENCES movie (id),
    PRIMARY KEY (franchise_id, position),
    -- Rule C14; deferred so a movie can move between positions in one transaction.
    CONSTRAINT uk_franchise_movie_movie UNIQUE (movie_id) DEFERRABLE INITIALLY DEFERRED
);

INSERT INTO franchise_movie (franchise_id, position, movie_id)
SELECT franchise_id, franchise_order - 1, id FROM movie WHERE franchise_id IS NOT NULL;

ALTER TABLE movie
    DROP CONSTRAINT uk_movie_franchise_order,
    DROP CONSTRAINT ck_movie_franchise,
    DROP COLUMN franchise_id,
    DROP COLUMN franchise_order;
