# ADR-0008: Documentation in Markdown with Mermaid diagrams

- **Status:** Accepted
- **Date:** 2026-11-16

## Context

The documentation has to be comfortable to read on GitHub, versioned with the code and editable in any editor.

## Decision

The documentation is written in [Markdown](https://docs.github.com/get-started/writing-on-github/getting-started-with-writing-and-formatting-on-github/basic-writing-and-formatting-syntax) in `docs/`, with embedded [Mermaid](https://mermaid.js.org/) diagrams. GitHub [renders both natively](https://docs.github.com/get-started/writing-on-github/working-with-advanced-formatting/creating-diagrams).

## Alternatives considered

- **HTML:** GitHub shows the source, not the page. Editing it and reviewing its changes is also uncomfortable.
- **PDF or office documents:** they are binary, their changes cannot be compared, and they are not reviewed in PRs.
- **Diagrams as images:** they go out of date and cannot be reviewed as text.

## Consequences

- ✅ It reads on GitHub without extra tools, and changes are reviewed as text.
- ⚠️ Mermaid gives less visual control than a design tool.
