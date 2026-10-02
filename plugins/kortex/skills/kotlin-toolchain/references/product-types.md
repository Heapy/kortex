# Product Types

Kotlin Toolchain `v0.13.0`. One module produces exactly one product.

## Declaring

```yaml
product: jvm/app
```

```yaml
product:
  type: kmp/lib
  platforms: [jvm, android, iosArm64, iosSimulatorArm64]
```

`platforms` accepts leaf platform names only, never family names like `ios` or `apple`. The short form derives the
platform list from the type.

| Type | Supported platforms |
|---|---|
| `jvm/app` | `jvm` |
| `jvm/lib` | `jvm` |
| `kmp/lib` | any; must be listed explicitly |
| `android/app` | `android` |
| `ios/app` | `iosArm64` (device), `iosSimulatorArm64` (simulator) |
| `js/app` | `js` |
| `wasm-js/app` | `wasmJs` |
| `wasm-wasi/app` | `wasmWasi` |
| `linux/app` | `linuxX64`, `linuxArm64` |
| `macos/app` | `macosArm64` (`macosX64` deprecated) |
| `windows/app` | `mingwX64` |
| `jvm/amper-plugin` | `jvm` |

Apple Intel is being phased out. `iosX64` is no longer accepted for `ios/app` — it remains available for `kmp/lib`,
though Compose libraries already do not support it. `macosX64` is deprecated since Kotlin 2.3.20 and is no longer in
the `macos/app` defaults.

## `jvm/app`

Entry point: a top-level `main` in a `main.kt` file (case-insensitive) in `src`. The function takes no parameters or
one `Array<String>`. Override with:

```yaml
settings:
  jvm:
    mainClass: org.example.myapp.MyMainKt
```

Kotlin compiles top-level declarations into a class named after the file: `myMain.kt` becomes `MyMainKt`.

`build` produces a regular JAR. `package` produces an executable JAR in the Spring Boot loader format — which is why
`spring-boot-loader` shows up inside it, regardless of whether the app uses Spring. That format keeps dependency JARs
intact instead of merging them, so signatures, manifests, and service-loader resources stay valid.

`layout: maven-like` is available for Maven migration.

## `jvm/lib`

Sources in `src` (Kotlin and Java can be mixed in the same folder), resources in `resources`, tests in `test`, test
resources in `testResources`.

`package` is not defined by default. Enabling Maven Central publishing adds the `maven-central-bundle` format, and
since it is the only one, `kotlin package` then produces the ZIP bundle ready for upload to the Central Portal.

`kotlin publish <repository>` publishes it. `layout: maven-like` is available.

## `kmp/lib`

A reusable Kotlin Multiplatform library. `product.platforms` must list concrete leaf platforms.

Publishable — see `publishing.md`. KMP metadata compilation and cinterop commonization are supported;
since `0.13`, Compose resources and SwiftPM dependency metadata are also published.

## `android/app`

Entry point is declared in `src/AndroidManifest.xml`, per the standard Android manifest rules.

`build` creates an APK. `package` creates a release AAB, minified and obfuscated with R8 and signed when signing is enabled. `proguard-rules.pro` and
`google-services.json` are auto-detected beside `module.yaml`.

`./kotlin run` installs and starts it.

Duplicate Java resources from dependencies fail packaging in `MergeJavaResWorkAction` with `2 files found with path
...`. Fix with `settings.android.resourcePackaging`, choosing the rule that matches the resource's semantics:

- `excludes` — omit matching resources from the APK
- `pickFirsts` — package only the first match
- `merges` — concatenate all matches into one entry

Set `settings.android.namespace` explicitly for apps; `applicationId` defaults from it. Use `abiFilters` to
package only ABIs supported by every required native library. Identity, SDK levels, and signing are covered in
`settings.md`.

For Google Play, enable signing, run `kotlin package -m <app>`, and use the AAB path printed by the CLI. The IDE's
"Generate Signed App Bundle or APK" action does not support Kotlin Toolchain projects. Increment `versionCode`
for each upload; store upload credentials and keystores outside version control.

## `ios/app`

Requires an Xcode project named `module.xcodeproj` in the module root. `kotlin init` and the IDE wizard create it; on a
from-scratch project a default buildable one is generated after the first build, and can then be customized and
committed.

Entry point is a Swift `@main` struct in any Swift file in `src`. Not customizable.

To retrofit an existing Xcode project, ensure the target has:

1. `Debug` and `Release` build configurations, each with `KOTLIN_CLI_WRAPPER_PATH` set to the wrapper path relative to
   the Kotlin module root;
2. a script build phase named **`Build Kotlin`** running:

   ```bash
   # !KOTLIN INTEGRATION STEP!
   # This script is managed by the Kotlin Toolchain, do not edit manually!
   "${KOTLIN_CLI_WRAPPER_PATH}" tool xcode-integration
   ```

A specific Xcode scheme is enforced for the project.

Kotlin code reaches Swift through the generated `KotlinModules` framework, built from the `ios/app` module itself, the
modules it depends on, and all transitive external dependencies. Only declarations from your own Kotlin source are
visible to Swift — external dependencies are bundled but not exposed.

Pure Swift APIs are not directly callable from Kotlin. Since `0.13`, SwiftPM can expose Objective-C-visible APIs
to Kotlin on Apple targets, including `kmp/lib`; see [dependencies](dependencies.md#swiftpm-dependencies).

Xcode must already be installed, its license accepted, and first-launch setup completed. The toolchain checks this
environment and can provision required Xcode components; it does not install Xcode itself. For App Store/TestFlight
distribution, configure signing, bundle ID, deployment target, and version/build in `module.xcodeproj`, then archive
and export through Xcode or `xcodebuild`. This is separate from `kotlin publish`, which publishes libraries.

## `js/app`

Incomplete preview. `build` emits `.mjs` under `build/tasks/_<module>_linkJs`. The CLI cannot run it — use an external
JS runtime. `package` is not supported.

## `wasm-js/app`

Browser-targeted Kotlin/Wasm. Entry point is a top-level `main` in `src`; multiple `main` functions are unsupported and
the chosen one is unspecified.

`build` packages the app under `build/tasks/_<module>_buildWasmJsAppWasmJs<Debug|Release>`, containing the `.wasm`
module, `.mjs` loaders, JS dependencies, the Skiko Wasm runtime, and an `index.html`.

`run` starts a local server and opens the app in a browser.

A custom `index.html` can be placed in the module's `resources` folder. Available template variables:

- `{{kotlin.moduleName}}` — module name
- `{{kotlin.moduleFile}}` — the `.mjs` wrapper that loads the app
- `{{kotlin.scripts}}` — the minimal script set required to load the app, including the wrapper and the import-map
  loader

Direct NPM dependencies cannot be declared. NPM dependencies required by a KMP library you depend on (for example
`@js-joda/core` for `kotlinx-datetime`) are downloaded and packed automatically.

`kotlin test -m <module> --platform wasmJs` runs tests in provisioned Chromium via Playwright in `0.13`
(KTC-5576). The tagged Markdown still says unsupported; the tagged task implementation and integration tests confirm
the runner. `package` remains unsupported.

## `wasm-wasi/app`

Incomplete preview. Entry point is a top-level `main` in `src`.

`build` emits the `.wasm` plus an `.mjs` loader under
`build/artifacts/CompiledWebArtifact/<module-name>wasmWasi<debug|release>`.

The CLI cannot run it. Use Node.js, Deno, WasmEdge, or another WASI runtime on the `.mjs` wrapper. `package` is not
supported. There is no Wasm-WASI test runner in `0.13`.

## `linux/app`, `macos/app`, `windows/app`

Entry point is a top-level `main` in a `main.kt` file (case-insensitive) in `src`, taking no parameters or one
`Array<String>`. Override with:

```yaml
settings:
  native:
    entryPoint: org.example.myapp.myMainFun
```

`build` links an executable: `.exe` on Windows, `.kexe` elsewhere. `package` is not supported.

`settings.kotlin.debug` and `settings.kotlin.optimization` default per build variant (debug info in debug
builds, optimization in release builds). `settings.kotlin.linkerOptions` passes extra linker
arguments. `settings.kotlin.compileIncrementally` also enables Native dependency caches for non-optimized
debug linking on supported targets in `0.13`; see [settings](settings.md).

## `jvm/amper-plugin`

A local build plugin module. See `plugins.md`. Plugins cannot be published or consumed as a published dependency.
