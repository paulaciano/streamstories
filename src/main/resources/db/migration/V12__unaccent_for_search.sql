-- Searching by text ignores accents: "orbita" finds "Órbita" and "nandu" finds "Ñandú".
-- unaccent is a trusted extension, so the owner of the database can create it without being a superuser.
CREATE EXTENSION IF NOT EXISTS unaccent;
