# GitHub Actions

Heapy maintains four Apache-2.0 actions, available in GitHub Marketplace. Their action versions are independent of
the Kotlin Toolchain version. Read the linked README and `action.yml` at the selected release for the full contract.

| Action | Use it for |
|---|---|
| [`Heapy/setup-ktc`](https://github.com/Heapy/setup-ktc) | Install a checksum-verified CLI wrapper and cache toolchain downloads, dependencies, and JDKs |
| [`Heapy/update-ktc`](https://github.com/Heapy/update-ktc) | Update both wrappers, validate with build/check, and open or maintain an upgrade PR |
| [`Heapy/ktc-check`](https://github.com/Heapy/ktc-check) | Build and run checks, summarize JUnit results, annotate failures, and upload reports even on failure |
| [`Heapy/ktc-publish`](https://github.com/Heapy/ktc-publish) | Run checks and prepare a Maven Central bundle or explicitly publish to a configured Maven repository |

## Setup and checks

Check out the project before setup. `version: auto` detects the committed wrapper pin; without wrappers, setup
v1.0.1 falls back to 0.13.0, not this skill's 0.12.2 snapshot. Use an explicit version when there is no project pin.
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

Setup cache keys omit branch names so feature branches can reuse eligible default-branch caches. With
`cache-read-only: auto`, all PRs, including forks, restore only; push, scheduled, and manual runs may save.
GitHub enforces branch/repository access: fork PRs in the parent can restore eligible base caches, while pushes
inside a fork use the fork's own cache. Project build outputs and `~/.konan` are not cached. Use
`cache-key-suffix` to invalidate a namespace and never execute untrusted fork code under `pull_request_target`.

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

