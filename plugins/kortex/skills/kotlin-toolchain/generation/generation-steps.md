# Kotlin Toolchain skill generation steps

Generated on 2026-06-30 in `/Users/yoda/dev/pet/kortex`.

## Goal

Create a better `plugins/kortex/skills/kotlin-toolchain/SKILL.md` by:

1. Reading upstream Kotlin Toolchain documentation.
2. Saving all docs into one aggregate file.
3. Compressing the aggregate into a short operational summary.
4. Running `claude -p` as a second opinion.
5. Comparing both outputs.
6. Generating the final `SKILL.md`.
7. Saving enough process detail to regenerate future versioned skill files.

## Upstream discovery

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

## Upstream checkout

Commands used:

```shell
git clone --depth 1 --branch main --filter=blob:none --sparse https://github.com/JetBrains/kotlin-toolchain.git /private/tmp/kotlin-toolchain-main-docs
git clone --depth 1 --branch v0.11.1 --filter=blob:none --sparse https://github.com/JetBrains/kotlin-toolchain.git /private/tmp/kotlin-toolchain-v0.11.1-docs
git -C /private/tmp/kotlin-toolchain-main-docs sparse-checkout set --skip-checks docs examples README.md
git -C /private/tmp/kotlin-toolchain-v0.11.1-docs sparse-checkout set --skip-checks docs examples README.md
git -C /private/tmp/kotlin-toolchain-main-docs rev-parse HEAD
git -C /private/tmp/kotlin-toolchain-v0.11.1-docs rev-parse HEAD
```

## Aggregate generation

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

## Reading and compression

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

## Claude second opinion

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

## Version diff checks

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

## Generated artifacts

- `SKILL.md`: default v0.11.1 skill.
- `SKILL-main.md`: current upstream main skill.
- `scripts/aggregate-upstream-docs.sh`: reusable aggregate generator.
- `generation/upstream-docs-main.md`: full main docs aggregate.
- `generation/upstream-docs-v0.11.1.md`: full v0.11.1 docs aggregate.
- `generation/generation-steps.md`: this reproducibility log.

## Regenerating For A Future Version

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
