# GitHub Actions

Heapy maintains four Apache-2.0 actions, available in GitHub Marketplace. Their action versions are independent of
the Kotlin Toolchain version. Read the linked README and `action.yml` at the selected release for the full contract.

| Action | Use it for |
|---|---|
| [`Heapy/setup-ktc`](https://github.com/Heapy/setup-ktc) | Install a checksum-verified CLI wrapper and cache toolchain downloads, dependencies, JDKs, and Kotlin/Native data |
| [`Heapy/update-ktc`](https://github.com/Heapy/update-ktc) | Update both wrappers, validate with build/check, and open or maintain an upgrade PR |
| [`Heapy/ktc-check`](https://github.com/Heapy/ktc-check) | Build and test selected platforms, run plugin checks, summarize JUnit results, annotate failures, and upload reports even on failure |
| [`Heapy/ktc-publish`](https://github.com/Heapy/ktc-publish) | Run checks and prepare a Maven Central bundle or explicitly publish to a configured Maven repository |

## Setup and checks

Check out the project before setup. `version: auto` detects the committed wrapper pin; without wrappers, setup
[v1.1.0](https://github.com/Heapy/setup-ktc/releases/tag/v1.1.0) falls back to 0.13.0. Use an explicit version when
there is no project pin rather than relying on an action's fallback to track this skill.
The actions require Node.js 22+ and Bash, available on the supported GitHub-hosted Linux, macOS, and Windows runners.
Run setup before `ktc-check` or `ktc-publish`; use the same `working-directory` for each action in a nested project.

```yaml
name: Kotlin CI
on: [push, pull_request]
permissions:
  contents: read
jobs:
  check:
    runs-on: ubuntu-24.04 # Select a host compatible with the project's targets.
    steps:
      - uses: actions/checkout@v7
      - uses: Heapy/setup-ktc@v1
        with:
          version: auto
      - uses: Heapy/ktc-check@v1
```

The example uses major tags for readability. Prefer verified release commit SHAs in maintained workflows and let
Dependabot update them. `ktc-check` builds first by default and needs no `checks: write` permission. Set a unique
`artifact-name` when multiple matrix entries share the same OS, architecture, and job ID.

### Platform selection

Since [ktc-check v1.1.0](https://github.com/Heapy/ktc-check/releases/tag/v1.1.0), the `platforms` input accepts comma-
or whitespace-separated target names. Select targets declared by your modules that can build and run on the runner;
cross-compiling an executable does not make that runner able to test it. For example, on a Linux x64 runner:

```yaml
- uses: Heapy/ktc-check@v1
  with:
    checks: tests
    platforms: linuxX64, jvm
    artifact-name: kotlin-reports-linuxX64-jvm
```

With `platforms` set, the action selects platforms for `kotlin build` and runs built-in tests through
`kotlin test --platform ...`. Kotlin Toolchain 0.13's `kotlin check` does not accept platform selection, so plugin
checks run separately with the selected modules and their normal platform behavior, without repeating built-in
tests. An empty `platforms` input preserves the existing build/check commands.

`modules`, `checks`, `skip`, and `build: false` still apply. Selecting only plugin checks does not run built-in
tests. Use `build: false` when an earlier step has already built the required executables. See the
[v1.1.0 target table](https://github.com/Heapy/ktc-check/blob/v1.1.0/README.md#platform-selection) for Kotlin Toolchain
0.13.0 target identifiers and deprecated targets; actual support also depends on product type and runner.

### Caching

Setup cache keys omit branch names so feature branches can reuse eligible default-branch caches. With
`cache-read-only: auto`, all PRs, including forks, restore only; push, scheduled, and manual runs may save.
GitHub enforces branch/repository access: fork PRs in the parent can restore eligible base caches, while pushes
inside a fork use the fork's own cache. Project build outputs are not cached. Use
`cache-key-suffix` to invalidate a namespace and never execute untrusted fork code under `pull_request_target`.

Since setup v1.1.0, Kotlin/Native data is cached by default in a separate namespace, alongside the toolchain caches.
The native path is `~/.konan`, or an existing `KONAN_DATA_DIR`; a custom path must be absolute and contain no line
breaks. Setup preserves that environment setting and existing contents. Native cache keys use the same OS,
architecture, toolchain, configuration, and `cache-key-suffix` boundaries, with the same `cache-read-only` policy.
No project-model detection is needed; if the native directory does not exist, there is no native cache to save.

Set `cache-konan: false` to disable only native caching, or `cache: false` to disable both toolchain and native
caching. The `konan-cache-hit` output reports whether the exact native cache key was restored and is empty when
native caching is disabled.

```yaml
- uses: Heapy/setup-ktc@v1
  with:
    cache-konan: false
```

## Wrapper upgrades and publication

Run `update-ktc` on a trusted scheduled or manual workflow with `contents: write` and `pull-requests: write`.
It defaults to the latest stable toolchain and validates the changed wrappers before opening a PR. Set
`create-pull-request: false` for a local-only update. PRs created with `GITHUB_TOKEN` do not trigger ordinary
downstream PR workflows; use an appropriately scoped GitHub App token if those checks must run automatically.

`ktc-publish` defaults to `mode: bundle`, `repository: mavenCentral`, and `check: true`. Actual publication requires
`mode: publish` and the project's [publishing configuration](publishing.md). Pass Central credentials through
`central-username`/`central-password` and PGP secrets through `signing-key`/`signing-passphrase`, using GitHub secrets.
The action preserves the project's Central `publishingMode`: `published: true` means the CLI publish command
succeeded, and a manual Central release may still be pending.

Use platform-appropriate test jobs followed by one publishing job to avoid duplicate Maven coordinates. macOS is
a practical publishing host for libraries with Apple targets, but not a guarantee that every target can be built
or tested there: verify cinterop dependencies, SDKs, and the selected toolchain's host support. See Kotlin's
[host requirements](https://kotlinlang.org/docs/multiplatform/multiplatform-publish-lib-setup.html#host-requirements)
for the underlying constraints; Gradle-specific instructions there do not configure Kotlin Toolchain.
The initial `ktc-publish` v1.0.0 integration tests cover JVM Maven Local publication on three OSes and a signed
Central bundle; they do not establish all-target KMP publication coverage.
