# kortex

Agent Skills for Kotlin development, packaged as an extension/plugin for **Junie**, **Codex**, and **Claude Code**.

Installing the plugin is non-destructive: Junie, Claude Code, and Codex install into isolated extension/plugin caches
instead of copying into your personal skills directories.

## Install

**Codex**

```
codex plugin marketplace add Heapy/kortex
codex plugin add kortex@kortex
```

**Claude Code**

```
/plugin marketplace add Heapy/kortex
/plugin install kortex@kortex
```

**Junie**

```
/extensions marketplace add Heapy/kortex
/extensions install kortex
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

**Junie**

```
Use the kotlin-toolchain skill to create a setup for a Kotlin Native CLI application
```
