# ADR-0010: Account reactivation requires an admin's approval

- **Status:** Accepted
- **Date:** 2026-11-16

## Context

An account can become inactive because its holder deactivates it or because an administrator does. In both cases, enabling it again is a decision for the platform, not for the user alone.

## Decision

- Only an `ADMIN` can reactivate an account.
- The inactive user can **request** reactivation by sending their credentials (`POST /auth/reactivation-requests`). That creates a `ReactivationRequest` in `PENDING` status, and there can be at most one pending request per user.
- An admin approves or rejects the request. Approval reactivates the account. Each resolution records who made it and when.
- An admin can also reactivate an account directly from user administration.

## Alternatives considered

- **Automatic reactivation on login:** the user could undo a deactivation made by an admin.
- **A `PENDING_REACTIVATION` status on the user:** it mixes two concepts and loses the history of requests and resolutions.

## Consequences

- ✅ The platform fully controls who gets access again.
- ✅ Every request and every decision can be traced.
- ⚠️ Since there are no notifications, the user learns about the approval when they try to log in again.
