# Known Issues

Get issue status: https://youtrack.jetbrains.com/issue/KTC-XXXX

The tracker moved from the `AMPER` project to `KTC` in `0.12`. Always re-check status before relying on a workaround
here — this file is a snapshot, not a live feed.

## 0.12.0

- KTC-4871 — plugins cannot be published
- KTC-5603 — compiler diagnostics of every severity use an `ERROR` prefix (re-verify: `0.12` reworked task
  diagnostics via KTC-5515)
- KTC-5698 — Compose resources are not part of a KMP library publication
- KTC-5576 — Wasm-JS tests are not supported

## Fixed In 0.12.0

- KTC-5573 — cinterop klibs missing from test compilation and linking. Fixed in `0.12.0-dev-4187`, so `0.12.0`
  carries the fix. The workaround below is no longer needed; `0.12` also commonizes cinterop klibs and makes them
  visible in test sources.

## Issue List

### KTC-4871 Plugins publication

Kotlin Toolchain supports only local build plugins. A plugin cannot be packaged, published, and then consumed as a
published dependency. Keep the plugin as a `jvm/amper-plugin` module in the project (or vendor its sources into the
project), register it in `project.yaml`, and enable it locally. There is no workaround for consuming a published
plugin.

### KTC-5603 Compiler diagnostics of every severity use an `ERROR` prefix

Kotlin compiler diagnostics are logged with an `ERROR` prefix even when their actual severity is `warning:` or
`info:`. The build can still finish with `Build successful` and exit code 0. Use the severity inside the compiler
message, the final build result, and the process exit code instead of the outer log prefix.

There is no general workaround that corrects the prefix. For partial-linkage diagnostics specifically, make unresolved
symbols fail the build by adding:

```yaml
settings:
  kotlin:
    freeCompilerArgs: [ -Xpartial-linkage-loglevel=ERROR ]
```

`0.12` added nicely formatted task diagnostics (KTC-5515) and stopped replaying compiler warnings on cache hits
(KTC-4491). Neither is listed as fixing this issue, so check the current behavior before assuming it is gone.

### KTC-5698 Compose resources are not published with KMP libraries

A `kmp/lib` that uses `composeResources` publishes fine, but its resources are not part of the publication. Consumers
get the code without the resources, and fail at runtime if something tries to load one.

No workaround inside the publication. Ship the resources through a separate channel, or keep resource-owning code in
the consuming application until this lands.

### KTC-5576 Wasm-JS tests are not supported

`test` and `test@wasmJs` sources in a `wasm-js/app` module are not run. Test the logic from a module targeting a
platform that does have a test runner, and keep Wasm-specific code thin.

### KTC-5573 Cinterop klibs missing from test compilation (fixed)

Kept for projects still on `0.11.x`. A klib generated from a module's auto-discovered `cinterop/*.def` files was wired
into the main compilation and link, but not into the module's test compilation or test link. A direct cinterop
reference in test code failed to compile with unresolved references. Reaching the same declaration through main code
could compile and link, then fail at runtime with `IrLinkageError` because partial linkage inserted a throwing stub.
Repeating the dependency in `test-dependencies` or marking it `exported: true` did not help.

There was no portable manifest-only workaround. An absolute `-library` path worked but was machine-specific, while
`${module.rootDir}` references are not supported in `module.yaml`. The practical route was to put cinterop-dependent
assertions in a separate `macos/app` module's `main()`, run that binary from the normal test suite, and assert its
exit code, keeping the remaining logic behind a pure-Kotlin interface testable with a fake.

Upgrade to `0.12.0` instead of applying any of this.
