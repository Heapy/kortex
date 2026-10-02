# Migrating From 0.12 To 0.13

Based on [release 0.13.0](https://github.com/JetBrains/kotlin-toolchain/releases/tag/v0.13.0), published 2026-10-01,
and tag `abc7a5f7579e9ef267107782048c76777f0bf932`. The shared topic references target this release.

## Upgrade An Existing Project

For an authorized upgrade, run `./kotlin update --target-version 0.13.0` and retain both changed wrappers. Then:

1. Replace explicit `layout: amper` with `layout: default`, or remove the key to use the default. Source directory
   names do not change; `maven-like` is unchanged. `jvm/amper-plugin` is still the product type for build plugins.
2. Replace `AMPER_BUILD_DIR` in CI or shell scripts with `KOTLIN_TOOLCHAIN_BUILD_DIR`. `--build-dir` overrides it.
3. Set `settings.android.namespace` on every `android/app`. `applicationId` defaults to that namespace; setting only
   `applicationId` does not satisfy the namespace diagnostic. Keep the existing application ID when upgrading an app.
4. Check Android library namespaces. Omitted namespaces now derive from `publishing.group` and the sanitized effective
   artifact ID (which defaults to the module name), or from a generated module-specific fallback when no group exists.
   Set an explicit namespace if source code or consumers depend on generated `R` / `BuildConfig` package names.
5. Review the new defaults below. Pin versions if the project needs to preserve its old compiler or framework versions.
6. Prefer `--compose-hot-reload` over the old `--compose-hot-reload-mode`. Eligible JVM Compose runs now enable hot
   reload automatically; pass `--no-compose-hot-reload` for a normal run.
7. Run `./kotlin show settings`, then the relevant build and test commands. Exercise newly supported Wasm-JS tests and
   published Compose resources on the platforms the project actually ships.

An `android/app` identity block can be as small as:

```yaml
settings:
  android:
    namespace: com.example.app
    # applicationId defaults to namespace; preserve an existing distinct ID explicitly.
```

## Changed Defaults

| Setting | 0.12.2 | 0.13.0 |
|---|---|---|
| Kotlin | 2.4.10 | 2.4.20 |
| Compose Multiplatform | 1.11.1 | 1.12.1 |
| Ktor | 3.5.2 | 3.6.0 |
| Spring Boot | 4.1.0 | 4.1.1 |
| KSP | 2.3.11 | 2.3.12 |
| kotlinx.rpc | 0.10.3 | 0.10.4 |
| Lombok | 1.18.46 | 1.18.48 |

JDK 25, JUnit Platform 6.1.3, Android SDK 37 / minSdk 24 / Build Tools 37.0.0, Compose Hot Reload 1.2.0,
kotlinx.serialization 1.11.0, and DataFrame 1.0.0-rc01 are unchanged. Minimum runtime JDK 17 and compiler Kotlin
2.2.20 are also unchanged.

## New Capabilities

- **SwiftPM interop:** remote and local package objects work in Apple fragments; Kotlin imports Objective-C-visible
  APIs. SwiftPM metadata accompanies library publications. Libraries with local Swift packages may publish only to
  `mavenLocal`. See [dependencies](dependencies.md#swiftpm-dependencies).
- **Compose resources:** KMP publications now include them and published resource dependencies can be consumed.
  Public accessors still require `settings.compose.resources.exposedAccessors: true`.
- **Tests:** Wasm-JS runs in provisioned Chromium via Playwright. Wasm-WASI has no registered test runner. JUnit tag
  filters use `--include-tag` / `--exclude-tag`; other platforms count as untagged. See [CLI](cli.md).
- **Compiler settings:** `settings.kotlin.explicitApi` is `strict`, `warning`, or `disable` and ignores tests.
  `languageVersion` and `apiVersion` accept strings. Native `compileIncrementally` reuses external dependency caches
  for non-optimized binary linking on supported targets, not incremental klib compilation.
- **Android:** `abiFilters` restricts native libraries packaged from `jniLibs` and dependencies. Packaging warns about
  inconsistent native-library coverage across ABIs.
- **Project setup:** `kotlin init` adds a KMP wizard; `kotlin update --create` installs missing wrappers.
- **Maven integration:** library-catalog references work in `project.yaml`'s `mavenPlugins` and in per-mojo dependencies.
  `settings.publishing.pom.description` can satisfy Central's description requirement without a module description.
- **Apple setup:** the CLI checks Xcode and provisions components; Xcode installation, license acceptance, and first
  launch remain prerequisites.

## Corrections To The Tagged Markdown

The raw aggregate is preserved as upstream wrote it. These source files at the same tag resolve discrepancies:

- [DefaultVersions.kt](https://github.com/JetBrains/kotlin-toolchain/blob/abc7a5f7579e9ef267107782048c76777f0bf932/sources/frontend-api/src/org/jetbrains/amper/frontend/schema/DefaultVersions.kt): Kotlin defaults to `2.4.20`, despite `reference/module.md` still saying `2.4.10`.
- [Android settings](https://github.com/JetBrains/kotlin-toolchain/blob/abc7a5f7579e9ef267107782048c76777f0bf932/sources/frontend-api/src/org/jetbrains/amper/frontend/schema/androidSettings.kt) and [app diagnostic](https://github.com/JetBrains/kotlin-toolchain/blob/abc7a5f7579e9ef267107782048c76777f0bf932/sources/frontend/schema/src/org/jetbrains/amper/frontend/diagnostics/AndroidApplicationNamespaceMissing.kt): apps require `namespace`; libraries derive it. The Markdown's `org.example.namespace` default is stale.
- [Kotlin settings](https://github.com/JetBrains/kotlin-toolchain/blob/abc7a5f7579e9ef267107782048c76777f0bf932/sources/frontend-api/src/org/jetbrains/amper/frontend/schema/kotlin/kotlinSettings.kt): language/API versions accept strings, despite the Markdown's enum labels.
- [Wasm-JS tasks](https://github.com/JetBrains/kotlin-toolchain/blob/abc7a5f7579e9ef267107782048c76777f0bf932/sources/amper-cli/src/org/jetbrains/amper/tasks/wasm/taskBuilderWasmJs.kt) and [browser runner](https://github.com/JetBrains/kotlin-toolchain/blob/abc7a5f7579e9ef267107782048c76777f0bf932/sources/amper-cli/src/org/jetbrains/amper/tasks/wasm/BrowserTestTask.kt): browser tests are registered and run; the product page still says unsupported.
- [RunCommand.kt](https://github.com/JetBrains/kotlin-toolchain/blob/abc7a5f7579e9ef267107782048c76777f0bf932/sources/amper-cli/src/org/jetbrains/amper/cli/commands/RunCommand.kt): hot reload is selected automatically for eligible JVM Compose runs; the old flag remains a deprecated alias.
- [TestCommand.kt](https://github.com/JetBrains/kotlin-toolchain/blob/abc7a5f7579e9ef267107782048c76777f0bf932/sources/amper-cli/src/org/jetbrains/amper/cli/commands/TestCommand.kt): tag-expression semantics and module-selection requirements are enforced by the CLI, though the testing page has not been updated.
