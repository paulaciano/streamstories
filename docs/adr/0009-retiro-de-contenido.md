# ADR-0009: El contenido se retira, no se borra

- **Estado:** Aceptado
- **Fecha:** 2026-11-16

## Contexto

El catálogo cambia: hay películas, series o episodios que dejan de ofrecerse. Pero tienen actividad asociada (progreso, historial, puntuaciones), y esa actividad es del usuario y alimenta los promedios y las recomendaciones.

## Decisión

El contenido tiene una **disponibilidad** (`availability`): `AVAILABLE` o `RETIRED`. Está en `media_item` (películas y series completas) y en `episode`.

El contenido retirado:
- no se puede reproducir: reportar progreso devuelve `409 content-unavailable`;
- no se recomienda y no aparece en los listados ni en las búsquedas del catálogo;
- no admite puntuaciones nuevas;
- sigue accesible por ID y aparece en el historial del usuario, marcado como retirado;
- conserva todos sus datos y su actividad asociada.

Un episodio es reproducible solo si tanto él como su serie están disponibles. El borrado físico solo se permite sobre contenido sin ninguna actividad, por ejemplo uno cargado por error.

## Alternativas consideradas

- **Borrado en cascada:** destruye la actividad de los usuarios.
- **Borrado lógico genérico (`deleted_at`) con filtros globales:** oculta el registro en todas partes, incluido el historial, y mezcla "retirado" con "eliminado".
- **Ventanas de disponibilidad con fechas:** es más flexible, pero excede las necesidades actuales. Puede incorporarse más adelante.

## Consecuencias

- ✅ No se pierde actividad de los usuarios.
- ✅ Un contenido retirado puede volver a ofrecerse.
- ⚠️ Las consultas del catálogo, "Seguir viendo" y las recomendaciones tienen que filtrar por disponibilidad. Se cubre con tests.
