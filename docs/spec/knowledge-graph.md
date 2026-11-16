---
type: Process
title: Code knowledge graph
stage: all
---

# Code knowledge graph (`docs/kb/`)

The repo has two OKF v0.1 bundles: [docs/spec](index.md) says what to build; [docs/kb](../kb/index.md) records what was built. `scripts/kb.py` builds the code graph from the Kotlin sources with no model tokens (the Graphify idea) and checks both bundles.

## Commands
- `python3 scripts/kb.py query <symbol|file|module>`: where it is, who uses it, what it uses (at most 40 lines).
- `python3 scripts/kb.py build`: rebuilds `docs/kb/graph.json`, `docs/kb/index.md` and the generated block of every module file; creates a file for each new module.
- `python3 scripts/kb.py check`: OKF rules for both bundles, broken links, stale graph, undocumented or deleted modules. Exit code 0 = OK.

## `docs/kb/` layout
- `index.md`: generated map (modules, hubs, entry points). Never edit by hand.
- `graph.json`: generated file and symbol graph. Never edit by hand.
- `modules/<module>.md`: one OKF concept per Kotlin package, `type: Module`. You write only `## Purpose` (1-3 lines) and `## Rules` (short bullets), and keep `spec:` in the frontmatter pointing at the spec files it implements. Everything between the `kb:generated` markers is rewritten by `build`.
- `log.md`: one short entry per stage: what changed in the code and why.

## Writing OKF files
Every Markdown file you add to `docs/spec/` or `docs/kb/`:
- starts with YAML frontmatter that has a non-empty `type` (only `index.md` and `log.md` have none);
- covers one concept;
- links related concepts with relative Markdown links, which are the graph's edges.

Record spec changes in [log](log.md).

## When
- Before searching code: `query` first ([token-budget](token-budget.md)).
- After adding, moving or deleting Kotlin files or public symbols: `build`.
- End of every stage: see [definition-of-done](definition-of-done.md).
