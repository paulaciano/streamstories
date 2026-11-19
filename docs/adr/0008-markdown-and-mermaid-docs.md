# ADR-0008: Documentación en Markdown con diagramas Mermaid

- **Estado:** Aceptado
- **Fecha:** 2026-11-16

## Contexto

La documentación tiene que poder leerse cómodamente en GitHub, versionarse junto con el código y editarse en cualquier editor.

## Decisión

La documentación se escribe en [Markdown](https://docs.github.com/get-started/writing-on-github/getting-started-with-writing-and-formatting-on-github/basic-writing-and-formatting-syntax) dentro de `docs/`, con diagramas [Mermaid](https://mermaid.js.org/) embebidos. GitHub [muestra ambos de forma nativa](https://docs.github.com/get-started/writing-on-github/working-with-advanced-formatting/creating-diagrams).

## Alternativas consideradas

- **HTML:** GitHub muestra el código fuente, no la página. Además, editarlo y revisar sus cambios es incómodo.
- **PDF o documentos de oficina:** son binarios, sus cambios no se pueden comparar y no se revisan en los PR.
- **Diagramas como imágenes:** se desactualizan y no se pueden revisar como texto.

## Consecuencias

- ✅ Se lee en GitHub sin herramientas adicionales y los cambios se revisan como texto.
- ⚠️ Mermaid ofrece menos control visual que una herramienta de diseño.
