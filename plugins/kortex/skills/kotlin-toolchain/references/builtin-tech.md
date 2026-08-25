# Built-In Technologies

Kotlin Toolchain `v0.12.0`. Prefer the short settings forms unless you need customization.

## Compose Multiplatform

```yaml
settings:
  compose: enabled
```

Enabling it:

- configures the Compose compiler plugin;
- adds `org.jetbrains.compose.runtime:runtime` and `org.jetbrains.compose.components:components-resources`
  implicitly (the second one is new in `0.12`);
- enables the `$compose.*` catalog for optional Compose modules, versioned by `settings.compose.version` (1.11.1).

Useful catalog entries: `$compose.foundation`, `$compose.material`, `$compose.ui`,
`$compose.desktop.currentOs`, `$compose.hotReload.runtimeApi`.

Since `0.12`, the compose-resources library is skipped when a module has no resources. The docs still describe the
dependency as unconditional; the release notes record the change.

### Resources

Put resources under `composeResources`. Accessors are generated into `<sanitized-name>.generated.resources`.

`<sanitized-name>` is derived from `settings.publishing.group` and `artifactId` when those are set, otherwise from the
module name. Sanitizing lowercases it, turns `-` into `_`, and prefixes `_` if it starts with a digit. So module
`my-kmp-module` with no publishing settings yields `my_kmp_module.generated.resources`. The publishing-derived part is
new in `0.12` — a module that gains publishing settings changes its generated package.

Override with:

```yaml
settings:
  compose:
    resources:
      packageName: com.example.myapp.resources
      exposedAccessors: true
      nameOfResClass: Res
```

Compose Multiplatform resources are **not** included in a published KMP library yet (KTC-5698).

### Hot Reload

`settings.compose.experimental.hotReload.version` defaults to 1.2.0. Run it from the CLI with
`./kotlin run --compose-hot-reload-mode`, or from the IDE, which needs the Kotlin Toolchain plugin and a module with a
`jvm` target.

`0.12` added a filesystem watcher to the standalone CLI and an MCP server for the reload loop. **The `v0.12.0` docs
still carry the old warning that the CLI does not watch the filesystem — that text is stale.** The shipped code
watches both source and build-model paths, and a change to `module.yaml` triggers a full rebuild-and-reload.

## kotlinx.serialization

```yaml
settings:
  kotlin:
    serialization: json
```

`serialization: enabled` gives the compiler plugin, the core runtime, and the catalog without a format. Naming a
format is a shortcut for `enabled: true` plus that format's dependency.

Known formats: `json`, `json-io`, `json-okio`, `hocon`, `protobuf`, `cbor`, `properties`.

Full form:

```yaml
settings:
  kotlin:
    serialization:
      enabled: true
      version: 1.11.0
      format: json
```

`enabled` is implied when `format` is set. In `0.12`, `format` is a plain string rather than an enum.

## Ktor

```yaml
settings:
  ktor: enabled
```

Applies the Ktor BOM (`applyBom: true` by default), contributes Ktor entries to a built-in library catalog at
version 3.5.2, and adds the `io.ktor.development=true` system property when the app runs under `kotlin run`. In `0.12`
the catalog is aligned with the published `ktor-version-catalog`, so some entry names differ from `0.11.x`.

## Spring Boot

```yaml
settings:
  springBoot: enabled
```

At version 4.1.0 this:

- applies the Spring Boot dependencies BOM, so starters can be declared without versions;
- configures `all-open` with the `spring` preset;
- configures `no-arg` with the `jpa` preset;
- passes `-parameters` to `javac` and `-java-parameters`, `-Xjsr305=strict` to `kotlinc`;
- adds catalog entries and the `classes` runtime classpath mode so devtools work.

**Changed in `0.12`:** it no longer adds `spring-boot-starter` and `spring-boot-starter-test` for you. Declare the
starters you actually need:

```yaml
dependencies:
  - org.springframework.boot:spring-boot-starter-web

test-dependencies:
  - org.springframework.boot:spring-boot-starter-test
```

Also new: `no-arg` now uses the `jpa` preset rather than `spring`.

## kotlinx.rpc

```yaml
settings:
  kotlin:
    rpc: enabled
```

| Attribute | Default |
|---|---|
| `enabled` | `false` |
| `version` | 0.10.3 |
| `applyBom` | `true` |
| `annotationTypeSafetyEnabled` | `true` |

Enabling it turns on `@Rpc` code generation, applies the BOM, adds
`org.jetbrains.kotlinx:kotlinx-rpc-core` implicitly (new in `0.12`), and contributes catalog entries starting with
`$kotlin.rpc.`.

Disabling `annotationTypeSafetyEnabled` is unsafe and only warranted when the type-safety analysis rejects valid code.

## Kotlin DataFrame

New in `0.12`.

```yaml
settings:
  kotlin:
    dataframe: enabled
```

| Attribute | Default |
|---|---|
| `enabled` | `false` |
| `version` | 1.0.0-rc01 |

IDE support for the plugin ships in the Kotlin Toolchain IDEA plugin.

## Power Assert

Enriches assertion failure messages with intermediate values. Present since `0.11.x`; `0.12` documents it as a
first-class `settings.kotlin` block and adds the runtime library implicitly.

```yaml
settings:
  kotlin:
    powerAssert: enabled
```

| Attribute | Default |
|---|---|
| `enabled` | `false` |
| `functions` | `[kotlin.assert]` |

Add fully-qualified function names to `functions` to transform assertions beyond `kotlin.assert()`. The Power Assert
runtime library is added implicitly when the plugin is enabled.

## KSP

KSP2 only — processors must be KSP2-compatible. KSP1 is no longer part of KSP releases.

```yaml
settings:
  kotlin:
    ksp:
      version: 2.3.11
      processors:
        - androidx.room:room-compiler:2.7.0-alpha12
        - //my-processor          # a local jvm/lib module
        - $libs.some.processor
      processorOptions:
        someOption: value
```

Each processor entry can be a local module path, a catalog reference, or Maven coordinates.

In multiplatform modules KSP runs per platform. Restrict it with a qualified block:

```yaml
settings@android:
  kotlin:
    ksp:
      processors:
        - androidx.room:room-compiler:2.7.0-alpha12
```

Generated code is only visible to the platform it was generated for. There is no way to make it visible to common
sources.

## Parcelize

```yaml
settings:
  android:
    parcelize: enabled
```

Generates `Parcelable` implementations for `@Parcelize`-annotated classes. For classes in common code where the real
annotation is unavailable, declare your own annotation and register it:

```yaml
settings:
  android:
    parcelize:
      enabled: true
      additionalAnnotations: [ com.example.MyCommonParcelize ]
```

## Lombok

```yaml
settings:
  lombok: enabled
```

Adds the Lombok dependency (1.18.46), the Java annotation processor, and the Kotlin compiler plugin. JVM only.

## Java Annotation Processing

```yaml
settings:
  java:
    annotationProcessing:
      processors:
        - org.mapstruct:mapstruct-processor:1.6.3
```

JVM and Android.

## Third-Party Compiler Plugins

The escape hatch when there is no built-in shortcut:

```yaml
settings:
  kotlin:
    compilerPlugins:
      - id: com.example.plugin
        dependency: com.example:kotlin-plugin:1.0.0
        options:
          key: value
```

IDE support for these is best effort.

Shortcuts that do exist: all-open, no-arg, JS plain objects, Parcelize, Power Assert, Compose, serialization,
kotlinx.rpc, DataFrame, and Lombok.
