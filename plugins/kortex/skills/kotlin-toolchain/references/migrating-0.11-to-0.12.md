# Migrating `0.11.x` → `0.12.0`

Released 2026-08-25. Upstream release notes:
<https://github.com/JetBrains/kotlin-toolchain/releases/tag/v0.12.0>

Run the upgrade with `./kotlin update`, then work through the list below. Everything here is a *breaking* change or a
rename — additive features are in the last section.

## Environment Requirements

| Requirement | `0.11.x` | `0.12.0` |
|---|---|---|
| JDK to run the toolchain | — | **17 minimum** (KTC-5708) |
| Default JDK for builds | 21 | **25** (KTC-5221) |
| `settings.kotlin.version` floor | — | **2.2.20 minimum** (KTC-5712) |

A build machine on JDK 11 or a module pinned to Kotlin 2.1 stops working. New diagnostics report both cases, plus JDK
and Kotlin version compatibility mismatches.

## CLI

Removed options (KTC-5419) — CI scripts using them fail outright:

| Removed | Use instead |
|---|---|
| `--root` | `--project-dir` |
| `--build-output` | — |
| `--shared-caches-root` | `--shared-cache-dir` or `KOTLIN_SHARED_CACHE_DIR` |

Only `--shared-cache-dir` is documented upstream; the rest come from the release notes. Verify with
`kotlin <command> --help`.

`kotlin publish -m <module>` now requires an explicit `--transitive` when the selected modules depend on other local
modules (KTC-5729). The `--modules` option of `publish` was renamed to align with `--module` elsewhere (KTC-5728).

A globally installed wrapper now resolves the project's own wrapper version by walking up the directory tree, instead
of using its own.

## Paths

`//` project-root paths are supported (KTC-2139) and are now the documented default for module dependencies,
`apply:` template refs, plugin refs, and KSP processor paths.

```yaml
# 0.11.x
dependencies:
  - ../shared
apply:
  - ../common.module-template.yaml

# 0.12.0
dependencies:
  - //shared
apply:
  - //common.module-template.yaml
```

Relative paths still work for module dependencies but upstream flags them as possibly deprecated later. Two places
where `//` must **not** appear: `project.yaml`'s `modules:` list (root-relative already, and `//` is rejected), and
anything still running on `0.11.x`.

Windows users on `0.11.x` who saw `//` treated as a UNC path: fixed in `0.12` (KTC-5748).

## Product Types

| `0.11.x` | `0.12.0` |
|---|---|
| `wasmJs/app` | `wasm-js/app` |
| `wasmWasi/app` | `wasm-wasi/app` |

The `0.11.x` docs disagreed with themselves: the `module.yaml` reference table listed `wasmJs/app`/`wasmWasi/app` while
the product-type page already wrote `wasm-js/app`/`wasm-wasi/app`. `0.12` uses only the hyphenated names. Check what a
pinned `0.11.x` CLI actually accepts before calling this a clean rename.

`ios/app` no longer accepts `iosX64` (KTC-5462). `macos/app` dropped `macosX64` from its defaults (KTC-5702).
`watchosArm32` is deprecated (KTC-5552), as are `macosX64` and `tvosX64`.

An `ios/app` module listing `iosX64` fails. Remove it — `kmp/lib` may keep it, though Compose libraries do not support
it.

## Settings Renames

| `0.11.x` | `0.12.0` |
|---|---|
| `settings.mavenCentral.publishingMode` | `settings.publishing.mavenCentral.publishingMode` |
| `settings.android.maxSdk` | deprecated (KTC-5625) |
| `settings.kotlin.optIns` as an enum list | plain string list |
| `settings.kotlin.serialization.format` as an enum | plain string |

Genuinely new settings in `0.12`: `settings.kotlin.dataframe`, `settings.kotlin.compileIncrementally`,
`settings.kotlin.optimization`, `settings.kotlin.linkerOptions`, `settings.android.resourcePackaging`, and
`settings.android.buildToolsVersion`.

Not new, despite what a quick diff suggests: `settings.publishing`, `settings.kotlin.powerAssert`, and
`settings.kotlin.rpc` all existed in `0.11.x`. Only `publishingMode` actually moved, and the `0.12` reference documents
the other two more fully than `0.11` did.

`settings.kotlin.debug` no longer defaults to `true` flat — it is enabled in debug variants, and `optimization` in
release variants.

## Default Version Bumps

| Setting | `0.11.1` | `0.12.0` |
|---|---|---|
| `settings.kotlin.version` | 2.3.20 | 2.4.10 |
| `settings.compose.version` | 1.10.3 | 1.11.1 |
| `settings.compose.experimental.hotReload.version` | 1.0.0 | 1.2.0 |
| `settings.kotlin.serialization.version` | 1.10.0 | 1.11.0 |
| `settings.kotlin.ksp.version` | 2.3.6 | 2.3.11 |
| `settings.ktor.version` | 3.4.1 | 3.5.2 |
| `settings.springBoot.version` | 4.0.5 | 4.1.0 |
| `settings.lombok.version` | 1.18.38 | 1.18.46 |
| `settings.jvm.test.junitPlatformVersion` | 6.0.1 | 6.1.3 |
| `settings.android.compileSdk` | 36 | 37 |
| `settings.android.minSdk` | 21 | 24 |

`minSdk` jumping from 21 to 24 changes what devices an app supports. Pin it explicitly if that matters.

The Ktor dependency catalog is now aligned with the published `ktor-version-catalog` (KTC-5237), so some Ktor catalog
entry names changed.

## Spring Boot

`settings.springBoot: enabled` no longer adds `spring-boot-starter` and `spring-boot-starter-test`. Declare them:

```yaml
dependencies:
  - org.springframework.boot:spring-boot-starter-web

test-dependencies:
  - org.springframework.boot:spring-boot-starter-test
```

The `no-arg` plugin now uses the `jpa` preset rather than `spring`.

## JDK Distributions

Removed from the allow-list: `bisheng`, `kona`, `openLogic`, `oracle`, `zuluPrime`, `semeruCertified`.
Added: `graalVM`, `oracleGraalVM`.

`oracle` was "Oracle JDK; requires license". There is no drop-in replacement: `oracleOpenJdk` is the free Oracle
OpenJDK build and needs no licence acknowledgement, `oracleGraalVM` is the licensed one but a different JDK. Pick the
one that matches the intent, or drop the constraint. Update `acknowledgedLicenses` to match whichever you choose.

## Templates

Templates may now apply other templates. That is additive, but the new sibling-conflict rule is not: two templates
with no precedence relationship that set the same scalar to different values now **fail the build**. A `0.11.x`
project that relied on `apply:` ordering to pick a winner will break.

Fix it by setting the value in `module.yaml`, or by introducing a template that applies both and decides. See
`templates.md`.

## Xcode Integration

The script build phase was renamed from `Build Kotlin with Amper` to `Build Kotlin`, and the marker comment from
`# !AMPER KMP INTEGRATION STEP!` to `# !KOTLIN INTEGRATION STEP!`.

The `FRAMEWORK_SEARCH_PATHS` entry pointing at `$(TARGET_BUILD_DIR)/AmperFrameworks` is no longer required. A specific
Xcode scheme is now enforced for the project (KTC-5687).

Hand-migrated Xcode projects need updating.

## Plugin Authors

| `0.11.x` | `0.12.0` |
|---|---|
| `${module.sources}` | `${module.kotlinJavaSources}` |
| `markOutputAs` | top-level `generated:` block |

New: `${module.classes}`, `generated.cinteropDefinitions`, and `listOf(...)` with constant elements as a sequence
default. Task action parameters need explicit types in their defaults.

## Elsewhere

- YouTrack moved from the `AMPER` project to `KTC`. Old issue links redirect, but new reports go to `KTC`.
- The IDEA plugin ID changed from `23076-amper` to `31850-kotlin-toolchain`. The docs give IntelliJ IDEA 2026.1.2+ as
  the plugin's floor; the release notes say the `0.12` IDE improvements specifically require 2026.2.1.
- The plugin tutorial repository moved to `JetBrains/kotlin-toolchain-plugins-tutorial`.
- `project.yaml`'s `modules:` examples dropped the `./` prefix. Both forms still parse; upstream now writes bare
  paths and recommends sorting them alphabetically.
- Glob patterns in `modules:` gained `{a,b}` alternation and `[!abc]` negation.
- Android `package` docs now say R8 rather than ProGuard for minification and obfuscation.
- The `0.11.x` product table advertised `linuxX86` for `linux/app`, which `0.12` corrects to `linuxX64`. A build that
  copied `linuxX86` from that table needs fixing.

## What You Gain

Worth knowing about after the upgrade:

- **KMP library publishing** actually works, with Gradle module metadata, commonized cinterop bindings, and KMP
  metadata compilation (KTC-719, KTC-721, KTC-5272, KTC-5437).
- **Incremental Kotlin compilation** for JVM targets, on by default with Kotlin ≥ 2.4.0 (KTC-4511).
- `wasm-js/app` packaging grew up: `build` went from a bare `.wasm` plus loader to a full browser bundle, and `run` is
  new — it serves the app locally and opens a browser. Plus a customizable `index.html` and transitive NPM
  dependencies, installed with `pnpm` (KTC-5569, KTC-5570, KTC-5572).
- Repository overriding and disabling, `mavenLocal` for both resolve and publish, dependency classifiers and packaging
  types, and `swiftPackage:`/`localSwiftPackage:` dependencies for `ios/app`.
- `settings.android.resourcePackaging` for duplicate Java resource failures.
- Tab completion for `do` command names and `-m/--module` values.
- Compose Hot Reload from the standalone CLI with a filesystem watcher, plus an MCP server for it.
- A JVM+Android common fragment can depend on plain JVM libraries; a `jvm/lib` can be an Android fragment dependency.
- Bundled C headers in a sibling `include` directory are picked up by `cinterop` with no configuration.
- A missing credentials file no longer breaks an unrelated build.
