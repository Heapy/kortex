# Kotlin 1.4.x – 1.6.x (2020–2022) — historical reference

Features from before the "modern" window; kept here for completeness. Everything below is long Stable (except where
noted) and safe to use in any Kotlin 2.x codebase.

| Version | Date | Highlights |
|---|---|---|
| 1.4.0 | August 17, 2020 | `fun interface`, trailing comma, explicit API mode |
| 1.4.20 | November 23, 2020 | invokedynamic string concat (experimental), Path extensions |
| 1.4.30 | February 3, 2021 | Previews: records, sealed interfaces, value classes |
| 1.5.0 | May 5, 2021 | JVM records, sealed interfaces, value classes stable |
| 1.5.20 | June 24, 2021 | indy string concat default, JSpecify, Lombok plugin |
| 1.5.30 | August 24, 2021 | Previews: sealed-when exhaustiveness, annotation instantiation |
| 1.6.0 | November 16, 2021 | Duration API stable, suspend conversions stable |
| 1.6.20 | April 4, 2022 | Context receivers prototype, `T & Any` Beta |

Docs: [1.4.0](https://kotlinlang.org/docs/whatsnew14.html) · [1.4.30](https://kotlinlang.org/docs/whatsnew1430.html) ·
[1.5.0](https://kotlinlang.org/docs/whatsnew15.html) · [1.5.30](https://kotlinlang.org/docs/whatsnew1530.html) ·
[1.6.0](https://kotlinlang.org/docs/whatsnew16.html) · [1.6.20](https://kotlinlang.org/docs/whatsnew1620.html)

## Kotlin 1.4.0 (August 2020)

All stable at introduction:

- **SAM conversions for Kotlin interfaces (`fun interface`)** — lambdas convert to single-abstract-method Kotlin
  interfaces (previously Java-only):

  ```kotlin
  fun interface IntPredicate { fun accept(i: Int): Boolean }
  val isEven = IntPredicate { it % 2 == 0 }
  ```

- **Explicit API mode** for library authors — forces explicit visibility and return types on public API. Gradle:
  `explicitApi()` / `explicitApiWarning()`; CLI: `-Xexplicit-api={strict|warning}`.
- **Mixing named and positional arguments** — `reformat("str", uppercaseFirstLetter = false, '-')`.
- **Trailing comma** in parameter/argument lists, `when` entries, destructuring.
- **Callable reference improvements** — references to functions with default values, `Unit`-adaption, vararg
  adaptation, suspend conversion on references.
- **`break`/`continue` inside `when` in loops** without labels.
- **New type inference algorithm** by default; JVM IR and JS IR backends in Alpha (`-Xuse-ir`,
  `kotlin.js.compiler=ir`).

Stdlib: `kotlin-stdlib` added by default to Gradle projects; **`ArrayDeque`**; `setOfNotNull()`, `shuffled()`,
`onEachIndexed()`, `flatMapIndexed()`, `randomOrNull()`, `reduceOrNull()`, `runningFold()`/`scan()`,
`sumOf()`, `minOf()`/`maxOf()`, `removeFirst()`/`removeLast()`; StringBuilder `deleteRange`/`insertRange`,
`appendLine()`; `decodeToString()`/`encodeToByteArray()`; bit operations (`countOneBits()` etc.); common
`stackTraceToString()`/`addSuppressed()`/`@Throws`; delegation of one property to another;
`PropertyDelegateProvider`.

## Kotlin 1.4.20 – 1.4.30

- **invokedynamic string concatenation** — Experimental, JVM 9+: `-Xstring-concat={indy-with-constants|indy|inline}`
  (default from 1.5.20).
- **`kotlin.io.path` extensions** — Experimental (`@ExperimentalPathApi`): `Path("/base") / "sub"`,
  `listDirectoryEntries()`; Stable in 1.5.0.
- 1.4.30 previews (via `-language-version 1.5`): JVM records, sealed interfaces, package-wide sealed hierarchies,
  `value` classes Beta (`@JvmInline`, `init` blocks allowed); JVM IR backend Beta.
- Locale-agnostic case API and unambiguous `Char` conversions — Experimental (Stable in 1.5.0).

## Kotlin 1.5.0 (May 2021)

Stable language features:

- **JVM records**: `@JvmRecord data class User(val name: String, val age: Int)` (JDK 16+).
- **Sealed interfaces** — exhaustive `when` without `else`; a class can implement multiple sealed supertypes:

  ```kotlin
  sealed interface Polygon
  class Rectangle : Polygon
  class Triangle : Polygon
  fun draw(p: Polygon) = when (p) {
      is Rectangle -> TODO()
      is Triangle -> TODO()
  }
  ```

- **Package-wide sealed class hierarchies** — subclasses in any file of the same package/compilation unit.
- **Value classes** — `@JvmInline value class Password(val s: String)`; the `inline` modifier deprecated.

Compiler: JVM IR backend Stable and default; default JVM target raised 1.6 → 1.8; SAM adapters via `invokedynamic`
(revert: `-Xsam-conversions=class`); plain lambdas via indy experimental (`-Xlambdas=indy`).

Stdlib: **unsigned integer types Stable** (`UInt`, `ULong`, `UByte`, `UShort`); locale-agnostic
`uppercase()`/`lowercase()`; `Char.code`/`Char(65)`/`digitToInt()`; Path API Stable; `floorDiv()`/`mod()`;
`firstNotNullOf()`, `toBooleanStrict()`; kotlin-test `assertIs<T>()`, `assertContentEquals()`.

## Kotlin 1.5.20 – 1.5.30

- 1.5.20: **string concat via invokedynamic default** (JVM 9+; opt out `-Xstring-concat=inline`); JSpecify support
  (`-Xjspecify-annotations=strict`); Lombok compiler plugin Experimental.
- 1.5.30 previews (via `-language-version 1.6` or flags):
  - **Exhaustive `when` statements for sealed/Boolean subjects** — warnings 1.6.0, errors 1.7.
  - **Suspending functions as supertypes** — `class MyClass : suspend () -> Unit { ... }`; Stable in 1.6.0.
  - **Instantiation of annotation classes** — `processInfo(InfoMarker("default"))`; Stable in 1.6.0.
  - Recursive-generic type inference from upper bounds (`-Xself-upper-bound-inference`); default in 1.6.0.
  - Unrestricted builder inference (`-Xunrestricted-builder-inference`); default in 1.6.0, fully stable 1.7.0.
- Stdlib 1.5.30: `Duration.toString()` component output (`1d 12h`, `15h 20m`), `Duration.parse()`;
  `Regex.matchAt()`/`matchesAt()`; `splitToSequence()`.

## Kotlin 1.6.0 (November 2021)

- **Exhaustive `when` statements** for enum, sealed, Boolean — warnings by default (errors in 1.7).
- **Suspending functions as supertypes → Stable**; **suspend conversions → Stable** (regular functional values
  convert to suspending types in any position).
- **Instantiation of annotation classes → Stable** (JVM/JS; Native in 1.6.20).
- Recursive-generic inference default; builder inference improvements (`-Xenable-builder-inference`).
- **Annotations on class type parameters**: `class Box<@BoxContent T>` — emitted into bytecode.
- **Repeatable annotations with runtime retention** for JVM target 1.8.
- Stdlib:
  - **`kotlin.time.Duration` → Stable** — `import kotlin.time.Duration.Companion.seconds`;
    `10000.seconds.inWholeMinutes`; `DurationUnit` standalone enum.
  - `readln()` / `readlnOrNull()`; **`typeOf()` → Stable** on all platforms; **`buildList()`/`buildMap()`/`buildSet()`
    → Stable**; bit rotation Stable; `splitToSequence()` Stable.

## Kotlin 1.6.20 (April 2022)

- **Context receivers — prototype** (JVM only) — `-Xcontext-receivers`; explicitly not for production. Deprecated in
  2.0.20, removed in 2.3.20; superseded by context parameters (Stable in 2.4.0).

  ```kotlin
  interface LoggingContext { val log: Logger }

  context(LoggingContext)
  fun startBusinessOperation() {
      log.info("Operation has started")
  }
  ```

- **Definitely non-nullable types `T & Any` — Beta** (`-language-version 1.7`); Stable in 1.7.0.
- `@JvmDefaultWithCompatibility` for interfaces with `-Xjvm-default=all`.
- Experimental parallel compilation of a single module: `-Xbackend-threads=N`.
- Hierarchical multiplatform project structure on by default; Kotlin/Native new memory manager Alpha.
