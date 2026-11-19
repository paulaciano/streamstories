# 1. Visión y alcance

## 1.1 Propósito

StreamStories es el backend de una plataforma de catálogo audiovisual bajo demanda. Permite explorar películas y series, registrar lo que cada persona mira, calificar títulos y recibir recomendaciones personalizadas.

## 1.2 Estrategia: construcción progresiva

El sistema se construye de forma iterativa. Empieza con un **MVP** y suma funcionalidad en fases (ver [Hoja de ruta](07-hoja-de-ruta.md)). Esta documentación describe la **visión completa**, para que la arquitectura del MVP soporte las fases siguientes sin reescribir la base.

Cada fase debe terminar con un sistema desplegable, probado y documentado.

## 1.3 Alcance

| ✅ Dentro del alcance | ❌ Fuera del alcance |
|----------------------|---------------------|
| API REST documentada con OpenAPI | Interfaz gráfica, web o móvil ([ADR-0003](adr/0003-api-rest-sin-frontend.md)) |
| Gestión del catálogo: películas, series, temporadas, episodios y géneros | Subir, almacenar, transcodificar o servir video ([ADR-0004](adr/0004-video-como-metadato.md)) |
| Retiro de contenido sin perder la actividad asociada | Pagos, planes y facturación de suscripciones |
| Registro e inicio de sesión, roles USER y ADMIN, desactivación de cuentas y reactivación aprobada por un admin | Perfiles múltiples por cuenta (una cuenta corresponde a un espectador) |
| Historial y progreso de reproducción ("Seguir viendo") | Notificaciones por email |
| Puntuaciones de películas y series | DRM, control parental y restricciones por región |
| Recomendaciones basadas en géneros y puntuaciones | Recomendaciones basadas en machine learning |
| Lista de seguimiento (`Watchlist`) y sagas (`Franchise`) | Funciones sociales: amigos, comentarios, compartir |

## 1.4 Actores

| Actor | Descripción | Rol técnico |
|-------|-------------|-------------|
| **Visitante** | Persona sin sesión. Puede registrarse, iniciar sesión o pedir la reactivación de su cuenta. | — (anónimo) |
| **Espectador** | Persona con cuenta activa. Explora el catálogo, mira contenido, califica y recibe recomendaciones. | `USER` |
| **Administrador** | Gestiona el catálogo, las cuentas y las solicitudes de reactivación. | `ADMIN` |
| **Cliente de la API** | Cualquier aplicación que consuma la API. | — |

## 1.5 Capacidades

### Catálogo (`Catalog`)

El catálogo reúne los títulos (`MediaItem`), que tienen dos formatos:

- **Película (`Movie`):** contenido de una sola entrega. Se gestiona y se reproduce como una unidad independiente.
- **Serie (`Series`):** contenido episódico con jerarquía:
  - **Serie (`Series`):** la obra completa, con título, sinopsis e imagen de portada.
  - **Temporada (`Season`):** agrupación numerada dentro de una serie.
  - **Episodio (`Episode`):** la unidad reproducible dentro de una temporada.

**Géneros (`Genre`):** cada película o serie puede tener **varios** géneros a la vez (por ejemplo, *Action* + *Sci-Fi*). Al explorar un género aparecen películas y series juntas.

**Sagas (`Franchise`):** agrupan películas relacionadas en un orden, por ejemplo una trilogía en el orden en que conviene verla. Una película pertenece como máximo a una saga. La saga solo ordena: borrarla no borra sus películas, y quien no es administrador no ve las películas retiradas dentro de ella.

**Retiro de contenido:** el contenido no se borra cuando deja de ofrecerse. Se **retira**: ya no se puede reproducir ni se recomienda, pero se conservan todos sus datos y la actividad de los usuarios asociada a él. Se retiran películas, series o temporadas completas ([ADR-0011](adr/0011-retiro-por-temporada.md)).

> **Dos conceptos de diseño.** El sistema distingue:
> - **Título** (`MediaItem`): lo que se *explora, clasifica y califica*. Es una película o una serie.
> - **Contenido reproducible** (`Playable`): lo que se *mira* y tiene duración y progreso. Es una película o un episodio.
>
> Una película cumple los dos roles. Una serie solo es título y un episodio solo es reproducible. Ver [Modelo de dominio](03-modelo-de-dominio.md).

### Usuarios y cuentas (`Identity`)

- Cada usuario tiene un **estado de cuenta** (`AccountStatus`): `ACTIVE` o `INACTIVE`.
- Tras varias contraseñas incorrectas seguidas, el inicio de sesión de ese email se bloquea por unos minutos, para frenar los intentos de adivinar contraseñas ([Seguridad](05-seguridad.md)).
- Una cuenta inactiva **no puede usar la plataforma**, pero **conserva íntegros** su historial y sus puntuaciones.
- La cuenta la puede desactivar su titular o un administrador. **Solo un administrador puede reactivarla.** El usuario puede pedir la reactivación con sus credenciales, y un admin la aprueba o la rechaza ([ADR-0010](adr/0010-reactivacion-aprobada-por-admin.md)).
- Cada usuario tiene un rol: `USER` o `ADMIN`.

### Actividad (`Activity`)

- **Progreso de reproducción:** por cada película o episodio que el usuario empezó, se guardan el punto exacto de reproducción, la fecha y si lo terminó. Un contenido se considera **visto** al llegar al 90 % de su duración.
- **Seguir viendo:** muestra las películas sin terminar, en el punto donde quedaron, y cada serie con el último episodio visto.
- **Puntuaciones:** el usuario califica películas y series con un entero de **1 a 5**. Cada título muestra además su promedio general, con decimales.
- **Lista de seguimiento:** el usuario guarda películas y series que quiere ver más adelante, como un "para ver después". Es una lista personal, independiente de lo que ya vio o calificó: guardar un título no lo marca como empezado ni cambia sus recomendaciones. Si un título guardado se retira, sigue en la lista marcado como no disponible.

### Recomendaciones (`Recommendation`)

Un motor que cruza el historial y las puntuaciones del usuario para detectar sus preferencias (géneros recurrentes, formato preferido) y sugerir títulos disponibles que todavía no vio.

## 1.6 Requisitos no funcionales

| Atributo | Objetivo |
|----------|----------|
| **Mantenibilidad** | Código organizado por funcionalidad, con límites entre módulos verificados automáticamente. |
| **Calidad** | Tests automatizados en todos los niveles y CI obligatoria en cada pull request. |
| **Seguridad** | Contraseñas con hash, tokens de vida corta, ningún secreto en el repositorio y autorización por rol en el servidor. |
| **Documentación** | API autodocumentada con OpenAPI, decisiones registradas en ADR y un README que permite levantar el proyecto en minutos. |
| **Reproducibilidad** | Esquema de base de datos versionado con migraciones y entorno local levantado con Docker Compose. |
| **Observabilidad** | Endpoint de salud y logs estructurados. |

## 1.7 Glosario

| Término | Identificador | Significado |
|---------|---------------|-------------|
| Título | `MediaItem` | Película o serie. Se explora, se clasifica por género y se califica. |
| Contenido reproducible | `Playable` | Película o episodio. Tiene duración y progreso. |
| Disponibilidad | `Availability` | `AVAILABLE` o `RETIRED`. El contenido retirado no se reproduce ni se recomienda. |
| Progreso de reproducción | `WatchProgress` | Hasta dónde vio un usuario un contenido reproducible, y cuándo. |
| Historial | `WatchHistory` | Los registros de progreso de un usuario, ordenados por fecha. |
| Puntuación | `Rating` | Calificación entera de 1 a 5 que un usuario da a un título. |
| Solicitud de reactivación | `ReactivationRequest` | Pedido de un usuario inactivo para volver a usar la plataforma. Lo resuelve un admin. |
| Lista de seguimiento | `Watchlist` | Títulos que el usuario guarda para ver después. |
| Saga | `Franchise` | Agrupación ordenada de películas relacionadas. |
