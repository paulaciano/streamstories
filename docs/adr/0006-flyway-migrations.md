# ADR-0006: Database schema versioned with Flyway

- **Status:** Accepted
- **Date:** 2026-11-16

## Context

The schema evolves phase by phase. If Hibernate generates it (`ddl-auto=update`), the result is not reproducible, not fit for production, and the changes do not show up in PRs.

## Decision

- Schema changes are [Flyway](https://documentation.red-gate.com/flyway) SQL migrations in `src/main/resources/db/migration`.
- Hibernate runs with `ddl-auto=validate`: it checks that the entities match the schema, but never changes it.
- **A merged migration is never edited.** Any change is made with a new migration.
- Constraints (`CHECK`, `UNIQUE`, foreign keys) are defined in the database, not only in Java.

## Alternatives considered

- **[Liquibase](https://docs.liquibase.com/):** just as valid. Flyway is more direct with plain SQL, which is easy to read and review.
- **`ddl-auto=update`:** discarded for the reasons in the context.

## Consequences

- ✅ The schema is reproducible in any environment, including tests with Testcontainers.
- ✅ Database changes are reviewed as readable SQL.
- ⚠️ The SQL is written by hand. `validate` catches any mismatch with the entities on startup.
