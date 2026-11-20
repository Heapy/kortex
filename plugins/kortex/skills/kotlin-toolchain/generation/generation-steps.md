# Kotlin Toolchain Skill Generation

This file records the current `v0.14.1` snapshot and how to regenerate it. The skill ships one release snapshot;
Git history retains previous generations.

## Sources

Generated on 2026-11-20 from the release published on 2026-11-19.

- Repository: `https://github.com/JetBrains/kotlin-toolchain`
- Tag: `v0.14.1`
- SHA: `f53674bce744f0b5e9ff235110dcc1d565d2c114`
- Upstream `main` observed at `df3992adf0ec18811cde7a0a7546e0a933fc6e1e`
- [Release notes](https://github.com/JetBrains/kotlin-toolchain/releases/tag/v0.14.1)

## Reproduce The Aggregate

```shell
git clone --depth 1 --branch v0.14.1 --filter=blob:none --sparse \
  https://github.com/JetBrains/kotlin-toolchain.git <scratch>/upstream
git -C <scratch>/upstream sparse-checkout set --skip-checks docs examples sources README.md
bash plugins/kortex/skills/kotlin-toolchain/scripts/aggregate-upstream-docs.sh \
  <scratch>/upstream v0.14.1 f53674bce744f0b5e9ff235110dcc1d565d2c114 \
  plugins/kortex/skills/kotlin-toolchain/generation/upstream-docs-v0.14.1.md
```

The generator aggregates Markdown files under `docs/src`, sorted by source path. It normalizes CR line endings,
trailing horizontal whitespace, and final blank lines. Each document carries a source permalink.

The aggregate contains **9,758 lines over 53 Markdown documents**. The observed `main` has the same `docs/src` tree.
The only Markdown change since 0.14.0 is the landing page title. The previous aggregate is retained in Git history.

## Maintained Guidance

- `SKILL.md` carries the operational base, trigger description, reference map, and defaults.
- Topic references target `0.14.1`. The migration guide covers Android identity reversal, serialization deprecation,
  empty test discovery, new project creation, download mirrors, Android desugaring, and publication changes.
- Compiler/framework defaults are unchanged from `0.13.0`; desugaring adds an opt-in library default of `2.1.5`.
- The raw aggregate preserves upstream text. Maintained guidance follows tagged source where Markdown lags:
  Kotlin defaults, language/API version strings, and Wasm-JS testing. Source notes also clarify discovery versus
  execution for empty tests, Android dependency variant preference, and IPA export options.
- Publishing guidance now covers TestFlight, IPA packaging, signing subkeys, Maven Local's signing exemption,
  generated sources, klib identity compatibility, and optional klibs.io notifications.
- Codex sandbox caches, Heapy GitHub Actions, the local plugin catalog, and Native/Apple host notes retain their
  independently dated provenance. Updating this snapshot does not certify third-party plugins against `0.14.1`.

## Validation

- Skill frontmatter/naming validation passed; maintained relative Markdown links resolve; `git diff --check` passed.
- The aggregate was regenerated independently and compared byte-for-byte successfully.
- The tagged launcher pins Azul Zulu 25.38.201. The release notes distinguish the CLI runtime update from
  refreshing a JDK previously provisioned for a project.
- Both fixture wrappers pin 0.14.1. On macOS arm64, the CLI reported `0.14.1 (f53674b, 2026-11-19)`,
  listed `clean-shared-caches` in its help, and passed a JVM serialization round-trip test.
- The 0.14.0 snapshot was exercised with JVM serialization, empty and skipped test discovery, and Android settings
  fixtures. The 0.14.1 patch changes the launcher runtime and preserves those project defaults.

## Update For A Future Release

1. Resolve the release tag and SHA, read its release notes, and clone its docs, examples, and sources.
2. Generate its aggregate with `scripts/aggregate-upstream-docs.sh`.
3. Compare the new tag with the current snapshot and with upstream `main`. Read affected docs and tagged source.
4. Update `SKILL.md`, its trigger description, affected topic references, defaults, and known issues.
5. Refresh the migration guide for the transition into the new release. Remove superseded snapshots and historical
   guidance; retain source provenance and current documentation corrections.
6. Preserve independently maintained integration guidance, then update repository readmes and these generation notes.
7. Validate frontmatter, local reference links, aggregate reproducibility, and `git diff --check`. Run focused CLI
   fixtures for changed behavior; do not upload or publish artifacts merely to validate documentation.
8. When preparing a repository release, set its single version with `./scripts/release.main.kts <version>` so all
   host manifests stay synchronized. A skill snapshot update does not itself require a repository release.
