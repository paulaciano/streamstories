# ADR-0012: Rendimiento medido, sin caché por ahora

- **Estado:** Aceptado
- **Fecha:** 2026-11-19

## Contexto

La hoja de ruta pedía revisar los índices con `EXPLAIN ANALYZE` y cachear las recomendaciones y el catálogo si los datos lo justificaban. Para decidirlo con números, se cargó una base PostgreSQL 18 con un volumen generado:

| Datos | Cantidad |
|-------|---------:|
| Títulos (5.000 películas y 1.000 series) | 6.000 |
| Episodios | 30.000 |
| Usuarios | 20.000 |
| Registros de progreso | 700.000 |
| Puntuaciones | 300.000 |

Se midieron los endpoints con la aplicación empaquetada (mediana de varias llamadas, en una sola máquina) y las consultas con `EXPLAIN ANALYZE`.

## Mediciones

| Endpoint | Mediana |
|----------|--------:|
| `GET /catalog` | 31 ms |
| `GET /catalog?q=…` | 42 ms |
| `GET /catalog?genre=…` | 26 ms |
| `GET /me/history` | 37 ms |
| `GET /me/continue-watching` | 42 ms |
| `GET /me/ratings` | 17 ms |
| `GET /me/recommendations` | 386 ms → **146 ms** |

- Todas las consultas por usuario (historial, "Seguir viendo", puntuaciones, señales para las recomendaciones) y por título (promedio, actividad para C11) usan su índice y tardan menos de 1 ms en la base.
- La búsqueda por texto recorre la tabla de títulos (`unaccent` + `LIKE '%…%'`): 9 ms con 6.000 títulos. Un índice trigram no se justifica a este volumen.
- Las recomendaciones leen todo el catálogo y todas las puntuaciones en cada consulta. El costo no estaba en SQL (7 ms y 34 ms) sino en Java: Hibernate armaba 6.000 entidades y cargaba los géneros en unas 120 consultas, y las proyecciones de Spring Data creaban un proxy por fila. Se reemplazaron por dos consultas que devuelven filas simples.

## Decisión

- **No se agrega caché.** Con este volumen, ningún endpoint supera los 150 ms, y un caché sumaría invalidación (retiros, puntuaciones nuevas) y estado por instancia sin una necesidad medida.
- **No se agregan índices**: los existentes cubren todas las consultas.
- Las lecturas masivas (todo el catálogo, todos los promedios) usan consultas de filas simples, sin entidades ni proyecciones.

## Alternativas consideradas

- **Caché en memoria del catálogo y de los promedios:** bajaría las recomendaciones a unos 30 ms, pero cada instancia tendría su copia y habría que invalidarla al retirar contenido o al puntuar.
- **Tabla de promedios precalculados:** evita agregar todas las puntuaciones en cada consulta, a cambio de mantenerla actualizada en cada puntuación.
- **Índice trigram (`pg_trgm`) para la búsqueda:** necesita una función `unaccent` inmutable propia y no cambia nada a este volumen.

## Consecuencias

- ✅ Sin estado extra ni invalidaciones: todo sale siempre de la base.
- ⚠️ Las recomendaciones crecen con el tamaño del catálogo y la cantidad de puntuaciones. Si el catálogo o las puntuaciones crecen un orden de magnitud, conviene retomar la tabla de promedios precalculados o un caché con vencimiento corto.
