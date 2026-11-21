---
type: Policy
title: Token budget
description: "How to keep Claude's token use low."
tags: [token, budget, rules]
stage: all
---

# Token budget

Keep each session small: ask the graph first, then open only what the task touches. The graph is built by a script, so looking things up costs a few lines instead of whole files ([knowledge-graph](knowledge-graph.md)).

## Order of lookup (always)
1. `python3 scripts/kb.py query <symbol|file|module>`.
2. The module file in `docs/kb/modules/`: purpose, rules, spec links.
3. Only then read source files, and only those you will change. Use Grep/Glob only when the query finds nothing.

## Reading the spec
- Read [index](index.md), then only the files listed for the current stage. Follow a link only if the task needs it.
- Look at a mockup image only when building that screen.
- Don't re-read a file already read in this session unless it changed. Prefer a line range over a whole large file.

## Working
- Plan the stage's files once, then write them. Edit lines; don't rewrite whole files for small fixes.
- Build output: read the first error block only, fix it, rerun. Don't paste full Gradle logs into the reply.
- Keep the stage report short: lists, not prose.

## Optional: Graphify
Past ~100 source files, Graphify (github.com/safishamsi/graphify, installed with `graphify claude install`, then `/graphify .`) can add semantic links on top. `kb.py` stays the required build and check either way.
