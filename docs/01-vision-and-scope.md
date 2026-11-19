# 1. Vision and scope

## 1.1 Purpose

StreamStories is the backend of an on-demand streaming catalog. It lets people browse movies and series, records what each person watches, lets them rate titles and gives them personalized recommendations.

## 1.2 Strategy: incremental delivery

The system is built iteratively. It starts with an **MVP** and adds features in phases (see the [Roadmap](07-roadmap.md)). This documentation describes the **full vision**, so the MVP architecture supports the later phases without rewriting its foundations.

Each phase must end with a deployable, tested and documented system.

## 1.3 Scope

| ✅ In scope | ❌ Out of scope |
|------------|----------------|
| REST API documented with OpenAPI | Graphical, web or mobile interface ([ADR-0003](adr/0003-rest-api-without-frontend.md)) |
| Catalog management: movies, series, seasons, episodes and genres | Uploading, storing, transcoding or serving video ([ADR-0004](adr/0004-video-as-metadata.md)) |
| Retiring content without losing the related activity | Payments, plans and subscription billing |
| Sign-up and login, USER and ADMIN roles, account deactivation and reactivation approved by an admin | Several profiles per account (one account is one viewer) |
| Watch history and progress ("continue watching") | Email notifications |
| Ratings of movies and series | DRM, parental controls and regional restrictions |
| Recommendations based on genres and ratings | Machine learning recommendations |
| Watchlist (`Watchlist`) and franchises (`Franchise`) | Social features: friends, comments, sharing |

## 1.4 Actors

| Actor | Description | Technical role |
|-------|-------------|----------------|
| **Visitor** | A person without a session. Can sign up, log in or request the reactivation of their account. | — (anonymous) |
| **Viewer** | A person with an active account. Browses the catalog, watches content, rates titles and gets recommendations. | `USER` |
| **Administrator** | Manages the catalog, the accounts and the reactivation requests. | `ADMIN` |
| **API client** | Any application that uses the API. | — |

## 1.5 Capabilities

### Catalog (`Catalog`)

The catalog holds the titles (`MediaItem`), which come in two formats:

- **Movie (`Movie`):** content released as a single piece. It is managed and played as an independent unit.
- **Series (`Series`):** episodic content with a hierarchy:
  - **Series (`Series`):** the whole work, with a title, a synopsis and a cover image.
  - **Season (`Season`):** a numbered group within a series.
  - **Episode (`Episode`):** the playable unit within a season.

**Genres (`Genre`):** each movie or series can have **several** genres at once (for example, *Action* + *Sci-Fi*). Browsing a genre shows movies and series together.

**Franchises (`Franchise`):** group related movies in an order, for example a trilogy in the order it is best watched. A movie belongs to one franchise at most. A franchise only orders movies: deleting it does not delete them, and non-administrators do not see the retired movies within it.

**Retiring content:** content is not deleted when it stops being offered. It is **retired**: it can no longer be played or recommended, but all its data and the related user activity are kept. Movies, series or whole seasons are retired ([ADR-0011](adr/0011-retiring-by-season.md)).

> **Two design concepts.** The system distinguishes:
> - **Title** (`MediaItem`): what is *browsed, classified and rated*. It is a movie or a series.
> - **Playable content** (`Playable`): what is *watched*, with a duration and progress. It is a movie or an episode.
>
> A movie plays both roles. A series is only a title, and an episode is only playable. See the [Domain model](03-domain-model.md).

### Users and accounts (`Identity`)

- Each user has an **account status** (`AccountStatus`): `ACTIVE` or `INACTIVE`.
- After several wrong passwords in a row, logging in with that email is blocked for a few minutes, to slow down password guessing ([Security](05-security.md)).
- An inactive account **cannot use the platform**, but its history and ratings are **kept intact**.
- The account holder or an administrator can deactivate an account. **Only an administrator can reactivate it.** The user can request the reactivation with their credentials, and an admin approves or rejects it ([ADR-0010](adr/0010-admin-approved-reactivation.md)).
- Each user has one role: `USER` or `ADMIN`.

### Activity (`Activity`)

- **Watch progress:** for each movie or episode the user started, the exact playback position, the date and whether it was finished are saved. Content counts as **watched** once it reaches 90% of its duration.
- **Continue watching:** shows the unfinished movies, where they were left, and each series with its last watched episode.
- **Ratings:** the user rates movies and series with an integer from **1 to 5**. Each title also shows its overall average, with decimals.
- **Watchlist:** the user saves movies and series to watch later. It is a personal list, independent of what they watched or rated: saving a title does not mark it as started or change their recommendations. If a saved title is retired, it stays in the list, marked as not available.

### Recommendations (`Recommendation`)

An engine that combines the user's history and ratings to find their preferences (recurring genres, favorite format) and suggest available titles they have not watched yet.

## 1.6 Non-functional requirements

| Attribute | Goal |
|-----------|------|
| **Maintainability** | Code organized by feature, with module boundaries checked automatically. |
| **Quality** | Automated tests at every level, and CI required on every pull request. |
| **Security** | Hashed passwords, short-lived tokens, no secrets in the repository and role-based authorization on the server. |
| **Documentation** | A self-documented API with OpenAPI, decisions recorded as ADRs and a README that gets the project running in minutes. |
| **Reproducibility** | A database schema versioned with migrations, and a local environment started with Docker Compose. |
| **Observability** | A health endpoint and structured logs. |

## 1.7 Glossary

| Term | Identifier | Meaning |
|------|------------|---------|
| Title | `MediaItem` | A movie or a series. It is browsed, classified by genre and rated. |
| Playable content | `Playable` | A movie or an episode. It has a duration and progress. |
| Availability | `Availability` | `AVAILABLE` or `RETIRED`. Retired content is neither played nor recommended. |
| Watch progress | `WatchProgress` | How far, and when, a user watched a piece of playable content. |
| History | `WatchHistory` | A user's progress records, ordered by date. |
| Rating | `Rating` | An integer score from 1 to 5 that a user gives to a title. |
| Reactivation request | `ReactivationRequest` | An inactive user's request to use the platform again. An admin resolves it. |
| Watchlist | `Watchlist` | Titles the user saves to watch later. |
| Franchise | `Franchise` | An ordered group of related movies. |
