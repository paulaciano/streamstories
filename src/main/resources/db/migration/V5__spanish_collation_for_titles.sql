-- Sort titles and genre names as in Spanish (accents, ñ), whatever the default locale of the database is.
-- ICU collations do not depend on the operating system: the libc locale of an Alpine image sorts by byte value,
-- which puts "Órbita" after "Vecinos". Indexes on these columns are rebuilt automatically.

ALTER TABLE media_item ALTER COLUMN title TYPE varchar(200) COLLATE "es-x-icu";

ALTER TABLE genre ALTER COLUMN name TYPE varchar(50) COLLATE "es-x-icu";

ALTER TABLE season ALTER COLUMN title TYPE varchar(200) COLLATE "es-x-icu";

ALTER TABLE episode ALTER COLUMN title TYPE varchar(200) COLLATE "es-x-icu";
