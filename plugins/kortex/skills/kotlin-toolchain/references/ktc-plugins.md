# Local Plugin Distribution and Catalog

[Heapy/ktc-plugins](https://github.com/Heapy/ktc-plugins) installs Kotlin Toolchain build plugins from GitHub as
local source modules. Each plugin below has its own repository, dependency pins, examples, and verification.
This is separately maintained Heapy integration guidance for Toolchain **0.13.0**, outside the upstream snapshot.

Toolchain still loads local `jvm/amper-plugin` modules. The installer downloads sources at an explicit Git ref,
records their resolved commit and hashes, registers the module in `project.yaml`, and optionally enables it in a
consumer module. It does not add support for Maven-published build plugins. See [plugin authoring](plugins.md)
for the Toolchain API itself.

## Install and Enable

Obtain `ktc-plugins` using its [build and launcher instructions](https://github.com/Heapy/ktc-plugins#build-and-run-locally).
The examples assume a working `./ktc-plugins` launcher in the consumer root. A source build can select its executable
through `KTC_PLUGINS_BINARY`; published launchers, when available, pin and verify a release executable. The native
installer needs Git and curl; the subsequent build uses the consumer's Kotlin Toolchain wrapper.

For an existing JVM module named `library`:

```sh
./ktc-plugins add Heapy/ktc-bcv --branch main --enable-in library
./kotlin do apiDump -m library
./kotlin check apiCheck -m library
```

Review and commit the initial `library/api/library.api` baseline. Later checks fail on any API snapshot difference;
run `apiDump` only after reviewing an intended change.

`--branch main` is an explicit moving update source; the generated lockfile pins the exact installed commit. For an
immutable declaration use `--commit` with a verified full 40-character SHA, or select a published tag with `--tag`.
`--enable-in` takes a module directory relative to the project root. Without it, enable the plugin manually after
installation. The effective registration and activation for this example are:

```yaml
# project.yaml (merge with existing entries)
modules:
  - library
  - plugins/bcv
plugins:
  - //plugins/bcv
```

```yaml
# library/module.yaml
plugins:
  bcv: enabled
```

The installer performs that registration; do not add duplicate entries. Each plugin's README documents its settings.
Optional repository-level `templates/*.module-template.yaml` files can be copied and reviewed separately; installing
the plugin module does not install those sibling templates. Registration still belongs in `project.yaml`.

## Producer Descriptor and Consumer Lock

Each of the six catalog repositories contains a root **`ktc-plugin.yaml`**. For example:

```yaml
schemaVersion: 1
plugins:
  bcv:
    module: plugins/bcv
    licenseFiles: [LICENSE]
```

This is installer metadata: the map key is a producer selector, `module` identifies the self-contained source
module, and `licenseFiles` identifies repository-relative files to preserve. A single entry is selected
automatically; use `--plugin SELECTOR` when a producer offers several. Only declare files that exist.

The producer descriptor is distinct from the module's **`plugin.yaml`**, which registers Toolchain tasks, checks,
and commands. It is also distinct from the consumer's **`ktc-plugins.yaml`** declarations and generated
**`ktc-plugins.lock.yaml`**, which record the source/ref and resolved installation. `--name` changes the consumer
entry's alias, not the Toolchain plugin ID. Keep the source directory basename for plugins whose ID is inferred.

Installation defaults to `vendored`: commit the installed source, preserved licenses, manifests, lockfile, and
project/module changes. `--mode downloaded` instead keeps the plugin source ignored; commit the manifests, lockfile,
registration, launcher, and parent `plugins/.gitignore`, then restore sources before invoking Toolchain:

```sh
./ktc-plugins sync
./ktc-plugins verify
./kotlin check
```

`sync` restores the locked commit and does not advance a branch. `update bcv --dry-run` previews an update;
`update bcv` resolves the declared ref and advances the lock. Both refuse to overwrite locally modified installed
files. `status` and `verify` are read-only. `sync --offline` needs a cached source archive if sources are missing.
The installer does not commit changes or execute plugin code; subsequent builds execute the installed plugin.

Installer 0.2.0 introduced opt-in producer catalogs through `catalog.file` and `catalog.export` in the producer
descriptor. Declared producer `$libs.*` dependencies resolve to pinned coordinates; explicitly exported libraries
become managed consumer catalog entries. External local helper modules/templates remain unsupported. A source
module can also be installed without a descriptor by supplying `--path` and `--license-file`; consult the installer
README for the full contract.

Installer **0.3.0** also preserves producer `version.ref` aliases as shared managed consumer version entries.
Existing lockfiles still restore with `sync`; an explicit `update` adopts version references. New lockfiles containing
`catalog.versionRefs` require installer 0.3.0 or newer, so upgrade consumer launchers before adopting that format.
Producer descriptors remain `schemaVersion: 1` with `module`, `licenseFiles`, and optional `catalog`; no producer
manifest version bump or installer-version field is required.

## Available Plugins

All six repositories below target Kotlin Toolchain **0.13.0**. The selector is also the activation key under
`plugins:`; each module lives at `plugins/<selector>` in its producer repository.

| Repository | Selector | Engine | Checks and explicit commands | Scope |
|---|---|---|---|---|
| [Heapy/ktc-ktlint](https://github.com/Heapy/ktc-ktlint) | `ktlint` | ktlint 1.8.0 | `check ktlintCheck`; `do ktlintFormat` | Kotlin lint and formatting; includes module source/test roots |
| [Heapy/ktc-ktfmt](https://github.com/Heapy/ktc-ktfmt) | `ktfmt` | ktfmt 0.64 | `check ktfmtCheck`; `do ktfmtFormat` | Kotlin formatting with an isolated worker; needs a full JDK 17+ |
| [Heapy/ktc-bcv](https://github.com/Heapy/ktc-bcv) | `bcv` | binary-compatibility-validator 0.18.1 | `check apiCheck`; `do apiDump` | Public JVM API snapshots, including Kotlin visibility metadata |
| [Heapy/ktc-kover](https://github.com/Heapy/ktc-kover) | `kover` | Kover 0.9.11 | `check koverCheck`; `do koverReport` | Per-module JVM line coverage, HTML/XML reports, minimum threshold |
| [Heapy/ktc-dokka](https://github.com/Heapy/ktc-dokka) | `dokka` | Dokka 2.2.0 | `do dokkaHtml` | One HTML API site per JVM main compilation |
| [Heapy/ktc-jib](https://github.com/Heapy/ktc-jib) | `jib` | Jib Core 0.28.2 | `do jibTar`; `do jibPublish` | JVM application JARs packaged as Linux container images |

Run these through `./kotlin`, selecting a consumer with `-m <module>`. Checks join ordinary `./kotlin check`;
formatting, baseline updates, documentation, and image publication require explicit commands.

### Compatibility Boundaries

- **Formatting:** choose a formatting policy for each set of files. ktlint and ktfmt can disagree on formatting.
  Their pinned parsers reject Kotlin 2.4 bracket destructuring such as `val [a, b] = value`, even when the consumer
  compiler accepts it. Kotgent trials verified compatible modules, failed checks without source changes, and
  idempotent formatting; they do not establish full Kotlin 2.4 syntax coverage. A multi-module format can finish
  compatible modules before another module fails parsing.
- **API:** BCV checks snapshot equality, including compatible additions. It is a review gate, not a semantic
  classification of binary/source compatibility. This adapter does not validate Native/KLib, JS, Android variants,
  Swift, or TypeScript API.
- **Coverage:** Kover runs fresh instrumented JVM tests in a separate build directory. A plain `check` also runs
  ordinary tests, so tests run twice. Initial scope excludes Windows hosts, Android, Native/JS coverage,
  cross-module aggregation, and branch thresholds.
- **Documentation:** Dokka uses its CLI and symbols engine directly. Dokkatoo is a Gradle integration and is not
  required. No aggregated site or multiplatform source-set hierarchy is configured. Offline mode is enabled by
  default, so external API links may be unavailable.
- **Containers:** Jib requires a JVM application, an explicit `mainClass`, and an image reference. Use a JRE base
  compatible with the application's JVM release; the repository's `scratch` example is an archive fixture and
  cannot run Java. Pin production base images by digest. Registry publication requires explicit enablement,
  credential environment variable names, and `do jibPublish`; neither an ordinary build nor `jibTar` publishes.
  Kotlin/Native executables are outside this adapter's scope.

The 2026-10-05 trials used isolated [Kotgent](https://github.com/Heapy/kotgent) worktrees at
`ac1f35a21af210c0579b3536f326da96dace0ebb`. BCV, Kover, and Dokka passed on existing JVM build-plugin modules;
Kover measured 58/87 executable lines with 10 existing tests. Jib packaged a temporary JVM adapter using unchanged
Kotgent code and its runtime dependencies. The extracted application classpath ran on the host JVM, but container
execution and registry publication were not verified. Kotgent's daemon is Native-only. Each plugin README records
the full trial results and limitations.

### Other Heapy Local Plugins

These repositories predate the six adapters above; follow their own version and installation guidance:

| Repository | Module / activation key | Purpose and declared baseline |
|---|---|---|
| [Heapy/ktc-quarkus](https://github.com/Heapy/ktc-quarkus) | `plugins/quarkus` / `quarkus` | Quarkus augmentation, packaging, and development commands; Toolchain 0.12.2, Quarkus 3.39.4 |
| [Heapy/detekt-config](https://github.com/Heapy/detekt-config) | `plugins/heapy-detekt` / `heapy-detekt` | Heapy analysis/formatting policy and custom detekt rules; Toolchain 0.12.x, detekt 2.0.0-alpha.6 |

These are separate integrations, not evidence that every listed plugin supports every Toolchain version. The detekt
policy includes ktlint rules; account for that overlap when choosing a formatter.
