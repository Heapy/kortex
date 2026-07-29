# Reuse Kotlin Toolchain Caches In Codex

Codex `workspace-write` normally writes only inside the active workspace and configured writable roots. When several
projects or worktrees run Kotlin Toolchain, grant its shared user cache directories instead of relocating downloads
into every checkout.

## Cache Roots

Prefer the narrow Kotlin parent cache root because it can contain the CLI distribution plus download, extraction,
dependency, and transformed-library caches:

| Platform | Kotlin Toolchain cache root |
|---|---|
| macOS | `$HOME/Library/Caches/JetBrains/Kotlin` |
| Linux | `$HOME/.cache/JetBrains/Kotlin` |
| Windows | `%LOCALAPPDATA%\JetBrains\Kotlin` |

Also grant these only when the project needs them:

- `$HOME/.konan` for Kotlin/Native caches.
- `$HOME/.gradle` for adjacent Gradle builds; it is not a substitute for the Kotlin Toolchain cache root.

Keep project outputs such as `build/` local to each worktree. Do not grant the whole home directory or use
`danger-full-access` only to make caches writable.

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

## Relocating The Bootstrap Cache

`KOTLIN_CLI_BOOTSTRAP_CACHE_DIR` relocates only the wrapper/CLI bootstrap distribution:

```shell
export KOTLIN_CLI_BOOTSTRAP_CACHE_DIR="$HOME/.cache/JetBrains/Kotlin/cli"
```

It does not relocate every regular Kotlin Toolchain cache. On Linux, the regular cache respects XDG conventions, but
the bootstrap cache requires this explicit variable. Grant the resulting parent cache root to Codex rather than
assuming that `.gradle` or the project workspace covers it.
