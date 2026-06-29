---
name: kotlin-toolchain
description: Use when working with JetBrains Kotlin Toolchain, formerly Amper, including project.yaml or module.yaml configuration, Kotlin/JVM, Android, iOS, Kotlin Multiplatform, Kotlin/JS, Kotlin/Wasm, Kotlin/Native, server-side apps, dependencies, testing, publishing, plugins, built-in technologies, CLI wrapper/provisioning, or migrations from Maven.
---

# Kotlin Toolchain Context

Use this context whenever a task involves the Kotlin Toolchain. Prefer the raw
Markdown documentation links below when exact syntax or current details are
needed; they are more token-efficient than the rendered HTML site. The
canonical HTML for any page is produced by dropping `docs/src/`, dropping
`.md`, and prefixing with `https://kotlin-toolchain.org/dev/`.

The Kotlin Toolchain is a unified entry point into Kotlin, formerly Amper, by
the JetBrains Amper team. It builds JVM, Android, iOS, Kotlin Multiplatform,
Kotlin/JS, Kotlin/Wasm, Kotlin/Native, and server-side applications with
declarative YAML configuration in `project.yaml` and `module.yaml`. Status:
Alpha.

This index is an unofficial `llms.txt`; it is not published by JetBrains.
Links point to raw Markdown sources of the official docs on the `main` branch.
To pin a version, replace `main` with a tag or commit SHA.

## Getting Started

- [Overview](https://raw.githubusercontent.com/JetBrains/kotlin-toolchain/main/docs/src/getting-started/index.md): entry point and installation with SDKMAN or install script.
- [IDE setup](https://raw.githubusercontent.com/JetBrains/kotlin-toolchain/main/docs/src/getting-started/ide-setup.md): IntelliJ IDEA plugin setup.
- [Tutorial](https://raw.githubusercontent.com/JetBrains/kotlin-toolchain/main/docs/src/getting-started/tutorial.md): first project walkthrough.
- [Migrating from Maven](https://raw.githubusercontent.com/JetBrains/kotlin-toolchain/main/docs/src/getting-started/migrating-from-maven.md): migration guide.

## User Guide

- [Overview](https://raw.githubusercontent.com/JetBrains/kotlin-toolchain/main/docs/src/user-guide/index.md): user guide landing page.
- [Basic concepts](https://raw.githubusercontent.com/JetBrains/kotlin-toolchain/main/docs/src/user-guide/basics.md): modules, projects, settings model.
- [Dependencies](https://raw.githubusercontent.com/JetBrains/kotlin-toolchain/main/docs/src/user-guide/dependencies.md): declaring and scoping dependencies, catalogs.
- [Multiplatform modules](https://raw.githubusercontent.com/JetBrains/kotlin-toolchain/main/docs/src/user-guide/multiplatform.md): platform sets and shared code.
- [Testing](https://raw.githubusercontent.com/JetBrains/kotlin-toolchain/main/docs/src/user-guide/testing.md): test configuration and frameworks.
- [Templates](https://raw.githubusercontent.com/JetBrains/kotlin-toolchain/main/docs/src/user-guide/templates.md): reusable config templates.
- [Publishing](https://raw.githubusercontent.com/JetBrains/kotlin-toolchain/main/docs/src/user-guide/publishing.md): publishing artifacts to repositories.
- [Appendix: YAML primer](https://raw.githubusercontent.com/JetBrains/kotlin-toolchain/main/docs/src/user-guide/yaml-primer.md): YAML syntax refresher for config files.

## Product Types

- [Overview](https://raw.githubusercontent.com/JetBrains/kotlin-toolchain/main/docs/src/user-guide/product-types/index.md): list of supported product types.
- [Android application](https://raw.githubusercontent.com/JetBrains/kotlin-toolchain/main/docs/src/user-guide/product-types/android-app.md): `product: android/app`.
- [iOS application](https://raw.githubusercontent.com/JetBrains/kotlin-toolchain/main/docs/src/user-guide/product-types/ios-app.md): `product: ios/app`.
- [JVM application](https://raw.githubusercontent.com/JetBrains/kotlin-toolchain/main/docs/src/user-guide/product-types/jvm-app.md): `product: jvm/app`.
- [JVM library](https://raw.githubusercontent.com/JetBrains/kotlin-toolchain/main/docs/src/user-guide/product-types/jvm-lib.md): `product: jvm/lib`.
- [Kotlin Multiplatform library](https://raw.githubusercontent.com/JetBrains/kotlin-toolchain/main/docs/src/user-guide/product-types/kmp-lib.md): `product: kmp/lib`.
- [Kotlin/JS application](https://raw.githubusercontent.com/JetBrains/kotlin-toolchain/main/docs/src/user-guide/product-types/js-app.md): `product: js/app`.
- [Kotlin/Native application](https://raw.githubusercontent.com/JetBrains/kotlin-toolchain/main/docs/src/user-guide/product-types/native-app.md): `product: native/app`.
- [Kotlin/Wasm application](https://raw.githubusercontent.com/JetBrains/kotlin-toolchain/main/docs/src/user-guide/product-types/wasm-app.md): `product: wasm/app`.

## Built-In Technologies

- [Compose Multiplatform](https://raw.githubusercontent.com/JetBrains/kotlin-toolchain/main/docs/src/user-guide/builtin-tech/compose-multiplatform.md): `settings.compose: enabled`.
- [Kotlinx Serialization](https://raw.githubusercontent.com/JetBrains/kotlin-toolchain/main/docs/src/user-guide/builtin-tech/kotlinx-serialization.md): `settings.kotlin.serialization`.
- [Kotlinx RPC](https://raw.githubusercontent.com/JetBrains/kotlin-toolchain/main/docs/src/user-guide/builtin-tech/kotlinx-rpc.md): kotlinx-rpc integration.
- [Ktor](https://raw.githubusercontent.com/JetBrains/kotlin-toolchain/main/docs/src/user-guide/builtin-tech/ktor.md): Ktor server or client integration.
- [Lombok](https://raw.githubusercontent.com/JetBrains/kotlin-toolchain/main/docs/src/user-guide/builtin-tech/lombok.md): Lombok support for Java sources.
- [Spring Boot](https://raw.githubusercontent.com/JetBrains/kotlin-toolchain/main/docs/src/user-guide/builtin-tech/spring.md): Spring Boot integration.

## Plugins

- [Overview](https://raw.githubusercontent.com/JetBrains/kotlin-toolchain/main/docs/src/user-guide/plugins/overview.md): plugin system overview.
- [Quick start](https://raw.githubusercontent.com/JetBrains/kotlin-toolchain/main/docs/src/user-guide/plugins/quick-start.md): authoring a plugin quickly.
- [Checks](https://raw.githubusercontent.com/JetBrains/kotlin-toolchain/main/docs/src/user-guide/plugins/topics/checks.md): defining checks.
- [Custom commands](https://raw.githubusercontent.com/JetBrains/kotlin-toolchain/main/docs/src/user-guide/plugins/topics/custom-commands.md): adding custom CLI commands.
- [Plugin structure](https://raw.githubusercontent.com/JetBrains/kotlin-toolchain/main/docs/src/user-guide/plugins/topics/structure.md): layout of a plugin.
- [Configuration](https://raw.githubusercontent.com/JetBrains/kotlin-toolchain/main/docs/src/user-guide/plugins/topics/configuration.md): plugin configuration model.
- [References](https://raw.githubusercontent.com/JetBrains/kotlin-toolchain/main/docs/src/user-guide/plugins/topics/references.md): references between entities.
- [Tasks](https://raw.githubusercontent.com/JetBrains/kotlin-toolchain/main/docs/src/user-guide/plugins/topics/tasks.md): defining and wiring tasks.

## Advanced Topics

- [JDK provisioning](https://raw.githubusercontent.com/JetBrains/kotlin-toolchain/main/docs/src/user-guide/advanced/jdk-provisioning.md): toolchain and JDK resolution.
- [Java annotation processing](https://raw.githubusercontent.com/JetBrains/kotlin-toolchain/main/docs/src/user-guide/advanced/java-annotation-processing.md): apt support.
- [Kotlin Symbol Processing (KSP)](https://raw.githubusercontent.com/JetBrains/kotlin-toolchain/main/docs/src/user-guide/advanced/ksp.md): KSP integration.
- [Kotlin compiler plugins](https://raw.githubusercontent.com/JetBrains/kotlin-toolchain/main/docs/src/user-guide/advanced/kotlin-compiler-plugins.md): enabling compiler plugins.
- [Native interoperability (cinterop)](https://raw.githubusercontent.com/JetBrains/kotlin-toolchain/main/docs/src/user-guide/advanced/native-interop.md): C interop for Kotlin/Native.
- [Maven-like layout](https://raw.githubusercontent.com/JetBrains/kotlin-toolchain/main/docs/src/user-guide/advanced/maven-like-layout.md): alternative source layout.
- [Maven plugins](https://raw.githubusercontent.com/JetBrains/kotlin-toolchain/main/docs/src/user-guide/advanced/maven-plugins.md): using Maven plugins.

## CLI

- [Overview](https://raw.githubusercontent.com/JetBrains/kotlin-toolchain/main/docs/src/cli/index.md): command-line interface reference.
- [Wrapper and provisioning](https://raw.githubusercontent.com/JetBrains/kotlin-toolchain/main/docs/src/cli/provisioning.md): wrapper scripts and toolchain provisioning.

## Reference

- [project.yaml](https://raw.githubusercontent.com/JetBrains/kotlin-toolchain/main/docs/src/reference/project.md): full `project.yaml` schema reference.
- [module.yaml](https://raw.githubusercontent.com/JetBrains/kotlin-toolchain/main/docs/src/reference/module.md): full `module.yaml` schema reference.

## Optional

- [Home](https://raw.githubusercontent.com/JetBrains/kotlin-toolchain/main/docs/src/index.md): landing page with install snippets and minimal config examples.
- [FAQ](https://raw.githubusercontent.com/JetBrains/kotlin-toolchain/main/docs/src/faq.md): frequently asked questions.
- [Releases](https://github.com/JetBrains/kotlin-toolchain/releases): GitHub release notes in HTML.
- [Examples](https://github.com/JetBrains/kotlin-toolchain/tree/main/examples): example projects in the repository.
