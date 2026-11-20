# Publishing Libraries And iOS Apps

Kotlin Toolchain `v0.14.1`. Publishing is still in preview and likely to change.

## What Can Be Published

All library modules — `jvm/lib` and `kmp/lib` — on every Kotlin platform.

The docs put it as a compatibility guarantee: the publication format is consumable by Maven, Gradle, and any build
tool that reads the same formats, and consumers do not need the Kotlin Toolchain. The toolchain publishes
Gradle module metadata alongside `pom.xml` (KTC-5272).

Libraries binding to native C libraries are covered: `cinterop` bindings are published in commonized form for common
code and per platform, so consumers get the same C API without setting up interop themselves.

Since `0.13`, KMP publications include Compose resources and SwiftPM dependency metadata. A separate
`-swiftpm-metadata.json` artifact describes the library's declared Swift packages; transitive packages are collected
through the dependency graph. A library depending on a local Swift package can publish only to `mavenLocal`, because
the metadata contains an absolute machine-local path. Use remote Swift packages for shared publications.

Plugins still cannot be published — see `known-issues.md`.

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

When the selected modules depend on other local modules, the command fails
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

No credentials needed. Artifacts published to Maven Local are never signed, even with `signArtifacts: true`.
To inspect signatures locally, build a Maven Central bundle with `kotlin package --format=maven-central-bundle`.

## Maven Central

### Prerequisites

- a Central Portal account
- a Central Portal namespace matching your `group`
- a Central Portal user token — keep both the username and password parts

### Configuration

Sonatype requires javadoc and sources JARs, checksums, PGP signatures, and mandatory POM metadata.
In `0.13`, `settings.publishing.pom.description` can supply the description instead of the module-level
`description` (KTC-5845). The module description remains the default when the POM override is absent.

Example:

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
count down. Accepted values: `md5`, `sha1`, `sha256`, `sha512`. The setting also applies to non-Central repositories;
`.asc` signatures do not get additional checksum files.

### Credentials

| Variable | Meaning |
|---|---|
| `KOTLIN_TOOLCHAIN_MAVEN_CENTRAL_USERNAME` | Username part of the Central Portal user token |
| `KOTLIN_TOOLCHAIN_MAVEN_CENTRAL_PASSWORD` | Password part of the token |
| `KOTLIN_TOOLCHAIN_SIGNING_KEY` | ASCII-armored PGP private key — `gpg --export-secret-keys --armor <KEY_ID>` |
| `KOTLIN_TOOLCHAIN_SIGNING_KEY_PASSPHRASE` | Optional passphrase for that key |
| `KOTLIN_TOOLCHAIN_SIGNING_KEY_ID` | Select a signing key/subkey by short ID, long ID, or full fingerprint |

These are normally set on CI, not locally.

The only usable signing subkey is selected automatically; primary-key stubs without private material are ignored.
If several signing subkeys are usable, publication fails until `KOTLIN_TOOLCHAIN_SIGNING_KEY_ID` selects one.
Export just a signing subkey with `gpg --export-secret-subkeys --armor '<SUBKEY_ID>!'` when keeping the primary key
offline. Check consumer signature-verification support before adopting subkeys; upstream notes Maven/Nexus limitations.

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

Keep `manual` until the first deployment is proven. Released Maven Central artifacts cannot be removed — Sonatype's
policy is strict, and `auto` gives you no chance to inspect what went out.

### klibs.io Notification

`settings.publishing.mavenCentral.notifyKlibsIo: true` opts KMP libraries into a notification after a successful
Central upload; the default is `false`. It sends the Maven coordinates to klibs.io for indexing. With `manual`
publishing, still release the deployment in Central Portal within four days or klibs.io treats the notification as
invalid. The endpoint can be overridden with `KOTLIN_TOOLCHAIN_KLIBS_IO_API_URL`. Notification failures log a warning
without failing an otherwise successful publication.

### Publication Compatibility

`0.14` includes generated sources in published sources JARs and derives klib `unique_name` from publication
`group:artifact` coordinates (KTC-5471, KTC-6014). When migrating an already-published Gradle library, preserve its
coordinates and check consumption from an existing client. Publication without `-m` no longer requires the target
repository in unrelated modules that do not publish.

## iOS Apps And TestFlight

Preview in `0.14`. Run on macOS with Xcode and an Apple Developer Program membership. Set the bundle ID, signing
team, deployment target, and version/build in the module's `module.xcodeproj`. Use a new build number for each upload.

```shell
./kotlin package -m ios-app --format ipa
./kotlin publish testflight -m ios-app
```

The first command packages an IPA. Automatic signing gets generated development export options by default; manual
signing requires `--ipa-export-options-plist-path <ExportOptions.plist>`. Custom export options determine the export
behavior, so inspect them before running. Packaging needs macOS/Xcode; it does not itself mean a TestFlight upload.

The second command uploads a release build to App Store Connect for TestFlight processing. `testflight` is built in,
not a Maven repository declaration, and does not require `settings.publishing`. Only one `ios/app` can be selected;
`-m` is required when several exist. `--transitive` is ignored. This flow supports automatic signing for straightforward
setups; use Xcode, `xcodebuild`, or fastlane for manual signing and more complex workflows.

Configure an App Store Connect **team API key** with API access and the Admin role for automatic provisioning:

| Variable | Value |
|---|---|
| `KOTLIN_TOOLCHAIN_ASC_KEY_PATH` | Path to the private `.p8` file |
| `KOTLIN_TOOLCHAIN_ASC_KEY_ID` | Key ID |
| `KOTLIN_TOOLCHAIN_ASC_KEY_ISSUER_ID` | Issuer ID |

Interactive publishing can prompt for these values and save them to module-local `app-store-connect-auth.properties`,
which it adds to `.gitignore`. Keep this file and the `.p8` key out of version control. The publish command takes
values from the properties file before environment variables; CI fails if setup is incomplete instead of prompting.
IPA packaging reads the three environment variables as a set: provide all three or none.

The publish wizard checks automatic signing, can register the bundle ID, and guides creation of a missing app record
and the encryption declaration. It is an external upload and provisioning flow, not a local validation command.
