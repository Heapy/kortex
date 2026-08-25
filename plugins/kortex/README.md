# kortex

Kotlin- and JVM-focused agent skills for JetBrains Kotlin Toolchain, Kotlin/JVM, Multiplatform,
JShell, and related development workflows.

## What it covers

- Kotlin Toolchain declarative project setup with `module.yaml`, `project.yaml`, templates, dependencies, plugins,
  and the `kotlin` CLI.
- Version-aware guidance for Kotlin Toolchain `v0.12.x`, with a retained `v0.11.x` snapshot and a 0.11 -> 0.12
  migration guide.
- Kotlin/JVM, Android, iOS, Kotlin Multiplatform, Kotlin/JS, Kotlin/Wasm, Kotlin/Native, server-side apps, testing,
  publishing, migration, and toolchain provisioning.
- Modern Kotlin language features (2.0–2.4.x, including 2.4.10 and the 2.4.20-Beta1 EAP) with experimental compiler
  flags and per-version references back to Kotlin 1.4.
- Ktor 3.5.x server and client development: routing, requests/responses, plugins, serialization, authentication,
  sessions, WebSockets, SSE, engines, testing, deployment, and multiplatform client work.
- Java snippets and scratchpad work with JShell.

## Files

- `skills/kotlin-toolchain/SKILL.md` - default Kotlin Toolchain skill for `v0.12.x`; operational base plus a map of
  topic references.
- `skills/kotlin-toolchain/references/` - per-topic detail: CLI, project model, product types, dependencies, settings,
  built-in technologies, multiplatform, templates, publishing, plugins, Maven migration, the 0.11 -> 0.12 upgrade,
  known issues, and Codex cache setup.
- `skills/kotlin-toolchain/SKILL-0.11.md` - historical skill for projects still pinned to `v0.11.x`.
- `skills/kotlin-toolchain/generation/` - retained upstream documentation dumps and regeneration notes.
- `skills/kotlin-toolchain/scripts/aggregate-upstream-docs.sh` - helper for rebuilding upstream documentation dumps.
- `skills/main-kts/SKILL.md` - executable Kotlin `.main.kts` scripts.
- `skills/modern-kotlin/SKILL.md` - modern Kotlin language features and experimental flags.
- `skills/modern-kotlin/references/` - detailed per-version notes (1.4 through 2.4.20-Beta1) and the flag index.
- `skills/ktor/SKILL.md` - Ktor 3.5.x server and client development.
- `skills/ktor/references/` - compressed server and client guides.
- `skills/ktor/generation/generation-steps.md` - regeneration notes for the Ktor skill.
- `skills/jshell/SKILL.md` - Java snippets and scratchpad work with JShell.

## Usage

Ask the agent to use a skill explicitly when needed:

```text
Use the kotlin-toolchain skill to create a setup for a Kotlin Native CLI application
Use the jshell skill to evaluate this Java snippet without leaving an interactive process behind
```
