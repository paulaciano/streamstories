# ADR-0011: El contenido se retira por película, serie o temporada completa

- **Estado:** Aceptado
- **Fecha:** 2026-11-17
- **Reemplaza a:** [ADR-0009](0009-retiro-de-contenido.md)

## Contexto

El catálogo cambia: hay películas, series o temporadas que dejan de ofrecerse. Pero tienen actividad asociada (progreso, historial, puntuaciones), y esa actividad es del usuario y alimenta los promedios y las recomendaciones.

[ADR-0009](0009-retiro-de-contenido.md) permitía retirar películas, series y **episodios sueltos**. En la práctica, el contenido de una serie se deja de ofrecer por temporadas completas, igual que se licencia. Retirar episodios individuales deja temporadas con huecos, que el usuario no puede ver completas.

## Decisión

El contenido tiene una **disponibilidad** (`availability`): `AVAILABLE` o `RETIRED`. Está en:
- `media_item`, para películas y series completas;
- `season`, para temporadas completas.

Los episodios **no** tienen disponibilidad propia: un episodio es reproducible solo si su temporada y su serie están disponibles.

El contenido retirado:
- no se puede reproducir: reportar progreso devuelve `409 content-unavailable`;
- no se recomienda y no aparece en los listados, las búsquedas ni el detalle de una serie que ve un `USER`;
- no admite puntuaciones nuevas;
- sigue accesible por id (o por número, en el caso de una temporada) y aparece en el historial del usuario, marcado como retirado;
- conserva todos sus datos y su actividad asociada.

El borrado físico solo se permite sobre contenido sin ninguna actividad, por ejemplo uno cargado por error.

## Alternativas consideradas

- **Mantener el retiro por episodio (ADR-0009):** es más granular, pero deja temporadas incompletas y no corresponde a cómo se deja de ofrecer el contenido.
- **Permitir ambos niveles (temporada y episodio):** suma combinaciones que hay que filtrar y probar en "Seguir viendo" y en las recomendaciones, sin una necesidad real.
- **Borrado en cascada:** destruye la actividad de los usuarios.
- **Borrado lógico genérico (`deleted_at`) con filtros globales:** oculta el registro en todas partes, incluido el historial, y mezcla "retirado" con "eliminado".
- **Ventanas de disponibilidad con fechas:** es más flexible, pero excede las necesidades actuales. Puede incorporarse más adelante.

## Consecuencias

- ✅ No se pierde actividad de los usuarios.
- ✅ Un contenido retirado puede volver a ofrecerse.
- ✅ Una temporada nunca queda con episodios faltantes para el usuario.
- ⚠️ Las consultas del catálogo, "Seguir viendo" y las recomendaciones tienen que filtrar por la disponibilidad de la serie y de la temporada. Se cubre con tests.
- ⚠️ Si alguna vez hiciera falta retirar un episodio suelto, habría que volver a agregar la disponibilidad al episodio en un ADR nuevo.
