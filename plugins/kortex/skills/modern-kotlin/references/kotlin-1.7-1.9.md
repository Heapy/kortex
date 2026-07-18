# Kotlin 1.7.x – 1.9.x (2022–2023)

| Version | Date | Highlights |
|---|---|---|
| 1.7.0 | June 9, 2022 | K2 Alpha (JVM), builder inference stable, `T & Any` stable |
| 1.7.20 | September 29, 2022 | `..<` preview, `data object` preview, generic inline classes |
| 1.8.0 | December 28, 2022 | Old JVM backend removed, recursive Path copy/delete |
| 1.8.20 | April 25, 2023 | `Enum.entries` preview, `data object` refined, Base64, AutoCloseable |
| 1.9.0 | July 6, 2023 | K2 Beta (JVM); `entries`, `data object`, `..<`, time API stable |
| 1.9.20 | November 1, 2023 | K2 Beta all targets, KMP stable, wasmWasi |

Docs: [1.7.0](https://kotlinlang.org/docs/whatsnew17.html) · [1.7.20](https://kotlinlang.org/docs/whatsnew1720.html) ·
[1.8.0](https://kotlinlang.org/docs/whatsnew18.html) · [1.8.20](https://kotlinlang.org/docs/whatsnew1820.html) ·
[1.9.0](https://kotlinlang.org/docs/whatsnew19.html) · [1.9.20](https://kotlinlang.org/docs/whatsnew1920.html)

## Kotlin 1.7.0

### K2 compiler — Alpha (JVM only) — `-Xuse-k2`

~2x compilation speedup. Trajectory: plugin support 1.7.20 → via `-language-version 2.0` from 1.8.20 → Beta JVM
1.9.0 → Beta all targets 1.9.20 → Stable 2.0.0.

### Stable in 1.7.0

- **Definitely non-nullable types `T & Any`** (Beta since 1.6.20) — marks a generic type parameter as definitely
  non-nullable at the use site; main use is overriding Java methods with `@NotNull` arguments:

  ```kotlin
  fun <T> elvisLike(x: T, y: T & Any): T & Any = x ?: y
  elvisLike<String?>(null, "ok")   // OK
  // elvisLike<String?>(null, null) — compile error
  ```

- **Builder inference** — previously `-Xenable-builder-inference`; now activates automatically
  (`buildList { add("x") }` infers `List<String>`).
- **Opt-in requirements (`@RequiresOptIn` / `@OptIn`)** — no compiler flag needed anymore to define opt-in
  annotations; `-opt-in=<fq-name>` remains for module-wide opt-in.
- **Underscore operator for type arguments** — `Runner.run<SomeImplementation, _>()` infers the second argument.
- **Implementation by delegation to an inlined value of an inline class** —
  `@JvmInline value class BarWrapper(val bar: Bar) : Bar by bar`.
- **Callable references to functional interface constructors** (JVM) — `::Printer` for a `fun interface`.

### 1.7.0 stdlib

- Non-nullable `min()`/`max()`/`minBy()`/`maxBy()` reintroduced (throw on empty; `*OrNull()` variants remain).
- `Regex.matchAt()`/`matchesAt()` stable; named capturing groups on JS and Native.
- **`DeepRecursiveFunction` → Stable** — call stack on the heap, recursion depth 100 000+.
- `java.util.Optional` extensions Experimental (`getOrNull()`, `toList()`, …; Stable in 1.8.0).

## Kotlin 1.7.20

- **`..<` operator (rangeUntil) — Experimental preview** (`OpenEndRange<T>` API behind
  `@OptIn(ExperimentalStdlibApi::class)`, operator via `-language-version 1.8`). Stable in 1.9.0.

  ```kotlin
  when (value) {
      in 0.0..<0.25 -> {} // 0.25 excluded
      in 0.25..<0.5 -> {}
  }
  ```

- **`data object` — Experimental preview** (`-language-version 1.9`) — an `object` whose `toString()` prints its
  simple name; symmetry with `data class` in sealed hierarchies. Stable in 1.9.0.
- **Generic inline classes — Experimental** (`-language-version 1.8`):
  `@JvmInline value class UserId<T>(val value: T)`.
- **New builder inference restrictions** — ambiguous cases may now require explicit type arguments.
- Kotlin/Native new memory manager promoted to Beta and enabled by default.
- Stdlib: `java.nio.file.Path` tree traversal Experimental — `walk()`, `fileVisitor()`, `visitFileTree()`
  (`@OptIn(ExperimentalPathApi::class)`).

## Kotlin 1.8.0

No new syntax; compiler and stdlib work:

- **Old JVM backend removed** — `-Xuse-old-backend` no longer accepted.
- `-Xdebug` — disables optimizations for better debugging (e.g. coroutine "was optimized out").
- `-Xno-new-java-annotation-targets` — suppress `TYPE_USE`/`TYPE_PARAMETER` Java annotation targets.
- **JS IR compiler backend → Stable**; JVM target 19 supported.
- Stdlib:
  - **Recursive copy/delete for `Path`** — Experimental: `copyToRecursively()`, `deleteRecursively()`
    (`@OptIn(ExperimentalPathApi::class)`); still Experimental as of 2.4.x (the traversal extensions from 1.7.20
    became stable in 2.1.0).
  - **Comparable and subtractable `TimeMark`s** — Experimental; stabilized with the time API in 1.9.0.
  - `cbrt()` Stable; `TimeUnit`↔`DurationUnit` conversions Stable; Java `Optional` extensions Stable.
  - `kotlin-stdlib-jdk7`/`-jdk8` merged into `kotlin-stdlib`; stdlib compiled to JVM target 1.8.
  - `kotlin-reflect` performance: caches on `ClassValue`, faster `typeOf()`.

## Kotlin 1.8.20

- **K2 via `-language-version 2.0`** (`-Xuse-k2` deprecated).
- **`Enum.entries` — Experimental preview** (`@OptIn(ExperimentalStdlibApi::class)` + `-language-version 1.9`) —
  modern replacement for synthetic `values()`: a pre-allocated immutable `EnumEntries<E>` list. Stable in 1.9.0.

  ```kotlin
  enum class Color(val rgb: String) { RED("#FF0000"), ORANGE("#FF7F00") }
  fun findByRgb(rgb: String): Color? = Color.entries.find { it.rgb == rgb }
  ```

- **`data object` — refined preview** (`-language-version 1.9`): structural `equals()`/`hashCode()`, no
  `copy()`/`componentN()` to preserve singleton semantics.
- **Secondary constructors with bodies in inline (value) classes — preview** (`-language-version 1.9`); Stable in
  1.9.0.

  ```kotlin
  @JvmInline
  value class Person(private val fullName: String) {
      init { check(fullName.isNotBlank()) }
      constructor(name: String, lastName: String) : this("$name $lastName") {
          check(lastName.isNotBlank())
      }
  }
  ```

- **Java synthetic property references — Experimental** (`-language-version 1.9`): `persons.sortedBy(Person::age)`.
- Stdlib:
  - **`AutoCloseable` (common) — Experimental** with `use()`; Stable in 2.0.0.
  - **Base64 — Experimental** (`@OptIn(ExperimentalEncodingApi::class)`): `Base64.Default`, `UrlSafe`, `Mime`;
    Stable in 2.2.0.
  - **Common `@Volatile` (`kotlin.concurrent.Volatile`) — Experimental**; Stable in 1.9.0.
- First Experimental **Kotlin/Wasm** target.

## Kotlin 1.9.0

### Stable in 1.9.0

- **`Enum.entries`** — `values()` discouraged.
- **`data object`** — auto `toString()`/`equals()`/`hashCode()`; no `copy()`/`componentN()`.

  ```kotlin
  sealed interface ReadResult
  data class Number(val number: Int) : ReadResult
  data object EndOfFile : ReadResult
  println(EndOfFile) // EndOfFile
  ```

- **Secondary constructors with bodies in value classes.**
- **`..<` (rangeUntil) operator / `OpenEndRange`**: `for (n in 2..<10) { }`.
- **Time API** (`kotlin.time`): `TimeSource.Monotonic`, comparable/subtractable `TimeMark`,
  `measureTime`/`measureTimedValue`, `hasPassedNow()`.
- **Common `@Volatile`** (`kotlin.concurrent.Volatile`, JVM + Native).
- Common regex capture group by name: `MatchResult.groups["name"]`.

### 1.9.0 additions

- K2 — **Beta for JVM**; trial switch `kotlin.experimental.tryK2=true`.
- `Path.createParentDirectories()`.
- **`HexFormat` — Experimental** (`@OptIn(ExperimentalStdlibApi::class)`): `93.toHexString()`,
  `"001b638445e6".hexToByteArray()`, configurable separators/prefixes. Stable in 2.2.0.
- Kotlin/Native: `kotlinx.cinterop` split behind `@OptIn(ExperimentalForeignApi::class)` and
  `@OptIn(BetaInteropApi::class)`.

## Kotlin 1.9.20

- **K2 — Beta for ALL targets** (JVM, JS, Native, Wasm); enable with language version 2.0 or
  `kotlin.experimental.tryK2=true`; kapt preview via `kapt.use.k2=true`.
- **Kotlin Multiplatform → Stable** (platform milestone); **Kotlin/Native stdlib → Stable**;
  **Kotlin/Wasm → Alpha**.
- Java 21 bytecode target support.
- Stdlib:
  - **`enumEntries<T>()` — Experimental** (`@OptIn(ExperimentalStdlibApi::class)`): reified-generic companion to
    `Enum.entries`, replacement for `enumValues<T>()`. Stable in 2.0.0.
  - Kotlin/Native Experimental `AtomicIntArray`, `AtomicLongArray`, `AtomicArray<T>`.
  - WASI API support for the new `wasmWasi` target (`@WasmImport`); `wasm {}` renamed `wasmJs {}`.

## Stability arc (1.7 → 2.x)

| Feature | Introduced | Stable |
|---|---|---|
| Builder inference | pre-1.7 (`-Xenable-builder-inference`) | 1.7.0 |
| `T & Any` | 1.6.20 Beta | 1.7.0 |
| Opt-in requirements | 1.3 | 1.7.0 |
| `..<` / `OpenEndRange` | 1.7.20 | 1.9.0 |
| `data object` | 1.7.20 preview | 1.9.0 |
| `Enum.entries` | 1.8.20 preview | 1.9.0 |
| Value-class secondary constructors with bodies | 1.8.20 preview | 1.9.0 |
| Common `@Volatile` | 1.8.20 | 1.9.0 |
| Time API | 1.3.50 experimental | 1.9.0 |
| `enumEntries<T>()` | 1.9.20 | 2.0.0 |
| `AutoCloseable` (common) | 1.8.20 | 2.0.0 |
| Base64 | 1.8.20 | 2.2.0 |
| `HexFormat` | 1.9.0 | 2.2.0 |
| Path traversal (`walk`, `visitFileTree`) | 1.7.20 | 2.1.0 |
| K2 compiler | 1.7.0 Alpha | 2.0.0 |
| Generic inline classes | 1.7.20 | not yet stabilized |
| Java synthetic property references | 1.8.20 | not yet stabilized |
