# Publishing Libraries

Kotlin Toolchain `v0.12.0`. Publishing is still in preview and likely to change.

## What Can Be Published

All library modules — `jvm/lib` and `kmp/lib` — on every Kotlin platform. This is the headline change from `0.11.x`,
where only JVM libraries worked and KMP publications were incomplete and not consumable.

The docs put it as a compatibility guarantee: the publication format is consumable by Maven, Gradle, and any build
tool that reads the same formats, and consumers do not need the Kotlin Toolchain. Under the hood `0.12` publishes
Gradle module metadata alongside `pom.xml` (KTC-5272).

Libraries binding to native C libraries are covered: `cinterop` bindings are published in commonized form for common
code and per platform, so consumers get the same C API without setting up interop themselves.

Not yet covered: Compose Multiplatform resources are not part of a KMP publication (KTC-5698). Plugins cannot be
published at all — see `known-issues.md`.

## Regular Maven Repository

Three pieces: publication configuration, target repository URL, and credentials.

```yaml title="module.yaml"
product: jvm/lib

repositories:
  - id: someIdOfYourChoosing
    url: https://maven.pkg.github.com/my-org/my-repo
    publish: true
    credentials:
      file: creds.properties
      usernameKey: username
      passwordKey: password

settings:
  publishing:
    enabled: true
    group: org.example
    version: 1.0.0
    # artifactId defaults to the module name
```

```properties title="creds.properties"
username=someone
password=secret
```

`usernameKey` and `passwordKey` name the properties, not their values. Keep the file out of version control.

Publish everything that has publishing enabled and declares that repository:

```shell
kotlin publish someIdOfYourChoosing
```

Select specific modules with `-m`/`--module`, repeatable. Add `--transitive` to also publish the local modules they
depend on:

```shell
kotlin publish -m my-lib --transitive someIdOfYourChoosing
```

Since `0.12` the choice is not optional: when the selected modules depend on other local modules, the command fails
and asks for `--transitive` or `--non-transitive`.

If a module depends on other local modules, those must have publishing enabled too. Share the publishing block through
a template rather than repeating it.

## Local Maven Repository

```yaml title="module.yaml"
repositories:
  - url: mavenLocal
    publish: true
```

```shell
kotlin publish mavenLocal
```

No credentials needed. This is the fastest way to try a library from another project on the same machine. `0.12` adds
incremental caching and cleaner logging for this path.

## Maven Central

### Prerequisites

- a Central Portal account
- a Central Portal namespace matching your `group`
- a Central Portal user token — keep both the username and password parts

### Configuration

Sonatype requires javadoc and sources JARs, checksums, PGP signatures, and mandatory POM metadata:

```yaml title="module.yaml"
product: jvm/lib

description: A meaningful description for this specific module

settings:
  publishing:
    enabled: true
    group: com.example
    version: 1.0.0
    mavenCentral: enabled
    signArtifacts: true
    publishSources: true
    pom:
      url: https://example.com
      scm: https://github.com/my-org/example.git
      developers:
        - name: Joffrey Bion
      licenses:
        - name: MIT
          url: https://opensource.org/license/mit
```

`pom.scm` as a bare string is shorthand for `pom.scm.url`; `connection` and `developerConnection` are derived as
`scm:git:$url` unless set explicitly.

An empty JavaDoc JAR is added to the publication by default at the moment.

### POM Metadata

| Attribute | Default |
|---|---|
| `name` | module name |
| `description` | module `description` |
| `url` | `null` |
| `licenses` | `[]` — each has `name` and `url` |
| `scm` | — `url`, `connection`, `developerConnection` |
| `developers` | `[]` — `name` required; optional `id`, `url`, `email`, `organization`, `organizationUrl` |

Most of this is identical across a project. Put it in a shared template.

### Checksums

`settings.publishing.checksums` defaults to `[md5, sha1]` — exactly what Maven Central requires, keeping the file
count down. Accepted values: `md5`, `sha1`, `sha256`, `sha512`. `0.12` stopped publishing unnecessary checksum files
for `.asc` signatures, and honors this setting for non-Central repositories too.

### Credentials

| Variable | Meaning |
|---|---|
| `KOTLIN_TOOLCHAIN_MAVEN_CENTRAL_USERNAME` | Username part of the Central Portal user token |
| `KOTLIN_TOOLCHAIN_MAVEN_CENTRAL_PASSWORD` | Password part of the token |
| `KOTLIN_TOOLCHAIN_SIGNING_KEY` | ASCII-armored PGP private key — `gpg --export-secret-keys --armor <KEY_ID>` |
| `KOTLIN_TOOLCHAIN_SIGNING_KEY_PASSPHRASE` | Optional passphrase for that key |

These are normally set on CI, not locally.

### Publishing Command

```shell
kotlin publish mavenCentral
```

For a `jvm/lib` with Maven Central enabled, `kotlin package` becomes
`kotlin package --format=maven-central-bundle` and produces the ZIP bundle ready for manual upload.

### Publishing Mode

| Mode | Behavior |
|---|---|
| `manual` (default) | Upload the bundle, wait for validation, then stop. Release manually from the Central Portal UI. |
| `auto` | Upload, validate, and release without any manual step. |

```yaml
settings:
  publishing:
    mavenCentral:
      publishingMode: auto
```

In `0.11.x` this lived at `settings.mavenCentral.publishingMode`. Update the path when migrating.

Keep `manual` until the first deployment is proven. Released Maven Central artifacts cannot be removed — Sonatype's
policy is strict, and `auto` gives you no chance to inspect what went out.
