# Kotlin Toolchain skill generation steps

This is a running log. The **current** snapshot is `v0.12.1` — see
[2026-09-09: regeneration for v0.12.1](#2026-09-09-regeneration-for-v0121) at the end of this file. The two sections
before it record the `v0.11.1` and `v0.12.0` runs and are kept for reference; their artifact lists are superseded.

## 2026-06-30: initial generation for v0.11.1

Generated on 2026-06-30 in `/Users/yoda/dev/pet/kortex`.

### Goal

Create a better `plugins/kortex/skills/kotlin-toolchain/SKILL.md` by:

1. Reading upstream Kotlin Toolchain documentation.
2. Saving all docs into one aggregate file.
3. Compressing the aggregate into a short operational summary.
4. Running `claude -p` as a second opinion.
5. Comparing both outputs.
6. Generating the final `SKILL.md`.
7. Saving enough process detail to regenerate future versioned skill files.

### Upstream discovery

Commands used:

```shell
git ls-remote --tags https://github.com/JetBrains/kotlin-toolchain.git
git ls-remote --heads https://github.com/JetBrains/kotlin-toolchain.git
which claude
```

Relevant results:

- Current upstream `main`: `a049d011217fc302fcdece9c7d0f48eac184a88d`
- Latest release tag found: `v0.11.1` at `801e9d4b2d1c12a15cca4ac7efc8e3b5270721e0`
- Release branch found: `release/0.11`
- `claude` binary found at `/Users/yoda/.local/bin/claude`.

### Upstream checkout

Commands used:

```shell
git clone --depth 1 --branch main --filter=blob:none --sparse https://github.com/JetBrains/kotlin-toolchain.git /private/tmp/kotlin-toolchain-main-docs
git clone --depth 1 --branch v0.11.1 --filter=blob:none --sparse https://github.com/JetBrains/kotlin-toolchain.git /private/tmp/kotlin-toolchain-v0.11.1-docs
git -C /private/tmp/kotlin-toolchain-main-docs sparse-checkout set --skip-checks docs examples README.md
git -C /private/tmp/kotlin-toolchain-v0.11.1-docs sparse-checkout set --skip-checks docs examples README.md
git -C /private/tmp/kotlin-toolchain-main-docs rev-parse HEAD
git -C /private/tmp/kotlin-toolchain-v0.11.1-docs rev-parse HEAD
```

### Aggregate generation

Generator added. It writes all upstream Markdown content, sorted by source path, with CR line endings, trailing
horizontal whitespace, and final blank lines normalized for cleaner diffs:

```shell
plugins/kortex/skills/kotlin-toolchain/scripts/aggregate-upstream-docs.sh
```

Commands used:

```shell
bash plugins/kortex/skills/kotlin-toolchain/scripts/aggregate-upstream-docs.sh /private/tmp/kotlin-toolchain-main-docs main a049d011217fc302fcdece9c7d0f48eac184a88d plugins/kortex/skills/kotlin-toolchain/generation/upstream-docs-main.md
bash plugins/kortex/skills/kotlin-toolchain/scripts/aggregate-upstream-docs.sh /private/tmp/kotlin-toolchain-v0.11.1-docs v0.11.1 801e9d4b2d1c12a15cca4ac7efc8e3b5270721e0 plugins/kortex/skills/kotlin-toolchain/generation/upstream-docs-v0.11.1.md
```

Verification:

```shell
wc -l plugins/kortex/skills/kotlin-toolchain/generation/upstream-docs-main.md plugins/kortex/skills/kotlin-toolchain/generation/upstream-docs-v0.11.1.md
rg -c '^### docs/src/.+\.md$' plugins/kortex/skills/kotlin-toolchain/generation/upstream-docs-main.md plugins/kortex/skills/kotlin-toolchain/generation/upstream-docs-v0.11.1.md
diff -q plugins/kortex/skills/kotlin-toolchain/generation/upstream-docs-main.md plugins/kortex/skills/kotlin-toolchain/generation/upstream-docs-v0.11.1.md
```

Observed:

- `upstream-docs-main.md`: 7,738 lines.
- `upstream-docs-v0.11.1.md`: 7,557 lines.
- Both aggregates contain 48 upstream Markdown documents.
- Main and `v0.11.1` differ.

### Reading and compression

The `main` aggregate was read in ranges and targeted source files were checked where terminal output was too dense.
The local compression was used to draft `SKILL-main.md` and then removed from the retained artifacts.

Important docs and topics read:

- CLI and wrapper provisioning
- FAQ and Alpha caveats
- Getting started, tutorial, IDE setup, Maven migration
- `module.yaml` and `project.yaml` references
- Dependencies, catalogs, repositories, BOMs
- Multiplatform hierarchy, propagation, aliases, interop
- Product type pages: JVM, KMP, Android, iOS, JS, Wasm, Native
- Built-in technologies: Compose, serialization, RPC, Ktor, Lombok, Spring
- Advanced topics: JDK provisioning, annotation processing, KSP, compiler plugins, Maven-like layout, Maven plugins,
  native interop
- Templates, testing, YAML primer
- Plugin docs: overview, quick start, checks, commands, configuration, references, structure, tasks

### Claude second opinion

Smoke test:

```shell
claude -p "Return exactly: OK"
```

Second-opinion command:

```shell
claude -p --tools Read --permission-mode dontAsk "Read plugins/kortex/skills/kotlin-toolchain/generation/upstream-docs-main.md and produce an independent second-opinion compression for a Codex SKILL.md about JetBrains Kotlin Toolchain. Do not edit files. Keep it concise but include: activation scope, current source/ref caveats, project/module YAML model, product types, dependency syntax, multiplatform rules, settings/defaults that matter, built-in technologies, plugins/tasks, migration/publishing, and pitfalls. Return Markdown only."
```

Claude's output was used as a second opinion while drafting the skill files and then removed from the retained
artifacts.

### Version diff checks

Commands used:

```shell
git diff --no-index --stat /private/tmp/kotlin-toolchain-v0.11.1-docs/docs/src /private/tmp/kotlin-toolchain-main-docs/docs/src
git diff --no-index --name-status /private/tmp/kotlin-toolchain-v0.11.1-docs/docs/src /private/tmp/kotlin-toolchain-main-docs/docs/src
git diff --no-index --word-diff=plain /private/tmp/kotlin-toolchain-v0.11.1-docs/docs/src/reference/module.md /private/tmp/kotlin-toolchain-main-docs/docs/src/reference/module.md
git diff --no-index --word-diff=plain /private/tmp/kotlin-toolchain-v0.11.1-docs/docs/src/reference/project.md /private/tmp/kotlin-toolchain-main-docs/docs/src/reference/project.md
git diff --no-index --word-diff=plain /private/tmp/kotlin-toolchain-v0.11.1-docs/docs/src/user-guide/templates.md /private/tmp/kotlin-toolchain-main-docs/docs/src/user-guide/templates.md
git diff --no-index --word-diff=plain /private/tmp/kotlin-toolchain-v0.11.1-docs/docs/src/user-guide/dependencies.md /private/tmp/kotlin-toolchain-main-docs/docs/src/user-guide/dependencies.md
git diff --no-index --word-diff=plain /private/tmp/kotlin-toolchain-v0.11.1-docs/docs/src/user-guide/basics.md /private/tmp/kotlin-toolchain-main-docs/docs/src/user-guide/basics.md
git diff --no-index --word-diff=plain /private/tmp/kotlin-toolchain-v0.11.1-docs/docs/src/index.md /private/tmp/kotlin-toolchain-main-docs/docs/src/index.md
git diff --no-index --word-diff=plain /private/tmp/kotlin-toolchain-v0.11.1-docs/docs/src/getting-started/ide-setup.md /private/tmp/kotlin-toolchain-main-docs/docs/src/getting-started/ide-setup.md
```

Main/version differences used when making `SKILL.md` default to `v0.11.1`:

- `v0.11.1` docs used relative paths such as `./` and `../`; current main prefers `//` for dependencies/templates.
- `v0.11.1` templates could not contain `apply:`; current main allows nested templates.
- `v0.11.1` did not document the current nested-template and sibling-conflict-resolution behavior.
- `v0.11.1` IDE setup linked to old Amper plugin ID `23076-amper`; current main links to Kotlin Toolchain plugin
  ID `31850-kotlin-toolchain`.
- `v0.11.1` defaults included Android `compileSdk` 36, serialization 1.10.0, and KSP 2.3.6.

### Generated artifacts

- `SKILL.md`: default v0.11.1 skill.
- `SKILL-main.md`: current upstream main skill.
- `references/codex-sandbox-caches.md`: local Codex cache-sharing guidance; preserve it across snapshot regenerations.
- `scripts/aggregate-upstream-docs.sh`: reusable aggregate generator.
- `generation/upstream-docs-main.md`: full main docs aggregate.
- `generation/upstream-docs-v0.11.1.md`: full v0.11.1 docs aggregate.
- `generation/generation-steps.md`: this reproducibility log.

### Regenerating For A Future Version

1. Check upstream tags and branches:

   ```shell
   git ls-remote --tags https://github.com/JetBrains/kotlin-toolchain.git
   git ls-remote --heads https://github.com/JetBrains/kotlin-toolchain.git
   ```

2. Clone or fetch the target tag, for example `v<next-version>`.

3. Run `scripts/aggregate-upstream-docs.sh` with the target repo path, ref name, SHA, and output path such as
   `generation/upstream-docs-v<next-version>.md`.

4. Compare the new docs against the previous released version and current `main`.

5. Write `SKILL-<next-version>.md` as a version overlay:

   - Include source ref and SHA.
   - List behavior/default differences from `SKILL.md`.
   - Keep only version-specific guidance if the default skill still covers common concepts.

6. Optionally re-run `claude -p` on the new aggregate or on a local compression as a second opinion.

7. Update this log with the new commands and decisions.


## 2026-08-25: regeneration for v0.12.0

Upstream released `v0.12.0` on 2026-08-25. The skill was regenerated and restructured from one monolithic file into a
base skill plus topic references.

### Upstream discovery

```shell
git ls-remote --tags --refs https://github.com/JetBrains/kotlin-toolchain.git
curl -sL https://api.github.com/repos/JetBrains/kotlin-toolchain/releases/tags/v0.12.0
```

- Latest release tag: `v0.12.0` at `2039c5371bf5812f0061b2b11b6581b4e9de3a97`, published 2026-08-25.
- Upstream `main` at the time: `1cb8b790557e2ea6b9613a5ab50dd3fc1c59f54b`.
- Release notes body saved and read in full (197 lines: breaking changes, new features, cosmetics, usability, fixed
  bugs, IDE changes).

### Upstream checkout

```shell
git clone --depth 1 --branch v0.12.0 --filter=blob:none --sparse https://github.com/JetBrains/kotlin-toolchain.git kt-v0.12.0
git clone --depth 1 --branch v0.11.1 --filter=blob:none --sparse https://github.com/JetBrains/kotlin-toolchain.git kt-v0.11.1
git clone --depth 1 --branch main     --filter=blob:none --sparse https://github.com/JetBrains/kotlin-toolchain.git kt-main
git -C <each> sparse-checkout set --skip-checks docs examples README.md
```

### Aggregate generation

```shell
bash plugins/kortex/skills/kotlin-toolchain/scripts/aggregate-upstream-docs.sh \
  <scratch>/kt-v0.12.0 v0.12.0 2039c5371bf5812f0061b2b11b6581b4e9de3a97 \
  plugins/kortex/skills/kotlin-toolchain/generation/upstream-docs-v0.12.0.md
```

`upstream-docs-v0.12.0.md` is 8,279 lines over 48 upstream Markdown documents.

### Version diff checks

```shell
git diff --no-index --stat   kt-v0.11.1/docs/src kt-v0.12.0/docs/src
git diff --no-index -U2      kt-v0.11.1/docs/src kt-v0.12.0/docs/src > docs-diff.patch
git diff --no-index --stat   kt-v0.12.0/docs/src kt-main/docs/src
```

Observed:

- `v0.11.1` → `v0.12.0`: 34 files changed, 1,072 insertions, 353 deletions. The 2,381-line patch was read in full,
  then individual `v0.12.0` topic files were opened while writing each reference.
- `v0.12.0` → `main`: **1 line** in `user-guide/product-types/wasm-wasi-app.md`.

That last result is why `SKILL-main.md` and `generation/upstream-docs-main.md` were deleted rather than regenerated:
a separate main snapshot would be a near-duplicate free to drift. Reinstate the pair only if a future `main` diverges
meaningfully from the latest tag again.

### Structural decisions

- `SKILL.md` was rewritten against `v0.12.0` and reduced to an operational base plus a reference map. It still carries
  the version check, first moves, CLI list, product-type table, defaults table, and pitfalls inline — the split is not
  allowed to turn the entry point into a routing hop.
- The old monolithic file was renamed `SKILL-0.11.md` and marked historical in its frontmatter and body. Its `name`
  is `kotlin-toolchain-0-11`, mirroring what `SKILL-main.md` did.
- Detail moved into thirteen `references/*.md` files, roughly mirroring the upstream docs tree.
- `references/migrating-0.11-to-0.12.md` was written from the release notes plus the docs diff.
- `references/known-issues.md` was re-checked against the release notes: KTC-5573 is fixed; KTC-4871 and KTC-5603
  remain open; KTC-5698 and KTC-5576 were added from the `v0.12.0` docs.
- `references/codex-sandbox-caches.md` was updated for the removed `--shared-caches-root` and the new
  `KOTLIN_SHARED_CACHE_DIR` / `--shared-cache-dir`.

### Content flips from v0.11.1

Every one of these inverts a claim the old skill made, so none of its sentences could be carried over untouched:

- `//` project-root paths are supported and preferred; `./`/`../` guidance is obsolete.
- Templates can apply other templates, and sibling conflicts are now build errors.
- KMP publishing works, with Gradle module metadata and commonized cinterop.
- `wasmJs/app`/`wasmWasi/app` renamed to `wasm-js/app`/`wasm-wasi/app`; `wasm-js/app` gained `run`.
- `iosX64` dropped from `ios/app`; `macosX64` dropped from `macos/app` defaults; `watchosArm32` deprecated.
- Minimum JDK 17, minimum Kotlin 2.2.20, default JDK 25, default Kotlin 2.4.10, `minSdk` 24, `compileSdk` 37.
- New settings: `publishing`, `kotlin.dataframe`, `kotlin.powerAssert`, `kotlin.rpc`, `android.resourcePackaging`.
- Spring Boot no longer adds starters; `no-arg` uses the `jpa` preset.
- CLI options `--root`, `--build-output`, `--shared-caches-root` removed; `publish -m` needs `--transitive`.
- Plugin references: `module.sources` → `module.kotlinJavaSources`; `markOutputAs` → `generated:`.
- YouTrack project `AMPER` → `KTC`; IDEA plugin `23076-amper` → `31850-kotlin-toolchain`.

### Retained artifacts

- `SKILL.md` — default `v0.12.0` skill, base plus reference map.
- `SKILL-0.11.md` — historical `v0.11.1` skill.
- `references/` — thirteen topic references; `codex-sandbox-caches.md` is local guidance, preserve it across
  regenerations.
- `generation/upstream-docs-v0.12.0.md` — full `v0.12.0` docs aggregate.
- `generation/upstream-docs-v0.11.1.md` — full `v0.11.1` docs aggregate, kept alongside `SKILL-0.11.md`.
- `generation/generation-steps.md` — this log.
- `scripts/aggregate-upstream-docs.sh` — reusable aggregate generator.

### Regenerating for a future version

1. `git ls-remote --tags --refs https://github.com/JetBrains/kotlin-toolchain.git` for the latest tag, and
   `curl -sL https://api.github.com/repos/JetBrains/kotlin-toolchain/releases/tags/<tag>` for the release notes.
2. Sparse-clone the new tag, the current pinned tag, and `main`.
3. Run `scripts/aggregate-upstream-docs.sh` into `generation/upstream-docs-<tag>.md`.
4. Diff the new tag against the pinned one and read the whole patch. Diff it against `main` too — if they are close,
   do not create a main snapshot.
5. Update `SKILL.md` and every affected `references/*.md`. Update the frontmatter `description`: it is the trigger
   text, and it names the version.
6. Write `references/migrating-<old>-to-<new>.md` from the breaking-changes list, and fold the previous migration file
   into it or drop it once nobody is on that version.
7. Re-check `references/known-issues.md` against the release notes' fixed-bugs list.
8. Rename the outgoing `SKILL.md` to `SKILL-<old>.md` only if projects are plausibly still pinned there; otherwise
   delete it along with its aggregate.
9. Update the root `README.md`, `plugins/kortex/README.md`, and this log.
10. Bump the tree version with `./scripts/release.main.kts <version>`.

## 2026-09-09: regeneration for v0.12.1

Upstream released `v0.12.1` on 2026-09-08, a patch with two bug fixes and one new documentation page. The skill was
updated in place — no restructuring, no new SKILL variant.

### Upstream discovery

```shell
git ls-remote --tags https://github.com/JetBrains/kotlin-toolchain.git
git ls-remote --heads https://github.com/JetBrains/kotlin-toolchain.git main
curl -sL https://api.github.com/repos/JetBrains/kotlin-toolchain/releases/tags/v0.12.1
```

- Latest release tag: `v0.12.1` at `3f227ed2625bd3e91f53079c97433e1dbd639a30`, published 2026-09-08.
- Upstream `main` at the time: `2eb1dbaa05a53162120958c96c0bd9b969f2d165`.
- Release notes: 7 lines. No breaking changes, no new features. Fixed: KTC-5769 (a dependency-resolution error hidden
  behind `dependency was resolved but it's missing on disk`) and KTC-5799 (`publish mavenCentral` rejected with
  `not marked as publishable`).

### Aggregate generation

```shell
bash plugins/kortex/skills/kotlin-toolchain/scripts/aggregate-upstream-docs.sh \
  <scratch>/kt-v0.12.1 v0.12.1 3f227ed2625bd3e91f53079c97433e1dbd639a30 \
  plugins/kortex/skills/kotlin-toolchain/generation/upstream-docs-v0.12.1.md
```

`upstream-docs-v0.12.1.md` is 8,883 lines over 50 upstream Markdown documents. `upstream-docs-v0.12.0.md` (8,279
lines, 49 documents) was deleted: nothing consumes it, since no `SKILL-0.12.0.md` was created.

### Version diff checks

```shell
git diff --no-index --stat kt-v0.12.0/docs/src kt-v0.12.1/docs/src
git diff --no-index --stat kt-v0.12.1/docs/src kt-main/docs/src
git diff --no-index --stat kt-v0.12.0/examples kt-v0.12.1/examples
```

Observed:

- `v0.12.0` → `v0.12.1` docs: 2 files, +602. One new page, `getting-started/migrating-from-gradle.md` (596 lines),
  plus a cosmetic `stylesheets/extra.css` change. Every other doc is byte-identical, so existing references only
  needed their version line bumped.
- `v0.12.0` → `v0.12.1` examples: Amper → Kotlin Toolchain wording, and the IDEA plugin link moved to
  `31850-kotlin-toolchain`. Both already reflected in the skill since `0.12.0`.
- `v0.12.1` → `main`: 9 files, +209 −69 — real drift toward the next release, unlike the single line seen at the
  `v0.12.0` tag. Still no main snapshot: the skill tracks tagged releases, and `SKILL.md` now says so instead of
  claiming main and the tag agree.

### Changes made

- `references/gradle-migration.md` — new, written from the upstream page. It is the one substantive addition in this
  release, so the `SKILL.md` frontmatter `description` (the trigger text) gained "Gradle migration", the body's
  "do not apply to Gradle or Maven build editing" carve-out now names Gradle too, and the reference map gained a row.
- `references/known-issues.md` — new `Fixed In 0.12.1` section for KTC-5769 and KTC-5799; the open-issue heading moved
  from `0.12.0` to `0.12.1` (all four remain open).
- `references/publishing.md` — Maven Central configuration now states it requires `0.12.1`, since KTC-5799 has no
  configuration workaround on `0.12.0`.
- `references/maven-migration.md` — its "no Gradle converter" paragraph now points at `gradle-migration.md`.
- Version lines in eleven references, `SKILL.md`, `SKILL-0.11.md`, and `README.md` bumped to `v0.12.1`. Historical
  text — `migrating-0.11-to-0.12.md`, the `Fixed In 0.12.0` section, the `v0.12.0` log section — left at `0.12.0`.
- `builtin-tech.md` still flags the stale Compose hot-reload warning: it is unchanged in the `v0.12.1` docs.

### Deliberate non-changes

- **No `migrating-0.12.0-to-0.12.1.md`.** The release has no breaking changes; the upgrade is `./kotlin update` and
  nothing else. Do not add one on the next patch either unless the release notes list a breaking change.
- **No `SKILL-0.12.0.md`.** `0.12.0` and `0.12.1` share one skill; `SKILL.md` covers `v0.12.x`.
- The upstream page's link to the third-party `singleton11/kotlin-toolchain-skills` was not carried over.

### Retained artifacts

- `SKILL.md` — default `v0.12.1` skill, base plus reference map.
- `SKILL-0.11.md` — historical `v0.11.1` skill.
- `references/` — fifteen topic references; `codex-sandbox-caches.md` is local guidance, preserve it across
  regenerations, and `gradle-migration.md` is new in this run.
- `generation/upstream-docs-v0.12.1.md` — full `v0.12.1` docs aggregate.
- `generation/upstream-docs-v0.11.1.md` — full `v0.11.1` docs aggregate, kept alongside `SKILL-0.11.md`.
- `generation/generation-steps.md` — this log.
- `scripts/aggregate-upstream-docs.sh` — reusable aggregate generator.

### Upstream text not reproduced verbatim

`migrating-from-gradle.md` at `v0.12.1` maps `./gradlew tasks` to `./kotlin show tasks`. Checked against a real
`0.12.1` wrapper: the command exists and works, but it prints internal build tasks and their dependency edges, which
is not the analogue of `gradle tasks`. Upstream `main` reworded the row to `kotlin --help` / `kotlin show commands`
for that reason. `references/gradle-migration.md` gives the user-facing commands and explains what `show tasks`
actually prints. The same page's `maint.kt` and `setting.gradle(.kts)` typos were silently fixed.

`main` also adds a "Step 1: install the wrappers" section using `kotlin update --create`. That flag does not exist at
`v0.12.1`, so it was not ported.
