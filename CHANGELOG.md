# Changelog

Todos los cambios relevantes del proyecto se documentan en este archivo.

El formato sigue [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/) y el proyecto usa [versionado semántico](https://semver.org/lang/es/).

## [Unreleased]

## [0.0.1] - 2026-11-17

### Added

- Cuentas y autenticación (fase 1):
  - Registro, inicio de sesión con access token JWT y refresh token con rotación y detección de reutilización.
  - Desactivación de la propia cuenta, con efecto inmediato aunque el access token siga vigente.
  - Solicitudes de reactivación que resuelve un administrador.
  - Administración de usuarios: listado con filtros, cambio de estado y de rol, protección del último administrador activo.
  - Creación del administrador inicial a partir de variables de entorno.
- Entidad base con identificador UUIDv7 y auditoría de fechas de creación y modificación.
- Respuesta paginada uniforme y validación de los campos de orden permitidos.

### Changed

- Los errores de autenticación, autorización y constraints de la base también responden con Problem Details.

### Added

- Documentación del producto, la arquitectura y las decisiones de diseño (ADR).
- Base técnica del proyecto (fase 0):
  - Configuración con virtual threads, Open Session in View desactivado y validación del esquema con Flyway.
  - PostgreSQL local con Docker Compose.
  - Manejo global de errores con Problem Details (RFC 9457).
  - Especificación OpenAPI y Swagger UI.
  - Actuator limitado a `health` e `info`.
  - Verificación de módulos con Spring Modulith.
  - Tests de integración con Testcontainers.
  - CI con GitHub Actions, formato automático con Spotless y cobertura con JaCoCo.
  - Dependabot, plantilla de pull request y EditorConfig.

[Unreleased]: https://github.com/paulaciano/streamstories/compare/v0.0.1...HEAD
[0.0.1]: https://github.com/paulaciano/streamstories/releases/tag/v0.0.1
