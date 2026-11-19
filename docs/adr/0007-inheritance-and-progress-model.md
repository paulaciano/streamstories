# ADR-0007: `JOINED` inheritance for titles and exclusive foreign keys for progress

- **Status:** Accepted
- **Date:** 2026-11-16

## Context

Movies and series share attributes (title, synopsis, genres, ratings), and the catalog must be able to list them **mixed**. On the other hand, watch progress is recorded on movies **or** episodes, which are different entities.

## Decision

1. `MediaItem` is an abstract entity with [`JOINED` inheritance](https://docs.jboss.org/hibernate/orm/current/userguide/html_single/Hibernate_User_Guide.html#entity-inheritance-joined-table), with the `Movie` and `Series` subclasses and a `type` discriminator column. Genres and ratings point to `media_item`.
2. `watch_progress` has two nullable foreign keys (`movie_id` and `episode_id`), with `CHECK ((movie_id IS NOT NULL) <> (episode_id IS NOT NULL))` and a partial unique index for each of them together with `user_id`.

## Alternatives considered

- **`SINGLE_TABLE`:** queries are faster, but each subtype's own columns must be nullable, and the `NOT NULL` constraints are lost.
- **`TABLE_PER_CLASS`:** the mixed catalog needs a `UNION`, and there can be no foreign key to "any title".
- **Polymorphic reference (`content_type` + `content_id`):** it allows no foreign key, so the database cannot guarantee integrity.
- **Separate progress tables:** they duplicate logic and complicate "continue watching", which has to combine movies and episodes.

## Consequences

- ✅ The database guarantees referential integrity.
- ✅ The mixed catalog and the genre filter come from a single query.
- ⚠️ `JOINED` needs a `JOIN` to load each subtype's data, an acceptable cost at this volume.
- ⚠️ The progress invariant must also be guaranteed in the Java entity.
