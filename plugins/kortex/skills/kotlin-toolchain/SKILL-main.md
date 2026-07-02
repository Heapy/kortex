---
name: kotlin-toolchain-main
description: Current upstream main snapshot for JetBrains Kotlin Toolchain docs. Use when a project explicitly tracks main/dev behavior instead of the default v0.11.x skill.
---

# Kotlin Toolchain Main Snapshot

Use this skill for JetBrains Kotlin Toolchain work: declarative YAML build configuration, the `kotlin` CLI, product
types, dependency wiring, multiplatform layout, publishing, and local build plugins.

Do not apply this skill to Gradle or Maven build editing unless the task is explicitly about converting a Maven project
to Kotlin Toolchain. Kotlin Toolchain is not Gradle, not Maven, and not the old Amper `*.amper` syntax.

## Source Snapshot

This file is generated from the official upstream docs on `main`:

- Repository: `https://github.com/JetBrains/kotlin-toolchain`
- Ref: `main`
- SHA: `a049d011217fc302fcdece9c7d0f48eac184a88d`
- Full aggregate: `generation/upstream-docs-main.md`
- Generation notes: `generation/generation-steps.md`

The project is Alpha and the docs move quickly. Treat defaults and edge-case syntax as version-sensitive. When precision
matters, inspect the local project, run `./kotlin show ...`, and grep the pinned aggregate (it is ~300 KB — search it,
do not read it whole) or the current upstream docs.

Internal names still contain `Amper` in expected places: `jvm/amper-plugin`, `org.jetbrains.amper.plugins`, AMPER
YouTrack, and some distribution paths. Do not rename those to `kotlin`.

## Default Version

This file preserves the upstream `main` snapshot. The default `SKILL.md` currently targets `v0.11.x`; use this file only
when the user or repository explicitly tracks current main/dev behavior.


## First Moves

When working in a repo:

1. Inspect `module.yaml`, `project.yaml`, `*.module-template.yaml`, `libs.versions.toml`, `plugin.yaml`, and wrapper
   scripts before suggesting edits.
2. Prefer project-local `./kotlin` over a global `kotlin` command when the wrapper exists.
3. Use `./kotlin show modules|settings|dependencies|tasks|checks|commands` to understand the effective model.
4. Keep YAML declarative. Do not invent loops, conditionals, Gradle task wiring, or Maven lifecycle behavior.
5. Use current path notation: `//` for project-root paths in module dependencies, templates, plugin refs, and other
   path values; plain paths in `project.yaml.modules`.

Useful CLI commands:

- `./kotlin init`
- `./kotlin build`
- `./kotlin run -m <module>`
- `./kotlin test`
- `./kotlin package`
- `./kotlin publish <repoId|mavenCentral>`
- `./kotlin check [names] [--skip tests] [-m module]`
- `./kotlin do <command> [-m module]`
- `./kotlin task :<module>:<task>@<pluginId>` for debugging plugin tasks
- `./kotlin clean`
- `./kotlin update [--dev]`
- `./kotlin generate-completion <bash|zsh|fish>`

Wrapper/provisioning environment variables include `KOTLIN_CLI_BOOTSTRAP_CACHE_DIR`,
`KOTLIN_CLI_NO_WELCOME_BANNER`, `KOTLIN_CLI_JAVA_OPTIONS`, `KOTLIN_CLI_JAVA_HOME`, and
`KOTLIN_CLI_DOWNLOAD_ROOT`. The CLI is currently JVM-based, but this is an implementation detail.

## Installation And Wrapper

The `./kotlin` wrapper may not exist yet. To obtain the CLI:

- Global install: `sdk install kotlintoolchain` (SDKMAN), or the installer script
  `curl -fsSL https://kotl.in/install.sh | sh` (macOS/Linux) or
  `powershell -ExecutionPolicy ByPass -c "irm 'https://kotl.in/install.ps1' | iex"` (Windows). The script installs the
  wrapper into `~/.local/bin` and updates `PATH`; restart the shell afterward.
- Per-project wrapper: the IntelliJ IDEA new-project wizard generates the wrapper scripts, and creating a `module.yaml`
  in a blank project makes IDEA offer to add them. The wrapper is two small files, `kotlin` and `kotlin.bat`; check them
  into the project root so anyone can run `./kotlin` without a global install.
- Discover commands and flags with `kotlin --help` and `kotlin <command> --help` rather than guessing.

## Project Model

A project is rooted at `project.yaml`. A module is a directory containing `module.yaml`. A single-module project does
not need `project.yaml`; a root `module.yaml` is included implicitly in a multi-module project.

Each module produces exactly one product. Sources and resources belong to one module; modules share code by depending
on each other.

```yaml
# project.yaml
modules:
  - app
  - libs/lib1
  - plugins/*

plugins:
  - //plugins/build-config
```

`modules:` entries are root-relative path globs. They support `*`, `?`, and `[abc]`/`[a-z]`, but not recursive `**`.
Do not write `//app` in `modules:`.

Common `module.yaml` keys:

- `product`
- `dependencies` and `test-dependencies`
- `settings` and `test-settings`
- `repositories`
- `apply`
- `aliases`
- `layout`
- `description`
- `plugins`
- `pluginInfo` for `jvm/amper-plugin` modules

Path notation:

- Use `/` as the separator on all platforms.
- `//path` resolves from the project root and is preferred for module dependencies, templates, plugin dependencies, and
  other project paths.
- `./path` and `../path` resolve relative to the YAML file declaring them.
- Bare values like `my-lib` in `dependencies:` are parsed as external dependencies, not local modules.

Layouts:

- `amper` is the default: `src`, `test`, `resources`, `testResources`.
- `maven-like` preserves Maven/Gradle-style `src/main/kotlin`, `src/main/java`, `src/test/kotlin`, etc. It is only
  supported for `jvm/app` and `jvm/lib`.

## Product Types

Use short form when no platform list is needed:

```yaml
product: jvm/app
```

Use full form for explicit platforms:

```yaml
product:
  type: kmp/lib
  platforms: [jvm, android, iosArm64, iosSimulatorArm64, iosX64]
```

Current product types:

- `jvm/app`: JVM console, desktop, or server app. Default entry point is top-level `main` in case-insensitive
  `src/main.kt`; override with `settings.jvm.mainClass`. `package` creates an executable Spring Boot loader style JAR.
- `jvm/lib`: JVM library. `publish` can publish to Maven repositories; `package` is only meaningful by default when
  Maven Central publishing is enabled.
- `kmp/lib`: Kotlin Multiplatform library. `product.platforms` must list concrete leaf platforms, not family names.
  KMP publication is not supported/consumable in the current docs.
- `android/app`: entry point in `src/AndroidManifest.xml`. `build` creates APK; `package` creates AAB with R8
  minification/shrinking and signing. `proguard-rules.pro` and `google-services.json` are auto-detected beside
  `module.yaml`.
- `ios/app`: requires `module.xcodeproj` beside `module.yaml`; entry point is a Swift `@main` struct in `src`. For
  manual Xcode migration, keep `KOTLIN_CLI_WRAPPER_PATH`, the `Build Kotlin with Amper` phase, and framework search
  paths. Swift can reference Kotlin through `KotlinModules`; Kotlin cannot currently reference Swift.
- `js/app`: incomplete preview. `build` emits `.mjs` in `build/tasks/_<module>_linkJs`; run it with an external JS
  runtime, not the CLI.
- `wasmJs/app` and `wasmWasi/app`: incomplete preview. `build` emits `.wasm` plus a `.mjs` loader in
  `build/tasks/_<module>_linkWasmJs` or `_linkWasmWasi`; run via an external JS runtime, not the CLI.
- `linux/app`, `macos/app`, `windows/app`: native apps. Default entry point is top-level `main` in `src/main.kt`;
  override with `settings.native.entryPoint`. Build output is a `.kexe` binary (`.exe` on Windows). `package` is not
  supported yet.
- `jvm/amper-plugin`: local Kotlin Toolchain plugin module.

## Dependencies

Dependency forms:

```yaml
dependencies:
  - //ui/utils
  - io.ktor:ktor-client-core:2.2.0
  - $libs.ktor.client.cio
  - $compose.material3
  - bom: io.ktor:ktor-bom:2.2.0
  - io.ktor:ktor-serialization-kotlinx-json
  - org.postgresql:postgresql:42.3.3: runtime-only
  - io.ktor:ktor-client-core:2.2.0:
      exported: true
      scope: compile-only
```

Scopes:

- `all`: compile and runtime, default.
- `compile-only`: compile only, like Maven `provided`.
- `runtime-only`: runtime only.

`exported` defaults to `false`. Use it when dependency types appear in the module's public API and consumers must see
them at compile time. Avoid exporting implementation-only dependencies.

Catalogs:

- Project catalog: one `libs.versions.toml` at the project root or `gradle/libs.versions.toml`, not both. Only
  `[versions]` and `[libraries]` are supported.
- Toolchain catalogs: `$kotlin`, `$compose`, `$kotlin.serialization`, `$kotlin.rpc`, etc. appear when their toolchain is
  enabled and use that toolchain's configured version.

Repositories:

- Maven Central and Google are defaults.
- Add entries under `repositories:` as strings or objects with `id`, `url`, and optional `credentials`.
- Username/password auth is read from a `.properties` file via keys such as `usernameKey` and `passwordKey`.
- `mavenLocal` is a special repository value.

## Multiplatform

The hierarchy starts at `common` and includes `jvm`, `android`, `web` (`js`, `wasmJs`), `wasmWasi`, and native families
such as `linux`, `mingw`, `apple`, and `androidNative`. Not all platforms are equally supported or tested.

Rules:

- `product.platforms` accepts leaf platform names only.
- Use `src@platform`, `resources@platform`, `test@platform`, `dependencies@platform`, and `settings@platform`.
- Common code is visible to more-specific code, not the reverse.
- Put `expect` declarations in `src`; put `actual` declarations in `src@<platform>`.
- Common dependencies and settings propagate down. Scalars are overridden by more-specific sections; maps and lists are
  appended/merged.
- `aliases:` can define custom platform groups, for example `jvmAndAndroid`, and those aliases can be used in source
  dirs and qualified sections.
- KMP library dependencies auto-resolve platform-specific artifacts.
- KSP runs per platform; generated code is not visible to common sources.
- C/Objective-C interop is usually configured by placing `.def` files under `cinterop` or `cinterop@platform` with no
  YAML.
- `settings.android` is Android toolchain settings; `settings@android` is platform-qualified settings. They are not the
  same thing.

## Settings Defaults

Defaults from the pinned `main` docs:

- Default JDK major version: 21
- `settings.android.compileSdk`: 37
- `settings.android.minSdk`: 21
- `settings.kotlin.version`: 2.3.20
- `settings.compose.version`: 1.10.3
- `settings.compose.experimental.hotReload.version`: 1.0.0
- `settings.kotlin.serialization.version`: 1.11.0
- `settings.kotlin.ksp.version`: 2.3.9
- `settings.jvm.test.junitPlatformVersion`: 6.0.1
- `settings.ktor.version`: 3.4.1
- `settings.lombok.version`: 1.18.38
- `settings.springBoot.version`: 4.0.5

Always re-check defaults for a real project with `./kotlin show settings` or the current docs.

JDK provisioning is controlled by:

```yaml
settings:
  jvm:
    jdk:
      version: 21
      distributions: [temurin, zulu]
      selectionMode: auto
```

Selection modes are `auto`, `alwaysProvision`, and `javaHome`. Paid vendors such as Oracle require explicit
`acknowledgedLicenses`.

## Compiler And Runtime Settings

Common keys beyond the version defaults:

- `settings.jvm.release`: minimum JVM release the code must be compatible with — the bytecode target plus Java API and
  language limits. This is the "target Java N" knob, defaulting from `jdk.version`. Do not repurpose `jdk.version` for
  it.
- `settings.jvm.mainClass`: fully-qualified entry-point class for `jvm/app`.
- `settings.jvm.runtimeClasspathMode`: `jars` (default) or `classes`.
- `settings.kotlin.languageVersion`, `apiVersion`, `freeCompilerArgs`, `allWarningsAsErrors`, `progressiveMode`,
  `suppressWarnings`: standard Kotlin compiler knobs. Pass `-X` flags through `freeCompilerArgs`, for example
  `freeCompilerArgs: [ -Xexpect-actual-classes ]`.
- JVM test process: `settings.jvm.test.systemProperties`, `freeJvmArgs`, and `extraEnvironment` (also under
  `test-settings.jvm`).

## Built-In Technologies

Prefer short settings forms unless customization is needed:

- `settings.compose: enabled`: Compose compiler/runtime/catalog. Add `$compose.foundation`, `$compose.material3`,
  `$compose.desktop.currentOs`, or `$compose.hotReload.runtimeApi`. `composeResources` generates accessors; package can
  be customized with `settings.compose.resources.packageName`.
- `settings.kotlin.serialization: json`: serialization compiler/runtime plus JSON. Use `serialization: enabled` when
  you want the runtime and catalog but want to scope format dependencies manually.
- `settings.kotlin.rpc: enabled`: kotlinx.rpc compiler plugin, BOM, and `$kotlin.rpc.*` catalog entries.
- `settings.ktor: enabled`: Ktor BOM/catalog and Ktor development property on `kotlin run`.
- `settings.lombok: enabled`: Lombok dependency, Java annotation processor, and Kotlin compiler plugin.
- `settings.springBoot: enabled`: Spring Boot BOM, starters, test starter, all-open/no-arg `spring` presets,
  `-parameters`/`-java-parameters`, `-Xjsr305=strict`, catalog entries, and classes runtime classpath mode for devtools.
- `settings.android.parcelize: enabled`: Parcelize. For common models, define common annotations/interfaces and list
  `additionalAnnotations`.
- `settings.java.annotationProcessing.processors`: Java annotation processors for JVM/Android.
- `settings.kotlin.ksp.processors`: KSP2 processors; local processor modules and processor options are supported.
- `settings.kotlin.compilerPlugins`: low-level third-party compiler plugin escape hatch with `id`, `dependency`, and
  `options`. IDE support is best effort.
- Compiler plugin shortcuts include all-open, no-arg, JS plain objects, Parcelize, Power Assert, Compose,
  serialization, RPC, and Lombok.

## Android Identity And Signing

- Identity keys under `settings.android`: `namespace`, `applicationId` (defaults from `namespace`), `versionCode`,
  `versionName`, `compileSdk`, `minSdk`, `targetSdk` (defaults from `compileSdk`), and `maxSdk`.
- Release signing: `settings.android.signing: enabled` reads `keystore.properties` beside `module.yaml`, containing
  `storeFile`, `storePassword`, `keyAlias`, and `keyPassword`. Override the path with `signing.propertiesFile`. Generate
  a keystore with `./kotlin tool generate-keystore`. Do not commit the keystore or `keystore.properties`.

## Testing

Tests live in `test` and platform-qualified `test@platform` directories. `kotlin.test` is configured by default for each
platform. Use `test-dependencies:` for test-only dependencies and `test-settings:` for test-specific toolchain settings.

`settings.junit` supports `junit-5` (default), `junit-4`, and `none`. JVM test settings live under `settings.jvm.test`
or `test-settings.jvm`.

## Templates

Template files are named `<name>.module-template.yaml` and have module-like structure, but cannot contain `product:`.
Apply them with `apply:`.

```yaml
apply:
  - //common.module-template.yaml
```

Current main docs support nested templates. Each template is applied once even if reached multiple ways. Merge rules are
the same as multiplatform propagation: scalars override, maps/lists append. Module content is applied last and wins.
Conflicting sibling scalar values fail unless resolved by the module or a more-specific intermediate template.

## Maven Migration And Maven Plugins

Maven conversion:

```shell
kotlin tool convert-project
```

Use from the Maven reactor root. It creates `project.yaml` and `module.yaml` files, keeps `layout: maven-like`, maps
reactor dependencies to module dependencies, imports BOMs and repositories, and extracts publishing coordinates.
Unknown Maven plugins become disabled `mavenPlugins` entries. Profiles, extensions, exclusions, classifiers, optional
dependencies, system dependencies, and variable substitution require manual work.

Maven plugin integration is JVM-only and best-effort. Declare plugin coordinates project-wide in
`project.yaml.mavenPlugins`, then enable goals per module:

```yaml
mavenPlugins:
  maven-surefire-plugin.test: enabled
```

Goals with default Maven phases are wired into the lifecycle where possible. Explicit task names look like
`:app:maven-surefire-plugin.test`. Multiple executions, Maven extensions, custom dependency resolution, and report
aggregation are not generally supported.

## Publishing

Publishing is preview. Current docs support complete JVM library publishing; KMP publishing is incomplete and not
consumable.

Regular Maven repository publishing needs:

- `product: jvm/lib`
- `repositories` entry with `publish: true` and credentials
- `settings.publishing.enabled`, `group`, and `version`

Maven Central also needs `mavenCentral: enabled`, `signArtifacts: true`, `publishSources: true`, and required POM
metadata. Credentials are provided through:

- `KOTLIN_TOOLCHAIN_MAVEN_CENTRAL_USERNAME`
- `KOTLIN_TOOLCHAIN_MAVEN_CENTRAL_PASSWORD`
- `KOTLIN_TOOLCHAIN_SIGNING_KEY`
- `KOTLIN_TOOLCHAIN_SIGNING_KEY_PASSPHRASE`

Publishing mode is `manual` by default. `auto` releases without manual inspection; released Maven Central artifacts are
effectively permanent.

## Plugin Authoring

Plugins are local modules with:

```yaml
product: jvm/amper-plugin
```

Register them in `project.yaml.plugins`, then enable per module:

```yaml
plugins:
  build-config: enabled
```

Plugin pieces:

- `module.yaml`: plugin product, optional `pluginInfo.id`, optional `pluginInfo.settingsClass`.
- `src`: Kotlin implementation.
- `plugin.yaml`: declarative task registration, generated outputs, checks, and commands.

Task actions are top-level public Kotlin functions annotated `@TaskAction`, returning `Unit`, not extension/generic/
suspend/inline/context-parameter functions. Parameters must be configurable types. Any parameter containing `Path` must
be marked `@Input` or `@Output`; built-in file request types such as `ModuleSources`, `Classpath`, and
`CompilationArtifact` are always `@Input`.

`plugin.yaml` uses type tags and references:

```yaml
tasks:
  generate:
    action: !com.example.generateSources
      propertiesFile: ${module.rootDir}/config.properties
      generatedSourceDir: ${taskOutputDir}

generated:
  sources:
    - language: kotlin
      directory: ${tasks.generate.action.generatedSourceDir}
```

References are currently for `plugin.yaml`, not `module.yaml`. Useful references include `pluginSettings`,
`module.name`, `module.rootDir`, `module.runtimeClasspath`, `module.compileClasspath`, `module.kotlinJavaSources`,
`module.resources`, `module.jar`, `module.self`, `module.settings.*`, `project.rootDir`, and `taskOutputDir`.

Task dependencies are inferred from matching input/output paths; there is no manual task-dependency syntax. Outputs can
contribute `generated.sources`, `generated.resources`, or `generated.cinteropDefinitions`, optionally with `fragment`.
Checks and custom commands are tasks registered under `checks:` and `commands:`.

Configurable plugin settings are public `@Configurable` interfaces with read-only properties. `enabled` is reserved.
Defaults are supported for constants, enums, empty lists/maps, and nulls; explicit `Path` defaults are not supported.
Shorthand notation does not currently work with references.

## Pitfalls

- Alpha means defaults and syntax drift; verify exact behavior against a tag, SHA, or installed toolchain.
- Do not remove expected `Amper` names from plugin/product/package references.
- Use `//` for module dependencies/templates/plugin refs, but not in `project.yaml.modules`.
- `product.platforms` requires leaf platform names, not family shortcuts.
- `settings.android` and `settings@android` are different.
- `layout: maven-like` is only for JVM-only modules.
- One module has one product; source folders are not shared across modules.
- KSP is KSP2-only in current docs, and generated code is platform-specific in KMP.
- JS and Wasm apps cannot be run directly by the CLI.
- Native, JS, and Wasm product types do not support `package`.
- iOS requires Xcode integration and `module.xcodeproj`.
- Compose Hot Reload is useful from the IDE; CLI does not watch the filesystem.
- Use `exported` sparingly to avoid leaking implementation dependencies into consumer compile classpaths.
- Maven Central `auto` publishing and released artifacts are permanent decisions.
