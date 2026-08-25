# Multiplatform Modules

Kotlin Toolchain `v0.12.0`.

## Platform Hierarchy

```
common
 ├─ jvm
 ├─ android
 ├─ web
 │   ├─ js
 │   ╰─ wasmJs
 ├─ wasmWasi
 ╰─ native
     ├─ linux
     │   ├─ linuxX64
     │   ╰─ linuxArm64
     ├─ mingw
     │   ╰─ mingwX64
     ├─ apple
     │   ├─ macos
     │   │   ├─ macosX64 (deprecated)
     │   │   ╰─ macosArm64
     │   ├─ ios
     │   │   ├─ iosArm64
     │   │   ├─ iosSimulatorArm64
     │   │   ╰─ iosX64
     │   ├─ watchos
     │   │   ├─ watchosArm32 (deprecated)
     │   │   ├─ watchosArm64
     │   │   ├─ watchosDeviceArm64
     │   │   ╰─ watchosSimulatorArm64
     │   ╰─ tvos
     │       ├─ tvosArm64
     │       ├─ tvosSimulatorArm64
     │       ╰─ tvosX64 (deprecated)
     ╰─ androidNative
         ├─ androidNativeArm32
         ├─ androidNativeArm64
         ├─ androidNativeX64
         ╰─ androidNativeX86
```

Intermediate nodes are platform *families*, leaves are *platforms*. Not all of them are equally supported or tested.

`macosX64`, `watchosArm32`, and `tvosX64` are deprecated as of `0.12`. `iosX64` still exists for `kmp/lib` but is not
accepted by `ios/app` and is being phased out.

## Choosing Platforms

```yaml
product:
  type: kmp/lib
  platforms: [iosArm64, android, jvm]
```

`product.platforms` accepts platform names only, never family names. Family shortcuts would silently change the
platform list when Kotlin is bumped, so they are rejected.

## Platform Qualifier

The `@platform` suffix marks platform-specific directories and configuration sections. What follows `@` is a platform,
a platform family, or an alias.

Directories: `src@jvm`, `resources@jvm`, `test@jvm`, `testResources@jvm`, `cinterop@linux`.

Sections: `dependencies@ios`, `test-dependencies@ios`, `settings@android`, `test-settings@android`.

Common code is visible to more-specific code, never the reverse. Put `expect` declarations in `src` and `actual`
declarations in `src@<platform>`.

## Propagation

Common `dependencies:` and `settings:` propagate down to every platform section.

- Scalars are **overridden** by the more specific section.
- Lists are **concatenated**; mappings are **merged by key**. The docs phrase this as "mappings and lists are
  appended"; in practice two `settings:` blocks combine, and only conflicting scalar leaves are replaced.

Dependencies:

```yaml
product:
  type: kmp/lib
  platforms: [android, iosArm64, iosSimulatorArm64]

dependencies:
  - //foo
dependencies@ios:
  - //bar
dependencies@iosArm64:
  - //baz
```

Effective:

| Platform | Dependencies |
|---|---|
| `android` | `//foo` |
| `iosSimulatorArm64` | `//foo`, `//bar` |
| `iosArm64` | `//foo`, `//bar`, `//baz` |

Settings:

```yaml
settings:
  kotlin:
    languageVersion: 2.4
    freeCompilerArgs: [x]
  android:
    compileSdk: 33

settings@android:
  compose: enabled

settings@ios:
  kotlin:
    languageVersion: 2.3
    freeCompilerArgs: [y]

settings@iosSimulatorArm64:
  kotlin:
    freeCompilerArgs: [z]
```

Effective:

| Platform | `languageVersion` | `freeCompilerArgs` |
|---|---|---|
| `android` | 2.4 (from `settings`) | `[x]` |
| `iosArm64` | 2.3 (from `settings@ios`) | `[x, y]` |
| `iosSimulatorArm64` | 2.3 | `[x, y, z]` |

Think of it as merging maps: a scalar at a deeper level replaces the shallower one, a collection adds to it.

Templates use the exact same merging rules — see `templates.md`.

## Aliases

When a group of platforms has no common ancestor in the default hierarchy, define one:

```yaml
product:
  type: kmp/lib
  platforms: [iosArm64, android, jvm]

aliases:
  - jvmAndAndroid: [jvm, android]

dependencies@jvmAndAndroid:
  - org.lighthousegames:logging:1.3.0

settings@jvmAndAndroid:
  kotlin:
    freeCompilerArgs: [ -jvm-default=no-compatibility ]
```

The alias then works as a source directory too:

```
├─ src/
├─ src@jvmAndAndroid/  # sees src/
├─ src@jvm/            # sees src/ and src@jvmAndAndroid/
╰─ src@android/        # sees src/ and src@jvmAndAndroid/
```

## Multiplatform Dependencies

KMP library dependencies resolve their platform-specific artifacts automatically. Declare the common artifact in
`dependencies:` and add platform-specific ones in qualified blocks:

```yaml
dependencies:
  - io.ktor:ktor-client-core:2.3.0
dependencies@android:
  - io.ktor:ktor-client-android:2.3.0
dependencies@ios:
  - io.ktor:ktor-client-darwin:2.3.0
```

New in `0.12`: a JVM+Android "common" fragment can depend on plain JVM libraries, and a `jvm/lib` module can be added
as a dependency of an Android fragment.

## Native Interop (cinterop)

Zero configuration for the common case:

1. Create a `cinterop` directory in the module root, beside `src` and `test`.
2. Drop `.def` files into it.

The toolchain detects them and configures `cinterop` for every applicable native platform. Nothing goes into
`module.yaml`.

Use `cinterop@<platform>` to limit definitions to a platform or family. Alternatively, keep a single `.def` in the
common directory and use its own platform-specific keys (`compilerOpts.linux` vs `compilerOpts.osx`). Both work.

**Bundled headers**, new in `0.12`: put vendored C headers in an `include` directory next to the `.def` files. The
toolchain passes it to `cinterop` as an extra header search path, equivalent to `-I<path>`. No configuration needed.

Published KMP libraries carry `cinterop` bindings twice: commonized, for use from common code, and per platform. That
is what the publishing docs promise. The release notes add that `0.12` commonizes cinterop klibs (KTC-5437) and feeds
commonized cinterops into KMP metadata compilation (KTC-5585).

To generate or provision a `.def` file dynamically, write a plugin that contributes `generated.cinteropDefinitions`.

## KSP In Multiplatform Modules

KSP runs separately for each platform. Generated code is only visible to the platform it was generated for; there is
no way to expose it to common sources. Restrict processors to a platform with a qualified `settings@platform` block.

## Gotcha

`settings.android` is the Android **toolchain** settings block. `settings@android` is the platform-qualified settings
for the Android target. They are different things and both can appear in the same file.
