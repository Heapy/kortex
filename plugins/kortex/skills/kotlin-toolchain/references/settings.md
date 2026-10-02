# Settings Reference

Kotlin Toolchain `v0.13.0`. Always confirm against a real project with `./kotlin show settings -m <module>`.

`settings` configures the toolchains used to build the module. `test-settings` does the same for building and running
its tests, and overrides `settings` where they overlap. Both accept `@platform` qualifiers.

## Defaults At A Glance

| Setting | `0.13.0` |
| --- | --- |
| Default JDK major version | 25 |
| Minimum JDK to run the toolchain | 17 |
| `settings.kotlin.version` | 2.4.20 |
| Minimum `settings.kotlin.version` | 2.2.20 |
| `settings.android.compileSdk` | 37 |
| `settings.android.minSdk` | 24 |
| `settings.android.buildToolsVersion` | 37.0.0 |
| `settings.compose.version` | 1.12.1 |
| `settings.compose.experimental.hotReload.version` | 1.2.0 |
| `settings.kotlin.serialization.version` | 1.11.0 |
| `settings.kotlin.ksp.version` | 2.3.12 |
| `settings.kotlin.rpc.version` | 0.10.4 |
| `settings.kotlin.dataframe.version` | 1.0.0-rc01 |
| `settings.jvm.test.junitPlatformVersion` | 6.1.3 |
| `settings.ktor.version` | 3.6.0 |
| `settings.lombok.version` | 1.18.48 |
| `settings.springBoot.version` | 4.1.1 |

The `0.13.0` defaults above were checked against the tagged
[`DefaultVersions.kt`](https://github.com/JetBrains/kotlin-toolchain/blob/abc7a5f7579e9ef267107782048c76777f0bf932/sources/frontend-api/src/org/jetbrains/amper/frontend/schema/DefaultVersions.kt).
The tagged Markdown still says Kotlin `2.4.10`; the shipped default is `2.4.20`.

## `settings.jvm`

| Attribute | Default | Meaning |
|---|---|---|
| `jdk` | | JDK requirements used to validate `JAVA_HOME` or provision a JDK |
| `mainClass` | auto-detected | `jvm/app` only: fully-qualified entry-point class |
| `release` | from `jdk.version` | Minimum JVM release the code must be compatible with |
| `runtimeClasspathMode` | `jars` | `jars` builds local module deps as jars; `classes` puts compiled classes on the runtime classpath |
| `storeParameterNames` | `false` | Keep formal parameter names in class files, for reflection |
| `test` | | Test-process configuration |

`release` enforces compatibility on three levels: bytecode target for Kotlin and Java, the Java platform APIs available
to both, and the Java language constructs allowed in Java sources. Set to null it applies no constraint and compiler
defaults apply. This is the "target Java N" knob — do not repurpose `jdk.version` for it.

`settings.jvm.jdk` is covered in `cli.md`.

`settings.jvm.test`:

| Attribute | Default |
|---|---|
| `junitPlatformVersion` | 6.1.3 |
| `extraEnvironment` | `{}` |
| `freeJvmArgs` | `[]` |
| `systemProperties` | `{}` |

The same block exists under `test-settings.jvm`.

## `settings.junit`

`junit-5` (default), `junit-4`, or `none`. This also picks the Kotlin test flavor that is added automatically:
`kotlin-test-junit5`, `kotlin-test-junit`, or plain `kotlin-test`.

## `settings.kotlin`

| Attribute | Default | Meaning |
|---|---|---|
| `version` | 2.4.20 | Kotlin compiler and stdlib version |
| `languageVersion` | major.minor of `version` | Source compatibility string, e.g. `"2.4"` |
| `apiVersion` | from `languageVersion` | String; restrict bundled-library APIs to that language version |
| `allWarningsAsErrors` | `false` | |
| `suppressWarnings` | `false` | |
| `progressiveMode` | `false` | |
| `verbose` | `false` | |
| `freeCompilerArgs` | `[]` | Raw compiler options, e.g. `-Xexpect-actual-classes` |
| `optIns` | `[]` | String list of fully-qualified opt-in annotation names. |
| `compileIncrementally` | enabled for Kotlin ≥ 2.4.0 | Incremental JVM compilation; since `0.13`, also Native debug dependency caches |
| `explicitApi` | `disable` | `strict`, `warning`, or `disable`; ignored for test sources |
| `compilerPlugins` | `[]` | Third-party compiler plugins |
| `debug` | enabled in debug variants | Native only. |
| `optimization` | enabled in release variants | Native only. |
| `linkerOptions` | `[]` | Native only, extra linker arguments. |
| `allOpen`, `noArg`, `jsPlainObjects` | | Compiler plugin shortcuts |
| `serialization`, `rpc`, `dataframe`, `powerAssert`, `ksp` | | See `builtin-tech.md` |

`-X` flags go through `freeCompilerArgs`:

```yaml
settings:
  kotlin:
    freeCompilerArgs: [ -Xexpect-actual-classes ]
```

Native caches reuse compiled external dependencies during non-optimized binary linking on compiler-supported targets.
They do not incrementally compile a module into klibs or accelerate optimized release linking. Set
`settings@native.kotlin.compileIncrementally: false` to opt out; the default Kotlin `2.4.20` supports the feature.

## `settings.native`

| Attribute | Default | Meaning |
|---|---|---|
| `entryPoint` | `null` | Fully-qualified name of the entry-point function |

## `settings.android`

| Attribute | Default | Meaning |
|---|---|---|
| `namespace` | required for apps; derived for libraries | Package for generated `R` and `BuildConfig` |
| `applicationId` | from `namespace` | ID on device and in Play Store |
| `compileSdk` | 37 | API level to compile against; int or object |
| `targetSdk` | from `compileSdk` | |
| `minSdk` | 24 | |
| `buildToolsVersion` | 37.0.0 | SDK Build Tools version. |
| `versionCode` | 1 | |
| `versionName` | `unspecified` | |
| `signing` | | Release signing settings |
| `resourcePackaging` | empty | Duplicate Java resource handling. |
| `abiFilters` | `[]` (all) | ABIs whose native libraries enter the APK/AAB |
| `parcelize` | disabled | |

An `android/app` must set `namespace`; setting only `applicationId` is insufficient. `applicationId` defaults to
`namespace`. A library can omit `namespace`: it is derived from `publishing.group` plus the sanitized effective
artifact ID, or otherwise from a module-specific generated name. Pin it when code refers to generated classes.
The tagged Markdown's `org.example.namespace` default is stale; the schema and diagnostics enforce these rules.

`abiFilters: [arm64-v8a, x86_64]` restricts packaged `.so` files from both `jniLibs` and dependencies. It does not
build missing native binaries. The toolchain still warns if selected ABIs carry inconsistent library sets.

`maxSdk` is deprecated with an error diagnostic.

### `compileSdk` object form

| Attribute | Default | Meaning |
|---|---|---|
| `apiLevel` | 37 | API level |
| `minorApiLevel` | 0 | Minor API level |
| `sdkExtension` | `null` | SDK extension level |

```yaml
settings:
  android:
    compileSdk:
      apiLevel: 37
      minorApiLevel: 1
      sdkExtension: 2
```

### `resourcePackaging`

Glob-pattern lists, following Android's `Packaging.Resources` API:

| Attribute | Default | Effect |
|---|---|---|
| `excludes` | `[]` | Do not package matching resources |
| `merges` | `[]` | Concatenate matching resources into one entry |
| `pickFirsts` | `[]` | Package only the first match |

```yaml
settings:
  android:
    resourcePackaging:
      excludes:
        - META-INF/versions/9/OSGI-INF/MANIFEST.MF
```

### Signing

`settings.android.signing: enabled` reads `keystore.properties` beside `module.yaml`:

```properties
storeFile=keystore.jks
storePassword=...
keyAlias=...
keyPassword=...
```

Override the path with `signing.propertiesFile`. Generate a keystore with `./kotlin tool generate-keystore`. Never
commit the keystore or `keystore.properties`. A relative `storeFile` resolves from the module directory; run
`kotlin tool generate-keystore --properties-file keystore.properties` from that directory.

## `settings.publishing`

Configure Maven Central publication mode at `settings.publishing.mavenCentral.publishingMode`.

| Attribute | Default | Meaning |
|---|---|---|
| `enabled` | `false` | Enable `./kotlin publish` for this module |
| `group` | `null` | Maven groupId |
| `version` | `null` | Artifact version |
| `artifactId` | module name | Base artifact ID; for multiplatform libraries a platform suffix may be appended |
| `pom` | | POM metadata |
| `signArtifacts` | `false` | PGP-sign artifacts; key from `KOTLIN_TOOLCHAIN_SIGNING_KEY` |
| `publishSources` | `false` | Publish per-platform sources JARs |
| `checksums` | `[md5, sha1]` | Any of `md5`, `sha1`, `sha256`, `sha512` |
| `mavenCentral` | disabled | Central Portal publication |

Details and the `pom` sub-tree are in `publishing.md`.

## `settings.compose`

| Attribute | Default |
|---|---|
| `enabled` | `false` |
| `version` | 1.12.1 |
| `resources.packageName` | `""` |
| `resources.exposedAccessors` | `false` |
| `resources.nameOfResClass` | `"Res"` |
| `experimental.hotReload.version` | 1.2.0 |

## Other Toolchain Blocks

| Block | Notable keys |
|---|---|
| `settings.ktor` | `enabled`, `version` (3.6.0), `applyBom` (`true`) |
| `settings.springBoot` | `enabled`, `version` (4.1.1), `applyBom` (`true`) |
| `settings.lombok` | `enabled`, `version` (1.18.48) |
| `settings.java.annotationProcessing.processors` | Java annotation processors |

Per-technology behavior is in `builtin-tech.md`.

## `aliases`

Custom platform groups usable as `@platform` qualifiers:

```yaml
product:
  type: kmp/lib
  platforms: [jvm, android, iosArm64, iosSimulatorArm64]

aliases:
  - jvmAndAndroid: [jvm, android]

dependencies@jvmAndAndroid:
  - org.lighthousegames:logging:1.3.0
```

See `multiplatform.md`.
