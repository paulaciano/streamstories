# 4. Diseño de la API

La especificación completa y ejecutable se genera desde el código con [springdoc-openapi](https://springdoc.org/). Con la aplicación en marcha está disponible en:

- Swagger UI: `/swagger-ui.html`
- OpenAPI JSON: `/v3/api-docs`

Este documento define las **convenciones** y el **mapa de endpoints**.

## 4.1 Convenciones generales

| Tema | Convención |
|------|------------|
| Prefijo y versión | Todas las rutas van bajo `/api/v1`. Un cambio incompatible implica pasar a `/api/v2`. |
| Formato | JSON (`application/json`). Los errores usan `application/problem+json`. |
| Nombres de recursos | Sustantivos en plural y en *kebab-case*: `/movies`, `/continue-watching`. |
| Nombres de campos | *camelCase*: `releaseYear`, `durationSeconds`. |
| Fechas | [ISO 8601](https://www.iso.org/iso-8601-date-and-time-format.html) en UTC: `2026-11-16T14:30:00Z`. |
| Duraciones | Enteros en segundos, con el nombre explícito (`durationSeconds`). |
| Identificadores | UUID en formato string. |
| Recursos del usuario actual | Van bajo `/me`. El usuario nunca se pasa por parámetro: se obtiene del token. |
| Administración | La escritura del catálogo usa las mismas rutas del recurso, protegidas por rol `ADMIN`. La gestión de usuarios va bajo `/admin`. |

## 4.2 Métodos y códigos de estado

| Operación | Método | Éxito | Errores típicos |
|-----------|--------|-------|-----------------|
| Listar | `GET` | `200` | `400` filtros inválidos |
| Obtener | `GET` | `200` | `404` |
| Crear | `POST` | `201` + header `Location` | `400`, `409` duplicado |
| Reemplazar | `PUT` | `200` | `400`, `404`, `409` |
| Upsert idempotente | `PUT` | `200` | `400`, `404`, `409` |
| Modificación parcial | `PATCH` | `200` | `400`, `404`, `409` |
| Borrar | `DELETE` | `204` | `404`, `409` en uso |
| Acción asíncrona o pendiente de aprobación | `POST` | `202` | `400`, `401` |
| Sin token o token inválido | — | — | `401` |
| Rol insuficiente o cuenta inactiva | — | — | `403` |

## 4.3 Paginación, orden y filtros

Todos los listados están paginados.

```
GET /api/v1/catalog?genre=sci-fi&type=MOVIE&q=matrix&page=0&size=20&sort=title,asc
```

```json
{
  "content": [ { "...": "..." } ],
  "page": { "number": 0, "size": 20, "totalElements": 134, "totalPages": 7 }
}
```

- `size` vale 20 por defecto y como máximo 100: un valor mayor se reduce a 100.
- `sort` solo acepta los campos de una lista permitida.
- Los endpoints de catálogo aceptan el filtro `availability`, pero solo para `ADMIN`. Un `USER` siempre ve únicamente el contenido disponible.

## 4.4 Mapa de endpoints

Todas las rutas son relativas a `/api/v1`.
🔓 público · 👤 `USER` o `ADMIN` con cuenta activa · 🛠️ solo `ADMIN`

### Fase 1: autenticación y cuenta

| Método | Ruta | Acceso | Descripción |
|--------|------|:------:|-------------|
| `POST` | `/auth/register` | 🔓 | Crea una cuenta `USER` activa. |
| `POST` | `/auth/login` | 🔓 | Recibe email y contraseña y devuelve un access token y un refresh token. Si la cuenta está inactiva, responde `403` (`account-inactive`). |
| `POST` | `/auth/refresh` | 🔓 | Cambia un refresh token por un par nuevo (con rotación). |
| `POST` | `/auth/logout` | 👤 | Revoca el refresh token enviado. |
| `POST` | `/auth/reactivation-requests` | 🔓 | Con las credenciales de una cuenta inactiva, crea una solicitud de reactivación o devuelve la pendiente. Responde `202`, o `409` si la cuenta ya está activa. |
| `GET` | `/me` | 👤 | Datos del usuario actual. |
| `POST` | `/me/deactivate` | 👤 | Desactiva la propia cuenta y revoca sus tokens. Responde `204`. |
| `GET` | `/admin/users` | 🛠️ | Usuarios, filtrables por `status` y `role`. |
| `PATCH` | `/admin/users/{id}` | 🛠️ | Cambia el `status` o el `role` de un usuario. Si reactiva la cuenta, también aprueba la solicitud pendiente, si la hay. |
| `GET` | `/admin/reactivation-requests` | 🛠️ | Solicitudes de reactivación, filtrables por `status` (por defecto, `PENDING`). |
| `POST` | `/admin/reactivation-requests/{id}/approve` | 🛠️ | Aprueba la solicitud y reactiva la cuenta. |
| `POST` | `/admin/reactivation-requests/{id}/reject` | 🛠️ | Rechaza la solicitud. La cuenta sigue inactiva. |

### Fase 1: catálogo

| Método | Ruta | Acceso | Descripción |
|--------|------|:------:|-------------|
| `GET` | `/catalog` | 👤 | **Catálogo mixto** de películas y series. Filtros: `genre`, `type`, `q`. |
| `GET` | `/genres`, `/genres/{id}` | 👤 | Lista y detalle de géneros. |
| `POST` `PUT` `DELETE` | `/genres[/{id}]` | 🛠️ | ABM de géneros. El `slug` se genera a partir del nombre (`Ciencia Ficción` → `ciencia-ficcion`). |
| `GET` | `/movies`, `/movies/{id}` | 👤 | Listar y ver películas. |
| `POST` `PUT` | `/movies[/{id}]` | 🛠️ | Crear y modificar películas, incluidos sus `genreIds`. |
| `PATCH` | `/movies/{id}` | 🛠️ | Cambia la `availability` (retirar o volver a ofrecer). |
| `DELETE` | `/movies/{id}` | 🛠️ | Borra la película. Solo si no tiene actividad (C11). |
| `GET` | `/series`, `/series/{id}` | 👤 | Listar series y ver el detalle de una, con el resumen de sus temporadas. |
| `POST` `PUT` `PATCH` `DELETE` | `/series[/{id}]` | 🛠️ | ABM de series. `PATCH` cambia la `availability`. `DELETE` borra también temporadas y episodios, solo si nada tiene actividad. |
| `GET` | `/series/{id}/seasons/{number}` | 👤 | Una temporada con sus episodios. Una temporada retirada sigue accesible, indicando su `availability`. |
| `POST` | `/series/{id}/seasons` | 🛠️ | Agrega una temporada. |
| `PUT` `PATCH` `DELETE` | `/series/{id}/seasons/{number}` | 🛠️ | `PUT` modifica el título, `PATCH` cambia la `availability` (retira o vuelve a ofrecer la temporada completa) y `DELETE` la borra, solo si no tiene episodios. |
| `GET` | `/episodes/{id}` | 👤 | Detalle de un episodio, con `playable`: si su temporada y su serie están disponibles. |
| `POST` | `/series/{id}/seasons/{number}/episodes` | 🛠️ | Agrega un episodio. |
| `PUT` `DELETE` | `/episodes/{id}` | 🛠️ | Modifica o borra un episodio. Los episodios no se retiran sueltos: se retira la temporada. |

Para un `USER`, el contenido que no se puede reproducir (una película retirada, o un episodio de una temporada o serie retirada) se muestra con `videoUrl: null`, y el detalle de una serie omite las temporadas retiradas. Un `ADMIN` siempre ve todo.

### Fase 2: actividad

| Método | Ruta | Acceso | Descripción |
|--------|------|:------:|-------------|
| `PUT` | `/me/progress/movies/{movieId}` | 👤 | Reporta la posición de reproducción (upsert). Responde `409` si el contenido está retirado. |
| `PUT` | `/me/progress/episodes/{episodeId}` | 👤 | Lo mismo, para un episodio. |
| `GET` | `/me/continue-watching` | 👤 | Películas sin terminar y una entrada por serie con el último episodio visto, según las reglas A5 a A7. |
| `GET` | `/me/history` | 👤 | Historial completo y paginado: una fila por película o episodio visto, incluido el contenido retirado. |
| `PUT` | `/me/ratings/{mediaItemId}` | 👤 | Crea o reemplaza la puntuación de un título. |
| `DELETE` | `/me/ratings/{mediaItemId}` | 👤 | Quita la puntuación. |
| `GET` | `/me/ratings` | 👤 | Las puntuaciones del usuario. |

Seguir viendo y el historial van siempre de lo más reciente a lo más antiguo, e ignoran `sort`. Cada entrada es una película o una serie (`titleId`), y en las series incluye el episodio. `playable` en `false` marca el contenido retirado.

A partir de la fase 2, el detalle de un título (`GET /movies/{id}`, `GET /series/{id}`) incluye `averageRating` (con un decimal), `ratingCount` y la puntuación del usuario actual.

### Fase 3: recomendaciones

| Método | Ruta | Acceso | Descripción |
|--------|------|:------:|-------------|
| `GET` | `/me/recommendations` | 👤 | Títulos recomendados, cada uno con su `reason`. |

### Fase 4: evolución

| Método | Ruta | Acceso | Descripción |
|--------|------|:------:|-------------|
| `GET` `PUT` `DELETE` | `/me/watchlist[/{mediaItemId}]` | 👤 | Lista de seguimiento. |
| `GET` | `/franchises/{id}` | 👤 | Una saga con sus películas en orden. |
| `POST` `PUT` `DELETE` | `/franchises[/{id}]` | 🛠️ | ABM de sagas. |

## 4.5 Ejemplos

**Iniciar sesión**

```http
POST /api/v1/auth/login
Content-Type: application/json

{ "email": "ana@example.com", "password": "••••••••" }
```

```json
{
  "accessToken": "eyJ...",
  "refreshToken": "q3Jx…",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

Si la cuenta está inactiva y las credenciales son correctas:

```json
{
  "type": "urn:streamstories:problem:account-inactive",
  "title": "Cuenta inactiva",
  "status": 403,
  "detail": "La cuenta está inactiva. Solo un administrador puede reactivarla.",
  "reactivationRequestPending": false
}
```

**Crear una película (admin)**

```http
POST /api/v1/movies
Authorization: Bearer eyJ...
Content-Type: application/json

{
  "title": "Arrival",
  "synopsis": "A linguist works with the military to communicate with alien lifeforms.",
  "releaseYear": 2016,
  "durationSeconds": 6960,
  "videoUrl": "https://cdn.example.com/videos/arrival.m3u8",
  "coverImageUrl": "https://cdn.example.com/covers/arrival.jpg",
  "genreIds": ["4f1c…", "9a2e…"]
}
```

```http
HTTP/1.1 201 Created
Location: /api/v1/movies/0193a6f2-…
```

**Retirar una temporada completa (admin)**

```http
PATCH /api/v1/series/0193a6f2…/seasons/2
{ "availability": "RETIRED" }
```

**Reportar progreso**

```http
PUT /api/v1/me/progress/episodes/0193a7…
{ "positionSeconds": 1260 }
```

```json
{
  "episodeId": "0193a7…",
  "positionSeconds": 1260,
  "durationSeconds": 2700,
  "completed": false,
  "lastWatchedAt": "2026-11-16T21:04:11Z"
}
```

**Seguir viendo**

```http
GET /api/v1/me/continue-watching
```

```json
{
  "content": [
    {
      "type": "SERIES",
      "titleId": "0193a6f2…",
      "title": "Estación Austral",
      "coverImageUrl": "https://cdn.example.com/covers/estacion-austral.jpg",
      "episode": { "id": "0193a7…", "seasonNumber": 1, "number": 3, "title": "Deriva" },
      "positionSeconds": 1260,
      "durationSeconds": 2700,
      "completed": false,
      "playable": true,
      "lastWatchedAt": "2026-11-16T21:04:11Z"
    }
  ],
  "page": { "number": 0, "size": 20, "totalElements": 1, "totalPages": 1 }
}
```

**Puntuar un título**

```http
PUT /api/v1/me/ratings/0193a6f2…
{ "score": 4 }
```

## 4.6 Validación de entrada

- Los DTO de entrada son `record` con anotaciones de [Bean Validation](https://beanvalidation.org/): `@NotBlank`, `@Size`, `@Positive`, `@Min(1) @Max(5)`, etc.
- Los campos desconocidos en el JSON devuelven `400`, para que un error de tipeo no pase en silencio.
- Las URLs (`videoUrl`, `coverImageUrl`) deben usar `https://`.

## 4.7 Errores

Todos los errores siguen [Problem Details (RFC 9457)](https://www.rfc-editor.org/rfc/rfc9457), con el soporte nativo de [`ProblemDetail` en Spring](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-ann-rest-exceptions.html):

```json
{
  "type": "urn:streamstories:problem:validation-error",
  "title": "Datos inválidos",
  "status": 400,
  "detail": "La solicitud contiene 2 error(es) de validación.",
  "instance": "/api/v1/movies",
  "errors": [
    { "field": "title", "message": "no debe estar vacío" },
    { "field": "durationSeconds", "message": "debe ser mayor que 0" }
  ]
}
```

| `type` (sufijo) | Status | Cuándo |
|-----------------|:------:|--------|
| `validation-error` | 400 | El DTO no pasa la validación. |
| `malformed-request` | 400 | El JSON es ilegible o tiene un campo desconocido. |
| `unauthorized` | 401 | Falta el token, o es inválido o está vencido. Credenciales incorrectas. |
| `forbidden` | 403 | El rol no alcanza para la operación. |
| `account-inactive` | 403 | La cuenta está desactivada. La respuesta indica si hay una solicitud de reactivación pendiente. |
| `not-found` | 404 | El recurso no existe. |
| `conflict` | 409 | Duplicado (email, número de temporada…), recurso en uso, violación de una constraint de la base o modificación concurrente del mismo recurso. |
| `content-unavailable` | 409 | Se intenta reproducir o calificar contenido retirado. |
| `internal-error` | 500 | Error inesperado. Nunca se expone el stack trace. |

Los mensajes de error **no revelan información sensible**. Por ejemplo, un login fallido siempre responde "credenciales inválidas", sin aclarar si el email existe. El estado `account-inactive` solo se informa cuando las credenciales son correctas.

`content-unavailable` lo usan el progreso de reproducción y, desde que estén disponibles, las puntuaciones.
