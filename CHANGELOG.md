# Changelog

Todos los cambios relevantes del proyecto se documentan en este archivo.

El formato sigue [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/) y el proyecto usa [versionado semántico](https://semver.org/lang/es/).

## [Unreleased]

### Added

- Cuentas y autenticación (fase 1):
  - Registro, inicio de sesión con access token JWT y refresh token con rotación y detección de reutilización.
  - Desactivación de la propia cuenta, con efecto inmediato aunque el access token siga vigente.
  - Solicitudes de reactivación que resuelve un administrador.
  - Administración de usuarios: listado con filtros, cambio de estado y de rol, protección del último administrador activo.
  - Creación del administrador inicial a partir de variables de entorno.
- Catálogo (fase 1):
  - Géneros con slug generado a partir del nombre.
  - Películas y series con géneros, y series con temporadas y episodios numerados.
  - Catálogo mixto de películas y series con filtros por género, tipo y texto.
  - Retiro de contenido: lo retirado no aparece en los listados de los usuarios, pero sigue accesible por id y sin URL de video.
  - El borrado físico solo se permite sobre contenido sin actividad.
- Entidad base con identificador UUIDv7 y auditoría de fechas de creación y modificación.
- Respuesta paginada uniforme y validación de los campos de orden permitidos.

### Fixed

- Los números de temporada y episodio fuera de rango se truncaban al guardarse; ahora se validan (de 1 a 999).
- Reutilizar un refresh token vencido o revocado por logout cerraba todas las sesiones del usuario; ahora solo la reutilización de un token rotado se trata como robo.
- Aprobar y rechazar a la vez la misma solicitud de reactivación podía dejar datos inconsistentes; ahora se bloquea la solicitud.
- Dos pedidos simultáneos de reactivación devolvían `409`; ahora devuelven la misma solicitud pendiente.
- Cambiar solo los géneros de un título no actualizaba `updatedAt`.
- La comparación de entidades fallaba con proxies de Hibernate.
- Los valores inválidos rechazados por la base respondían `409` en lugar de `400`, y el log incluía el detalle de PostgreSQL.
- La paginación podía repetir u omitir elementos cuando había empates en el orden.
- El orden alfabético ponía los títulos acentuados al final ("Órbita" después de "Vecinos"); ahora se ordena como en español.

### Security

- La aplicación empaquetada ya no activa el perfil `dev` por defecto: sin `STREAMSTORIES_SECURITY_JWT_SECRET` no arranca, en lugar de usar la clave de desarrollo.

### Changed

- El contenido de una serie se retira por temporada completa, no por episodio (ADR-0011, que reemplaza a ADR-0009). `PATCH /series/{id}/seasons/{number}` cambia la disponibilidad de la temporada, y `PATCH /episodes/{id}` deja de existir.
- El catálogo tiene DTOs propios en la capa `api`, como identity.
- Edición concurrente de títulos con bloqueo optimista.
- Los mensajes de validación salen siempre en español.
- Login y reactivación verifican la contraseña fuera de la transacción, sin retener una conexión de la base.
- Los errores de autenticación, autorización y constraints de la base también responden con Problem Details.

## [0.0.1] - 2026-11-17

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
