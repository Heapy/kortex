# Kotlin 2.2.x (2025)

| Version | Date | Type |
|---|---|---|
| 2.2.0 | June 23, 2025 | Language release |
| 2.2.10 | August 14, 2025 | Bug fixes only |
| 2.2.20 | September 10, 2025 | Tooling release + language previews |
| 2.2.21 | October 23, 2025 | Bug fixes, Xcode 26 support |

Docs: [What's new in 2.2.0](https://kotlinlang.org/docs/whatsnew22.html) ·
[What's new in 2.2.20](https://kotlinlang.org/docs/whatsnew2220.html)

## Kotlin 2.2.0 — Language

### Context parameters (preview) — `-Xcontext-parameters`

Replaces the removed experimental context receivers. Functions and properties declare dependencies that are implicitly
available from the surrounding context; no manual threading of services through parameters. Context parameters are
named and referenced explicitly (unlike context receivers, which polluted the implicit scope).

```kotlin
interface UserService {
    fun log(message: String)
    fun findUserById(id: Int): String
}

context(users: UserService)
fun outputMessage(message: String) {
    users.log("Log: $message")
}

context(users: UserService)
val firstUser: String
    get() = users.findUserById(1)
```

[Docs](https://kotlinlang.org/docs/whatsnew22.html#preview-of-context-parameters)

### Context-sensitive resolution (preview) — `-Xcontext-sensitive-resolution`

Omit the type name when it is inferable from context (enum entries, sealed subclasses): in `when` subjects, return
positions, variable declarations, type checks, and parameter defaults.

```kotlin
enum class Problem { CONNECTION, AUTHENTICATION, DATABASE, UNKNOWN }

fun message(problem: Problem): String = when (problem) {
    CONNECTION -> "connection"
    AUTHENTICATION -> "authentication"
    DATABASE -> "database"
    UNKNOWN -> "unknown"
}
```

[Docs](https://kotlinlang.org/docs/whatsnew22.html#preview-of-context-sensitive-resolution)

### `@all` meta-target for properties (preview) — `-Xannotation-target-all`

Applies an annotation to all applicable targets of a property at once (`param`, `property`, `field`, `get`,
`setparam`, and `RECORD_COMPONENT` for `@JvmRecord`).

```kotlin
data class User(
    val username: String,
    @all:Email
    val email: String,
)
```

[Docs](https://kotlinlang.org/docs/whatsnew22.html#all-meta-target-for-properties)

### New defaulting rules for use-site annotation targets (preview) — `-Xannotation-default-target=param-property`

New default: an annotation propagates to `param` (if applicable) plus `property`; `field` when `property` is not
applicable. Previously only the first applicable target got the annotation. Revert to old behavior with
`-Xannotation-default-target=first-only`.

[Docs](https://kotlinlang.org/docs/whatsnew22.html#new-defaulting-rules-for-use-site-annotation-targets)

### Nested type aliases (Beta) — `-Xnested-type-aliases`

Type aliases inside classes, objects, and functions; they cannot capture outer-class type parameters.

```kotlin
class Dijkstra {
    typealias VisitedNodes = Set<Node>
    private fun step(visited: VisitedNodes) { /* ... */ }
}
```

[Docs](https://kotlinlang.org/docs/whatsnew22.html#support-for-nested-type-aliases)

### Promoted to Stable in 2.2.0 (no flag needed)

All three were previewed in 2.1.0:

- **Guard conditions in `when`** — `is X if cond ->` syntax.
- **Non-local `break` and `continue`** — from lambdas passed to inline functions.
- **Multi-dollar string interpolation** — `$$"..."`.

[Docs](https://kotlinlang.org/docs/whatsnew22.html#stable-features-guard-conditions-non-local-break-and-continue-and-multi-dollar-interpolation)

## Kotlin 2.2.0 — Stdlib

- **`Base64` API → Stable** (`kotlin.io.encoding.Base64`; experimental since 1.8.20). Four schemes: `Base64.Default`
  (RFC 4648 §4), `Base64.UrlSafe` (§5), `Base64.Mime` (RFC 2045, 76-char lines), `Base64.Pem` (64-char lines). JVM
  stream extensions `encodingWith`/`decodingWith`.

  ```kotlin
  Base64.encode("fo".encodeToByteArray()) // "Zm8="
  Base64.UrlSafe.encode(foobarBytes)      // "Zm9vYmFy"
  Base64.decode("Zm8=")
  ```

  [Docs](https://kotlinlang.org/docs/whatsnew22.html#stable-base64-encoding-and-decoding)
- **`HexFormat` API → Stable** (`kotlin.text.HexFormat`; experimental since 1.9.0). `93.toHexString() // "5d"`.
  [Docs](https://kotlinlang.org/docs/whatsnew22.html#stable-hexadecimal-parsing-and-formatting-with-the-hexformat-api)

## Kotlin 2.2.0 — Compiler flags and defaults

- **Interface functions compile to JVM default methods by default.** New stable option `-jvm-default` replaces the
  deprecated `-Xjvm-default`. Values: `enable` (new default: default impls + `DefaultImpls` bridges),
  `no-compatibility` (default impls only), `disable` (pre-2.2.0 behavior). Gradle DSL:
  `jvmDefault = JvmDefaultMode.NO_COMPATIBILITY`.
  [Docs](https://kotlinlang.org/docs/whatsnew22.html#changes-to-default-method-generation-for-interface-functions)
- **Unified warning management** (Experimental): `-Xwarning-level=DIAGNOSTIC_NAME:(error|warning|disabled)`.
  [Docs](https://kotlinlang.org/docs/whatsnew22.html#kotlin-compiler-unified-management-of-compiler-warnings)
- **Annotations in Kotlin metadata** (Experimental): `-Xannotations-in-metadata`; read via `kotlin-metadata-jvm` with
  `@OptIn(ExperimentalAnnotationsInMetadata::class)`.
  [Docs](https://kotlinlang.org/docs/whatsnew22.html#support-for-reading-and-writing-annotations-in-kotlin-metadata)
- **`@JvmExposeBoxed`** (Experimental): annotation `kotlin.jvm.JvmExposeBoxed` or module-wide `-Xjvm-expose-boxed` —
  exposes boxed value-class constructors and functions to Java.
  [Docs](https://kotlinlang.org/docs/whatsnew22.html#improved-java-interop-with-inline-value-classes)
- **Improved `@JvmRecord` support**: warning on `@Target` mismatch, `@all:` propagation to record components.
- **Java 24 bytecode support** on Kotlin/JVM.
- Deprecations/removals: `-language-version 1.6`/`1.7` dropped; Gradle `kotlinOptions {}` is now an error (use
  `compilerOptions {}`); REPL deprecated (opt back in with `-Xrepl`); `kotlin-android-extensions` removed; Ant support
  deprecated.
- Gradle: experimental binary compatibility validation in KGP (`abiValidation {}`, tasks
  `checkLegacyAbi`/`updateLegacyAbi`); experimental Build Tools API (`kotlin.compiler.runViaBuildToolsApi=true`).
- Kotlin/Native: LLVM 16 → 19; experimental `kotlin.native.binary.pagedAllocator=false`; experimental Latin-1 strings
  `kotlin.native.binary.latin1Strings=true`.
- Kotlin/JS: `@JsPlainObject` `copy` moved to companion; typealiases allowed in `@JsModule` files; `@JsExport` on
  `expect` declarations; `Promise<Unit>` exportable.

## Kotlin 2.2.20 (September 10, 2025)

Previews of Kotlin 2.3.0 behavior plus experimental features. The first two are enabled with `-language-version 2.3`
(Gradle: `languageVersion.set(KotlinVersion.KOTLIN_2_3)`), not a `-X` flag.

### Improved overload resolution for lambdas with `suspend` function types (preview, default in 2.3.0)

A plain lambda resolves to the non-suspend overload; `suspend { }` resolves to the suspend one — no more ambiguity
error.

```kotlin
fun transform(block: () -> Int) {}
fun transform(block: suspend () -> Int) {}

fun test() {
    transform({ 42 })         // () -> Int
    transform(suspend { 42 }) // suspend () -> Int
}
```

[Docs](https://kotlinlang.org/docs/whatsnew2220.html#improved-overload-resolution-for-lambdas-with-suspend-function-types)

### `return` statements in expression bodies with explicit return types (preview, default in 2.3.0)

```kotlin
fun getDisplayNameOrDefault(userId: String?): String =
    getDisplayName(userId ?: return "default")
```

Allowed only when the return type is explicit; without one it remains an error.
[Docs](https://kotlinlang.org/docs/whatsnew2220.html#support-for-return-statements-in-expression-bodies-with-explicit-return-types)

### Data-flow-based exhaustiveness checks for `when` (Experimental) — `-Xdata-flow-based-exhaustiveness`

The compiler accounts for earlier checks and early returns, so already-eliminated enum/sealed cases don't force an
`else` branch.

```kotlin
fun getPermissionLevel(role: UserRole): Int {
    if (role == UserRole.ADMIN) return 99
    return when (role) {
        UserRole.MEMBER -> 10
        UserRole.GUEST -> 1
    } // no else needed
}
```

[Docs](https://kotlinlang.org/docs/whatsnew2220.html#data-flow-based-exhaustiveness-checks-for-when-expressions)

### Reified types in `catch` clauses (preview, default in 2.4.0) — `-Xallow-reified-type-in-catch`

```kotlin
inline fun <reified ExceptionType : Throwable> handleException(block: () -> Unit) {
    try { block() } catch (e: ExceptionType) {
        println("Caught: ${e::class.simpleName}")
    }
}
```

[Docs](https://kotlinlang.org/docs/whatsnew2220.html#support-for-reified-types-in-catch-clauses)

### Improved Kotlin contracts (Experimental, `@OptIn(ExperimentalContracts::class)`)

- **Generics in contract type assertions** — `-Xallow-contracts-on-more-functions`:
  `contract { returns(true) implies (this@isHttpError is Result.Failed<Failure.HttpError>) }`
  [Docs](https://kotlinlang.org/docs/whatsnew2220.html#support-for-generics-in-contract-type-assertions)
- **Contracts in property accessors and specific operator functions** (`invoke`, `contains`, `rangeTo`, `rangeUntil`,
  `componentN`, `iterator`, unary operators, `inc`, `dec`) — `-Xallow-contracts-on-more-functions`.
  [Docs](https://kotlinlang.org/docs/whatsnew2220.html#support-for-contracts-inside-property-accessors-and-specific-operator-functions)
- **`returnsNotNull()` conditional contract** — `-Xallow-condition-implies-returns-contracts` plus
  `@OptIn(ExperimentalExtendedContracts::class)`:
  `contract { (encoded != null) implies (returnsNotNull()) }`
  [Docs](https://kotlinlang.org/docs/whatsnew2220.html#support-for-the-returnsnotnull-function-in-contracts)
- **`holdsIn` keyword** — `-Xallow-holdsin-contract` plus `@OptIn(ExperimentalExtendedContracts::class)` — a condition
  is assumed true inside a lambda, enabling smart casts in DSLs:

  ```kotlin
  contract {
      callsInPlace(block, InvocationKind.AT_MOST_ONCE)
      condition holdsIn block
  }
  ```

  [Docs](https://kotlinlang.org/docs/whatsnew2220.html#new-holdsin-keyword)

### 2.2.20 — Promotions

- **Kotlin/Wasm → Beta.**
- **Cross-platform compilation of Kotlin libraries (klib from any host) → Stable**;
  `kotlin.native.enableKlibsCrossCompilation` no longer needed.
- **Swift export available by default** (still Experimental, but no longer requires
  `kotlin.experimental.swift-export.enabled`).

### 2.2.20 — Stdlib

- **`KClass.isInterface` on Kotlin/JS** — Experimental, `@OptIn(ExperimentalStdlibApi::class)`.
- **Update functions for common atomics** — Experimental, `@OptIn(ExperimentalAtomicApi::class)`: `update()`,
  `updateAt()`, `fetchAndUpdate()`, `fetchAndUpdateAt()`, `updateAndFetch()`, `updateAndFetchAt()`.

  ```kotlin
  val counter = AtomicLong(0)
  counter.update { it + 1 }
  val previous = counter.fetchAndUpdate { it * 2 }
  ```

- **`copyOf(n) { init }` overload for arrays** — Experimental, `@OptIn(ExperimentalStdlibApi::class)`; resize with an
  initializer, avoids a nullable `Array<T?>` result: `row1.copyOf(4) { "default" }`.

### 2.2.20 — Compiler flags and defaults

- **JVM: `invokedynamic` for `when` expressions** (Experimental) — `-Xwhen-expressions=indy`, requires JVM target 21+.
  Compiles type-check `when` chains to a single `invokedynamic` type switch. Constraints: only `is`/`null` conditions,
  no guards, at least 2 conditions besides `else`. (Lambdas via `invokedynamic` have been the JVM default since
  Kotlin 2.0.0.)
- **JS: `Long` as `BigInt`** (Experimental) — `-Xes-long-as-bigint` (needs ES2020 target); plus
  `-XXLanguage:+JsAllowLongInExportedDeclarations` to allow `Long` in `@JsExport` declarations.
- **Native**: stack canaries `kotlin.native.binary.stackProtector=yes|strong|all`; smaller release binaries
  `kotlin.native.binary.smallBinary=true` (Experimental); KDoc export to Obj-C headers now default (`-Xexport-kdoc` no
  longer needed); x86_64 Apple targets demoted to Tier 2.
- **Wasm**: `KClass.qualifiedName` is a compile error unless `-Xwasm-kclass-fqn` is passed; improved JS-interop
  exception handling by default.
- **Multiplatform**: shared `webMain`/`webTest` source sets for `js` + `wasmJs`.
- **Gradle**: preview FIR-based incremental compilation `kotlin.incremental.jvm.fir=true`.
- **kapt**: runs on K2 by default.
- New artifact `org.jetbrains.kotlin:kotlin-compiler-arguments-description` — machine-readable schema of all compiler
  options.
