# ADR-0004: El video se gestiona como metadato

- **Estado:** Aceptado
- **Fecha:** 2026-11-16

## Contexto

Distribuir video implica almacenarlo, transcodificarlo a formatos adaptativos ([HLS](https://datatracker.ietf.org/doc/html/rfc8216) o DASH), servirlo desde una CDN y protegerlo. Es un problema de infraestructura distinto del dominio de este sistema, que es el catálogo, los usuarios y su actividad.

## Decisión

Cada película y cada episodio guarda una `videoUrl` externa y su `durationSeconds`. La API **no sube, procesa ni sirve video**. El cliente reproduce desde la URL e informa la posición con `PUT /me/progress/...`.

## Alternativas consideradas

- **Servir los archivos desde la API con HTTP Range:** agrega almacenamiento e I/O pesado sin aportar al dominio.
- **Integrar almacenamiento de objetos con transcodificación:** es un sistema en sí mismo.

## Consecuencias

- ✅ El alcance se concentra en catálogo, usuarios y actividad.
- ✅ El progreso de reproducción se modela como en las plataformas reales: lo informa el cliente.
- ⚠️ La API confía en la posición que informa el cliente, y solo la valida contra la duración.
