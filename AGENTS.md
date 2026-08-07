# kortex

One tree, three hosts. The same skills ship as Claude Code plugins, Codex plugins, and Junie
extensions, so every change has to be made in each host's manifest, not just the one you are
looking at.

## Layout

| Path | What it is |
|---|---|
| `plugins/<plugin>/skills/<skill>/SKILL.md` | the skill itself: YAML frontmatter (`name`, `description`) then the body |
| `plugins/<plugin>/plugin.json` | portable Agent Plugins manifest; no host reads it yet |
| `plugins/<plugin>/.claude-plugin/plugin.json` | Claude Code manifest |
| `plugins/<plugin>/.codex-plugin/plugin.json` | Codex manifest |
| `plugins/<plugin>/extension.json` | Junie manifest |
| `.claude-plugin/marketplace.json` | Claude Code marketplace index |
| `.junie-extension/marketplace.json` | Junie marketplace index |
| `docs/` | field reports; not shipped with any plugin |

A skill's `description` is what the host matches a user request against — write it as the set of
phrases that should trigger it, not as a summary.

## Specs

Two specifications govern the tree, and they split cleanly: one owns the skill, the other owns the
directory around it.

| Spec | Owns | Where |
|---|---|---|
| Agent Skills | `SKILL.md`: frontmatter fields, `name`/`description` limits, `scripts/`, `references/`, `assets/` | <https://agentskills.io/specification> |
| Agent Plugins 1.0.0 | the plugin directory: `plugin.json` in the plugin root, skills discovered at `skills/`, MCP at `mcp.json` | <https://github.com/agentplugins/agent-plugins-spec>, [`spec/1.0.0.md`](https://github.com/agentplugins/agent-plugins-spec/blob/main/spec/1.0.0.md), [`plugin.schema.json`](https://github.com/agentplugins/agent-plugins-spec/blob/main/schemas/1.0.0/plugin.schema.json) |

Agent Plugins says nothing about `SKILL.md` — it delegates to Agent Skills. Constraints worth
keeping in mind while editing: `name` must match the skill's directory name and is limited to
lowercase alphanumerics and single hyphens; `description` is capped at 1024 characters; skills are
found one level under `skills/` and nowhere deeper.

The root `plugin.json` is the portable manifest. Its schema is closed (`additionalProperties:
false`), so host-specific keys — Claude's `components`, Codex's `interface` — cannot go in it; they
stay in the host manifests. The spec's own escape hatch is an `extensions` object keyed by
reverse-domain namespace, unused here: it would be a third copy of data no host reads, free to
drift. Neither marketplace manifest is covered by any spec.

## Releasing

**The tree has one version, not one per plugin.** It is written into twelve lines across eight
manifests: `metadata.version` and one entry per plugin in each of the two marketplace manifests,
plus the three versioned manifests of each plugin. All twelve always hold the same value. Do not
edit them by hand — one gets forgotten, and then the manifests disagree with each other.

```sh
./scripts/release.main.kts 1.2.0
```

One argument, the version to set. Every manifest gets it, whether or not that plugin changed —
there is nothing to decide and nothing to leave behind. A manifest that is not valid JSON, or that
carries no `version` key, stops the run before anything is written.

Nothing else happens: no commit, no tag, no push. Review with `git diff`, then commit.

**Running it.** Needs a JDK on `PATH` and the Kotlin 2.4.10+ script runner, which is `kotlinr`.
`kotlin` here is the Kotlin Toolchain CLI, a different program — it will not run the script. Use
`./scripts/release.main.kts` or `kotlinr scripts/release.main.kts`.
