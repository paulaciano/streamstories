# Registro de decisiones de arquitectura (ADR)

Un **ADR** ([*Architecture Decision Record*](https://adr.github.io/)) documenta una decisión importante: el contexto en que se tomó, la decisión, las alternativas que se descartaron y sus consecuencias.

**Cuándo se escribe uno:** cuando la decisión es costosa de revertir, afecta a varios módulos o una persona razonable podría haber elegido otra opción.

**Reglas:**
- Cada ADR tiene un número que no se reutiliza.
- Un ADR aceptado **no se edita** en lo sustancial. Si la decisión cambia, se escribe uno nuevo que lo reemplaza, y el anterior pasa a estado *Reemplazado por ADR-XXXX*.
- Los nuevos ADR parten de la [plantilla](plantilla.md).

| ADR | Título | Estado |
|-----|--------|--------|
| [0001](0001-registrar-decisiones-con-adr.md) | Registrar las decisiones de arquitectura con ADR | Aceptado |
| [0002](0002-monolito-modular.md) | Monolito modular organizado por funcionalidad | Aceptado |
| [0003](0003-api-rest-sin-frontend.md) | El sistema es exclusivamente una API REST | Aceptado |
| [0004](0004-video-como-metadato.md) | El video se gestiona como metadato | Aceptado |
| [0005](0005-autenticacion-jwt.md) | Autenticación stateless con JWT y refresh tokens rotativos | Aceptado |
| [0006](0006-migraciones-flyway.md) | Esquema de base de datos versionado con Flyway | Aceptado |
| [0007](0007-modelo-herencia-y-progreso.md) | Herencia `JOINED` para títulos y FK excluyentes para el progreso | Aceptado |
| [0008](0008-documentacion-markdown-mermaid.md) | Documentación en Markdown con diagramas Mermaid | Aceptado |
| [0009](0009-retiro-de-contenido.md) | El contenido se retira, no se borra | Aceptado |
| [0010](0010-reactivacion-aprobada-por-admin.md) | La reactivación de cuentas requiere aprobación de un admin | Aceptado |
