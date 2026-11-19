-- Rules C4 and C5: season and episode numbers go from 1 to 999, also for data that does not go through the API.
ALTER TABLE season DROP CONSTRAINT ck_season_number;
ALTER TABLE season ADD CONSTRAINT ck_season_number CHECK (number BETWEEN 1 AND 999);

ALTER TABLE episode DROP CONSTRAINT ck_episode_number;
ALTER TABLE episode ADD CONSTRAINT ck_episode_number CHECK (number BETWEEN 1 AND 999);
