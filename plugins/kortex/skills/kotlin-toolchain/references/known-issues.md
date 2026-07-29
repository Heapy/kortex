# Known Issues

Get issue status:  https://youtrack.jetbrains.com/issue/KTC-XXXX

## 0.11.1

- KTC-4871
- KTC-5603
- KTC-5573

## Issue list

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

### KTC-5573 Cinterop klibs are missing from test compilation and linking

A klib generated from a module's auto-discovered `cinterop/*.def` files is wired into the main compilation and link,
but not into the module's test compilation or test link. A direct cinterop reference in test code fails to compile with
unresolved references. Reaching the same declaration through main code may compile and link, then fail at runtime with
`IrLinkageError` because partial linkage inserted a throwing stub. Repeating the dependency in `test-dependencies` or
marking it `exported: true` does not help.

There is no portable manifest-only workaround. An absolute `-library` path works but is machine-specific, while
`${module.rootDir}` references are not supported in `module.yaml`. Put cinterop-dependent assertions in a separate
`macos/app` module's `main()`, run that binary from the normal test suite, and assert its exit code. Keep the remaining
logic behind a pure-Kotlin interface so it can be unit-tested with a fake.
