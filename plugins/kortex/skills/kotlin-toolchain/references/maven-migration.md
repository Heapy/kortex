# Maven Migration And Maven Plugins

Kotlin Toolchain `v0.12.0`.

There is no conversion tool for Gradle projects at the moment: Gradle build scripts contain arbitrary code, which
makes a deterministic conversion hard to define. Upstream suggests AI agents for that job instead. Maven does have a
converter.

## Converting A Maven Project

Run from the Maven reactor root:

```shell
kotlin tool convert-project
```

Options:

- `--pom <path>` — point at a specific `pom.xml` instead of the current directory. If it belongs to a reactor, all
  related modules are converted.
- `--overwrite-existing` — by default the converter fails when `project.yaml` or a `module.yaml` already exists.
- `--enable-compatibility-plugins` — generate unknown Maven plugin entries already enabled, accepting the risk of
  untested plugin configurations. New in `0.12`.

### What It Produces

- `project.yaml` at the reactor root with the module list;
- a `module.yaml` in each module directory, single-module projects included;
- `layout: maven-like` everywhere, so `src/main/java`, `src/test/kotlin` and friends keep working without moving
  files.

### Dependencies

- Reactor module dependencies become `//` module dependencies, e.g. `- //my-lib`. In `0.11.x` they became relative
  paths.
- External dependencies keep their Maven coordinates.
- Parent POM BOMs are imported as `bom:` entries, including transitive parents, ordered outermost first.
- Repositories, including those inherited from parent POMs, land in `repositories`.
- `groupId`, `artifactId`, and `version` land in `settings.publishing`.

Scope mapping:

| Maven scope | Kotlin Toolchain | Section |
|---|---|---|
| `compile` (default) | compile + runtime + `exported: true` | `dependencies` |
| `provided` | `scope: compile-only` | `dependencies` |
| `runtime` | `scope: runtime-only` | `dependencies` |
| `test` | compile + runtime | `test-dependencies` |
| `system` | not supported | — |
| `import` (BOM) | `bom:` prefix | `dependencies` |

Every `compile`-scoped dependency comes out `exported: true`. That is the safe default, because Maven's `compile`
scope is transitively visible — but it is rarely what you want long term. Strip `exported` from anything that is not
part of the module's public API.

### Maven Plugin Handling

Plugins whose functionality the toolchain provides natively get their configuration extracted:

| Maven plugin | Becomes |
|---|---|
| `maven-compiler-plugin` | `settings.jvm.release`, `settings.java.freeCompilerArgs`, `settings.java.annotationProcessing`, `settings.jvm.storeParameterNames` |
| `kotlin-maven-plugin` | `settings.kotlin.*` (version, compiler plugins, args), `settings.jvm.release` |
| `spring-boot-maven-plugin` | `settings.springBoot`, `product: jvm/app`, `settings.jvm.mainClass` |
| `maven-surefire-plugin` | `test-settings.jvm.freeJvmArgs`, `extraEnvironment`, `systemProperties` |

`maven-jar-plugin`, `maven-clean-plugin`, `maven-install-plugin`, `maven-source-plugin` and similar need no conversion
— the toolchain covers them.

Everything else is downloaded, its descriptor parsed, and each goal written into `mavenPlugins` with
`enabled: false`.

### Manual Work Afterwards

The converter is best-effort. These need hands:

1. Run `./kotlin build` and `./kotlin test` to see what broke.
2. Review `project.yaml` and every `module.yaml`.
3. Remove `exported` from implementation-only dependencies.
4. Triage the `mavenPlugins` section: enable what is needed, delete the rest, and clean up the coordinates in
   `project.yaml`.
5. Optionally extract repeated versions into `libs.versions.toml`.
6. Optionally leave `maven-like` behind: move `src/main/kotlin/` to `src/` and `src/test/kotlin/` to `test/`, then
   delete the `layout: maven-like` line.

Not handled by the converter, and needing manual migration: **profiles** (the toolchain has no equivalent — build
configuration can only vary by platform), **extensions**, **dependency exclusions**, **dependency classifiers**,
**optional dependencies**, **system-scoped dependencies**, and **variable substitution** (values are inlined where
possible; for dependencies, use a library catalog instead).

`0.12` fixed several converter and POM-parsing bugs: variable interpolation in activation-profile paths, system
properties in `pom.xml` substitution, unix-family profiles on macOS, and profile activation when all criteria are
satisfied.

## Maven Plugins At Build Time

JVM-only. Modules must be `jvm/app` or `jvm/lib`. This is a prototype that upstream says may be dropped at any time.

Declare coordinates project-wide:

```yaml title="project.yaml"
modules:
  - app

mavenPlugins:
  - org.apache.maven.plugins:maven-surefire-plugin:3.5.3
  - org.apache.maven.plugins:maven-checkstyle-plugin:3.6.0
```

Enable goals (mojos) per module with the `pluginArtifactId.goalName` key:

```yaml title="app/module.yaml"
product: jvm/app

mavenPlugins:
  maven-surefire-plugin.test: enabled
```

### Configuration

```yaml
mavenPlugins:
  maven-surefire-plugin.test:
    enabled: true
    configuration:
      includes:
        - "*Smoke*"
```

Keys under `configuration` map directly to the goal's documented parameters. The toolchain reads the plugin descriptor
to resolve parameter types, so IDE completion and validation work for supported types. Complex POJO parameter types
are not supported.

`PlexusConfiguration` parameters take raw XML:

```yaml
mavenPlugins:
  maven-enforcer-plugin.enforce:
    enabled: true
    configuration:
      rules: "
        <rules>
          <requireJavaVersion>
            <version>[21,)</version>
          </requireJavaVersion>
        </rules>"
```

Extra dependencies a mojo needs go under its own `dependencies` key:

```yaml
mavenPlugins:
  maven-checkstyle-plugin.checkstyle:
    enabled: true
    dependencies:
      - io.spring.nohttp:nohttp-checkstyle:0.0.11
    configuration:
      configLocation: ./nohttp-checkstyle.xml
      includes: "**/*"
```

### Source Generation

Source-generating plugins integrate with the compilation pipeline automatically:

```yaml title="app/module.yaml"
product: jvm/app

dependencies:
  - com.google.protobuf:protobuf-kotlin:4.33.0

mavenPlugins:
  protobuf-maven-plugin.generate:
    enabled: true
    configuration:
      protocVersion: 4.33.0
      sourceDirectories: [ ./src ]
      kotlinEnabled: true
```

### Execution

Each enabled goal becomes a task named `pluginArtifactId.goal`, scoped to its module, addressed as
`:app:maven-surefire-plugin.test`.

Goals with a default Maven phase are wired into the lifecycle on a best-effort basis — `generate-sources` runs before
compilation, for instance. Mojos with no default phase are **not** wired in and must be run explicitly.

Not generally supported: multiple executions of the same goal, Maven extensions, custom dependency resolution, and
report aggregation.
