# ADR-0011: Content is retired by whole movie, series or season

- **Status:** Accepted
- **Date:** 2026-11-17
- **Supersedes:** [ADR-0009](0009-retiring-content.md)

## Context

The catalog changes: some movies, series or seasons stop being offered. But they have related activity (progress, history, ratings), and that activity belongs to the users and feeds the averages and the recommendations.

[ADR-0009](0009-retiring-content.md) allowed retiring movies, series and **single episodes**. In practice, a series' content stops being offered by whole seasons, the same way it is licensed. Retiring individual episodes leaves seasons with gaps, which the user cannot watch complete.

## Decision

Content has an **availability** (`availability`): `AVAILABLE` or `RETIRED`. It lives in:
- `media_item`, for whole movies and series;
- `season`, for whole seasons.

Episodes have **no** availability of their own: an episode is playable only if its season and its series are available.

Retired content:
- cannot be played: reporting progress returns `409 content-unavailable`;
- is not recommended and does not appear in the listings, the searches or the series detail that a `USER` sees;
- accepts no new ratings;
- is still reachable by id (or by number, for a season) and appears in the user's history, marked as retired;
- keeps all its data and its related activity.

Physical deletion is only allowed for content without any activity, for example content loaded by mistake.

## Alternatives considered

- **Keeping retirement by episode (ADR-0009):** it is finer-grained, but leaves incomplete seasons and does not match how content stops being offered.
- **Allowing both levels (season and episode):** it adds combinations to filter and test in "continue watching" and the recommendations, without a real need.
- **Cascading deletion:** it destroys the users' activity.
- **Generic soft delete (`deleted_at`) with global filters:** it hides the row everywhere, including the history, and mixes "retired" with "deleted".
- **Availability windows with dates:** more flexible, but beyond current needs. It can be added later.

## Consequences

- ✅ No user activity is lost.
- ✅ Retired content can be offered again.
- ✅ A season never has missing episodes for the user.
- ⚠️ The catalog queries, "continue watching" and the recommendations must filter by the availability of both the series and the season. Tests cover it.
- ⚠️ If a single episode ever had to be retired, availability would have to be added back to episodes in a new ADR.
