# Project Model

Kotlin Toolchain `v0.12.0`.

## Project And Modules

A project is rooted at the directory holding `project.yaml`. A module is any directory holding `module.yaml`. Each
module builds exactly one product.

A single-module project needs no `project.yaml`. In a multi-module project, a `module.yaml` sitting in the project root
is included implicitly and does not need to be listed.

Sources and resources belong to exactly one module. Two modules cannot share a source folder — this is what lets the
IDE always know which settings and dependencies apply to a file. Modules share code by depending on each other.

Only `module.yaml` is required. A module with no `src` at all is valid, for example one that only aggregates
dependencies.

## `project.yaml`

```yaml
modules:
  - app
  - libs/lib1
  - libs/lib2
  - plugins/*

plugins:
  - //plugins/build-config
```

`modules:` entries are path globs **relative to the project root, without a `//` prefix**. Prefixing them is neither
supported nor necessary. Only directories that actually contain `module.yaml` are picked up. Sorting the list
alphabetically is recommended: it reduces merge conflicts and makes a module easy to find.

Glob syntax:

- `*` — zero or more characters within one path component
- `?` — exactly one character
- `[abc]`, `[a-z]` — one character from the set or range; a leading `!` negates it, so `[!abc]` is one character not in
  the set
- `{a,b}` — one of the comma-separated alternatives
- `**` for recursive matching is **not** supported

`plugins:` lists plugin modules that become available to the project. Listing one here does not enable it — modules
opt in individually. These entries use `//` notation. A plugin module referenced here must also appear in `modules:`,
otherwise the build reports an error.

`mavenPlugins:` at project level registers Maven plugin coordinates; see `maven-migration.md`.

## Path Notation

All configuration files use `/` as the separator on every platform. Backslashes must not be used, even on Windows.

**Project-root paths (`//`)** resolve from the directory containing `project.yaml` (or the single `module.yaml`).
This is the preferred form everywhere a `Path` is expected: module dependencies, `apply:` template refs, plugin refs,
KSP processor paths, and plugin settings.

```yaml
dependencies:
  - //libs/lib1
apply:
  - //common.module-template.yaml
```

**Relative paths** (`./foo.txt`, `../bar.bin`, `resources/picture.jpg`) resolve against the directory of the YAML file
that declares them. They still work, but `//` is preferred: moving a YAML file does not invalidate `//` paths.

For module dependencies specifically, a relative path must be explicit — it has to start with a dot. `./my-nested`
and `../my-sibling` are module dependencies; a bare `my-lib` is parsed as an external Maven dependency. Upstream notes
that relative module dependencies may be deprecated and removed later.

Module dependencies must stay inside the project: the target has to be listed in `project.yaml` and cannot live
outside the project root.

Two exceptions to the `//` preference:

- `project.yaml`'s `modules:` list, which is already root-relative and rejects `//`.
- Projects pinned to `0.11.x`, where `//` does not exist yet.

## `module.yaml` Keys

| Key | Purpose |
|---|---|
| `product` | Product type and platforms |
| `dependencies`, `dependencies@platform` | Main dependencies |
| `test-dependencies`, `test-dependencies@platform` | Test-only dependencies |
| `settings`, `settings@platform` | Toolchain settings |
| `test-settings`, `test-settings@platform` | Test-only toolchain settings |
| `repositories` | Maven repositories for resolution and publishing |
| `apply` | Module templates to merge in |
| `aliases` | Custom platform groups |
| `layout` | `amper` (default) or `maven-like` |
| `description` | Human-readable module description; feeds the published POM |
| `plugins` | Enable and configure project plugins for this module |
| `mavenPlugins` | Enable and configure Maven plugin mojos (prototype) |
| `pluginInfo` | Only for `jvm/amper-plugin` modules: `id`, `settingsClass` |

## Module Layout

Default `amper` layout:

```
my-module/
├─ src/                 # main sources
├─ src@jvm/             # platform-qualified sources
├─ resources/           # main resources
├─ test/                # test sources
├─ testResources/       # test-only resources
├─ cinterop/            # .def files for native interop
╰─ module.yaml
```

`maven-like` layout preserves Maven and Gradle trees — `src/main/kotlin`, `src/main/java`, `src/main/resources`,
`src/test/kotlin`, and so on. It is only supported for `jvm/app` and `jvm/lib`, and exists for Maven migration.

Platform qualification works for `src`, `resources`, `test`, `testResources`, and `cinterop`.

## Introspection

`./kotlin show settings -m <module>` prints the effective configuration after templates and platform propagation are
resolved. Use it before editing anything non-trivial, and to confirm what a template actually contributed.

Also available: `show modules`, `show dependencies`, `show tasks`, `show checks`, `show commands`.
