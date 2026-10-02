# Kotlin Toolchain Skill Generation

This file records the current `v0.13.0` snapshot and how to regenerate it. The skill ships one release snapshot;
Git history retains previous generations.

## Sources

Generated on 2026-10-02 from the release published on 2026-10-01.

- Repository: `https://github.com/JetBrains/kotlin-toolchain`
- Tag: `v0.13.0`
- SHA: `abc7a5f7579e9ef267107782048c76777f0bf932`
- Upstream `main` observed at `8f64e5c40ef20eae068e562bf3744815d51ba131`
- [Release notes](https://github.com/JetBrains/kotlin-toolchain/releases/tag/v0.13.0)

## Reproduce The Aggregate

```shell
git clone --depth 1 --branch v0.13.0 --filter=blob:none --sparse \
  https://github.com/JetBrains/kotlin-toolchain.git <scratch>/upstream
git -C <scratch>/upstream sparse-checkout set --skip-checks docs examples sources README.md
bash plugins/kortex/skills/kotlin-toolchain/scripts/aggregate-upstream-docs.sh \
  <scratch>/upstream v0.13.0 abc7a5f7579e9ef267107782048c76777f0bf932 \
  plugins/kortex/skills/kotlin-toolchain/generation/upstream-docs-v0.13.0.md
```

The generator aggregates Markdown files under `docs/src`, sorted by source path. It normalizes CR line endings,
trailing horizontal whitespace, and final blank lines. Each document carries a source permalink.

The aggregate contains **9,353 lines over 50 Markdown documents**. At generation time, `main` had identical docs
and README; only 12 example wrapper files differed (+210/-84), so no separate main snapshot was created.

## Maintained Guidance

- `SKILL.md` carries the operational base, trigger description, reference map, and defaults.
- Topic references describe the current release. The migration guide covers upgrading into it, including layout,
  environment variables, Android identity, hot-reload flags, and changed defaults.
- The raw aggregate preserves upstream text. Where it is stale, the maintained guidance follows tagged source:
  Kotlin defaults, Android namespaces, language/API version strings, Wasm-JS tests, and Compose hot reload.
  The migration guide links the source files used to resolve these discrepancies.
- Codex sandbox caches and Heapy GitHub Actions are separately maintained integration guidance.

The update includes SwiftPM interop and publication metadata, Compose resource publication, Wasm-JS browser tests,
JUnit tag expressions, explicit API mode, Native dependency caches, Android ABI filters, wrapper creation,
Maven-plugin catalog references, and the POM description override. Wasm-WASI has no test runner, and SwiftPM interop
exposes Objective-C-compatible APIs.

## Validation

- Validate skill frontmatter and naming, relative Markdown links, manifest consistency, and `git diff --check`.
- Rebuild the aggregate independently and compare its bytes.
- Install exact released wrappers into a temporary `jvm/lib` fixture with
  `kotlin update --create --target-version 0.13.0 --target-dir <fixture>`.
  The release reports `0.13.0 (abc7a5f, 2026-10-01)`; tagged example wrappers still pin a development build.
- Run `./kotlin test --include-tag 'fast & !slow'` with `layout: default`, `explicitApi: strict`, an explicitly typed
  public library function, ordinary test declarations, a passing `fast` test, and a deliberately failing `slow` test.
  Main and test compilation passed, exactly the `fast` test ran, and the command exited 0.

The local sandbox blocked a JVM host `sysctl` probe, so release-wrapper setup and the JVM smoke test were run with
approved broader permissions in the temporary fixture. No Android, Apple, or browser runtime build was run; those
sections were checked against the tagged schema, task wiring, integration tests, and release notes.

## Update For A Future Release

1. Resolve the release tag and SHA, read its release notes, and clone its docs, examples, and sources.
2. Generate its aggregate with `scripts/aggregate-upstream-docs.sh`.
3. Compare the new tag with the current snapshot and with upstream `main`. Read affected docs and tagged source.
4. Update `SKILL.md`, its trigger description, affected topic references, defaults, and known issues.
5. Refresh the migration guide for the transition into the new release. Remove superseded snapshots and historical
   guidance; retain source provenance and current documentation corrections.
6. Preserve the local sandbox and GitHub Actions guidance, then update repository readmes and these generation notes.
7. Run focused validation and any build fixtures needed for changed behavior.
8. Set the repository version with `./scripts/release.main.kts <version>` and review the diff. This update targets
   repository version `0.17.0` across all versioned host manifests.
