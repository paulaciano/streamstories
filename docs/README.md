# StreamStories documentation

StreamStories is a **REST API** for an on-demand streaming catalog: movies, series with seasons and episodes, genres, watch history, ratings and personalized recommendations.

This folder describes **what** the system does, **how** it is organized and **why** each design decision was made. The documentation lives next to the code: every significant change updates it in the same pull request.

## Contents

| # | Document | What it answers |
|---|----------|-----------------|
| 1 | [Vision and scope](01-vision-and-scope.md) | What problem does the system solve? What is in and out of scope? |
| 2 | [Architecture](02-architecture.md) | How is it organized? Which technologies does it use? |
| 3 | [Domain model and data](03-domain-model.md) | Which entities exist, how do they relate and which rules do they follow? |
| 4 | [API design](04-api.md) | Which endpoints does it expose, and with which conventions? |
| 5 | [Security](05-security.md) | How do users authenticate? Who can do what? |
| 6 | [Quality and workflow](06-quality-and-workflow.md) | How is the code tested, integrated and delivered? |
| 7 | [Roadmap](07-roadmap.md) | In which order is it built, and when is each phase done? |
| 8 | [AI-assisted development](08-ai-assisted-development.md) | How is AI used in development without losing quality or control? |
| — | [Architecture decision records (ADR)](adr/README.md) | Why X and not Y? |

For a quick read, **Vision and scope** and **Architecture** are enough. Before contributing code, read everything in order.

## Conventions

- **Language:** the documentation, the code and the API are in English. The README also has a [Spanish version](../README.es.md). Technical identifiers (entities, classes, endpoints, fields) are written `like this`, as in the code.
- **Format:** Markdown with [Mermaid](https://mermaid.js.org/) diagrams, which GitHub renders natively ([ADR-0008](adr/0008-markdown-and-mermaid-docs.md)).
- **Decisions:** every decision that is expensive to reverse is recorded as an [ADR](adr/README.md).
- **Keywords:** "must" marks a mandatory requirement, "should" a strong recommendation and "may" something optional.
