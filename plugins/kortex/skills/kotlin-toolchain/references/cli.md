# CLI, Wrapper, And Provisioning

Kotlin Toolchain `v0.12.0`.

## Getting The CLI

The `./kotlin` wrapper is two small files, `kotlin` and `kotlin.bat`, checked into the project root. Anyone who clones
the project can run `./kotlin build` with no installation.

Global install, when there is no wrapper yet:

- SDKMAN: `sdk install kotlintoolchain`
- macOS/Linux script: `curl -fsSL https://kotl.in/install.sh | sh`
- Windows: `powershell -ExecutionPolicy ByPass -c "irm 'https://kotl.in/install.ps1' | iex"`

The script installs into `~/.local/bin` and updates `PATH`; restart the shell afterwards.

The IntelliJ IDEA new-project wizard generates the wrapper. Creating a `module.yaml` in a blank project makes IDEA
offer to add it.

## Project-Local Version Detection

New in `0.12`. A globally installed wrapper does not blindly use its own version. It walks up from the current
directory looking for a directory that has `project.yaml` or `module.yaml` **and** its own `kotlin` wrapper. If it
finds one, it reads that wrapper's version and distribution checksum and uses those instead.

So the project wrapper is the source of truth even when the command is a global `kotlin`.

## Commands

Discover everything with `kotlin --help` and `kotlin <command> --help` rather than guessing.

| Command | What it does |
|---|---|
| `kotlin init` | Create a new project from a template |
| `kotlin build` | Compile and link all code |
| `kotlin run [-m <module>]` | Run an application module |
| `kotlin test` | Run tests |
| `kotlin package` | Produce a distributable artifact. `jvm/app` and `android/app` only; `jvm/lib` gains it when Maven Central publishing is on |
| `kotlin publish <repoId\|mavenCentral\|mavenLocal>` | Publish modules |
| `kotlin check [names]` | Run registered checks; `--skip tests`, `-m <module>` |
| `kotlin do <command>` | Run a custom command registered by a plugin |
| `kotlin task :<module>:<task>@<pluginId>` | Run one task directly, for debugging plugins |
| `kotlin show modules\|settings\|dependencies\|tasks\|checks\|commands` | Introspect the effective model |
| `kotlin clean` | Remove build output and caches |
| `kotlin update [--dev]` | Update the wrapper and distribution to the latest release |
| `kotlin generate-completion <bash\|zsh\|fish>` | Emit a shell completion script |
| `kotlin tool convert-project` | Convert a Maven reactor |
| `kotlin tool generate-keystore` | Create an Android signing keystore |
| `kotlin tool xcode-integration` | Xcode build-phase entry point |

`show settings -m <module>` prints the effective configuration after templates and platform propagation are merged. It
is the fastest way to answer "what does this module actually build with".

Tab completion covers command names, module names in `-m/--module`, and `do` command names.

### Publish Selection

`kotlin publish <repoId>` publishes every module that has publishing enabled and declares that repository. To narrow
it:

```shell
kotlin publish -m my-lib --transitive someRepoId
```

`-m`/`--module` can repeat. Since `0.12`, when the selected modules depend on other local modules the command stops
and asks you to pass `--transitive` or `--non-transitive` — it will not decide for you.

### Removed Options

`--root`, `--build-output`, and `--shared-caches-root` were removed in `0.12` (KTC-5419). Scripts and CI jobs that
still pass them fail. `--shared-cache-dir` (or `KOTLIN_SHARED_CACHE_DIR`) is the documented replacement for the last
one; `--project-dir` covers the project root. The upstream docs do not enumerate CLI options, so confirm the exact
spelling with `kotlin <command> --help` before editing a CI script.

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
`KOTLIN_SHARED_CACHE_DIR` or `--shared-cache-dir`, which wins over the variable. Both are new in `0.12`; the regular
cache does respect XDG on Linux.

Provisioning is safe to run concurrently. Parallel CLI invocations do not disturb each other.

## Other Environment Variables

| Variable | Effect |
|---|---|
| `KOTLIN_CLI_NO_WELCOME_BANNER` | Any non-empty value silences the first-run banner. Useful in CI. |
| `KOTLIN_CLI_JAVA_OPTIONS` | JVM options for the CLI process itself |
| `KOTLIN_CLI_JAVA_HOME` | Use this JRE for the CLI instead of provisioning one. You own its validity. |
| `KOTLIN_CLI_DOWNLOAD_ROOT` | Maven root to fetch the distribution from; defaults to `https://packages.jetbrains.team/maven/p/amper/amper` |

The last three are documented as "use at your own risk". The CLI being a JVM application is an implementation detail
that may change.

## JDK Provisioning

By default the toolchain does not constrain the JDK vendor but expects a specific major version: **25** in `0.12`
(21 in `0.11.x`). With the default `selectionMode: auto` it checks `JAVA_HOME` first and provisions a matching JDK via
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

Supported distributions in `0.12`: `temurin`, `zulu`, `corretto`, `jetbrains`, `oracleOpenJdk`, `microsoft`,
`dragonwell`, `liberica`, `sapMachine`, `semeru`, `graalVM`, and `oracleGraalVM` (requires license).

`0.11.x` accepted `bisheng`, `kona`, `openLogic`, `oracle`, `zuluPrime`, and `semeruCertified`. Those are gone in
`0.12`. `oracle` (Oracle JDK, licensed) has no drop-in replacement: `oracleOpenJdk` is Oracle's free OpenJDK build,
`oracleGraalVM` is a licensed but different JDK. Pick by intent or drop the constraint.

Restricting `distributions` to a paid vendor without listing it in `acknowledgedLicenses` is an error.
