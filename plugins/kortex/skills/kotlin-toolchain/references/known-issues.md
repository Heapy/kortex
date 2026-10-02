# Known Issues

Get issue status: https://youtrack.jetbrains.com/issue/KTC-XXXX

Issues use the `KTC` tracker project. Re-check status before relying on a workaround; this file records the
`0.13.0` snapshot.

## 0.13.0

- KTC-4871 — plugins still cannot be published.
- The tagged docs contain stale defaults and feature limitations; see
  [migration source notes](migrating-0.12-to-0.13.md#corrections-to-the-tagged-markdown).

## Fixed Or Improved In 0.13.0

- KTC-5698 / KTC-3585 — Compose resources can now be published with and consumed from KMP libraries.
- KTC-5576 — Wasm-JS tests run through a Chromium browser runner; this does not add a Wasm-WASI test runner.
- KTC-4240 / KTC-4791 — Native and JS/Wasm compiler warnings no longer use error logging.
- KTC-5795 — incremental builds restore deleted `.class` outputs instead of succeeding with missing classes.
- KTC-5845 — a POM description can satisfy Maven Central validation without duplicating the module description.
- KTC-5973 — wrapper failures no longer leave the CLI distribution cache corrupted.

Other fixes include KSP/Room failures and hangs, Compose resources in Android AARs, and dependency resolution bugs.
See the [release notes](https://github.com/JetBrains/kotlin-toolchain/releases/tag/v0.13.0) for the full list.

## Issue List

### KTC-4871 Plugins publication

Kotlin Toolchain supports only local build plugins. A plugin cannot be packaged, published, and then consumed as a
published dependency. Keep the plugin as a `jvm/amper-plugin` module in the project (or vendor its sources into the
project), register it in `project.yaml`, and enable it locally. There is no workaround for consuming a published
plugin.
