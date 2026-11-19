# Changelog

Todos los cambios relevantes del proyecto se documentan en este archivo.

El formato sigue [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/) y el proyecto usa [versionado semántico](https://semver.org/lang/es/).

## [Unreleased]

## [0.3.0] - 2026-11-19

### Added

- Recomendaciones personalizadas (`GET /me/recommendations`), basadas en los géneros de los títulos que el usuario calificó y terminó, con el motivo de cada una. Sin actividad, se recomiendan los títulos mejor calificados por la comunidad. El contenido retirado nunca se recomienda.

## [0.2.0] - 2026-11-18

### Added

- Progreso de reproducción: `PUT /me/progress/movies/{id}` y `PUT /me/progress/episodes/{id}` guardan la posición exacta y marcan el contenido como visto al 90 %. El contenido retirado no acepta progreso nuevo.
- El contenido con progreso o puntuaciones ya no se puede borrar, solo retirar.
- Seguir viendo (`GET /me/continue-watching`): las películas sin terminar y una entrada por serie con el último episodio visto, mientras quede algo por ver.
- Historial de reproducción (`GET /me/history`), paginado e incluido el contenido retirado.
- Puntuaciones de 1 a 5 para películas y series (`/me/ratings`). El detalle de cada título muestra el promedio, la cantidad y la puntuación propia. Un título retirado conserva sus puntuaciones, pero no acepta nuevas.
- `ETag` en el detalle de películas y series, e `If-Match` opcional para cambiarlas sin pisar el cambio de otro administrador (`412` si la versión no coincide).
- La búsqueda por texto ignora los acentos: "orbita" encuentra "Órbita".
- Los refresh tokens vencidos se borran una vez por día.

### Changed

- La API responde en inglés: mensajes de error, títulos de los Problem Details, validaciones y la especificación OpenAPI. El contenido del catálogo no cambia.
- Los errores del framework, como `405` y `415`, también tienen un `type` de StreamStories (`method-not-allowed`, `unsupported-media-type`, `not-acceptable`).
- Un título de temporada en blanco se guarda como `null`, igual que una temporada sin título.
- Borrar una serie verifica la actividad de todos sus episodios en una sola consulta, no en una por episodio.

### Fixed

- Un número con decimales en un campo entero se truncaba (`4.5` se guardaba como `4`); ahora responde `400`.
- Una página muy lejana (por ejemplo, `?page=30000000&size=100`) respondía `500`; ahora responde `400`.
- Un `null` dentro de `genreIds` respondía `500`; ahora es un error de validación.
- Un texto en un campo numérico (`"score": "4"`) se convertía a número; ahora responde `400`.
- La base de datos ahora también limita los números de temporada y episodio a 999.

### Security

- La configuración del perfil `dev` (clave JWT y administrador de desarrollo) ya no entra en el jar.
- La aplicación no arranca si la contraseña del administrador inicial es débil o es el valor de ejemplo.
- PostgreSQL de Docker Compose solo escucha en `127.0.0.1`.
- Los errores reenviados por el contenedor ya no se convierten en `401` en las peticiones que no son `GET`.

## [0.1.0] - 2026-11-17

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
- Datos de ejemplo en el perfil `dev`: catálogo de prueba y administrador de desarrollo, que se crean solo si no existen.
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

[Unreleased]: https://github.com/paulaciano/streamstories/compare/v0.3.0...HEAD
[0.3.0]: https://github.com/paulaciano/streamstories/compare/v0.2.0...v0.3.0
[0.2.0]: https://github.com/paulaciano/streamstories/compare/v0.1.0...v0.2.0
[0.1.0]: https://github.com/paulaciano/streamstories/compare/v0.0.1...v0.1.0
[0.0.1]: https://github.com/paulaciano/streamstories/releases/tag/v0.0.1
