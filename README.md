# kortex

Agent Skills for Kotlin development, packaged as a plugin for both **Codex** and **Claude Code**.

Installing the plugin is non-destructive: Claude Code namespaces the skills as `plugin:skill`, and Codex installs into an isolated plugin cache — neither touches your personal `~/.claude/skills` or `~/.codex/skills`.

## Install

**Claude Code**

```
/plugin marketplace add Heapy/kortex
/plugin install kortex@kortex
```

**Codex**

```
codex plugin marketplace add Heapy/kortex
codex plugin add kortex@kortex
```

## Usage

**Codex**

```
$kortex:kotlin-toolchain convert current project to Kotlin Toolchain
```

**Claude Code**

```
/kortex:kotlin-toolchain create a setup for a Kotlin Native CLI application
```
