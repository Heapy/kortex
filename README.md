# kortex

Agent Skills packaged as extensions/plugins for **Junie**, **Codex**, and **Claude Code**.

Installing the plugins is non-destructive: Junie, Claude Code, and Codex install into isolated extension/plugin caches
instead of copying into your personal skills directories.

## Plugins

### `kortex`

Kotlin and JVM development skills:

- `kotlin-toolchain` - JetBrains Kotlin Toolchain v0.11.x projects, migrations, dependencies, and multiplatform builds.
- `main-kts` - standalone executable Kotlin `.main.kts` scripts and script dependencies.
- `modern-kotlin` - Kotlin 2.0-2.4 language features, standard-library APIs, and experimental flags.
- `ktor` - Ktor 3.5.x server and client development.
- `jshell` - Java snippets and scratchpad work with JShell.

### `kotgent`

Repository-aware backlog workflows through the `kotgent` CLI:

- `create-tasks` - create focused backlog tasks from a request, specification, issue list, or plan.
- `sync-tasks` - compare active tasks with repository guidance, code, tests, TODOs, and Git history.
- `review-tasks` - audit todo tasks for stale, duplicate, absorbed, contradictory, or completed work.
- `take-task` - safely claim a specific or next eligible task and continue into its work workflow.
- `work-task` - implement, verify, and submit the task linked to the current session for human review.

### `heapy`

Personal engineering and repository-maintenance workflows:

- `fix-issues` - triage, reproduce, diagnose, fix, and verify repository issues.
- `amnesia` - recap a returning user's session and the work completed since their last message.
- `call-codex` - drive the Codex CLI as an independent reviewer or scoped executor.
- `message-claude` - send a message from an external process to a chosen Claude Code session.
- `message-codex` - queue a message from an external process to a chosen Codex session.
- `clean-claude-memory` - audit and prune Claude Code project memory.
- `clean-comments-and-guidance` - reduce low-value comments, docstrings, `CLAUDE.md`, and `AGENTS.md` guidance.

## Install

**Codex**

```
codex plugin marketplace add Heapy/kortex
codex plugin add kortex@kortex
codex plugin add heapy@kortex
codex plugin add kotgent@kortex
```

**Claude Code**

```
/plugin marketplace add Heapy/kortex
/plugin install kortex@kortex
/plugin install heapy@kortex
/plugin install kotgent@kortex
```

**Junie**

```
/extensions marketplace add Heapy/kortex
/extensions install kortex
/extensions install heapy
/extensions install kotgent
```

## Usage

| Host | Invocation |
|---|---|
| Codex | `$<plugin>:<skill> <request>` |
| Claude Code | `/<plugin>:<skill> <request>` |
| Junie | `Use the <skill> skill to <request>` |

For example:

```text
$kortex:jshell evaluate this Java snippet and return the result
$kotgent:review-tasks audit the todo backlog against the current repository
$heapy:fix-issues fix issue #123 and verify the change
```
