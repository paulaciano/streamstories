# ADR-0009: Content is retired, not deleted

- **Status:** Superseded by [ADR-0011](0011-retiring-by-season.md)
- **Date:** 2026-11-16

## Context

The catalog changes: some movies, series or episodes stop being offered. But they have related activity (progress, history, ratings), and that activity belongs to the users and feeds the averages and the recommendations.

## Decision

Content has an **availability** (`availability`): `AVAILABLE` or `RETIRED`. It lives in `media_item` (whole movies and series) and in `episode`.

Retired content:
- cannot be played: reporting progress returns `409 content-unavailable`;
- is not recommended and does not appear in the catalog listings or searches;
- accepts no new ratings;
- is still reachable by id and appears in the user's history, marked as retired;
- keeps all its data and its related activity.

An episode is playable only if both it and its series are available. Physical deletion is only allowed for content without any activity, for example content loaded by mistake.

## Alternatives considered

- **Cascading deletion:** it destroys the users' activity.
- **Generic soft delete (`deleted_at`) with global filters:** it hides the row everywhere, including the history, and mixes "retired" with "deleted".
- **Availability windows with dates:** more flexible, but beyond current needs. It can be added later.

## Consequences

- ✅ No user activity is lost.
- ✅ Retired content can be offered again.
- ⚠️ The catalog queries, "continue watching" and the recommendations must filter by availability. Tests cover it.
