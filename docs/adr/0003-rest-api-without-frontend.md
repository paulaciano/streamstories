# ADR-0003: The system is only a REST API

- **Status:** Accepted
- **Date:** 2026-11-16

## Context

It must be decided whether the system includes a user interface or is limited to the backend.

## Decision

StreamStories is **only the backend**: a JSON REST API documented with [OpenAPI](https://www.openapis.org/) and explorable from Swagger UI. It has no graphical interface. If a frontend were ever needed, it would be a separate project that uses this API.

## Alternatives considered

- **Server-side rendering (Thymeleaf):** it couples the presentation to the backend and offers no API that other clients can reuse.
- **API + frontend in the same repository:** it widens the scope and mixes two different life cycles.

## Consequences

- ✅ The API is the product's contract, and any client can use it.
- ✅ Swagger UI lets anyone try every endpoint without extra tools.
- ⚠️ If a frontend is added, CORS will have to be configured.
