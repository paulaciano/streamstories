-- The smallest unit that can be retired is the season, not the episode (ADR-0011).

ALTER TABLE season ADD COLUMN availability varchar(10) NOT NULL DEFAULT 'AVAILABLE';

ALTER TABLE season
    ADD CONSTRAINT ck_season_availability CHECK (availability IN ('AVAILABLE', 'RETIRED'));

-- A season whose episodes were all retired one by one keeps being unavailable as a whole.
UPDATE season s
SET availability = 'RETIRED'
WHERE EXISTS (SELECT 1 FROM episode e WHERE e.season_id = s.id)
  AND NOT EXISTS (SELECT 1 FROM episode e WHERE e.season_id = s.id AND e.availability = 'AVAILABLE');

ALTER TABLE season ALTER COLUMN availability DROP DEFAULT;

ALTER TABLE episode DROP COLUMN availability;
