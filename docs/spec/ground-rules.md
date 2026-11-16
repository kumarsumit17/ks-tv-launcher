---
type: Rules
title: Ground rules
stage: all
---

# Ground rules

- Inspect the repo first (Gradle files, version catalog, Kotlin/AGP, SDK levels, existing sources). Keep what works. If there is no Android project, create one with Gradle Kotlin DSL and `gradle/libs.versions.toml`.
- Use the latest **stable** versions that resolve on this machine. Never invent a library, artifact name or API. If you can't confirm one exists, use another approach.
- No TODOs, stubs or fake data in shipped code. Mockup values are illustrations; the app starts empty.
- Never claim a build or test passed unless the command finished with success. If something outside the code blocks you (missing SDK, no network), stop and say exactly what is missing.
- Follow [token-budget](token-budget.md).
