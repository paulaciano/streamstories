# ADR-0002: A modular monolith organized by feature

- **Status:** Accepted
- **Date:** 2026-11-16

## Context

The domain has well-defined areas (identity, catalog, activity and recommendations) that grow in phases. The team is small and there is no need to scale parts of the system separately. The goal is clear boundaries between areas, without paying the operational cost of a distributed system.

## Decision

The system is **a single Spring Boot application** split into modules by feature (`identity`, `catalog`, `activity`, `recommendation`, `shared`), each with the `api`, `application` and `domain` sublayers. Modules only talk through each other's public API, and [Spring Modulith](https://docs.spring.io/spring-modulith/reference/) checks those rules in a test that runs in CI.

## Alternatives considered

- **Microservices:** they add networking, eventual consistency, several deployments and distributed observability, without a problem that justifies them.
- **Packages by layer** (`controllers/`, `services/`, `repositories/`): they do not prevent arbitrary dependencies, and each feature loses its cohesion.
- **Separate Maven modules:** they enforce stronger boundaries, but complicate the build. Spring Modulith achieves verifiable boundaries with less friction.

## Consequences

- ✅ A single deployment, one database and simple local transactions.
- ✅ Boundaries are checked automatically.
- ✅ A module can be extracted as a service later without redesigning the domain.
- ⚠️ Each module's public API must be designed with care.
