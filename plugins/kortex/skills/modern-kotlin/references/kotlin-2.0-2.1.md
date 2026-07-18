# Kotlin 2.0.x – 2.1.x (2024 – early 2025)

| Version | Date | Type |
|---|---|---|
| 2.0.0 | May 21, 2024 | Language release — Stable K2 compiler |
| 2.0.10 | August 6, 2024 | Bug fixes |
| 2.0.20 | August 22, 2024 | Tooling release |
| 2.0.21 | October 10, 2024 | Bug fixes |
| 2.1.0 | November 27, 2024 | Language release |
| 2.1.10 | January 27, 2025 | Bug fixes |
| 2.1.20 | March 20, 2025 | Tooling release |
| 2.1.21 | May 13, 2025 | Bug fixes |

Docs: [What's new in 2.0.0](https://kotlinlang.org/docs/whatsnew20.html) ·
[2.0.20](https://kotlinlang.org/docs/whatsnew2020.html) ·
[2.1.0](https://kotlinlang.org/docs/whatsnew21.html) ·
[2.1.20](https://kotlinlang.org/docs/whatsnew2120.html) ·
[K2 migration guide](https://kotlinlang.org/docs/k2-compiler-migration-guide.html)

## Kotlin 2.0.0 — K2 compiler Stable

Stable on all platforms (JVM, Native, Wasm, JS), enabled by default; default language version 2.0. Fall back with
`languageVersion = 1.9` if needed. No new syntax in 2.0.0 — the user-visible changes are K2 behavior improvements.

### Smart-cast improvements (default, no flags)

1. **Local variables and further scopes** — a boolean variable capturing a type check smart-casts inside
   `if`/`when`/`while`:

   ```kotlin
   val isCat = animal is Cat
   if (isCat) {
       animal.purr() // works in K2, didn't in K1
   }
   ```

2. **Type checks with logical `or`** — `if (x is A || x is B)` smart-casts to the closest common supertype (K1 gave
   `Any`).
3. **Inline functions** — treated as having an implicit `callsInPlace` contract, so variables captured in inline
   lambdas can be smart-cast.
4. **Properties with function types** — class `val` properties of function type are smart-cast
   (`if (provider.callback != null) provider.callback()`).
5. **Exception handling** — smart-cast info propagates into `catch`/`finally` with accurate nullability.
6. **Increment/decrement operators** — type changes after `++`/`--` are tracked.

[Docs](https://kotlinlang.org/docs/whatsnew20.html#smart-cast-improvements)

### Multiplatform (K2)

- Strict compile-time separation of common and platform sources — common code can no longer accidentally resolve to
  platform declarations.
- `actual` declarations may be more permissive than `expect` (e.g. `expect internal class` → public `actual class`).

### Lambdas via `invokedynamic` by default

Was opt-in `-Xlambdas=indy` since 1.5.0; now default. Opt out: `-Xlambdas=class`. Keep a specific lambda serializable
with `@JvmSerializableLambda`. Indy lambdas are not serializable, don't support `reflect()`, and have an unhelpful
`toString()`.
[Docs](https://kotlinlang.org/docs/whatsnew20.html#generation-of-lambda-functions-using-invokedynamic)

### 2.0.0 promotions and stdlib

- kapt with K2 → Stable; compiler plugins stable with K2 (all-open, AtomicFU, Lombok, no-arg, Parcelize,
  SAM-with-receiver, serialization); Power-assert stays Experimental.
- Compose compiler Gradle plugin `org.jetbrains.kotlin.plugin.compose` — new, ships with Kotlin.
- Stdlib: **`enumEntries<T>()` → Stable**; **`AutoCloseable` (common) → Stable** with `use()`;
  `String.toCharArray(destination)` common Stable.
- JVM bytecode up to Java 22. Gradle 6.8.3–8.5.

## Kotlin 2.0.20 (August 22, 2024)

### Data class `copy()` visibility to match constructor (migration phase)

In future releases `copy()` gets the same visibility as the primary constructor; 2.0.20 warns where behavior will
change. Flags/annotations:

- `-Xconsistent-data-class-copy-visibility` — opt in to new behavior module-wide.
- `@ConsistentCopyVisibility` — per-class opt-in now.
- `@ExposedCopyVisibility` — per-class opt-out of declaration-site warnings.

```kotlin
data class PositiveInteger private constructor(val number: Int) {
    companion object {
        fun create(number: Int): PositiveInteger? =
            if (number > 0) PositiveInteger(number) else null
    }
}
// 2.0.20: warning — non-public constructor exposed via generated 'copy()'
```

[Docs](https://kotlinlang.org/docs/whatsnew2020.html#data-class-copy-function-to-have-the-same-visibility-as-constructor)

### Context receivers deprecated

Experimental context receivers (`-Xcontext-receivers`, from 1.6.20) warn on every use from 2.0.20; replaced by
context parameters (removed entirely in 2.3.20).
[Docs](https://kotlinlang.org/docs/whatsnew2020.html#phased-replacement-of-context-receivers-with-context-parameters)

### 2.0.20 stdlib

- **`kotlin.uuid.Uuid`** — new, Experimental (`@OptIn(ExperimentalUuidApi::class)`): `Uuid.random()`, `Uuid.parse()`,
  `Uuid.fromByteArray()`, JVM interop `toJavaUuid()`/`toKotlinUuid()`. Stable in 2.4.0.
- `HexFormat.NumberHexFormat.minLength` — Experimental.
- `Base64` decoder now requires padding by default; configure via `.withPadding(Base64.PaddingOption)`.
- Compose: strong skipping mode on by default; Kotlin/Native concurrent GC marking Experimental
  (`kotlin.native.binary.gc=cms`).

## Kotlin 2.1.0 (November 27, 2024)

### Guard conditions in `when` (Preview) — `-Xwhen-guards`

Extra `if` condition after the primary branch condition. Stable in 2.2.0.

```kotlin
when (animal) {
    is Animal.Dog -> animal.feedDog()
    is Animal.Cat if !animal.mouseHunter -> animal.feedCat()
    else -> println("Unknown animal")
}
```

[Docs](https://kotlinlang.org/docs/whatsnew21.html#guard-conditions-in-when-with-a-subject)

### Non-local `break` and `continue` (Preview) — `-Xnon-local-break-continue`

`break`/`continue` inside lambdas passed to inline functions affect the enclosing loop. Stable in 2.2.0.

```kotlin
for (element in elements) {
    val v = element.nullableMethod() ?: run {
        log.warning("null element"); continue // non-local continue
    }
}
```

[Docs](https://kotlinlang.org/docs/whatsnew21.html#non-local-break-and-continue)

### Multi-dollar string interpolation (Preview) — `-Xmulti-dollar-interpolation`

`$$"..."` — interpolation triggers only on `$$`; a single `$` is a literal. Ideal for JSON schema, templating, regex.
Stable in 2.2.0.

```kotlin
val jsonSchema = $$"""
{ "$schema": "https://json-schema.org/draft/2020-12/schema",
  "title": "$${simpleName ?: "unknown"}" }
"""
```

[Docs](https://kotlinlang.org/docs/whatsnew21.html#multi-dollar-string-interpolation)

### Stable in 2.1.0

- **`@SubclassOptInRequired`** — require opt-in to implement/extend an API:

  ```kotlin
  @SubclassOptInRequired(UnstableApi::class)
  interface CoreLibraryApi
  ```

- **Improved overload resolution for functions with generic lambdas** — the compiler discards overloads whose generic
  parameter can't accept a lambda.
- **Improved exhaustiveness checks for `when` with sealed upper bounds** — no `else` needed for
  `fun <T : Result> render(result: T) = when (result) { ... }`.

### K2 compiler checks (2.1.0)

- **`-Wextra`** — extra warnings (`CAN_BE_VAL`, `UNUSED_VARIABLE`, `REDUNDANT_NULLABLE`,
  `ASSIGNED_VALUE_IS_NEVER_READ`, `UNREACHABLE_CODE`, and more). Gradle: `compilerOptions { extraWarnings.set(true) }`.
- **`-Xsuppress-warning=WARNING_NAME`** — global warning suppression (Experimental).
- **JSpecify nullability severity strict by default** — mismatches are errors; control:
  `-Xnullability-annotations={ignore|warning|strict}`.
- Java 23 bytecode support; default language version 2.1.

### 2.1.0 stdlib

- Deprecations raised to error: `Char.toLowerCase()`/`toUpperCase()`, `String.toLowerCase()`/`toUpperCase()` (use
  `lowercase()`/`uppercase()`), `appendln()` (use `appendLine()`).
- **Stable file-tree traversal** for `java.nio.file.Path` (Experimental since 1.7.20): `Path.walk()`,
  `fileVisitor {}`, `Path.visitFileTree()`.

## Kotlin 2.1.20 (March 20, 2025)

Tooling release; no new syntax. Highlights:

- **K2 kapt is now the default** (opt out: `kapt.use.k2=false`).
- **Common atomic types** — `kotlin.concurrent.atomics`, Experimental (`@OptIn(ExperimentalAtomicApi::class)`):
  `AtomicInt`, `AtomicLong`, `AtomicBoolean`, `AtomicReference`; JVM interop `asJavaAtomic()`/`asKotlinAtomic()`.

  ```kotlin
  @OptIn(ExperimentalAtomicApi::class)
  fun demo() {
      val processedItems = AtomicInt(0)
      processedItems += 1
      println(processedItems.load())
  }
  ```

- **`kotlin.time.Clock` and `kotlin.time.Instant`** — Experimental (`@OptIn(ExperimentalTime::class)`), migrated from
  kotlinx-datetime into the stdlib; converters `toKotlinInstant()`/`toJavaInstant()`. Stable in 2.3.0.
- **Uuid improvements**: `Uuid.parse()` accepts hex-and-dash and plain hex; `parseHexDash()`/`toHexDashString()`;
  `Uuid` is `Comparable`.
- Lombok plugin: `@SuperBuilder` supported.
- Kotlin/Native: Experimental pre-codegen inlining `-Xbinary=preCodegenInlineThreshold=40`.
- Kotlin/Wasm: `-Xwasm-generate-dwarf`.
- KMP: Experimental `executable {}` DSL replacing the Gradle Application plugin.
