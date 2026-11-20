# Migrating From 0.13 To 0.14

The project model and CLI changes below shipped in [0.14.0](https://github.com/JetBrains/kotlin-toolchain/releases/tag/v0.14.0).
The current patch updates the CLI runtime to Azul Zulu 25.38.201 (JDK 25.0.5.1+2) for CVE-2026-114938. [0.14.1 release notes](https://github.com/JetBrains/kotlin-toolchain/releases/tag/v0.14.1)

## Upgrading From 0.14.0

Run `./kotlin update --target-version 0.14.1` and retain both updated wrappers. For projects using a provisioned JDK,
run `./kotlin clean` in each project so provisioning can pick up the latest patch on the next build.
See [JDK cache refresh](cli.md#refreshing-provisioned-jdks) for machine-wide cleanup options.

## Upgrading From 0.13

For an authorized upgrade, run `./kotlin update --target-version 0.14.1` and retain both changed wrappers. Refresh provisioned JDKs as described above, then:

1. Set `settings.android.applicationId` explicitly on every `android/app`. In `0.13` it defaulted from `namespace`;
   in `0.14`, `namespace` defaults from `applicationId` instead. Preserve the existing application ID exactly.
   Keep a distinct namespace when the generated `R` / `BuildConfig` package differs from the app ID.
2. Replace `serialization: json` or `serialization.format: json` with `serialization: enabled` and an explicit
   `$kotlin.serialization.json` dependency. Do the equivalent for other formats, retaining scopes and qualifiers.
3. Review CI test selections. `kotlin test` now exits 1 when no tests are discovered after filters. Fix empty
   selections rather than masking the exit code. Discovered but skipped tests count, so an all-skipped suite can pass.
4. Run `./kotlin show settings`, then build and test the platforms the project ships. If it publishes a library,
   verify a local publication and a consumer, especially when migrating existing Gradle-published klibs.

For an app whose namespace and app ID were both `com.example.app`, the identity block becomes:

```yaml
settings:
  android:
    applicationId: com.example.app
    # namespace defaults to applicationId; set it only if a different package is needed.
```

Serialization becomes:

```yaml
settings:
  kotlin:
    serialization: enabled

dependencies:
  - $kotlin.serialization.json
```

Projects upgrading from before `0.13` must also replace `layout: amper` with `layout: default` and `AMPER_BUILD_DIR`
with `KOTLIN_TOOLCHAIN_BUILD_DIR`. The product type `jvm/amper-plugin` and plugin package names remain unchanged.

## Defaults

Compiler and framework defaults are unchanged from `0.13.0`: Kotlin 2.4.20, JDK 25, Compose 1.12.1, Hot Reload 1.2.0,
Ktor 3.6.0, Spring Boot 4.1.1, KSP 2.3.12, kotlinx.rpc 0.10.4, serialization 1.11.0, DataFrame 1.0.0-rc01,
JUnit Platform 6.1.3, Lombok 1.18.48, and Android SDK 37 / minSdk 24 / Build Tools 37.0.0.
Runtime JDK 17 and compiler Kotlin 2.2.20 remain the minimums. The new, opt-in Android desugaring feature defaults
to `com.android.tools:desugar_jdk_libs:2.1.5`. See [settings](settings.md).

## New Capabilities And Fixes

- **Android:** `settings.android.desugaring: enabled` backports supported Java APIs and can declare library consumer
  requirements. Dependency resolution handles `BuildTypeAttr` ambiguity by preferring release; it does not select
  dependencies according to the consumer's debug/release build. See [dependencies](dependencies.md).
- **Project creation:** `kotlin new <directory>` creates a new project directory; `kotlin init` initializes an existing
  directory. The installer script can self-update. Neither replaces deliberate updates to a project wrapper pin.
- **Downloads:** repository and individual tool download roots can be overridden for mirrors; see [CLI](cli.md).
- **iOS:** `kotlin package --format ipa` packages an app; `kotlin publish testflight` uploads one app through App Store
  Connect. Publishing requires automatic signing and a team API key. See [publishing](publishing.md#ios-apps-and-testflight).
- **Library publishing:** Maven Local skips signing, signing subkeys can be selected explicitly, generated sources
  enter sources JARs, and klib identities use publication coordinates. `notifyKlibsIo` optionally sends KMP coordinates
  after a Central upload; it does not release a deployment held for manual confirmation.
- **Compose:** Hot Reload tracks dependency-module changes correctly, and the docs now describe its filesystem watcher
  and MCP integration. Compose resource incrementality is fixed.
- **Checks:** Android checks use debug test tasks by default instead of running both debug and release variants.
- **Resolution and packaging:** fixes cover Native Kotest variants, incompatible single-platform dependencies,
  versionless group/name catalog entries, executable-JAR collisions, and explicit JVM release targets on newer JDKs.

## Corrections To The Tagged Markdown

The aggregate preserves upstream text. These tagged source files resolve remaining gaps or ambiguous release wording:

- [DefaultVersions.kt](https://github.com/JetBrains/kotlin-toolchain/blob/f53674bce744f0b5e9ff235110dcc1d565d2c114/sources/frontend-api/src/org/jetbrains/amper/frontend/schema/DefaultVersions.kt): Kotlin is `2.4.20`; the module reference still lists `2.4.10`.
- [Kotlin settings](https://github.com/JetBrains/kotlin-toolchain/blob/f53674bce744f0b5e9ff235110dcc1d565d2c114/sources/frontend-api/src/org/jetbrains/amper/frontend/schema/kotlin/kotlinSettings.kt): language/API versions are strings, despite the reference's enum labels.
- [Wasm-JS task wiring](https://github.com/JetBrains/kotlin-toolchain/blob/f53674bce744f0b5e9ff235110dcc1d565d2c114/sources/amper-cli/src/org/jetbrains/amper/tasks/wasm/taskBuilderWasmJs.kt): browser tests are registered; the product page still says unsupported. Wasm-WASI still has no runner.
- [TestCommand.kt](https://github.com/JetBrains/kotlin-toolchain/blob/f53674bce744f0b5e9ff235110dcc1d565d2c114/sources/amper-cli/src/org/jetbrains/amper/cli/commands/TestCommand.kt) and [integration tests](https://github.com/JetBrains/kotlin-toolchain/blob/f53674bce744f0b5e9ff235110dcc1d565d2c114/sources/test-integration/amper-cli-test/test/org/jetbrains/amper/cli/test/testing/TestCommandTest.kt): failure checks discovery across the selected tasks; all-skipped discovered tests succeed.
- [Dependency resolver](https://github.com/JetBrains/kotlin-toolchain/blob/f53674bce744f0b5e9ff235110dcc1d565d2c114/sources/dependency-resolution/src/org/jetbrains/amper/dependency/resolution/maven.kt): Android variant disambiguation prefers release, independently of the consumer build type.
- [PackageCommand.kt](https://github.com/JetBrains/kotlin-toolchain/blob/f53674bce744f0b5e9ff235110dcc1d565d2c114/sources/amper-cli/src/org/jetbrains/amper/cli/commands/PackageCommand.kt): IPA export options can be supplied with `--ipa-export-options-plist-path`; manual signing requires them.
