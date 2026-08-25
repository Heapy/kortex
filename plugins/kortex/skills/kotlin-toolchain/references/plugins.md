# Build Plugins

Kotlin Toolchain `v0.12.0`. Plugins are local to a project — they cannot be published or consumed as a published
dependency (KTC-4871).

## Structure

A plugin is a normal Kotlin module with `product: jvm/amper-plugin`:

```
plugins/build-config/
├─ module.yaml       # product + pluginInfo
├─ plugin.yaml       # task registration, generated outputs, checks, commands
╰─ src/              # Kotlin implementation
```

```yaml title="plugins/build-config/module.yaml"
product: jvm/amper-plugin

pluginInfo:
  id: build-config          # optional, defaults to the module name
  settingsClass: com.example.BuildConfigSettings   # optional
```

`settingsClass` is the fully-qualified name of a `@Configurable` interface. It must be declared in the plugin's own
`src`, not pulled from a dependency.

The plugin ID defaults to the module name and is used everywhere the plugin is referenced. Leave it at the default
unless you have a reason: the format is not well defined yet, and sharing plugins across projects is unsupported
anyway.

## Registering And Enabling

Register in `project.yaml`. The plugin module must appear in **both** lists, or the build reports an error:

```yaml title="project.yaml"
modules:
  - app
  - plugins/build-config

plugins:
  - //plugins/build-config
```

Registration alone does nothing. Enable per module — there is no project-wide plugin concept:

```yaml title="app/module.yaml"
plugins:
  build-config: enabled
```

```yaml title="app/module.yaml"
plugins:
  build-config:
    enabled: true
    someSetting: value
```

If many modules need the same plugin with the same settings, enable it from a shared template.

A plugin developed inside the project but not used by it belongs in `modules:` only, not `plugins:`.

## Task Actions

Top-level Kotlin functions annotated `@TaskAction`. Restrictions:

- top-level and public;
- returns `Unit`;
- not an extension, generic, `suspend`, or `inline` function, and no context parameters;
- every parameter type must be a configurable type.

```kotlin
@TaskAction
fun generateSources(
    @Input propertiesFile: Path,
    @Output generatedSourceDir: Path,
) { /* ... */ }
```

The marker goes before the parameter name, not on the type.

### Path Roles

Any parameter whose type contains a `Path` — directly, or inside `List<Path>`, `Map<String, Path>`, or a
`@Configurable` interface — must be marked `@Input` or `@Output`. Built-in file-requesting types (`ModuleSources`,
`Classpath`, `CompilationArtifact`) are **always** `@Input`.

Parameters that reference no `Path` are inputs implicitly and need no annotation.

### Execution Avoidance

Controlled per action by `@TaskAction(executionAvoidance = ...)`:

- `ExecutionAvoidance.Automatic` (default) — re-run when the action classpath changes, when non-path argument values
  change, or when declared `@Input`/`@Output` file trees change. An action that declares no outputs always re-runs.
- `ExecutionAvoidance.Disabled` — always re-run.

Only file attributes and modification times are inspected; file contents are not hashed.

Design around this: split a "build the distribution" task from a "publish it" task, so the first can be incremental
while the second, which has undeclarable side effects, is not.

### Runtime

Tasks run in an isolated JVM environment with a plugin-only classloader. Static global state is not guaranteed to
survive between invocations. There is no runtime API beyond the configuration system — use whatever libraries you
need. For logging, write to `System.out`/`System.err`; the toolchain attributes the output to the task in its log.

## `plugin.yaml`

```yaml
tasks:
  generate:
    action: !com.example.generateSources
      propertiesFile: ${module.rootDir}/config.properties
      generatedSourceDir: ${taskOutputDir}

generated:
  sources:
    - language: kotlin
      directory: ${tasks.generate.action.generatedSourceDir}

checks:
  - lint

commands:
  - updateBaseline
```

The `action` property needs an explicit YAML type tag: `!` followed by the fully-qualified name of the `@TaskAction`
function. The action object's properties map to that function's parameters.

The same action can be registered under several task names with different arguments. Tasks are registered once in
`plugin.yaml` and instantiated per module where the plugin is enabled.

Task names are local to the plugin — two plugins may use the same name. Internally a task is addressed as
`:<module-name>:<task-name>@<plugin-id>`, which is what `./kotlin task` takes.

## References

References are for `plugin.yaml`, not `module.yaml`.

| Reference | Type | Meaning |
|---|---|---|
| `pluginSettings` | `pluginInfo.settingsClass` | The plugin's settings from the module's `plugins.<id>` block |
| `module.name` | `string` | Module display name |
| `module.rootDir` | `path` | Absolute path to the module root |
| `module.runtimeClasspath` | `Classpath` | Resolved runtime classpath (JVM, main) |
| `module.compileClasspath` | `Classpath` | Compile classpath plus the module's compilation result |
| `module.kotlinJavaSources` | `ModuleSources` | Kotlin and Java sources (JVM, main) |
| `module.resources` | `ModuleSources` | Resources (JVM, main) |
| `module.jar` | `CompilationArtifact` | Compiled JAR (JVM, main) |
| `module.classes` | `CompilationArtifact` | Directory of compiled classes. New in `0.12`. |
| `module.self` | `Dependency.Local` | A dependency pointing at the module itself |
| `module.settings.**` | matches the setting | e.g. `module.settings.publishing.version` |
| `project.rootDir` | `path` | Absolute project root |
| `taskOutputDir` | `path` | The task's own output directory |

`0.12` documents `module.kotlinJavaSources` consistently. The `0.11.x` docs were inconsistent — the reference table
already listed `module.kotlinJavaSources` while the overview and task examples still used `module.sources`. Update any
`plugin.yaml` still on `module.sources`.

Shorthand notation does not currently work with references.

## Contributing Back To The Build

Two steps: mark the path `@Output` in the action, then declare its kind in a top-level `generated:` block.

| Section | Content |
|---|---|
| `generated.sources` | Directory of sources compiled with the module; `language: kotlin` (default) or `java` |
| `generated.resources` | Directory of resources bundled with the module |
| `generated.cinteropDefinitions` | A `.def` file processed by `cinterop` |

Entries accept an optional `fragment` to scope them to a platform fragment.

The `0.11.x` docs were inconsistent here too: the overview described a `markOutputAs` mechanism while the tasks page
already documented all three `generated.*` sections. `0.12` documents only the `generated:` block. The declaration
lives at the registration site rather than in Kotlin because a generic action — `unzip`, say — does not know whether
what it produced is sources.

## Task Dependencies

Inferred automatically from matching `@Input`/`@Output` paths: if task A declares an `@Output` and task B declares an
`@Input` that matches it, B depends on A. Paths match when equal, or when one is an ancestor or descendant of the
other — `/foo/bar` matches `/foo/bar/out.txt`, but not `/foo/baz`.

There is no manual task-dependency syntax.

An `@Input` pointing inside the build directory with no task producing a matching `@Output` raises a warning, which
usually means a misconfigured path.

Use `@Input(inferTaskDependency = false)` to suppress inference for one parameter. The classic case is a baseline
file: an "update" task writes it and a "check" task reads it, and inferring a dependency would make "update" always
run first and hide the problem. Suppressing inference affects wiring only — the file still counts for execution
avoidance.

## Checks And Commands

Both are ordinary tasks, listed in the corresponding `plugin.yaml` section.

```yaml
tasks:
  lint:
    action: !com.example.runDetekt
      sources: ${module.kotlinJavaSources}
  updateBaseline:
    action: !com.example.runDetektForBaseline
      sources: ${module.kotlinJavaSources}
      outputFile: ${module.rootDir}/detekt/baseline.xml

checks:
  - lint

commands:
  - updateBaseline
```

Checks run via `./kotlin check`, which with no arguments runs the tests plus every registered check. Narrow it with
names (`./kotlin check detekt apiCheck`), `--skip tests`, or `-m/--module` (repeatable). List them with
`./kotlin show checks`.

Commands run via `./kotlin do <name>`, also accepting `-m/--module`. List them with `./kotlin show commands`.

## Plugin Settings

Public `@Configurable` interfaces with read-only properties. `enabled` is reserved.

```kotlin
@Configurable
interface BuildConfigSettings {
    val packageName: String
    val fields: Map<String, String>
}
```

Defaults are supported for constants, enum constant references, empty and constant-element lists, empty maps, and
nulls. Explicit `Path` defaults are not supported. Task parameters use ordinary Kotlin default syntax and require
explicit types:

```kotlin
@TaskAction fun myAction(
    myBoolean: Boolean = false,
    myString: String = "default",
) { /* ... */ }
```

## Debugging

Run one task directly:

```shell
./kotlin task :app:generate@build-config
```
