# Reuse Kotlin Toolchain Caches In Codex

Codex `workspace-write` normally writes only inside the active workspace and configured writable roots. When several
projects or worktrees run Kotlin Toolchain, grant its shared user cache directories instead of relocating downloads
into every checkout.

## Two Caches

`0.12` splits them cleanly, and both can be relocated:

| Cache | Holds | Relocate with |
|---|---|---|
| Bootstrap | The wrapper's copy of the CLI distribution | `KOTLIN_CLI_BOOTSTRAP_CACHE_DIR` |
| Shared | Downloaded dependencies, JDKs, tools — shared by all projects | `KOTLIN_SHARED_CACHE_DIR`, or `--shared-cache-dir` which wins over it |

`KOTLIN_SHARED_CACHE_DIR` and `--shared-cache-dir` are new in `0.12`. The `0.11.x` flag `--shared-caches-root` was
removed — a script still passing it fails.

Pointing both variables at one directory you already grant is usually simpler than enumerating default cache roots.

## Default Cache Roots

If you leave the defaults in place, grant the narrow Kotlin parent cache root. It covers the CLI distribution plus
download, extraction, dependency, and transformed-library caches:

| Platform | Kotlin Toolchain cache root |
|---|---|
| macOS | `$HOME/Library/Caches/JetBrains/Kotlin` |
| Linux | `$HOME/.cache/JetBrains/Kotlin` |
| Windows | `%LOCALAPPDATA%\JetBrains\Kotlin` |

Grant these only when the project needs them:

- `$HOME/.konan` for Kotlin/Native caches.
- `$HOME/.gradle` for adjacent Gradle builds; it is not a substitute for the Kotlin Toolchain cache root.

Keep project outputs such as `build/` local to each worktree. Do not grant the whole home directory or switch to
`danger-full-access` just to make caches writable.

## Persistent Codex Configuration

Use absolute paths in the user-level `~/.codex/config.toml`. For example, on macOS:

```toml
sandbox_mode = "workspace-write"

[sandbox_workspace_write]
writable_roots = [
  "/Users/alice/Library/Caches/JetBrains/Kotlin",
  "/Users/alice/.konan",
]
```

Preserve existing roots such as `.gradle` when extending an existing array. Start a new Codex session after changing
the configuration, then use `/status` to verify the effective writable roots.

Writable roots do not grant network access. Initial downloads require an already warmed cache, a scoped approval, or
`sandbox_workspace_write.network_access = true` when the user intentionally allows outbound access.

## One-Off Access

For one CLI session, prefer `--add-dir` over changing to full access:

```shell
codex \
  --add-dir "$HOME/Library/Caches/JetBrains/Kotlin" \
  --add-dir "$HOME/.konan"
```

Adjust the Kotlin root for Linux or Windows and omit `.konan` for projects without Kotlin/Native.

## Concurrency

Provisioning is designed to be concurrency-safe. Parallel `kotlin` invocations across worktrees sharing one cache root
do not corrupt each other, so one shared root is the right answer rather than a cache per worktree.

## Quieting CI Output

Set `KOTLIN_CLI_NO_WELCOME_BANNER=1` when the distribution is provisioned on every run.
