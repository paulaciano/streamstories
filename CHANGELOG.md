# Changelog

All notable changes to this project are documented in this file.

The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and the project uses [semantic versioning](https://semver.org/).

## [Unreleased]

### Changed

- The documentation in `docs/`, the ADRs and this changelog are now in English, with English file names. The README has an English version (`README.md`) and a Spanish one (`README.es.md`).
- The README explains step by step how to run and try the project locally.

## [1.0.0] - 2026-11-19

### Added

- Watchlist (`/me/watchlist`): movies and series to watch later. Adding and removing are idempotent, and a retired title cannot be added, but it stays in the lists that already had it.
- Franchises (`/franchises`): movies grouped in order. A movie belongs to one franchise at most, and deleting the franchise does not delete its movies.
- Container image with Cloud Native Buildpacks (`./mvnw spring-boot:build-image`).

### Changed

- The recommendations read the catalog and the averages with queries that return plain rows: with 6,000 titles and 300,000 ratings, the response goes from about 390 ms to about 150 ms ([ADR-0012](docs/adr/0012-measured-performance-without-cache.md)).

### Security

- Login attempt limit: after 5 wrong passwords for an email within 15 minutes, login and reactivation for that email answer `429`. Attempts are stored in the database, so the limit holds for every instance.

## [0.3.0] - 2026-11-19

### Added

- Personalized recommendations (`GET /me/recommendations`), based on the genres of the titles the user rated and finished, each with its reason. Without activity, the titles best rated by the community are recommended. Retired content is never recommended.

## [0.2.0] - 2026-11-18

### Added

- Watch progress: `PUT /me/progress/movies/{id}` and `PUT /me/progress/episodes/{id}` save the exact position and mark the content as watched at 90%. Retired content accepts no new progress.
- Content with progress or ratings can no longer be deleted, only retired.
- Continue watching (`GET /me/continue-watching`): unfinished movies and one entry per series with its last watched episode, while there is something left to watch.
- Watch history (`GET /me/history`), paginated and including retired content.
- Ratings from 1 to 5 for movies and series (`/me/ratings`). Each title's detail shows the average, the count and the user's own score. A retired title keeps its ratings, but accepts no new ones.
- `ETag` in the detail of movies and series, and an optional `If-Match` to change them without overwriting another administrator's change (`412` if the version does not match).
- Text search ignores accents: "orbita" finds "Órbita".
- Expired refresh tokens are deleted once a day.

### Changed

- The API answers in English: error messages, Problem Details titles, validation messages and the OpenAPI specification. The catalog content does not change.
- Framework errors, such as `405` and `415`, also get a StreamStories `type` (`method-not-allowed`, `unsupported-media-type`, `not-acceptable`).
- A blank season title is stored as `null`, like a season without a title.
- Deleting a series checks the activity of all its episodes in a single query, instead of one per episode.

### Fixed

- A number with decimals in an integer field was truncated (`4.5` was stored as `4`); it now answers `400`.
- A very distant page (for example, `?page=30000000&size=100`) answered `500`; it now answers `400`.
- A `null` inside `genreIds` answered `500`; it is now a validation error.
- Text in a numeric field (`"score": "4"`) was converted to a number; it now answers `400`.
- The database now also limits season and episode numbers to 999.

### Security

- The `dev` profile configuration (JWT key and development administrator) is no longer in the jar.
- The application does not start if the initial administrator's password is weak or is the example value.
- PostgreSQL in Docker Compose only listens on `127.0.0.1`.
- Errors forwarded by the container no longer become `401` on requests that are not `GET`.

## [0.1.0] - 2026-11-17

### Added

- Accounts and authentication (phase 1):
  - Sign-up, login with a JWT access token, and a refresh token with rotation and reuse detection.
  - Deactivating your own account, effective immediately even while the access token is still valid.
  - Reactivation requests that an administrator resolves.
  - User administration: listing with filters, changing status and role, protecting the last active administrator.
  - Creating the initial administrator from environment variables.
- Catalog (phase 1):
  - Genres with a slug derived from the name.
  - Movies and series with genres, and series with numbered seasons and episodes.
  - A mixed catalog of movies and series with filters by genre, type and text.
  - Retiring content: retired content does not appear in the users' listings, but it is still reachable by id, without a video URL.
  - Physical deletion is only allowed for content without activity.
- Sample data in the `dev` profile: a sample catalog and a development administrator, created only if they do not exist.
- A base entity with a UUIDv7 identifier and auditing of the creation and modification dates.
- A uniform paginated response and validation of the allowed sort fields.

### Fixed

- Out-of-range season and episode numbers were truncated when stored; they are now validated (from 1 to 999).
- Reusing a refresh token that had expired or been revoked by a logout closed all the user's sessions; now only reusing a rotated token is treated as theft.
- Approving and rejecting the same reactivation request at once could leave inconsistent data; the request is now locked.
- Two simultaneous reactivation requests returned `409`; they now return the same pending request.
- Changing only the genres of a title did not update `updatedAt`.
- Entity comparison failed with Hibernate proxies.
- Invalid values rejected by the database answered `409` instead of `400`, and the log included the PostgreSQL detail.
- Pagination could repeat or skip items when the sort had ties.
- Alphabetical order put accented titles last ("Órbita" after "Vecinos"); it now sorts as in Spanish.

### Security

- The packaged application no longer enables the `dev` profile by default: without `STREAMSTORIES_SECURITY_JWT_SECRET` it does not start, instead of using the development key.

### Changed

- A series' content is retired by whole season, not by episode (ADR-0011, which supersedes ADR-0009). `PATCH /series/{id}/seasons/{number}` changes the season's availability, and `PATCH /episodes/{id}` no longer exists.
- The catalog has its own DTOs in the `api` layer, like identity.
- Concurrent edits of titles use optimistic locking.
- Validation messages are always in Spanish.
- Login and reactivation check the password outside the transaction, without holding a database connection.
- Authentication, authorization and database constraint errors also answer with Problem Details.

## [0.0.1] - 2026-11-17

### Added

- Documentation of the product, the architecture and the design decisions (ADR).
- Technical base of the project (phase 0):
  - Configuration with virtual threads, Open Session in View disabled and schema validation with Flyway.
  - Local PostgreSQL with Docker Compose.
  - Global error handling with Problem Details (RFC 9457).
  - OpenAPI specification and Swagger UI.
  - Actuator limited to `health` and `info`.
  - Module verification with Spring Modulith.
  - Integration tests with Testcontainers.
  - CI with GitHub Actions, automatic formatting with Spotless and coverage with JaCoCo.
  - Dependabot, a pull request template and EditorConfig.

[Unreleased]: https://github.com/paulaciano/streamstories/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/paulaciano/streamstories/compare/v0.3.0...v1.0.0
[0.3.0]: https://github.com/paulaciano/streamstories/compare/v0.2.0...v0.3.0
[0.2.0]: https://github.com/paulaciano/streamstories/compare/v0.1.0...v0.2.0
[0.1.0]: https://github.com/paulaciano/streamstories/compare/v0.0.1...v0.1.0
[0.0.1]: https://github.com/paulaciano/streamstories/releases/tag/v0.0.1
