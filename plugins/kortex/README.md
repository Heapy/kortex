# kortex

Junie extension that provides Kotlin-focused agent skills for JetBrains Kotlin Toolchain, Kotlin/JVM,
Multiplatform, and related build workflows.

## What it covers

- Kotlin Toolchain declarative project setup with `module.yaml`, `project.yaml`, templates, dependencies, plugins,
  and the `kotlin` CLI.
- Version-aware guidance for Kotlin Toolchain `v0.11.x`, with a separate upstream `main` snapshot.
- Kotlin/JVM, Android, iOS, Kotlin Multiplatform, Kotlin/JS, Kotlin/Wasm, Kotlin/Native, server-side apps, testing,
  publishing, migration, and toolchain provisioning.

## Files

- `skills/kotlin-toolchain/SKILL.md` - default Kotlin Toolchain skill for `v0.11.x`.
- `skills/kotlin-toolchain/SKILL-main.md` - current upstream `main` snapshot.
- `skills/kotlin-toolchain/generation/` - retained upstream documentation dumps and regeneration notes.
- `skills/kotlin-toolchain/scripts/aggregate-upstream-docs.sh` - helper for rebuilding upstream documentation dumps.

## Usage

Ask Junie to use the skill explicitly when needed:

```text
Use the kotlin-toolchain skill to create a setup for a Kotlin Native CLI application
```
