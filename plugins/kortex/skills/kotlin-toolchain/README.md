# Kotlin Toolchain skill

This directory contains the Agent Skill for
[JetBrains Kotlin Toolchain](https://github.com/JetBrains/kotlin-toolchain), formerly Amper. It covers the declarative
YAML project model, the `kotlin` CLI, supported product types, dependencies, multiplatform projects, publishing, and
local build plugins.

The [local plugin catalog](references/ktc-plugins.md) describes Heapy's `ktc-plugins` source installer, producer
descriptors, and the separately maintained ktlint, ktfmt, BCV, Kover, Dokka, and Jib integrations.

The default skill is based on Kotlin Toolchain `v0.13.0` at
`abc7a5f7579e9ef267107782048c76777f0bf932`. The [migration guide](references/migrating-0.12-to-0.13.md)
covers upgrading an existing project to this release.

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
| [`SKILL.md`](SKILL.md) | Current entry point and operational guidance for `v0.13.x` |
| [`references/`](references/) | Detailed topic guides loaded only when a task needs them |
| [`generation/`](generation/) | Current upstream documentation aggregate and regeneration notes |
| [`scripts/aggregate-upstream-docs.sh`](scripts/aggregate-upstream-docs.sh) | Rebuilds a normalized aggregate from an upstream checkout |
| [`agents/openai.yaml`](agents/openai.yaml) | OpenAI-facing display metadata and default prompt |

## Updating the snapshot

Follow [`generation/generation-steps.md`](generation/generation-steps.md) when a new Kotlin Toolchain version is
released. In outline:

1. Pin the new upstream tag and SHA, and read its release notes.
2. Rebuild the documentation aggregate with `scripts/aggregate-upstream-docs.sh`.
3. Diff the new docs against both the current pinned release and upstream `main`.
4. Update `SKILL.md`, its trigger description, and every affected reference.
5. Preserve the local Codex sandbox guidance, refresh the migration guide and known-issue notes, and record the work in the
   generation log.
6. Update the repository documentation and use the repository release script for any version bump.

`SKILL.md` and the topic references are the maintained guidance. The files under `generation/` are the source snapshot
and provenance, not content that an agent should load wholesale during ordinary use.
