# ADR-0001: Record architecture decisions as ADRs

- **Status:** Accepted
- **Date:** 2026-11-16

## Context

The code shows *what* was built, but not *why*. Without a record, the reasoning behind each decision gets lost and the same discussions come back. This matters even more in a project developed with AI assistance, because the assistant needs to know the decisions in force so it does not contradict them.

## Decision

Each significant architecture decision is recorded as an ADR in `docs/adr/`, with a short format (context, decision, alternatives and consequences) based on the one [proposed by Michael Nygard](https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions).

## Alternatives considered

- **Only PR comments:** they end up scattered and are hard to find.
- **GitHub wiki:** it is separate from the code and not versioned with it.

## Consequences

- ✅ Decisions are reviewed in the same PR as the code that implements them.
- ✅ The context is available to anyone who joins the project.
- ⚠️ Writing the ADR adds a small cost to each important decision.
