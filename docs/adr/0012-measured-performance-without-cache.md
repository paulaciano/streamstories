# ADR-0012: Measured performance, without a cache for now

- **Status:** Accepted
- **Date:** 2026-11-19

## Context

The roadmap asked to review the indexes with `EXPLAIN ANALYZE` and to cache the recommendations and the catalog if the data justified it. To decide with numbers, a PostgreSQL 18 database was loaded with a generated volume:

| Data | Amount |
|------|-------:|
| Titles (5,000 movies and 1,000 series) | 6,000 |
| Episodes | 30,000 |
| Users | 20,000 |
| Progress records | 700,000 |
| Ratings | 300,000 |

The endpoints were measured on the packaged application (median of several calls, on a single machine), and the queries with `EXPLAIN ANALYZE`.

## Measurements

| Endpoint | Median |
|----------|-------:|
| `GET /catalog` | 31 ms |
| `GET /catalog?q=…` | 42 ms |
| `GET /catalog?genre=…` | 26 ms |
| `GET /me/history` | 37 ms |
| `GET /me/continue-watching` | 42 ms |
| `GET /me/ratings` | 17 ms |
| `GET /me/recommendations` | 386 ms → **146 ms** |

- Every query by user (history, "continue watching", ratings, signals for the recommendations) and by title (average, activity for C11) uses its index and takes under 1 ms in the database.
- Text search scans the titles table (`unaccent` + `LIKE '%…%'`): 9 ms with 6,000 titles. A trigram index is not justified at this volume.
- The recommendations read the whole catalog and every rating on each request. The cost was not in SQL (7 ms and 34 ms) but in Java: Hibernate built 6,000 entities and loaded genres in about 120 queries, and Spring Data projections created a proxy per row. They were replaced with two queries that return plain rows.

## Decision

- **No cache is added.** At this volume no endpoint goes over 150 ms, and a cache would add invalidation (retirements, new ratings) and per-instance state without a measured need.
- **No indexes are added**: the existing ones cover every query.
- Bulk reads (the whole catalog, every average) use queries that return plain rows, without entities or projections.

## Alternatives considered

- **In-memory cache of the catalog and the averages:** it would bring the recommendations to about 30 ms, but each instance would have its own copy, to invalidate whenever content is retired or rated.
- **A table of precomputed averages:** it avoids aggregating every rating on each request, in exchange for keeping it up to date on each rating.
- **Trigram index (`pg_trgm`) for search:** it needs a custom immutable `unaccent` function, and changes nothing at this volume.

## Consequences

- ✅ No extra state and no invalidation: everything always comes from the database.
- ⚠️ The recommendations grow with the size of the catalog and the number of ratings. If either grows by an order of magnitude, the table of precomputed averages or a short-lived cache should be revisited.
