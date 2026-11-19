# Architecture decision records (ADR)

An **ADR** ([*Architecture Decision Record*](https://adr.github.io/)) documents an important decision: the context it was made in, the decision itself, the alternatives that were discarded and its consequences.

**When to write one:** when the decision is expensive to reverse, affects several modules, or a reasonable person could have chosen another option.

**Rules:**
- Each ADR has a number that is never reused.
- An accepted ADR is **not edited** in substance. If the decision changes, a new one replaces it, and the old one becomes *Superseded by ADR-XXXX*.
- New ADRs start from the [template](template.md).

| ADR | Title | Status |
|-----|-------|--------|
| [0001](0001-record-decisions-with-adrs.md) | Record architecture decisions as ADRs | Accepted |
| [0002](0002-modular-monolith.md) | A modular monolith organized by feature | Accepted |
| [0003](0003-rest-api-without-frontend.md) | The system is only a REST API | Accepted |
| [0004](0004-video-as-metadata.md) | Video is handled as metadata | Accepted |
| [0005](0005-jwt-authentication.md) | Stateless authentication with JWT and rotating refresh tokens | Accepted |
| [0006](0006-flyway-migrations.md) | Database schema versioned with Flyway | Accepted |
| [0007](0007-inheritance-and-progress-model.md) | `JOINED` inheritance for titles and exclusive foreign keys for progress | Accepted |
| [0008](0008-markdown-and-mermaid-docs.md) | Documentation in Markdown with Mermaid diagrams | Accepted |
| [0009](0009-retiring-content.md) | Content is retired, not deleted | Superseded by ADR-0011 |
| [0010](0010-admin-approved-reactivation.md) | Account reactivation requires an admin's approval | Accepted |
| [0011](0011-retiring-by-season.md) | Content is retired by whole movie, series or season | Accepted |
| [0012](0012-measured-performance-without-cache.md) | Measured performance, without a cache for now | Accepted |
