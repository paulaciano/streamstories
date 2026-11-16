# ADR-0007: Herencia `JOINED` para títulos y FK excluyentes para el progreso

- **Estado:** Aceptado
- **Fecha:** 2026-11-16

## Contexto

Películas y series comparten atributos (título, sinopsis, géneros, puntuaciones) y el catálogo tiene que poder listarlas **mezcladas**. Por otro lado, el progreso de reproducción se registra sobre películas **o** episodios, que son entidades distintas.

## Decisión

1. `MediaItem` es una entidad abstracta con [herencia `JOINED`](https://docs.jboss.org/hibernate/orm/current/userguide/html_single/Hibernate_User_Guide.html#entity-inheritance-joined-table), con las subclases `Movie` y `Series` y una columna discriminadora `type`. Los géneros y las puntuaciones apuntan a `media_item`.
2. `watch_progress` tiene dos FK que admiten nulos (`movie_id` y `episode_id`), con `CHECK ((movie_id IS NOT NULL) <> (episode_id IS NOT NULL))` y un índice único parcial para cada una junto con `user_id`.

## Alternativas consideradas

- **`SINGLE_TABLE`:** las consultas son más rápidas, pero las columnas propias de cada subtipo tienen que admitir nulos y se pierden las restricciones `NOT NULL`.
- **`TABLE_PER_CLASS`:** el catálogo mixto requiere `UNION` y no se puede tener una FK hacia "cualquier título".
- **Referencia polimórfica (`content_type` + `content_id`):** no admite FK, así que la base no puede garantizar la integridad.
- **Tablas de progreso separadas:** duplican lógica y complican "Seguir viendo", que necesita combinar películas y episodios.

## Consecuencias

- ✅ La base garantiza la integridad referencial.
- ✅ El catálogo mixto y el filtro por género salen de una sola consulta.
- ⚠️ `JOINED` necesita un `JOIN` para cargar los datos de cada subtipo, un costo aceptable para este volumen.
- ⚠️ El invariante del progreso también tiene que garantizarse en la entidad Java.
