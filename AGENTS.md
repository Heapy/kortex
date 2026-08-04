# kortex

One tree, three hosts. The same skills ship as Claude Code plugins, Codex plugins, and Junie
extensions, so every change has to be made in each host's manifest, not just the one you are
looking at.

## Layout

| Path | What it is |
|---|---|
| `plugins/<plugin>/skills/<skill>/SKILL.md` | the skill itself: YAML frontmatter (`name`, `description`) then the body |
| `plugins/<plugin>/.claude-plugin/plugin.json` | Claude Code manifest |
| `plugins/<plugin>/.codex-plugin/plugin.json` | Codex manifest |
| `plugins/<plugin>/extension.json` | Junie manifest |
| `.claude-plugin/marketplace.json` | Claude Code marketplace index |
| `.junie-extension/marketplace.json` | Junie marketplace index |
| `docs/` | field reports; not shipped with any plugin |

A skill's `description` is what the host matches a user request against — write it as the set of
phrases that should trigger it, not as a summary.

## Releasing

**The tree has one version, not one per plugin.** It is written into ten lines across six
manifests: `metadata.version` and one entry per plugin in each of the two marketplace manifests,
plus the two manifests of each plugin. All ten always hold the same value. Do not edit them by
hand — one gets forgotten, and then the manifests disagree with each other.

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
