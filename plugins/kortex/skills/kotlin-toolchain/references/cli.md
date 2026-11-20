# CLI, Wrapper, And Provisioning

Kotlin Toolchain `v0.14.1`.

## Getting The CLI

The `./kotlin` wrapper is two small files, `kotlin` and `kotlin.bat`, checked into the project root. Anyone who clones
the project can run `./kotlin build` with no installation.

Global install, when there is no wrapper yet:

- SDKMAN: `sdk update`, then `sdk install kotlintoolchain`
- macOS/Linux script: `curl -fsSL https://kotl.in/install.sh | sh`
- Windows: `powershell -ExecutionPolicy ByPass -c "irm 'https://kotl.in/install.ps1' | iex"`

The script installs into `~/.local/bin` and updates `PATH`; restart the shell afterwards.

The IntelliJ IDEA new-project wizard generates the wrapper. Creating a `module.yaml` in a blank project makes IDEA
offer to add it. Since `0.13`, `kotlin update --create` creates missing wrappers without an interactive prompt;
use `--target-version 0.14.1` to pin the release rather than selecting whatever is latest.

## Refreshing Provisioned JDKs

The 0.14.1 wrapper launches the CLI with Azul Zulu 25.38.201 (JDK 25.0.5.1+2), patched for CVE-2026-114938.
Project builds can use separately provisioned JDKs. The toolchain caches the selected JDK indefinitely per
project. Run `./kotlin clean` in the project to refresh the selection on the next build.

For a machine-wide refresh, `./kotlin clean-shared-caches` also clears downloaded dependencies. To keep those
dependencies, locate the shared cache via `./kotlin --help` under `--shared-cache-dir`, then remove only the
cached JDK entries from both `download.cache` and `extract.cache`. [Upstream release notes](https://github.com/JetBrains/kotlin-toolchain/releases/tag/v0.14.1)

## Project-Local Version Detection

A globally installed wrapper does not blindly use its own version. It walks up from the current
directory looking for a directory that has `project.yaml` or `module.yaml` **and** its own `kotlin` wrapper. If it
finds one, it reads that wrapper's version and distribution checksum and uses those instead.

So the project wrapper is the source of truth even when the command is a global `kotlin`.

## Commands

Discover everything with `kotlin --help` and `kotlin <command> --help` rather than guessing.

| Command | What it does |
|---|---|
| `kotlin new <directory>` | Create a project in a new directory; prompts for a name when interactive |
| `kotlin init [--target-dir <existing-directory>]` | Initialize an existing directory; current directory by default |
| `kotlin build` | Compile and link all code |
| `kotlin run [-m <module>]` | Run an application module |
| `kotlin test` | Run tests |
| `kotlin package` | Package a JVM app JAR, Android AAB, or iOS IPA; `jvm/lib` gains a Central bundle when Central publishing is on |
| `kotlin publish <repoId\|mavenCentral\|mavenLocal\|testflight>` | Publish libraries, or upload one iOS app to TestFlight |
| `kotlin check [names]` | Run registered checks; `--skip tests`, `-m <module>` |
| `kotlin do <command>` | Run a custom command registered by a plugin |
| `kotlin task :<module>:<task>@<pluginId>` | Run one task directly, for debugging plugins |
| `kotlin show modules\|settings\|dependencies\|tasks\|checks\|commands` | Introspect the effective model |
| `kotlin clean` | Remove build output and caches |
| `kotlin update [--target-version <version> \| --dev] [--create]` | Update/create wrappers and verify the selected distribution; latest stable by default |
| `kotlin generate-completion <bash\|zsh\|fish>` | Emit a shell completion script |
| `kotlin tool convert-project` | Convert a Maven reactor |
| `kotlin tool generate-keystore` | Create an Android signing keystore |
| `kotlin tool xcode-integration` | Xcode build-phase entry point |
| `kotlin compose-hot-reload-mcp-server` | MCP connection to a running Compose Hot Reload app |

`show settings -m <module>` prints the effective configuration after templates and platform propagation are merged. It
is the fastest way to answer "what does this module actually build with".

Tab completion covers command names, module names in `-m/--module`, and `do` command names.

### Publish Selection

`kotlin publish <repoId>` publishes every module that has publishing enabled and declares that repository. To narrow
it:

```shell
kotlin publish -m my-lib --transitive someRepoId
```

`-m`/`--module` can repeat. When the selected modules depend on other local modules the command stops
and asks you to pass `--transitive` or `--non-transitive` — it will not decide for you.

`testflight` is a built-in destination for one `ios/app`; do not add it to `repositories`. Select an app with `-m`
when the project has several. `--transitive` is ignored. See [publishing](publishing.md#ios-apps-and-testflight).

### Tests And Tag Filters

```shell
./kotlin test -m core --include-tag 'fast & !flaky'
./kotlin test -m core --exclude-tag slow
./kotlin test -m web --platform wasmJs
```

`--include-tag` / `--exclude-tag` accept JUnit tag expressions (`!`, `&`, `|`, parentheses, `any()`, `none()`).
Repeated includes use OR; exclusions remove matches. Quote expressions for the shell. Only JVM and Android tests
have tags; other platforms count as untagged and run only when the expressions allow untagged tests. When any test
filter is used in a multi-module project, select modules with `-m` / `--include-module` or `--exclude-module`.

Wasm-JS tests run in Chromium provisioned via Playwright in `0.13`. A Wasm-WASI test runner is not registered.
Wasm class filters support `*`, not `?`; use package-qualified patterns rather than an uppercase class-name prefix.
These details come from the tagged CLI and test tasks; the tagged Wasm-JS Markdown still says tests are unsupported.

Since `0.14`, the command exits with code 1 if no tests are discovered across the selected tasks, including when
filters match nothing. The error lists the applied filters. Discovered but skipped tests count, so all-skipped tests
can still succeed. This is a discovery check in `test`, not a requirement that every module execute a test, and not
a new empty-test rule for `check`.

### Compose Runs

`kotlin run` automatically enables hot reload for eligible JVM Compose modules. Use `--no-compose-hot-reload` to
turn it off or `--compose-hot-reload` to request it explicitly. The old `--compose-hot-reload-mode` remains a hidden,
deprecated alias in `0.13`. See [built-in technologies](builtin-tech.md).

### Directory Options

Use `--project-dir` for the project root, `--build-dir` for build output, and `--shared-cache-dir` for the shared
cache. Confirm command-specific options with `kotlin <command> --help` before editing a CI script.

## Caches

Two distinct caches:

**Bootstrap cache** — the wrapper's copy of the CLI distribution.

| OS | Directory |
|---|---|
| macOS | `$HOME/Library/Caches/JetBrains/Kotlin/cli` |
| Linux | `$HOME/.cache/JetBrains/Kotlin/cli` |
| Windows | `%LOCALAPPDATA%\JetBrains\Kotlin\cli` |

Relocate with `KOTLIN_CLI_BOOTSTRAP_CACHE_DIR`. XDG conventions are not honored here.

**Shared cache** — downloaded dependencies, JDKs, and tools, shared across all projects. Relocate with
`KOTLIN_SHARED_CACHE_DIR` or `--shared-cache-dir`, which wins over the variable. The shared cache respects XDG on Linux.

Provisioning is safe to run concurrently. Parallel CLI invocations do not disturb each other.

## Other Environment Variables

| Variable | Effect |
|---|---|
| `KOTLIN_TOOLCHAIN_BUILD_DIR` | Build output root; `--build-dir` takes precedence. Replaces `AMPER_BUILD_DIR` in `0.13`. |
| `KOTLIN_CLI_NO_WELCOME_BANNER` | Any non-empty value silences the first-run banner. Useful in CI. |
| `KOTLIN_CLI_JAVA_OPTIONS` | JVM options for the CLI process itself |
| `KOTLIN_CLI_JAVA_HOME` | Use this JRE for the CLI instead of provisioning one. You own its validity. |
| `KOTLIN_CLI_DOWNLOAD_ROOT` | Maven root to fetch the distribution from; defaults to `https://packages.jetbrains.team/maven/p/amper/amper` |

The Java overrides are documented as "use at your own risk". The CLI being a JVM application is an implementation
detail that may change.

## Download Mirrors

`0.14` supports these environment overrides. Blank values are ignored; mirrors must preserve the upstream path
layout. Names and defaults are version-sensitive. A Maven repository override does not redirect every tool download.

| Variable | Controls |
|---|---|
| `KOTLIN_DEFAULT_MAVEN_CENTRAL_URL` | Default Maven Central repository, compiler artifacts, and tooling |
| `KOTLIN_TOOLCHAIN_GOOGLE_REPOSITORY_URL` | Google Maven dependencies and Android tooling |
| `KOTLIN_TOOLCHAIN_REPOSITORY_URL` | Toolchain tooling and plugin artifacts |
| `KOTLIN_TOOLCHAIN_KT_BOOTSTRAP_REPOSITORY_URL` | Kotlin development compiler and Native distributions |
| `KOTLIN_TOOLCHAIN_ANDROID_DOWNLOAD_URL_ROOT` | Android SDK and system-image downloads |
| `KOTLIN_TOOLCHAIN_JETBRAINS_DOWNLOAD_URL` | JetBrains CDN, including the JDK provisioning list |
| `KOTLIN_CLI_DOWNLOAD_ROOT` | Wrapper scripts and CLI distribution; `update --repository` wins |
| `KOTLIN_TOOLCHAIN_NODE_JS_DIST_URL` | Node.js distribution base URL |
| `KOTLIN_TOOLCHAIN_PNPM_DOWNLOAD_ROOT_URL` | pnpm release downloads |
| `KOTLIN_TOOLCHAIN_XCBEAUTIFY_DOWNLOAD_ROOT_URL` | xcbeautify release downloads |
| `KOTLIN_TOOLCHAIN_JAEGER_DOWNLOAD_ROOT_URL` | Jaeger release downloads |

For default URLs and appended paths, search `cli/provisioning.md` in the pinned aggregate. The installer script
self-updates in this release; that is separate from upgrading a project's pinned wrappers with `kotlin update`.

## JDK Provisioning

By default the toolchain does not constrain the JDK vendor but expects major version **25**. With the default
`selectionMode: auto` it checks `JAVA_HOME` first and provisions a matching JDK via
the Foojay Discovery API if that does not fit.

The toolchain itself now requires JDK 17 or newer to run.

```yaml
settings:
  jvm:
    jdk:
      version: 25
      distributions: [temurin, zulu]
      selectionMode: auto
      acknowledgedLicenses: []
```

| Property | Default | Meaning |
|---|---|---|
| `version` | toolchain default (25) | Major JDK version; the latest update in that line is preferred |
| `distributions` | `null` | Allow-list of vendors; `null` accepts any known distribution |
| `selectionMode` | `auto` | `auto`, `alwaysProvision`, or `javaHome` |
| `acknowledgedLicenses` | `[]` | Distributions whose commercial license you accept |

Selection modes:

- `auto` — use `JAVA_HOME` if it matches, otherwise provision.
- `alwaysProvision` — ignore `JAVA_HOME`, always use the toolchain-managed JDK (downloading it the first time).
- `javaHome` — require `JAVA_HOME` to match and fail otherwise. Provisioning is disabled.

Supported distributions: `temurin`, `zulu`, `corretto`, `jetbrains`, `oracleOpenJdk`, `microsoft`,
`dragonwell`, `liberica`, `sapMachine`, `semeru`, `graalVM`, and `oracleGraalVM` (requires license).

Restricting `distributions` to a paid vendor without listing it in `acknowledgedLicenses` is an error.
