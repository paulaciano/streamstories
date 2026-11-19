# ADR-0001: Registrar las decisiones de arquitectura con ADR

- **Estado:** Aceptado
- **Fecha:** 2026-11-16

## Contexto

El código muestra *qué* se construyó, pero no *por qué*. Sin un registro, el razonamiento detrás de cada decisión se pierde y las mismas discusiones vuelven a aparecer. Esto es especialmente importante en un proyecto que se desarrolla con asistencia de IA, porque el asistente necesita conocer las decisiones vigentes para no contradecirlas.

## Decisión

Cada decisión de arquitectura significativa se registra como un ADR en `docs/adr/`, con un formato breve (contexto, decisión, alternativas y consecuencias) basado en el [propuesto por Michael Nygard](https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions).

## Alternativas consideradas

- **Solo comentarios en los PR:** quedan dispersos y son difíciles de encontrar.
- **Wiki de GitHub:** está separada del código y no se versiona junto con él.

## Consecuencias

- ✅ Las decisiones se revisan en el mismo PR que el código que las implementa.
- ✅ El contexto queda disponible para quien se sume al proyecto.
- ⚠️ Escribir el ADR agrega un pequeño costo a cada decisión importante.
