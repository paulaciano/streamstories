# ADR-0004: Video is handled as metadata

- **Status:** Accepted
- **Date:** 2026-11-16

## Context

Delivering video means storing it, transcoding it to adaptive formats ([HLS](https://datatracker.ietf.org/doc/html/rfc8216) or DASH), serving it from a CDN and protecting it. That is an infrastructure problem, separate from this system's domain: the catalog, the users and their activity.

## Decision

Each movie and each episode stores an external `videoUrl` and its `durationSeconds`. The API **does not upload, process or serve video**. The client plays from the URL and reports the position with `PUT /me/progress/...`.

## Alternatives considered

- **Serving the files from the API with HTTP Range:** it adds storage and heavy I/O without adding to the domain.
- **Integrating object storage with transcoding:** it is a system in itself.

## Consequences

- ✅ The scope stays on the catalog, the users and their activity.
- ✅ Watch progress is modeled as real platforms do it: the client reports it.
- ⚠️ The API trusts the position the client reports, and only validates it against the duration.
