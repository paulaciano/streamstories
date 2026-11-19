# 4. API design

The full, executable specification is generated from the code with [springdoc-openapi](https://springdoc.org/). With the application running, it is available at:

- Swagger UI: `/swagger-ui.html`
- OpenAPI JSON: `/v3/api-docs`

This document defines the **conventions** and the **endpoint map**.

## 4.1 General conventions

| Topic | Convention |
|-------|------------|
| Prefix and version | Every route is under `/api/v1`. An incompatible change means moving to `/api/v2`. |
| Format | JSON (`application/json`). Errors use `application/problem+json`. |
| Resource names | Plural nouns in *kebab-case*: `/movies`, `/continue-watching`. |
| Field names | *camelCase*: `releaseYear`, `durationSeconds`. |
| Dates | [ISO 8601](https://www.iso.org/iso-8601-date-and-time-format.html) in UTC: `2026-11-16T14:30:00Z`. |
| Durations | Integers in seconds, with an explicit name (`durationSeconds`). |
| Identifiers | UUIDs as strings. |
| Current user's resources | Under `/me`. The user is never passed as a parameter: it comes from the token. |
| Administration | Catalog writes use the resource's own routes, protected by the `ADMIN` role. User management is under `/admin`. |

## 4.2 Methods and status codes

| Operation | Method | Success | Typical errors |
|-----------|--------|---------|----------------|
| List | `GET` | `200` | `400` invalid filters |
| Get | `GET` | `200` | `404` |
| Create | `POST` | `201` + `Location` header | `400`, `409` duplicate |
| Replace | `PUT` | `200` | `400`, `404`, `409` |
| Idempotent upsert | `PUT` | `200` | `400`, `404`, `409` |
| Partial change | `PATCH` | `200` | `400`, `404`, `409` |
| Delete | `DELETE` | `204` | `404`, `409` in use |
| Asynchronous action, or one waiting for approval | `POST` | `202` | `400`, `401` |
| Missing or invalid token | — | — | `401` |
| Insufficient role or inactive account | — | — | `403` |

## 4.3 Pagination, sorting and filters

Every listing is paginated.

```
GET /api/v1/catalog?genre=sci-fi&type=MOVIE&q=matrix&page=0&size=20&sort=title,asc
```

```json
{
  "content": [ { "...": "..." } ],
  "page": { "number": 0, "size": 20, "totalElements": 134, "totalPages": 7 }
}
```

- `size` defaults to 20 and is at most 100: a larger value is reduced to 100.
- A page that starts beyond the largest offset the database supports (`page × size` greater than 2,147,483,647) answers `400`.
- `sort` only accepts the fields of an allowed list.
- Catalog endpoints accept the `availability` filter, but only for `ADMIN`. A `USER` always sees available content only.

### Concurrent changes to titles

The detail of a movie or a series, and the responses of its `POST`, `PUT` and `PATCH`, include an `ETag` header with the title's version. If the client sends it back in `If-Match` when changing the title, the change only applies if nobody modified it since it was read; otherwise the answer is `412` (`precondition-failed`). Without `If-Match`, the change is not conditional.

The version covers the title's data, not its seasons or episodes: adding an episode does not change the series' `ETag`.

## 4.4 Endpoint map

Every route is relative to `/api/v1`.
🔓 public · 👤 `USER` or `ADMIN` with an active account · 🛠️ `ADMIN` only

### Phase 1: authentication and account

| Method | Route | Access | Description |
|--------|-------|:------:|-------------|
| `POST` | `/auth/register` | 🔓 | Creates an active `USER` account. |
| `POST` | `/auth/login` | 🔓 | Takes an email and a password and returns an access token and a refresh token. If the account is inactive, it answers `403` (`account-inactive`). After 5 wrong passwords in 15 minutes, `429` (`too-many-requests`). |
| `POST` | `/auth/refresh` | 🔓 | Exchanges a refresh token for a new pair (with rotation). |
| `POST` | `/auth/logout` | 👤 | Revokes the given refresh token. |
| `POST` | `/auth/reactivation-requests` | 🔓 | With the credentials of an inactive account, creates a reactivation request or returns the pending one. Answers `202`, or `409` if the account is already active. Shares the login attempt limit. |
| `GET` | `/me` | 👤 | The current user's data. |
| `POST` | `/me/deactivate` | 👤 | Deactivates your own account and revokes its tokens. Answers `204`. |
| `GET` | `/admin/users` | 🛠️ | Users, filterable by `status` and `role`. |
| `PATCH` | `/admin/users/{id}` | 🛠️ | Changes a user's `status` or `role`. Reactivating the account also approves its pending request, if there is one. |
| `GET` | `/admin/reactivation-requests` | 🛠️ | Reactivation requests, filterable by `status` (`PENDING` by default). |
| `POST` | `/admin/reactivation-requests/{id}/approve` | 🛠️ | Approves the request and reactivates the account. |
| `POST` | `/admin/reactivation-requests/{id}/reject` | 🛠️ | Rejects the request. The account stays inactive. |

### Phase 1: catalog

| Method | Route | Access | Description |
|--------|-------|:------:|-------------|
| `GET` | `/catalog` | 👤 | **Mixed catalog** of movies and series. Filters: `genre`, `type`, `q` (searches the title, ignoring case and accents). |
| `GET` | `/genres`, `/genres/{id}` | 👤 | List and detail of genres. |
| `POST` `PUT` `DELETE` | `/genres[/{id}]` | 🛠️ | Genre management. The `slug` is derived from the name (`Ciencia Ficción` → `ciencia-ficcion`). |
| `GET` | `/movies`, `/movies/{id}` | 👤 | List and view movies. |
| `POST` `PUT` | `/movies[/{id}]` | 🛠️ | Create and change movies, including their `genreIds`. |
| `PATCH` | `/movies/{id}` | 🛠️ | Changes the `availability` (retire, or offer again). |
| `DELETE` | `/movies/{id}` | 🛠️ | Deletes the movie, only if it has no activity (C11). |
| `GET` | `/series`, `/series/{id}` | 👤 | List series and view the detail of one, with a summary of its seasons. |
| `POST` `PUT` `PATCH` `DELETE` | `/series[/{id}]` | 🛠️ | Series management. `PATCH` changes the `availability`. `DELETE` also deletes seasons and episodes, only if none of them has activity. |
| `GET` | `/series/{id}/seasons/{number}` | 👤 | A season with its episodes. A retired season is still reachable, showing its `availability`. |
| `POST` | `/series/{id}/seasons` | 🛠️ | Adds a season. |
| `PUT` `PATCH` `DELETE` | `/series/{id}/seasons/{number}` | 🛠️ | `PUT` changes the title, `PATCH` changes the `availability` (retires or offers again the whole season) and `DELETE` deletes it, only if it has no episodes. |
| `GET` | `/episodes/{id}` | 👤 | Episode detail, with `playable`: whether its season and its series are available. |
| `POST` | `/series/{id}/seasons/{number}/episodes` | 🛠️ | Adds an episode. |
| `PUT` `DELETE` | `/episodes/{id}` | 🛠️ | Changes or deletes an episode. Episodes are not retired one by one: the season is. |

For a `USER`, content that cannot be played (a retired movie, or an episode of a retired season or series) has `videoUrl: null`, and a series detail leaves out its retired seasons. An `ADMIN` always sees everything.

### Phase 2: activity

| Method | Route | Access | Description |
|--------|-------|:------:|-------------|
| `PUT` | `/me/progress/movies/{movieId}` | 👤 | Reports the playback position (upsert). Answers `409` if the content is retired. |
| `PUT` | `/me/progress/episodes/{episodeId}` | 👤 | The same, for an episode. |
| `GET` | `/me/continue-watching` | 👤 | Unfinished movies and one entry per series with its last watched episode, following rules A5 to A7. |
| `GET` | `/me/history` | 👤 | The full, paginated history: one row per movie or episode watched, including retired content. |
| `PUT` | `/me/ratings/{titleId}` | 👤 | Creates or replaces the rating of a movie or a series. Answers `409` if the title is retired. |
| `DELETE` | `/me/ratings/{titleId}` | 👤 | Removes the rating, even if the title is retired. Answers `204` even if there was none. |
| `GET` | `/me/ratings` | 👤 | The user's ratings, most recent first. A retired title has `available: false`. |

Continue watching, the history, the ratings and the watchlist always go from the most recent, and ignore `sort`. Recommendations ignore `sort` too: they follow the algorithm's order. Each entry is a movie or a series (`titleId`), and series entries include the episode. `playable: false` marks retired content.

The detail of a title (`GET /movies/{id}`, `GET /series/{id}`) includes `ratings`: the average with one decimal (`average`, `null` if nobody rated it), the number of ratings (`count`) and the current user's score (`mine`, `null` if they did not rate it). Listings do not include it.

### Phase 3: recommendations

| Method | Route | Access | Description |
|--------|-------|:------:|-------------|
| `GET` | `/me/recommendations` | 👤 | Recommended titles, each with its `reason`, following the [algorithm](03-domain-model.md#35-recommendation-engine-phase-3). Paginated, with a fixed order. |

### Phase 4: evolution

| Method | Route | Access | Description |
|--------|-------|:------:|-------------|
| `GET` | `/me/watchlist` | 👤 | The watchlist, most recently added first. A retired title has `available: false`. |
| `PUT` | `/me/watchlist/{titleId}` | 👤 | Adds a movie or a series. Answers `204` even if it was already there, and `409` if the title is retired. |
| `DELETE` | `/me/watchlist/{titleId}` | 👤 | Removes a title, even if it is retired. Answers `204` even if it was not there. |
| `GET` | `/franchises` | 👤 | List of franchises, paginated and sortable by `name`. |
| `GET` | `/franchises/{id}` | 👤 | A franchise with its movies in order. A `USER` does not see the retired ones. |
| `POST` | `/franchises` | 🛠️ | Creates a franchise with a `name` and `movieIds`: the order of the list is the order of the franchise. `409` if the name already exists or a movie is already in another franchise. |
| `PUT` | `/franchises/{id}` | 🛠️ | Replaces the name and the whole list. Movies that are not in the new list leave the franchise. |
| `DELETE` | `/franchises/{id}` | 🛠️ | Deletes the franchise. Its movies stay in the catalog. |

## 4.5 Examples

**Log in**

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

If the account is inactive and the credentials are correct:

```json
{
  "type": "urn:streamstories:problem:account-inactive",
  "title": "Inactive account",
  "status": 403,
  "detail": "The account is inactive. Only an administrator can reactivate it.",
  "reactivationRequestPending": false
}
```

**Create a movie (admin)**

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

**Retire a whole season (admin)**

```http
PATCH /api/v1/series/0193a6f2…/seasons/2
{ "availability": "RETIRED" }
```

**Report progress**

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

**Continue watching**

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

**Recommendations**

```http
GET /api/v1/me/recommendations
```

```json
{
  "content": [
    {
      "titleId": "0193a6f2…",
      "type": "SERIES",
      "title": "Estación Austral",
      "coverImageUrl": "https://cdn.example.com/covers/estacion-austral.jpg",
      "reason": "Because you like Ciencia ficción and Drama"
    }
  ],
  "page": { "number": 0, "size": 20, "totalElements": 1, "totalPages": 1 }
}
```

**Rate a title**

```http
PUT /api/v1/me/ratings/0193a6f2…
{ "score": 4 }
```

```json
{
  "titleId": "0193a6f2…",
  "type": "SERIES",
  "title": "Estación Austral",
  "coverImageUrl": "https://cdn.example.com/covers/estacion-austral.jpg",
  "available": true,
  "score": 4,
  "ratedAt": "2026-11-16T21:10:42Z"
}
```

In the series detail:

```json
"ratings": { "average": 4.3, "count": 12, "mine": 4 }
```

## 4.6 Input validation

- Input DTOs are `record`s with [Bean Validation](https://beanvalidation.org/) annotations: `@NotBlank`, `@Size`, `@Positive`, `@Min(1) @Max(5)`, and so on.
- Unknown fields in the JSON answer `400`, so a typo does not go unnoticed.
- A number with decimals in an integer field (for example, `"score": 4.5`) or text in a numeric field (`"score": "4"`) answers `400`, instead of being converted.
- A blank season title is stored as "no title" (`null`).
- URLs (`videoUrl`, `coverImageUrl`) must use `https://`.

## 4.7 Errors

Every error follows [Problem Details (RFC 9457)](https://www.rfc-editor.org/rfc/rfc9457), with the native support of [`ProblemDetail` in Spring](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-ann-rest-exceptions.html):

```json
{
  "type": "urn:streamstories:problem:validation-error",
  "title": "Invalid data",
  "status": 400,
  "detail": "The request has 2 validation error(s).",
  "instance": "/api/v1/movies",
  "errors": [
    { "field": "title", "message": "must not be blank" },
    { "field": "durationSeconds", "message": "must be greater than 0" }
  ]
}
```

| `type` (suffix) | Status | When |
|-----------------|:------:|------|
| `validation-error` | 400 | The DTO fails validation. |
| `malformed-request` | 400 | The JSON is unreadable or has an unknown field. |
| `unauthorized` | 401 | The token is missing, invalid or expired. Wrong credentials. |
| `forbidden` | 403 | The role is not enough for the operation. |
| `account-inactive` | 403 | The account is deactivated. The response tells whether a reactivation request is pending. |
| `not-found` | 404 | The resource does not exist. |
| `method-not-allowed` | 405 | The endpoint exists, but does not accept that HTTP method. |
| `not-acceptable` | 406 | The client asks in `Accept` for a format the API does not offer. |
| `conflict` | 409 | A duplicate (email, season number…), a resource in use, a database constraint violation or a concurrent change to the same resource. |
| `content-unavailable` | 409 | Trying to play, rate or add to the watchlist retired content. |
| `precondition-failed` | 412 | The `If-Match` does not match the title's current version. |
| `unsupported-media-type` | 415 | The body is not sent as `application/json`. |
| `too-many-requests` | 429 | Too many wrong passwords for the email. `Retry-After` tells how many seconds are left. |
| `internal-error` | 500 | An unexpected error. The stack trace is never exposed. |

The API speaks English: `title`, `detail` and validation messages are always in English, whatever `Accept-Language` the client sends. Catalog content (titles, synopses, genres) is returned as it was loaded.

Error messages **do not reveal sensitive information**. For example, a failed login always answers "Invalid credentials.", without telling whether the email exists. The `account-inactive` status is only reported when the credentials are correct.
