# Kotlin Toolchain skill

This directory contains the version-aware Agent Skill for
[JetBrains Kotlin Toolchain](https://github.com/JetBrains/kotlin-toolchain), formerly Amper. It covers the declarative
YAML project model, the `kotlin` CLI, supported product types, dependencies, multiplatform projects, publishing, and
local build plugins.

The default skill is based on Kotlin Toolchain `v0.12.0` at
`2039c5371bf5812f0061b2b11b6581b4e9de3a97`. Projects using Kotlin Toolchain `v0.11.x` should use
[`SKILL-0.11.md`](SKILL-0.11.md).

## Installation

The skill is distributed as part of the `kortex` plugin. Add the `Heapy/kortex` marketplace, then install the plugin
for your host.

### Codex

```shell
codex plugin marketplace add Heapy/kortex
codex plugin add kortex@kortex
```

### Claude Code

```text
/plugin marketplace add Heapy/kortex
/plugin install kortex@kortex
```

### Junie

```text
/extensions marketplace add Heapy/kortex
/extensions install kortex
```

## Usage

After installation, invoke the skill using the syntax for your host:

| Host | Example |
|---|---|
| Codex | `$kortex:kotlin-toolchain add a Kotlin Multiplatform library module` |
| Claude Code | `/kortex:kotlin-toolchain add a Kotlin Multiplatform library module` |
| Junie | `Use the kotlin-toolchain skill to add a Kotlin Multiplatform library module` |

The agent should first read the version from the project's `kotlin` wrapper. The current entry point explains how to
handle projects whose pinned version differs from the default snapshot.

## Directory map

| Path | Purpose |
|---|---|
| [`SKILL.md`](SKILL.md) | Current entry point and operational guidance for `v0.12.x` |
| [`SKILL-0.11.md`](SKILL-0.11.md) | Historical guidance for projects pinned to `v0.11.x` |
| [`references/`](references/) | Detailed topic guides loaded only when a task needs them |
| [`generation/`](generation/) | Pinned upstream documentation aggregates and the regeneration log |
| [`scripts/aggregate-upstream-docs.sh`](scripts/aggregate-upstream-docs.sh) | Rebuilds a normalized aggregate from an upstream checkout |
| [`agents/openai.yaml`](agents/openai.yaml) | OpenAI-facing display metadata and default prompt |

## Updating the snapshot

Follow [`generation/generation-steps.md`](generation/generation-steps.md) when a new Kotlin Toolchain version is
released. In outline:

1. Pin the new upstream tag and SHA, and read its release notes.
2. Rebuild the documentation aggregate with `scripts/aggregate-upstream-docs.sh`.
3. Diff the new docs against both the current pinned release and upstream `main`.
4. Update `SKILL.md`, its trigger description, and every affected reference.
5. Preserve the local Codex sandbox guidance, refresh migration and known-issue notes, and record the work in the
   generation log.
6. Update the repository documentation and use the repository release script for any version bump.

`SKILL.md` and the topic references are the maintained guidance. The files under `generation/` are source snapshots
and provenance, not content that an agent should load wholesale during ordinary use.
