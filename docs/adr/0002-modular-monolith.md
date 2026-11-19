# ADR-0002: Monolito modular organizado por funcionalidad

- **Estado:** Aceptado
- **Fecha:** 2026-11-16

## Contexto

El dominio tiene áreas bien diferenciadas (identidad, catálogo, actividad y recomendaciones) que crecen por fases. El equipo es chico y no hay necesidad de escalar partes del sistema por separado. Se buscan límites claros entre áreas sin pagar el costo operativo de un sistema distribuido.

## Decisión

El sistema es **una sola aplicación Spring Boot** dividida en módulos por funcionalidad (`identity`, `catalog`, `activity`, `recommendation`, `shared`), cada uno con las subcapas `api`, `application` y `domain`. Los módulos se comunican solo a través de la API pública de cada uno, y [Spring Modulith](https://docs.spring.io/spring-modulith/reference/) verifica esas reglas en un test que corre en CI.

## Alternativas consideradas

- **Microservicios:** agregan red, consistencia eventual, múltiples despliegues y observabilidad distribuida, sin un problema que lo justifique.
- **Paquetes por capa** (`controllers/`, `services/`, `repositories/`): no impiden dependencias arbitrarias y se pierde la cohesión de cada funcionalidad.
- **Módulos Maven separados:** imponen límites más fuertes, pero complican el build. Spring Modulith logra límites verificables con menos fricción.

## Consecuencias

- ✅ Un único despliegue, una base de datos y transacciones locales simples.
- ✅ Los límites se verifican automáticamente.
- ✅ Un módulo puede extraerse como servicio más adelante sin rediseñar el dominio.
- ⚠️ Hay que diseñar con cuidado la API pública de cada módulo.
