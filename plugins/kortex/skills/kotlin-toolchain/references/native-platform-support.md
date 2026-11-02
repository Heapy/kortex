# Native Hosts, Apple Platforms, And CI

Compatibility snapshot checked **2026-11-02**, alongside KTC **0.13.0** (default Kotlin **2.4.20**).
Recheck the sources below for the project's effective versions; runner labels and SDK availability change independently.

## Check Four Independent Constraints

Record the KTC wrapper pin and `./kotlin show settings -m <module>` before diagnosing platform failures.
Distinguish the **compiler host**, **output target**, **SDK/deployment version**, and **test execution environment**.
A working JVM-based KTC launcher or ARM64 JDK does not prove that its Native compiler can run on that host.
A `.klib` build does not prove that an executable/framework links, and linking does not prove tests ran.

Useful diagnostics on Unix hosts:

```shell
uname -sm
./kotlin show settings -m <module>
./kotlin show tasks
# On macOS:
sw_vers
xcode-select -p
xcodebuild -version
xcodebuild -showsdks
xcrun simctl list devices available
```

On Apple Silicon, also check the selected JDK/process architecture if tooling unexpectedly reports x86_64;
prefer a native ARM64 toolchain. A simulator runtime must actually be installed. Select a compatible full Xcode
with `DEVELOPER_DIR` when necessary; standalone Command Line Tools do not provide iOS simulator SDKs.

## Kotlin/Native Host And Target Support

[Kotlin's support table](https://kotlinlang.org/docs/native-target-support.html) lists macOS ARM64/x64,
Linux x64, and Windows x64 as compiler hosts. **Linux ARM64 is a target, not a supported compiler host.**
Apple final binaries require macOS; Linux/Windows can produce Apple `.klib` artifacts only without cinterop
dependencies. KTC product/task restrictions and native dependencies can narrow these compiler capabilities.

| Apple target | Kotlin status in this snapshot |
|---|---|
| `macosArm64`, `iosArm64`, `iosSimulatorArm64` | Tier 1 |
| `tvosArm64`, `tvosSimulatorArm64`, `watchosArm64`, `watchosSimulatorArm64` | Tier 2 |
| `iosX64`, `watchosDeviceArm64` | Tier 3 |
| `macosX64`, `watchosX64`, `tvosX64` | Deprecated since 2.3.20 |
| `watchosArm32` | Deprecated since 2.4.20 |

Tier 3, deprecated, and removed are different states. Do not invent a removal version.
`linuxArm64` is Tier 2, but has no out-of-the-box test runner in Kotlin's table.
Its Gradle/IDE “Running tests” column is not a promise about KTC task support.

KTC's [tagged product documentation](https://github.com/JetBrains/kotlin-toolchain/blob/v0.13.0/docs/src/reference/module.md#product)
and this skill's [platform hierarchy](multiplatform.md) add separate constraints:

- `ios/app` accepts `iosArm64` and `iosSimulatorArm64`; `iosX64` remains a `kmp/lib` option, with dependency limits
  (notably Compose). Prefer the ARM64 pair for new iOS projects.
- `macos/app` defaults to `macosArm64`; do not silently restore deprecated Intel targets.
- `watchosX64` is absent from the KTC 0.13 hierarchy. Its Kotlin deprecation does not make it a valid KTC platform.
- Do not invent `visionos*` leaf names because Xcode includes visionOS SDKs; check the pinned KTC platform model.

For existing libraries, inspect consumers and publication promises before removing targets. For new CI matrices,
include Intel Apple targets only for an explicit compatibility need, with verified compiler, SDK, and dependencies.

## macOS 27 And Xcode 27

**macOS 27 runs only on Apple Silicon**, per [Apple's compatibility list](https://support.apple.com/en-us/127255).
**Xcode 27 also runs only on Apple Silicon**, but its macOS 27 SDK can back-deploy Universal Intel/ARM64 apps to
older macOS versions. Host restrictions alone do not forbid Intel cross-compilation.
See [Xcode 27 release notes](https://developer.apple.com/documentation/xcode-release-notes/xcode-27-release-notes).

Xcode's host OS requirement is separate: Apple's [Xcode compatibility table](https://developer.apple.com/xcode/system-requirements)
lists macOS 26.6 or later for Xcode 27. Do not require macOS 27 solely because the Xcode major is 27.
Check the chosen Kotlin release's Xcode support as well; installing the newest Xcode is not proof of compatibility.

For iOS/tvOS/watchOS, device and simulator binaries are different targets even when both use ARM64.
Run simulator tests against `*SimulatorArm64`; a successful `iosArm64` framework build is not an iOS test run.
Device execution needs a separate device/signing workflow. Do not assume Rosetta supplies an Intel simulator
runtime or resolves Kotlin target deprecations. Preserve older deployment requirements only where the selected
Kotlin compiler, SDK, dependencies, and test environment can all satisfy them.

## Linux ARM64: Build On x64, Execute On ARM64

Use separate CI jobs when Linux ARM64 runtime coverage is required:

1. On **Linux x64**, cross-compile the `linuxArm64` test executable. Inspect `./kotlin show tasks` and the pinned
   CLI's help/source to select the test compile/link task without running it; do not invent a Gradle task name or
   a KTC `--no-run` option. Confirm that the output contains tests, not just the application's main function.
2. Upload that executable with its resources and target-native shared libraries. Use `file` to verify AArch64 ELF;
   preserve executable permissions in an archive or restore them after download. Check glibc/loader compatibility
   and cinterop libraries for the destination. An x64 library cannot satisfy an ARM64 link or load.
3. In a dependent **Linux ARM64** job, download and execute the built tests directly. Propagate their exit status
   and retain reports. Do not call KTC `test`/`check` there if that would invoke the unsupported Native compiler.

Do not pass `linuxArm64` to an x64 `ktc-check` job expecting it to execute ARM64 tests. Build-only coverage must be
reported as such. If the pinned KTC cannot expose the required test artifact, report that limitation and inspect
its task implementation before claiming ARM64 coverage. Emulation is an explicit alternative with different
coverage/performance characteristics, not evidence of native compiler-host support.

## GitHub Runner Selection

Check the live [runner-images matrix](https://github.com/actions/runner-images#available-images) and the chosen
image's installed-software README. At this snapshot:

| Job purpose | Example label | Architecture / constraint |
|---|---|---|
| Linux Native compilation; x64 tests | `ubuntu-24.04` | x64; can build Linux ARM64 artifacts |
| Execute prebuilt Linux ARM64 tests | `ubuntu-24.04-arm` | ARM64; do not compile K/N here |
| Apple builds / ARM64 simulator tests | `macos-26` | ARM64; verify selected Xcode/runtime |
| Xcode 27 image | `xcode-27` | ARM64; verify current availability/status |
| Intentional legacy Intel Mac coverage | `macos-26-intel` | x64; cannot run Xcode 27 |

Do not synthesize a `macos-27` label from an OS version: the documented Xcode 27 image uses `xcode-27`.
`macos-latest` is moving and currently ARM64; a versioned label still receives image updates.
Inspect job logs for the actual image version and `runner.arch`, and keep caches separated by architecture.
See [GitHub Actions](github-actions.md) for platform filters and action contracts. Recheck the selected action's
release and `action.yml`; upgrading an action or runner cannot add a missing Kotlin/Native compiler host.
