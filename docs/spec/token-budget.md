---
type: Rules
title: Token budget
stage: all
---

# Token budget

Rules that keep each session small. They follow the Graphify idea: consult a map first, then open only what the task touches.

## Reading
- Read [index](index.md), then only the files listed for the current stage. Follow a link to another concept only if the task needs it.
- Look at a mockup image only when building that screen. Don't re-open it to check small details already written in the spec.
- Read [code-map](code-map.md) before any Glob/Grep. Open a source file only to change it, or when the map can't answer the question.
- Don't re-read a file already read in this session unless it changed. Prefer reading a line range over a whole large file.

## Working
- Plan the stage's files once, then write them. Don't rewrite whole files for small fixes; edit the lines.
- Build output: read the first error block only, fix it, rerun. Don't paste full Gradle logs into the reply.
- Keep the stage report short: lists, not prose.

## Code map upkeep
At the end of every stage, update [code-map](code-map.md): one line per source file with its role and key classes/composables, grouped by package. Keep it under ~150 lines. This is the cheap version of a code knowledge graph.

## Optional: Graphify
Once the code passes ~100 files, a code knowledge graph pays off. If the user has installed Graphify (github.com/safishamsi/graphify) with its Claude Code hook (`graphify claude install`), run `/graphify .` after each stage and query the graph for cross-file questions before searching. On a small repo, or for an edit to a file you already know, skip it; the code map is enough.
