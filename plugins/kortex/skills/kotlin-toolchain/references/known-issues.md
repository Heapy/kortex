# Known Issues

Get issue status: https://youtrack.jetbrains.com/issue/KTC-XXXX

Issues use the `KTC` tracker project. Re-check status before relying on a workaround; this file records the
`0.14.1` snapshot.

## 0.14.1

- KTC-4871 — plugins still cannot be published.
- The tagged docs contain stale defaults and feature limitations; see
  [migration source notes](migrating-0.13-to-0.14.md#corrections-to-the-tagged-markdown).

## Fixed In 0.14.1

- KTC-6152 updates the CLI runtime for CVE-2026-114938. Projects with separately provisioned JDKs should
  [refresh their cached JDK](cli.md#refreshing-provisioned-jdks).

## Fixed Or Improved In 0.14.0

- KTC-5980 — Android apps require `applicationId`; `namespace` now defaults from it. This is a breaking change.
- KTC-5747 — `test` fails when no tests are discovered; discovered but skipped tests still count.
- KTC-5471 / KTC-6014 — generated sources enter sources JARs; klib identities preserve publication coordinates.
- KTC-6058 / KTC-6059 — Maven Local ignores signing; unrelated non-publishing modules no longer require the target repo.
- KTC-6045 / KTC-5983 — Hot Reload notices dependency-module changes; Compose resource incrementality is fixed.
- KTC-6048 — Android variant ambiguity prefers release. Consumer build type is not used for dependency selection.
- KTC-4543 / KTC-6037 / KTC-6129 — Native Kotest resolution, incompatible single-platform dependencies, and
  versionless group/name catalog entries are fixed.
- KTC-6011 / KTC-6051 / KTC-6033 — executable-JAR name collisions, JDK 8 release packaging, and explicit release
  targets with newer JDKs are fixed.
- KTC-6056 — Android checks no longer run both debug and release test tasks by default.

Other fixes include Wasm test reporting, Xcode error output, and versioned/shared incremental snapshots.
See the [release notes](https://github.com/JetBrains/kotlin-toolchain/releases/tag/v0.14.0) for the full list.

## Issue List

### KTC-4871 Plugins publication

Kotlin Toolchain supports only local build plugins. A plugin cannot be packaged, published, and then consumed as a
published dependency. Keep the plugin as a `jvm/amper-plugin` module in the project (or vendor its sources into the
project), register it in `project.yaml`, and enable it locally. There is no workaround for consuming a published
plugin.
