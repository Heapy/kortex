# Dependencies And Repositories

Kotlin Toolchain `v0.12.0`.

## Notation

```yaml
dependencies:
  - //my-other-module                        # local module
  - org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1
  - $libs.apache.commons.lang3               # project catalog
  - $compose.ui                              # toolchain catalog
  - bom: io.ktor:ktor-bom:2.2.0
  - swiftPackage: ...                        # iOS apps only
  - localSwiftPackage: ./path                # iOS apps only
```

| Item | Meaning |
|---|---|
| `//<project path>` | Another module in the codebase |
| `<groupId>:<artifactId>[:<version>[:<classifier>]][@<packaging>]` | Maven library; version, classifier, and packaging are optional |
| `$<catalog.key>` | Entry from a project or toolchain catalog |
| `bom: <groupId>:<artifactId>:<version>` | Import a BOM |
| `bom: $<catalog.key>` | Import a BOM from a catalog |
| `swiftPackage: ...` | Remote Swift package, `ios/app` only. New in `0.12`. |
| `localSwiftPackage: <path>` | Local Swift package, `ios/app` only. New in `0.12`. |

The upstream reference lists the two Swift package forms but does not spell out the payload of `swiftPackage:`. Check
`./kotlin show dependencies` or the module schema in the IDE before writing one.

### Module Dependencies

Use `//` paths from the project root:

```yaml
dependencies:
  - //app/nested-lib
  - //ui/utils
```

The target must be a module listed in `project.yaml`, and must live inside the project root.

A relative path works but must start with a dot: `./my-nested-module`, `../my-sibling`. A bare `my-lib` is read as an
external Maven dependency, not a module. Upstream may deprecate relative module dependencies later — prefer `//`.

### Classifiers And Packaging Types

Classifiers and packaging types are new in `0.12`. The full grammar is
`<groupId>:<artifactId>[:<version>[:<classifier>]][@<packaging>]` — version, classifier, and packaging are all
optional, and a classifier requires a version before it.

A classifier picks one of several artifacts published under the same coordinates, for example a platform-specific
build. A packaging type picks the kind of artifact — an executable, an Android library, an archive — instead of the
default jar.

Packaging type resolution, for libraries published in Maven format only:

1. the type declared in your coordinates wins;
2. otherwise the type the library declares in its own `pom.xml`;
3. otherwise a regular library archive is expected.

Declaring `@pom` means only the descriptor is used and no artifact is fetched — useful for aggregator libraries.
Conversely, a library that declares `pom` for itself still contributes its jar if it published one, and it is not an
error when it did not.

Declaring a packaging type does not make the dependency artifact-only: transitive dependencies still resolve normally.

Libraries published with Gradle metadata describe their own artifacts, so packaging type has no effect on them.

## Scopes And Transitivity

| Scope | Effect |
|---|---|
| `all` | Compile and runtime. Default. |
| `compile-only` | Compile only, like Maven `provided`. |
| `runtime-only` | Runtime only. |

Short form and full form:

```yaml
dependencies:
  - io.ktor:ktor-client-core:2.2.0: compile-only
  - //ui/utils: runtime-only
```

```yaml
dependencies:
  - io.ktor:ktor-client-core:2.2.0:
      scope: compile-only
  - //ui/utils:
      scope: runtime-only
      exported: true
```

By default a module's dependencies are **not** added to the compilation of modules that depend on it. If `lib` depends
on `ktor-client-core` and `app` depends on `//lib`, then `app` gets Ktor at runtime but cannot reference Ktor classes
in its own code.

`exported: true` (or the `exported` short form) makes a dependency visible to dependent modules at compile time. Use it
only when the dependency's types appear in the module's public API. Exporting implementation-only dependencies leaks
them into every consumer's compile classpath.

## Catalogs

### Project Catalog

One user-defined catalog per project, in the Gradle version-catalog TOML format. It lives at `libs.versions.toml` in
the project root **or** at `gradle/libs.versions.toml` — never both.

Only `[versions]` and `[libraries]` are supported. `[bundles]` and `[plugins]` are not.

```toml
[versions]
ktor = "3.3.2"

[libraries]
ktor-client-auth = { module = "io.ktor:ktor-client-auth", version.ref = "ktor" }
ktor-client-contentNegotiation = { module = "io.ktor:ktor-client-content-negotiation", version.ref = "ktor" }
```

```yaml
dependencies:
  - $libs.ktor.client.auth
  - $libs.ktor.client.contentNegotiation
```

Keys map to accessors by the Gradle name-mapping rules — dashes become dots.

### Toolchain Catalogs

Implicit catalogs named after the toolchain in `settings`, carrying that toolchain's version:

```yaml
dependencies:
  - $kotlin.reflect
  - $compose.material
```

The catalog name matches the toolchain's name in `settings`, and its entries take that toolchain's configured version
— `$compose.*` from `settings.compose.version`, `$kotlin.serialization.*` from `settings.kotlin.serialization.version`,
`$kotlin.rpc.*` from `settings.kotlin.rpc.version`. A toolchain catalog only exists once its toolchain is enabled.
Enabling Ktor likewise contributes Ktor entries to a built-in catalog.

Catalog dependencies accept scope and visibility like any other:

```yaml
dependencies:
  - $compose.foundation: exported
  - $libs.db.engine: runtime-only
```

## BOMs

```yaml
dependencies:
  - bom: io.ktor:ktor-bom:2.2.0
  - io.ktor:ktor-client-core
```

Dependencies listed in the BOM no longer need a version, and versions declared in the BOM take part in conflict
resolution. This also works for catalog entries: a catalog library declared without a version resolves fine in modules
that import a BOM providing it.

## Repositories

### Defaults

| Name | ID | URL |
|---|---|---|
| Maven Central | `mavenCentral` | `https://repo1.maven.org/maven2` |
| Google | `mavenGoogle` | `https://maven.google.com` |

### Adding

```yaml
repositories:
  - https://repo.company.com/maven
  - id: internal
    url: https://repo.company.com/internal
```

A bare string is used as the `url`, and the `id` defaults to that url.

| Attribute | Default | Meaning |
|---|---|---|
| `url` | — | Repository URL. Always required, even for an entry that only disables a default. |
| `id` | from `url` | Identifier used by `kotlin publish <id>` and to override defaults |
| `credentials` | `null` | Username/password auth |
| `publish` | `false` | This repository can be a publish target |
| `resolve` | `true` | This repository is used to resolve dependencies |

### Overriding Or Disabling Defaults

New in `0.12`. Declaring a repository with a default repository's `id` replaces it. That is how to point at a company
mirror or attach credentials to Maven Central:

```yaml
repositories:
  - id: mavenCentral
    url: https://repo.mycompany.com/maven-central-mirror
    credentials:
      file: creds.properties
      usernameKey: username
      passwordKey: password
```

Setting `resolve: false` disables the default instead of replacing it, so lookups never go there:

```yaml
repositories:
  - id: mavenGoogle
    url: https://maven.google.com
    resolve: false
```

### Local Maven Repository

```yaml
repositories:
  - mavenLocal
```

`mavenLocal` is a special URL. It resolves from `~/.m2/repository`, and with `publish: true` it is also a publish
target — the quickest way to try a library in another project on the same machine.

### Authentication

Only username/password, read from a property file:

```yaml
repositories:
  - url: https://repo.company.com
    credentials:
      file: ../local.properties
      usernameKey: my.username
      passwordKey: my.password
```

```properties
my.username=someone
my.password=secret
```

`usernameKey` and `passwordKey` name the properties, they are not the values. Keep the file out of version control.

The docs disagree with themselves on the extension: the `module.yaml` reference says only `*.property` files are
supported, while every publishing example uses `creds.properties`. Copy whatever the project already uses, or try
`.properties` first — that is what the worked examples do.

A missing credentials file no longer breaks an unrelated build in `0.12` — it only fails the operation that needs it.
