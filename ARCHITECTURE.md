# Architecture Overview

## Layers
- Controller: HTTP adapters, only deals with DTOs.
- Service: Business logic.
- Repository / JPA: Data persistence.

## DTO Policy
- Never expose JPA entities directly in controller responses.
- Create dedicated request/response DTOs; map in service layer (MapStruct can be added later).
- Goal: decouple API surface from internal persistence changes.

## Logging
- Use SLF4J `log.info/debug/error` only (no System.out).
- Structured key=value layout defined in `logback-spring.xml`.
- Future: integrate traceId/spanId (Spring sleuth / micrometer tracing).

## Database Migrations
- Managed by Flyway under `db/migration` (versioned, idempotent seed data).

## Removed WebFlux
- Reactive stack removed to simplify; synchronous servlet stack (spring-boot-starter-web) suffices.
- If future high-concurrency streaming needed, reintroduce selectively.

## Testing Strategy
- Unit tests: services with mocks.
- Integration: Testcontainers PostgreSQL (schema + migrations validation).

