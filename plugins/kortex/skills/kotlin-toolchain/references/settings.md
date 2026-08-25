# Settings Reference

Kotlin Toolchain `v0.12.0`. Always confirm against a real project with `./kotlin show settings -m <module>`.

`settings` configures the toolchains used to build the module. `test-settings` does the same for building and running
its tests, and overrides `settings` where they overlap. Both accept `@platform` qualifiers.

## Defaults At A Glance

| Setting | `0.12.0` | `0.11.1` |
|---|---|---|
| Default JDK major version | 25 | 21 |
| Minimum JDK to run the toolchain | 17 | — |
| `settings.kotlin.version` | 2.4.10 | 2.3.20 |
| Minimum `settings.kotlin.version` | 2.2.20 | — |
| `settings.android.compileSdk` | 37 | 36 |
| `settings.android.minSdk` | 24 | 21 |
| `settings.android.buildToolsVersion` | 37.0.0 | — |
| `settings.compose.version` | 1.11.1 | 1.10.3 |
| `settings.compose.experimental.hotReload.version` | 1.2.0 | 1.0.0 |
| `settings.kotlin.serialization.version` | 1.11.0 | 1.10.0 |
| `settings.kotlin.ksp.version` | 2.3.11 | 2.3.6 |
| `settings.kotlin.rpc.version` | 0.10.3 | present, version not documented |
| `settings.kotlin.dataframe.version` | 1.0.0-rc01 | — |
| `settings.jvm.test.junitPlatformVersion` | 6.1.3 | 6.0.1 |
| `settings.ktor.version` | 3.5.2 | 3.4.1 |
| `settings.lombok.version` | 1.18.46 | 1.18.38 |
| `settings.springBoot.version` | 4.1.0 | 4.0.5 |

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
| `version` | 2.4.10 | Kotlin compiler and stdlib version |
| `languageVersion` | major.minor of `version` | Source compatibility level |
| `apiVersion` | from `languageVersion` | Restrict to declarations from that version of bundled libraries |
| `allWarningsAsErrors` | `false` | |
| `suppressWarnings` | `false` | |
| `progressiveMode` | `false` | |
| `verbose` | `false` | |
| `freeCompilerArgs` | `[]` | Raw compiler options, e.g. `-Xexpect-actual-classes` |
| `optIns` | `[]` | Fully-qualified opt-in annotation names. **String list in `0.12`**, was an enum list in `0.11`. |
| `compileIncrementally` | enabled for Kotlin ≥ 2.4.0 | Incremental JVM compilation. New in `0.12`. |
| `compilerPlugins` | `[]` | Third-party compiler plugins |
| `debug` | enabled in debug variants | Native only. Was a flat `true` in `0.11`. |
| `optimization` | enabled in release variants | Native only. New in `0.12`. |
| `linkerOptions` | `[]` | Native only, extra linker arguments. New in `0.12`. |
| `allOpen`, `noArg`, `jsPlainObjects` | | Compiler plugin shortcuts |
| `serialization`, `rpc`, `dataframe`, `powerAssert`, `ksp` | | See `builtin-tech.md` |

`-X` flags go through `freeCompilerArgs`:

```yaml
settings:
  kotlin:
    freeCompilerArgs: [ -Xexpect-actual-classes ]
```

## `settings.native`

| Attribute | Default | Meaning |
|---|---|---|
| `entryPoint` | `null` | Fully-qualified name of the entry-point function |

## `settings.android`

| Attribute | Default | Meaning |
|---|---|---|
| `namespace` | `org.example.namespace` | Package for generated `R` and `BuildConfig` |
| `applicationId` | from `namespace` | ID on device and in Play Store |
| `compileSdk` | 37 | API level to compile against; int or object |
| `targetSdk` | from `compileSdk` | |
| `minSdk` | 24 | |
| `buildToolsVersion` | 37.0.0 | SDK Build Tools version. New in `0.12`. |
| `versionCode` | 1 | |
| `versionName` | `unspecified` | |
| `signing` | | Release signing settings |
| `resourcePackaging` | empty | Duplicate Java resource handling. New in `0.12`. |
| `parcelize` | disabled | |

`maxSdk` is deprecated in `0.12`.

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
commit the keystore or `keystore.properties`.

## `settings.publishing`

The block itself already existed in `0.11.x`. What moved in `0.12` is `publishingMode`: it was
`settings.mavenCentral.publishingMode` and is now `settings.publishing.mavenCentral.publishingMode`.

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
| `version` | 1.11.1 |
| `resources.packageName` | `""` |
| `resources.exposedAccessors` | `false` |
| `resources.nameOfResClass` | `"Res"` |
| `experimental.hotReload.version` | 1.2.0 |

## Other Toolchain Blocks

| Block | Notable keys |
|---|---|
| `settings.ktor` | `enabled`, `version` (3.5.2), `applyBom` (`true`) |
| `settings.springBoot` | `enabled`, `version` (4.1.0), `applyBom` (`true`) |
| `settings.lombok` | `enabled`, `version` (1.18.46) |
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
