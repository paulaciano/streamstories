# ADR-0003: El sistema es exclusivamente una API REST

- **Estado:** Aceptado
- **Fecha:** 2026-11-16

## Contexto

Hay que definir si el sistema incluye una interfaz de usuario o si se limita al backend.

## Decisión

StreamStories es **solo el backend**: una API REST JSON documentada con [OpenAPI](https://www.openapis.org/) y explorable desde Swagger UI. No incluye interfaz gráfica. Si alguna vez hiciera falta un frontend, sería un proyecto independiente que consuma esta API.

## Alternativas consideradas

- **Renderizado en el servidor (Thymeleaf):** acopla la presentación al backend y no ofrece una API reutilizable por otros clientes.
- **API + frontend en el mismo repositorio:** amplía el alcance y mezcla dos ciclos de vida distintos.

## Consecuencias

- ✅ La API es el contrato del producto y puede consumirla cualquier cliente.
- ✅ Swagger UI permite probar todos los endpoints sin herramientas adicionales.
- ⚠️ Si se suma un frontend, habrá que configurar CORS.
