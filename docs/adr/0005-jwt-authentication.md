# ADR-0005: Stateless authentication with JWT and rotating refresh tokens

- **Status:** Accepted
- **Date:** 2026-11-16

## Context

Different kinds of HTTP clients use the API, and there is no browser session. It needs authentication without server-side state, with `USER` and `ADMIN` roles, and a deactivated account has to lose access immediately.

## Decision

- **JWT access token** signed with HS256, with a 15-minute lifetime, validated with [Spring Security OAuth2 Resource Server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html).
- Opaque **refresh token** with a 7-day lifetime. The database only stores its hash. It **is rotated** on each use and, if its reuse is detected, all the user's tokens are revoked.
- A filter checks on each request that the user is still `ACTIVE`.
- One role per user. Each account is one viewer, without several profiles.

## Alternatives considered

- **Cookie session:** it needs CSRF protection and server-side state, and is less natural for an API.
- **A single long-lived access token:** it cannot be revoked, and it is a risk if it leaks.
- **External identity provider** ([Keycloak](https://www.keycloak.org/), Auth0): it adds infrastructure that is not needed today. Since the resource server is standard, migrating later would only mean changing who issues the tokens.
- **RS256 (asymmetric key):** it adds value when third parties validate the tokens. With a single issuer and verifier, HS256 is enough.

## Consequences

- ✅ It is an industry standard, easy to test and without a server session.
- ✅ Deactivation applies immediately.
- ⚠️ Checking the account status adds a lightweight query per request, which can be cached.
- ⚠️ The HS256 secret must have at least 256 bits and only come from an environment variable.
