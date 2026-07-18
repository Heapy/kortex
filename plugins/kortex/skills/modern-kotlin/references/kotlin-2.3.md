# Kotlin 2.3.x (2025–2026)

| Version | Date | Type |
|---|---|---|
| 2.3.0 | December 16, 2025 | Language release |
| 2.3.10 | February 5, 2026 | Bug fixes only |
| 2.3.20 | March 16, 2026 | Tooling release + language previews |
| 2.3.21 | April 23, 2026 | Bug fixes only |

Docs: [What's new in 2.3.0](https://kotlinlang.org/docs/whatsnew23.html) ·
[What's new in 2.3.20](https://kotlinlang.org/docs/whatsnew2320.html) ·
[Compatibility guide 2.3](https://kotlinlang.org/docs/compatibility-guide-23.html)

## Kotlin 2.3.0 — Language

### Unused return value checker (Experimental) — `-Xreturn-value-checker=check|full`

The compiler warns when a non-`Unit` result of an expression is silently discarded. `check` mode reports only inside
scopes annotated `@MustUseReturnValues`; `full` marks the whole project and reports all ignored return values.
`@IgnorableReturnValue` exempts a function; suppress a single call site by assigning to the unnamed variable:
`val _ = computeValue()`.

```kotlin
fun formatGreeting(name: String): String {
    if (name.isBlank()) return "Hello, anonymous user!"
    if (!name.contains(' ')) {
        // Checker warns: this result is ignored
        "Hello, " + name.replaceFirstChar(Char::titlecase) + "!"
    }
    val (first, last) = name.split(' ')
    return "Hello, $first! Or should I call you Dr. $last?"
}
```

Feedback: KT-12719. [Docs](https://kotlinlang.org/docs/whatsnew23.html#unused-return-value-checker)

### Explicit backing fields (Experimental) — `-Xexplicit-backing-fields`

A property's backing field may have a different (typically mutable) type than the public type, replacing the
`private val _x` / `val x get() = _x` pattern. Smart-cast to the field type works inside the private scope.
Promoted to Stable in 2.4.0.

```kotlin
val city: StateFlow<String>
    field = MutableStateFlow("")

fun updateCity(newCity: String) {
    city.value = newCity // resolves to the MutableStateFlow backing field
}
```

Feedback: KT-14663. [Docs](https://kotlinlang.org/docs/whatsnew23.html#explicit-backing-fields)

### Context-sensitive resolution improvements (Experimental) — `-Xcontext-sensitive-resolution`

Sealed and enclosing supertypes of the current type are now part of the contextual scope (KT-77823); the compiler
warns when context-sensitive resolution makes resolution ambiguous with type operators/equalities (KT-77821).
[Docs](https://kotlinlang.org/docs/whatsnew23.html#changes-to-context-sensitive-resolution)

### Promoted in 2.3.0

- **Nested type aliases → Stable** (Beta since 2.2.0).
- **Data-flow-based exhaustiveness checks for `when` → Stable** (Experimental since 2.2.20).
- **`return` in expression bodies with explicit return types → enabled by default** (previously behind
  `-Xallow-return-in-expression-body`).
- **C and Objective-C library import (cinterop) → Beta** on Kotlin/Native.

## Kotlin 2.3.0 — Stdlib

- **`kotlin.time.Clock` and `kotlin.time.Instant` → Stable** (introduced Experimental in 2.1.20).
- **Uuid improvements** (Experimental, `@OptIn(ExperimentalUuidApi::class)`), KT-81395:
  - Null-returning parsers: `Uuid.parseOrNull()`, `Uuid.parseHexDashOrNull()`, `Uuid.parseHexOrNull()`.
  - Generators: `Uuid.generateV4()`, `Uuid.generateV7()` (`Uuid.random()` unchanged, still v4).
  - `Uuid.generateV7NonMonotonicAt(timestamp: Instant)`.

## Kotlin 2.3.0 — Compiler flags and defaults

- **JVM**: Java 25 bytecode target support.
- **Dropped**: `-language-version=1.8` entirely; `-language-version=1.9` dropped on non-JVM platforms.
- **Ant build system support removed.**
- **Kotlin/Native**: Swift export maps enums to native Swift enums and `vararg` to Swift variadics; explicit parameter
  names in Obj-C block types by default; release link tasks up to 40% faster; min Apple targets raised
  (iOS/tvOS 14.0, watchOS 7.0; override via `-Xoverride-konan-properties=minVersion.ios=12.0`); Intel Apple targets
  demoted to tier 3.
- **Kotlin/Wasm**: `KClass.qualifiedName` enabled by default; new exception-handling proposal default for `wasmWasi`
  (opt-in for `wasmJs`: `-Xwasm-use-new-exception-proposal`).
- **Kotlin/JS**: Experimental `@JsExport` of `suspend` functions as JS async (`-Xenable-suspend-function-exporting`,
  KT-56281); Experimental `LongArray` as `BigInt64Array` (`-Xes-long-as-bigint`, KT-79284); enabled by default:
  unified companion access, `@JsStatic` in interface companions, `@JsQualifier` on individual declarations,
  `@JsExport.Default` (emits `export default`).
- **Gradle**: compatible 7.6.3–9.0.0; `kotlin-android` plugin errors with AGP 9.0.0+ (AGP 9 has built-in Kotlin
  support); KMP `androidTarget` must migrate to `com.android.kotlin.multiplatform.library`.
- **Compose compiler**: ProGuard mappings for Compose stack traces in R8-minified apps;
  `Composer.setDiagnosticStackTraceMode(ComposeStackTraceMode.GroupKeys)`.

## Kotlin 2.3.20 (March 16, 2026)

### Name-based destructuring (Experimental) — `-Xname-based-destructuring=<mode>`

Destructuring by property name instead of position. Square-bracket syntax `val [a, b] = x` becomes explicit
positional destructuring. Modes: `only-syntax` (enables the new syntax without changing old behavior),
`name-mismatch` (warns when positional destructuring names don't match property names), `complete` (short
parenthesized form becomes name-based). Stable release (syntax-only) planned for 2.5.

```kotlin
data class User(val username: String, val email: String)

val user = User("alice", "alice@example.com")
(val mail = email, val name = username) = user   // name-based, explicit form
val [username, email] = user                     // position-based, new bracket syntax
```

Feedback: KT-19627. [Docs](https://kotlinlang.org/docs/whatsnew2320.html)

### Breaking changes in 2.3.20

- **Overload resolution with context parameters**: context-parameter overloads are no longer considered more specific
  than non-context overloads; previously compiling calls can become ambiguity errors.
- **Context receivers removed**: the old experimental context receivers (`-Xcontext-receivers`) are no longer
  supported; migrate to [context parameters](https://kotlinlang.org/docs/context-parameters.html).

### 2.3.20 — Promotions

- **Lombok compiler plugin**: Experimental → Alpha.
- **JPA compiler plugin (`kotlin.plugin.jpa`) → Stable**; now applies `all-open` with the JPA preset in addition to
  `no-arg`.
- **Maven simplified setup → Stable**: `<extensions>true</extensions>` auto-registers `src/main/kotlin` and adds
  `kotlin-stdlib`.

### 2.3.20 — Stdlib

- **`Map.Entry.copy()`** — Experimental, `@OptIn(ExperimentalStdlibApi::class)`: immutable snapshot of an entry that
  stays valid after map mutation.

  ```kotlin
  val toRemove = map.entries.filter { it.key % 2 == 0 }.map { it.copy() }
  map.entries.removeAll(toRemove)
  ```

### 2.3.20 — Compiler flags and defaults

- **JVM**: `-Xnullability-annotations=@io.vertx.codegen.annotations:strict` — recognize Vert.x `@Nullable`;
  JetBrains `@Unmodifiable`/`@UnmodifiableView` on Java methods make returned collections read-only in Kotlin
  (warning now, error planned in 2.5.0).
- **Kotlin/Native**: Experimental `-Xccall-mode=direct` cinterop mode (KT-83218); `macosX64`, `tvosX64`, `watchosX64`
  deprecated.
- **Kotlin/Wasm**: module initialization at instantiation (no external `_initialize()`); major string performance
  work; Experimental `@nativeInvoke` (`@OptIn(ExperimentalWasmJsInterop::class)`).
- **Kotlin/JS**: `-Xenable-implementing-interfaces-from-typescript` — implement `@JsExport`ed Kotlin interfaces from
  TypeScript; Experimental SWC transpilation backend (`kotlin.js.delegated.transpilation=true`).
- **Gradle**: compatible 7.6.3–9.3.0; Kotlin/JVM compilation runs via Build Tools API by default; ABI-validation
  tasks renamed `checkLegacyAbi` → `checkKotlinAbi`, `updateLegacyAbi` → `updateKotlinAbi` (`checkKotlinAbi` wired
  into `check`).
