# Documentación de StreamStories

StreamStories es una **API REST** para una plataforma de catálogo audiovisual bajo demanda: películas, series con temporadas y episodios, géneros, historial de visualización, puntuaciones y recomendaciones personalizadas.

Esta carpeta describe **qué** hace el sistema, **cómo** está organizado y **por qué** se tomó cada decisión de diseño. La documentación se mantiene junto con el código: cada cambio importante la actualiza en el mismo pull request.

## Índice

| # | Documento | Qué responde |
|---|-----------|--------------|
| 1 | [Visión y alcance](01-vision-y-alcance.md) | ¿Qué problema resuelve el sistema? ¿Qué entra y qué no entra? |
| 2 | [Arquitectura](02-arquitectura.md) | ¿Cómo está organizado? ¿Qué tecnologías usa? |
| 3 | [Modelo de dominio y datos](03-modelo-de-dominio.md) | ¿Qué entidades existen, cómo se relacionan y qué reglas cumplen? |
| 4 | [Diseño de la API](04-api.md) | ¿Qué endpoints expone y con qué convenciones? |
| 5 | [Seguridad](05-seguridad.md) | ¿Cómo se autentican los usuarios? ¿Quién puede hacer qué? |
| 6 | [Calidad y flujo de trabajo](06-calidad-y-flujo-de-trabajo.md) | ¿Cómo se prueba, se integra y se entrega el código? |
| 7 | [Hoja de ruta](07-hoja-de-ruta.md) | ¿En qué orden se construye y cuándo se da por terminada cada fase? |
| 8 | [Desarrollo asistido por IA](08-desarrollo-con-ia.md) | ¿Cómo se usa la IA en el desarrollo sin perder calidad ni control? |
| — | [Registro de decisiones (ADR)](adr/README.md) | ¿Por qué se eligió X y no Y? |

Para una lectura rápida, alcanza con **Visión y alcance** y **Arquitectura**. Antes de contribuir con código conviene leer todo en orden.

## Convenciones

- **Idioma:** esta documentación está en español. La API y el código están en inglés, incluidos los mensajes de error y la especificación OpenAPI. Los identificadores técnicos (entidades, clases, endpoints, campos) se escriben `así`, igual que en el código.
- **Formato:** Markdown con diagramas [Mermaid](https://mermaid.js.org/), que GitHub muestra de forma nativa ([ADR-0008](adr/0008-documentacion-markdown-mermaid.md)).
- **Decisiones:** toda decisión que sea costosa de revertir se registra como un [ADR](adr/README.md).
- **Palabras clave:** "debe" indica un requisito obligatorio, "debería" una recomendación fuerte y "puede" algo opcional.
