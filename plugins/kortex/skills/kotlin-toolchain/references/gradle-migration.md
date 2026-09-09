# Gradle Migration

Kotlin Toolchain `v0.12.1`.

There is no `convert-project` equivalent for Gradle: build scripts are arbitrary code, so a faithful automatic
translation is not definable. Migration is by hand, module by module. For Maven projects use the converter described in
[`maven-migration.md`](maven-migration.md) instead.

## Concept Mapping

| Gradle | Kotlin Toolchain |
|---|---|
| Build | Project |
| Root project | Root module |
| Subproject | Module |
| Convention plugin (plain configuration) | Template (`<name>.module-template.yaml`) |
| Plugin (custom tasks, codegen) | Local `jvm/amper-plugin` module |
| `settings.gradle(.kts)` | `project.yaml` |
| `build.gradle(.kts)` | `module.yaml` |
| `gradle/libs.versions.toml` | `libs.versions.toml` at the project root — same format |
| `gradlew` / `gradlew.bat` / `gradle-wrapper.properties` | `kotlin` / `kotlin.bat` wrapper scripts |
| `gradle.properties` | No equivalent — everything lives in `module.yaml` |

Gradle's `gradle/libs.versions.toml` location still works, but only to ease migration; move the file to the project
root. Only `[versions]` and `[libraries]` are read — `[plugins]` does not apply and `[bundles]` is not supported.

## Order Of Work

1. Write `project.yaml`. Each `include(":libs:lib1")` becomes a directory path entry, `libs/lib1`. A single-module
   project can skip the file, but still wants it for local plugin declarations.
2. Turn every convention plugin into a `<name>.module-template.yaml`. Unconditional `subprojects { ... }`
   configuration becomes one `common.module-template.yaml`; conditional blocks each become their own template. Keeping
   all templates in one `templates/` directory keeps the `//` references short.
3. Convert remaining custom plugin logic — check for a built-in equivalent first, then decide between dropping it and
   writing a local plugin.
4. Write a `module.yaml` per subproject: product type, `apply:` for each template that replaces a convention plugin,
   then the settings and dependencies below.
5. Move sources to the toolchain layout, or set `layout: maven-like` on JVM modules to leave them where they are.
6. Verify with `./kotlin build` and `./kotlin test`.
7. Delete the Gradle wrapper and scripts.

At step 1 the IDE reports errors on every module listed in `project.yaml`; they clear as each `module.yaml` appears.

## Product Type

No Gradle equivalent — it is inferred there from the applied plugins.

| Gradle plugins | Product type |
|---|---|
| `kotlin("jvm")` | `jvm/lib` |
| `kotlin("jvm")` + `application` | `jvm/app` |
| `kotlin("multiplatform")` | `kmp/lib` |

One module builds exactly one product. A Gradle module that produced several application types has to become a
`kmp/lib` holding the shared code, plus one application module per platform depending on it.

## Source Layout

JVM modules:

| Gradle | Kotlin Toolchain |
|---|---|
| `src/main/kotlin/`, `src/main/java/` | `src/` |
| `src/main/resources/` | `resources/` |
| `src/test/kotlin/` | `test/` |
| `src/test/resources/` | `testResources/` |

Gradle's JVM layout matches Maven's, so `jvm/app` and `jvm/lib` modules can set `layout: maven-like` and move nothing.
Multiplatform modules and any module with custom `srcDir` calls must move their files.

Multiplatform modules map each Kotlin source set to a `@platform`-qualified directory:

| Gradle | Kotlin Toolchain |
|---|---|
| `src/commonMain/kotlin/` | `src/` |
| `src/jvmMain/kotlin/` | `src@jvm/` |
| `src/iosArm64Main/kotlin/` | `src@iosArm64/` |
| `src/commonTest/kotlin/` | `test/` |
| `src/jvmTest/kotlin/` | `test@jvm/` |
| `src/commonMain/resources/` | `resources/` |
| `src/androidMain/resources/` | `resources@android/` |

The qualifier hierarchy is KGP's default hierarchy template, with the same visibility rules `dependsOn` gave: `src@ios`
sees `src`, `src@native`, and `src@apple`. Custom intermediate source sets — hand-written `dependsOn` edges — become
aliases:

```yaml title="module.yaml"
aliases:
  - jvmAndAndroid: [ jvm, android ]
```

Per-source-set dependencies follow the same qualifier: `commonMain` → `dependencies:`, `jvmMain` →
`dependencies@jvm:`, `commonTest` → `test-dependencies:`.

## Dependencies

| Gradle | Kotlin Toolchain | Section |
|---|---|---|
| `implementation("g:a:1.0")` | `- g:a:1.0` | `dependencies` |
| `api("g:a:1.0")` | `- g:a:1.0: exported` | `dependencies` |
| `compileOnly("g:a:1.0")` | `- g:a:1.0: compile-only` | `dependencies` |
| `runtimeOnly("g:a:1.0")` | `- g:a:1.0: runtime-only` | `dependencies` |
| `implementation(project(":libs:lib1"))` | `- //libs/lib1` | `dependencies` |
| `implementation(platform("g:bom:1.0"))` | `- bom: g:bom:1.0` | `dependencies` |
| `implementation(libs.ktor.client.core)` | `- $libs.ktor.client.core` | `dependencies` |
| `testImplementation("g:a:1.0")` | `- g:a:1.0` | `test-dependencies` |

Defaults match Gradle's: not exported. Mark `exported` only where the Gradle script said `api`.

`repositories { ... }` becomes the `repositories:` list, and most projects can drop it — Maven Central and Google's
Maven repository are configured by default.

## Settings

Kotlin support is built in, so nothing corresponds to applying KGP. The version still wants pinning, because the
default moves with every toolchain release.

| Gradle | Kotlin Toolchain |
|---|---|
| `kotlin("jvm") version "2.4.10"` | `settings.kotlin.version: 2.4.10` |
| `kotlin { jvm(); iosArm64() }` | `product.platforms: [ jvm, iosArm64 ]` |
| `application { mainClass = "com.example.Foo" }` | `settings.jvm.mainClass: com.example.Foo` |
| `java { toolchain { languageVersion = ... } }` | `settings.jvm.jdk.version` |
| `JvmVendorSpec.AZUL` | `settings.jvm.jdk.distributions: [ zulu ]` |
| `options.release` + `jvmTarget` | `settings.jvm.release` — one setting for both compilers |
| `-parameters` / `javaParameters` | `settings.jvm.storeParameterNames: true` |
| `compilerOptions { ... }` | `settings.kotlin.*`, rest via `settings.kotlin.freeCompilerArgs` |
| `options.compilerArgs` | `settings.java.freeCompilerArgs` |
| Kotlin compile task of one target | `settings@jvm:` and friends |
| Test-only compiler options | `test-settings:` |

A `jvm/app` whose `main` function sits in `main.kt` needs no `mainClass` at all — it is detected.

`settings.jvm.release` defaults to the JDK version, so a module on the default JDK 25 targets Java 25. Set it
explicitly whenever the artifact must run on anything older.

JDK provisioning needs no equivalent of `foojay-resolver-convention`: it is built in, and no JDK has to be installed.
`JAVA_HOME` is used when it satisfies the requirements and a JDK is downloaded otherwise.
`settings.jvm.jdk.selectionMode: javaHome` forbids downloads, matching
`org.gradle.java.installations.auto-download=false`; `alwaysProvision` ignores `JAVA_HOME` for reproducibility.

Frameworks that needed a Gradle plugin — Compose Multiplatform, kotlinx.serialization, kotlinx.rpc, KSP, Ktor, Spring,
Lombok — are built in; see [`builtin-tech.md`](builtin-tech.md). Kotlin compiler plugins are configured under
`settings.kotlin` with no plugin declaration.

## Tests

The JUnit Platform is on by default on JVM and Android (`settings.junit` defaults to `junit-5`) and `kotlin-test` is
preconfigured per platform, so `useJUnitPlatform()` and the test framework dependencies both disappear. A typical
module needs no test configuration: put tests in `test/` and run `./kotlin test`.

Test-task configuration maps to `settings.jvm.test`:

| Gradle `tasks.test` | Kotlin Toolchain |
|---|---|
| `jvmArgs(...)` | `settings.jvm.test.freeJvmArgs` |
| `systemProperty(k, v)` | `settings.jvm.test.systemProperties` |
| `environment(k, v)` | `settings.jvm.test.extraEnvironment` |

`settings.junit: junit-4` keeps a JUnit 4 project working; `settings.junit: none` disables the automatic setup.

## Everyday Commands

| Gradle | Kotlin Toolchain |
|---|---|
| `./gradlew build` | `./kotlin build` |
| `./gradlew test` | `./kotlin test` |
| `./gradlew :app:test` | `./kotlin test -m app` |
| `./gradlew run` | `./kotlin run` |
| `./gradlew :app:dependencies` | `./kotlin show dependencies -m app` |
| `./gradlew tasks` | `./kotlin --help`, plus `./kotlin show commands` for plugin commands |
| `./gradlew publishToMavenLocal` | `./kotlin publish mavenLocal` |
| `./gradlew clean` | `./kotlin clean` |
| `./gradlew --stop` | Nothing — there is no daemon |

The `v0.12.1` Gradle migration page maps `./gradlew tasks` to `./kotlin show tasks`. That command does run, but it
lists the toolchain's internal build tasks and their dependency edges — not something to drive a build with. The
user-facing lists are `./kotlin --help` for built-in commands and `./kotlin show commands` for the ones plugins
contribute. Upstream reworded the row this way after the tag.

Like `gradlew`, the `kotlin` and `kotlin.bat` wrappers are committed to the repository and provision everything on
first use.

## No Equivalent

- **`gradle.properties`** — no project-wide property file; configuration is per module.
- **Profiles by property or environment** — build configuration varies by platform only.
- **`[bundles]` in the version catalog** — list the libraries individually.
- **Published plugins** — plugins are local to a project (KTC-4871); a Gradle plugin consumed from a repository has no
  counterpart.
