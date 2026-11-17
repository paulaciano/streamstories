# Changelog

Todos los cambios relevantes del proyecto se documentan en este archivo.

El formato sigue [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/) y el proyecto usa [versionado semántico](https://semver.org/lang/es/).

## [Unreleased]

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
