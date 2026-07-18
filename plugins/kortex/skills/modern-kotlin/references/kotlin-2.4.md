# Kotlin 2.4.x (2026)

| Version | Date | Type |
|---|---|---|
| 2.4.0 | June 3, 2026 | Language release |
| 2.4.10 | July 14, 2026 | Bug fixes only — current latest stable |
| 2.4.20-Beta1 | June 24, 2026 | EAP for the 2.4.20 tooling release (final planned September 2026) |

Docs: [What's new in 2.4.0](https://kotlinlang.org/docs/whatsnew24.html) ·
[What's new in the EAP](https://kotlinlang.org/docs/whatsnew-eap.html) ·
[Compatibility guide 2.4](https://kotlinlang.org/docs/compatibility-guide-24.html) ·
[Blog: Kotlin 2.4.0 released](https://blog.jetbrains.com/kotlin/2026/06/kotlin-2-4-0-released/)

## Kotlin 2.4.0 — Language

### Explicit context arguments (Experimental) — `-Xexplicit-context-arguments`

Pass a context argument explicitly by parameter name at the call site, e.g. to disambiguate overloads. KEEP-0448.
IDE support from IntelliJ IDEA 2026.2. Promoted to Stable in 2.4.20-Beta1 (KT-86089).

```kotlin
context(emailSender: EmailSender) fun sendNotification() { println("email") }
context(smsSender: SmsSender)     fun sendNotification() { println("sms") }

context(defaultEmailSender: EmailSender, defaultSmsSender: SmsSender)
fun notifyUser() {
    sendNotification(emailSender = defaultEmailSender) // explicit context argument
    sendNotification(smsSender = defaultSmsSender)
}
```

[Docs](https://kotlinlang.org/docs/whatsnew24.html#explicit-context-arguments-for-context-parameters)

### Collection literals (Experimental) — `-Xcollection-literals`

Bracket syntax for collections; the type comes from the expected type and defaults to `List`. Custom types opt in via
an `operator fun of` in the companion. Limitation: cannot construct Java-defined collection types. KEEP-0416.

```kotlin
val shapes: MutableList<String> = ["triangle", "square", "circle"]
val fruit = ["apple", "banana", "cherry"] // infers List<String>

class Row(vararg val elements: Double) {
    companion object { operator fun of(vararg elements: Double) = Row(*elements) }
}
```

[Docs](https://kotlinlang.org/docs/whatsnew24.html#support-for-collection-literals)

### Improved compile-time constants (Experimental) — `-Xintrinsic-const-evaluation`

Extends const evaluation to unsigned-type operations, string functions (`.lowercase()`, `.uppercase()`, `.trim()`),
enum `.name`, and `KCallable` members; functions marked with the new `IntrinsicConstEvaluation` annotation. KEEP-0444.
[Docs](https://kotlinlang.org/docs/whatsnew24.html#improved-compile-time-constants)

### `returnsResultOf` contract (Experimental) — `-Xallow-returns-result-of`

Plus `@OptIn(ExperimentalContracts::class)`. Propagates unused-return-value checking through higher-order functions.
Caution: produces pre-release binaries incompatible with earlier K2 versions.

```kotlin
@OptIn(ExperimentalContracts::class)
inline fun <T, R> T.customLet(block: (T) -> R): R {
    contract { returnsResultOf(block) }
    return block(this)
}
// packageName?.customLet { builder.append(it) } — no unused-result warning
// packageName?.customLet { "kotlin.$it" }       — warns: result unused
```

[Docs](https://kotlinlang.org/docs/whatsnew24.html#improved-unused-result-checks-for-higher-order-functions)

### `@IntroducedAt` — version-based overloads (Experimental) — `@OptIn(ExperimentalVersionOverloading::class)`

The compiler emits hidden overloads matching each prior version's signature for binary compatibility of APIs that grow
optional parameters. Conflicts with `@JvmOverloads` produce a warning; `@IntroducedAt` wins.

```kotlin
@OptIn(ExperimentalVersionOverloading::class)
fun Button(
    label: String = "",
    @IntroducedAt("1.1") borderColor: Color = DefaultBorderColor,
    @IntroducedAt("1.2") borderWidth: Int = 1,
    onClick: () -> Unit,
) { }
```

[Docs](https://kotlinlang.org/docs/whatsnew24.html#new-introducedat-annotation-to-generate-version-based-overloads-for-optional-parameters)

### Behavior changes

- Deprecation warnings are no longer reported on the last segment of import directives, only at usage sites (KT-30155).

### Promoted in 2.4.0

- **Context parameters → Stable** (preview since 2.2.0). Still Experimental: explicit context arguments (above) and
  callable references to context-parameter functions. [Docs](https://kotlinlang.org/docs/context-parameters.html)
- **Explicit backing fields → Stable** (Experimental since 2.3.0).
  [Docs](https://kotlinlang.org/docs/properties.html#explicit-backing-fields)
- **`@all` meta-target for properties → Stable** (preview since 2.2.0, KT-73256).
- **New defaulting rules for use-site annotation targets → Stable** (preview since 2.2.0).
- **Annotations in metadata → enabled by default** (Experimental since 2.2.0), Kotlin/JVM.
- **Swift export → Alpha**: `suspend` → Swift `async`; `Flow<T>` → `AsyncSequence` with generics preserved.
- **Kotlin/Wasm incremental compilation → enabled by default** (disable: `kotlin.incremental.wasm=false`).

## Kotlin 2.4.0 — Stdlib

- **UUID API → Stable** (`kotlin.uuid.Uuid`): comparison operators, parsing both hex-and-dash and plain formats,
  null-returning parse variants. V4/V7 generation functions remain Experimental.
  [Docs](https://kotlinlang.org/docs/uuids.html)
- **Sorted-order checks → Stable** (iterables/arrays/sequences): `isSorted()`, `isSortedDescending()`,
  `isSortedWith(comparator)`, `isSortedBy(selector)`, `isSortedByDescending(selector)`.
- **`UInt.toBigInteger()` / `ULong.toBigInteger()`** — Stable, JVM only.
- **Map fallback functions** — Experimental, `@OptIn(ExperimentalStdlibApi::class)`: `getOrElseIfNull`,
  `getOrPutIfNull` (treat stored null as missing), `getOrElseIfMissing`, `getOrPutIfMissing` (only missing key;
  preserves stored nulls — useful for null-caching). KT-67337.

  ```kotlin
  @OptIn(ExperimentalStdlibApi::class)
  fun getCachedResponseOrQuery(key: String): Response? =
      cache.getOrPutIfMissing(key) { service.query(key) } // caches null results too
  ```

## Kotlin 2.4.0 — Compiler flags and defaults

- **K1 compiler support dropped**: `-language-version=1.9` no longer accepted; K2 only.
- **JVM**: Java 26 support; JVM bytecode target 26 (KT-84319).
- **klib intra-module inlining enabled by default** (Native/JS/Wasm). Disable: `-Xklib-ir-inliner=disabled`;
  experimental cross-module mode: `-Xklib-ir-inliner=full`.
- **Partial linkage always enabled**; `-Xpartial-linkage` deprecated. Log level:
  `-Xpartial-linkage-loglevel=INFO|WARNING|ERROR`.
- **Kotlin/Native**: CMS GC (concurrent marking) is the new default (revert: `kotlin.native.binary.gc=pmcs`);
  LLVM 19 → 21; Apple minimum deployment targets raised (iOS/tvOS 15.0, macOS 12.0, watchOS 8.0; override via
  `-Xoverride-konan-properties=minVersion.ios=14.0`).
- **Gradle**: supported 7.6.3–9.5.0; min AGP 8.5.2; default module name is now `{group}:{project_name}` on all
  platforms (revert: `compilerOptions.moduleName(project.name)`).
- **Maven**: auto-alignment of `jvmTarget` with `maven.compiler.release`/`target`; Maven toolchains support.
- **kapt**: `kapt.include.compile.classpath=false` stops discovering processors on the compile classpath.
- **Power-assert**: new runtime library — `CallExplanation` structure, `@PowerAssert` annotation.
- **Kotlin/JS** (Stable): `@JsExport` of value classes to TS; ES2015 constructs in `js()`; variance preserved in
  `.d.ts`; new `@JsNoRuntime` annotation; interfaces may export nested classes and named companions.
- **Kotlin/Wasm**: WebAssembly Component Model support — Experimental (KT-64569).

## Kotlin 2.4.10 (July 14, 2026)

Pure bug-fix release; no new language features. Notable fixes: KT-86939 (`const val` in nested Java annotation
arrays), KT-86728 (expected-type propagation in inline calls with elvis), KT-86501 (Native IrTypeAliasSymbolImpl on
iosSimulatorArm64), KT-87076 (`@file:CompilerOptions` JVM target ignored in `.main.kts`), Compose stability-inference
regression (b/522127447). New `kotlinr` binary in the distribution (KT-86930).
[Changelog](https://github.com/JetBrains/kotlin/releases/tag/v2.4.10)

## Kotlin 2.4.20-Beta1 (EAP, June 24, 2026)

### Language / K2

- **Explicit context arguments → Stable** (KT-86089).
- **`when` via `invokedynamic` enabled by default for JVM targets 21+** (KT-78079; was `-Xwhen-expressions=indy`).
- **Name-based destructuring**: stable release (syntax-only) planned for 2.5 (KT-86201).
- Experimental **language version 2.6** added (KT-85667); LV 1.9 dropped for JVM (KT-80590); K1 deprecated (KT-75372).
- Collection literals refinements (still `-Xcollection-literals`): companion-block `of` operator (KT-84295),
  inference in delegate expressions (KT-84333), resolution to companion block and extension `invoke` (KT-84289).
- **Companion blocks and extensions** — upcoming feature in development (KT-85770, KT-84861, KT-85188, KT-86053).
- Unit-conversions for arbitrary expressions in argument positions (KT-84393); warning when `_` is assigned a `Unit`
  expression (KT-84618).

### Stdlib

- **`StackTraceRecoverable` interface** — Experimental, `@OptIn(ExperimentalStdlibCoroutineSupportApi::class)`
  (KT-86595, KEEP-0461). Lets exception classes define copy-for-recovery for kotlinx.coroutines stack-trace recovery
  without depending on kotlinx.coroutines.

  ```kotlin
  @OptIn(ExperimentalStdlibCoroutineSupportApi::class)
  class FileEditException(val line: Int, private val detail: String, cause: Throwable? = null) :
      IllegalStateException("When editing line $line: $detail", cause),
      StackTraceRecoverable<FileEditException> {
      override fun copyForStackTraceRecovery() = FileEditException(line, detail, this)
  }
  ```

- `allEqual` function for `Iterable` (KT-10380).

### Toolchain

- **Kotlin/Native incremental compilation enabled by default** (KT-86657); disable: `kotlin.incremental.native=false`.
- **Kotlin/Wasm**: error on top-level `require()` in `@JsFun` (KT-86192); companion-object init order now
  superclass-first, matching JVM (KT-84267).
- **Kotlin/JS browser-testing DSL** — Experimental, `@OptIn(ExperimentalJsTestDsl::class)`: Mocha + Webpack +
  Playwright (`browserDefaults {}`, `chromium {}`, `firefox()`, `webkit {}`) (KT-66897).
- **Build Tools API for JS/Wasm/metadata**: default in Beta1; opt-in from Beta2 through 2.4.20 final via
  `kotlin.js.runViaBuildToolsApi=true` etc.; default again in 2.5.0.

[EAP docs](https://kotlinlang.org/docs/whatsnew-eap.html) ·
[Changelog](https://github.com/JetBrains/kotlin/releases/tag/v2.4.20-Beta1)
