# ADR-0006: Esquema de base de datos versionado con Flyway

- **Estado:** Aceptado
- **Fecha:** 2026-11-16

## Contexto

El esquema evoluciona fase a fase. Si Hibernate lo genera (`ddl-auto=update`), el resultado no es reproducible, no es apto para producción y los cambios no aparecen en los PR.

## Decisión

- Los cambios de esquema son migraciones SQL de [Flyway](https://documentation.red-gate.com/flyway) en `src/main/resources/db/migration`.
- Hibernate corre con `ddl-auto=validate`: verifica que las entidades coincidan con el esquema, pero nunca lo modifica.
- **Una migración ya mergeada no se edita nunca.** Cualquier cambio se hace con una migración nueva.
- Las constraints (`CHECK`, `UNIQUE`, FK) se definen en la base, no solo en Java.

## Alternativas consideradas

- **[Liquibase](https://docs.liquibase.com/):** igual de válido. Flyway es más directo con SQL plano, que se lee y se revisa con facilidad.
- **`ddl-auto=update`:** se descarta por lo explicado en el contexto.

## Consecuencias

- ✅ El esquema es reproducible en cualquier entorno, incluidos los tests con Testcontainers.
- ✅ Los cambios de base se revisan como SQL legible.
- ⚠️ El SQL se escribe a mano. `validate` detecta al arrancar cualquier diferencia con las entidades.
