# Experimental feature flags — consolidated index

Status as of Kotlin 2.4.10 (stable) / 2.4.20-Beta1 (EAP), July 2026. Flags are passed via
`compilerOptions { freeCompilerArgs.add("-X...") }` in Gradle, `<arg>` in kotlin-maven-plugin, or
`@file:CompilerOptions` in `.main.kts` scripts.

## Language feature flags — still needed in 2.4.x

| Flag | Since | Status | Feature |
|---|---|---|---|
| `-Xcollection-literals` | 2.4.0 | Experimental | Collection literals `["a", "b"]` with `operator fun of` |
| `-Xintrinsic-const-evaluation` | 2.4.0 | Experimental | Extended compile-time constant evaluation |
| `-Xallow-returns-result-of` | 2.4.0 | Experimental | `returnsResultOf` contract (+ `@OptIn(ExperimentalContracts::class)`) |
| `-Xexplicit-context-arguments` | 2.4.0 | Experimental in 2.4.0/2.4.10; Stable in 2.4.20-Beta1 | Explicit context arguments at call sites |
| `-Xname-based-destructuring=only-syntax\|name-mismatch\|complete` | 2.3.20 | Experimental (syntax-only Stable planned 2.5) | Name-based destructuring |
| `-Xreturn-value-checker=check\|full` | 2.3.0 | Experimental | Unused return value checker (`@MustUseReturnValues`, `@IgnorableReturnValue`, `val _ =`) |
| `-Xcontext-sensitive-resolution` | 2.2.0 | Experimental (improved in 2.3.0) | Omit type name for enum entries / sealed subtypes |
| `-Xdata-flow-based-exhaustiveness` | 2.2.20 | Stable in 2.3.0 — flag obsolete | Data-flow-based `when` exhaustiveness |
| `-Xallow-reified-type-in-catch` | 2.2.20 | preview | Reified type parameters in `catch` clauses |
| `-Xallow-contracts-on-more-functions` | 2.2.20 | Experimental | Contracts on property accessors / operators, generics in contracts |
| `-Xallow-condition-implies-returns-contracts` | 2.2.20 | Experimental | `(cond) implies (returnsNotNull())` contracts |
| `-Xallow-holdsin-contract` | 2.2.20 | Experimental | `holdsIn` contract keyword for lambda conditions |
| `-Xwhen-expressions=indy` | 2.2.20 | Default for JVM target 21+ since 2.4.20-Beta1 | `when` via `invokedynamic` type switch |
| `-Xnullability-annotations=@io.vertx.codegen.annotations:strict` | 2.3.20 | Stable mechanism | Vert.x `@Nullable` recognition |
| `-Xwarning-level=NAME:(error\|warning\|disabled)` | 2.2.0 | Experimental | Per-diagnostic warning severity |
| `-Xsuppress-warning=NAME` | 2.1.0 | Experimental | Global warning suppression |
| `-Wextra` | 2.1.0 | Stable | Extra compiler checks (`CAN_BE_VAL`, `UNUSED_VARIABLE`, …) |
| `-Xannotations-in-metadata` | 2.2.0 | Default since 2.4.0 — flag obsolete | Annotations in Kotlin metadata |
| `-Xjvm-expose-boxed` | 2.2.0 | Experimental | Expose boxed value classes to Java (`@JvmExposeBoxed`) |
| `-Xexplicit-backing-fields` | 2.3.0 | Stable in 2.4.0 — flag obsolete | Explicit backing fields (`field =`) |
| `-Xrepl` | 2.2.0 | — | Re-enable deprecated REPL |

## Flags fully superseded (feature became stable / default)

| Flag | Introduced | Feature stable since |
|---|---|---|
| `-Xcontext-parameters` | 2.2.0 | 2.4.0 (context parameters) |
| `-Xannotation-target-all` | 2.2.0 | 2.4.0 (`@all` meta-target) |
| `-Xannotation-default-target=param-property` | 2.2.0 | 2.4.0 (new defaulting rules; `first-only` reverted old behavior) |
| `-Xnested-type-aliases` | 2.2.0 | 2.3.0 |
| `-Xallow-return-in-expression-body` | 2.2.20 | 2.3.0 (`return` in expression bodies) |
| `-Xwhen-guards` | 2.1.0 | 2.2.0 (guard conditions) |
| `-Xnon-local-break-continue` | 2.1.0 | 2.2.0 |
| `-Xmulti-dollar-interpolation` | 2.1.0 | 2.2.0 |
| `-Xconsistent-data-class-copy-visibility` | 2.0.20 | default behavior in later 2.x (`@ConsistentCopyVisibility` / `@ExposedCopyVisibility` remain) |
| `-Xlambdas=indy` | 1.5.0 | default since 2.0.0 (`-Xlambdas=class` opts out) |
| `-Xuse-k2` | 1.7.0 | K2 default since 2.0.0; flag removed |
| `-Xenable-builder-inference` | 1.5.30 era | 1.7.0 |
| `-Xcontext-receivers` | 1.6.20 | never stabilized; deprecated 2.0.20, **removed 2.3.20** — use context parameters |
| `-Xjvm-default=...` | pre-2.2 | replaced by stable `-jvm-default=enable\|no-compatibility\|disable` in 2.2.0 |

## Platform-specific experimental flags (2.2–2.4)

| Flag | Since | Platform | Purpose |
|---|---|---|---|
| `-Xes-long-as-bigint` | 2.2.20 | JS | `Long` as `BigInt` (ES2020); 2.3.0 adds `LongArray` as `BigInt64Array` |
| `-XXLanguage:+JsAllowLongInExportedDeclarations` | 2.2.20 | JS | `Long` in `@JsExport` |
| `-Xenable-suspend-function-exporting` | 2.3.0 | JS | Export `suspend` functions as JS async |
| `-Xenable-implementing-interfaces-from-typescript` | 2.3.20 | JS | Implement `@JsExport`ed interfaces from TS |
| `-Xir-per-file` | 2.0.0 | JS | Per-file compilation |
| `-Xwasm-use-new-exception-proposal` | 2.0.0 | Wasm | New exception-handling proposal (default for `wasmWasi` since 2.3.0) |
| `-Xwasm-kclass-fqn` | 2.2.20 | Wasm | Allow `KClass.qualifiedName` (default since 2.3.0) |
| `-Xwasm-generate-dwarf` | 2.1.20 | Wasm | DWARF debug info |
| `-Xwasm-attach-js-exception` | 2.1.0 | Wasm | JS exception details via `JsException` |
| `-Xklib-ir-inliner=disabled\|full` | 2.4.0 | klib targets | Control intra-/cross-module klib inlining |
| `-Xpartial-linkage-loglevel=INFO\|WARNING\|ERROR` | 2.4.0 | klib targets | Partial-linkage log level (`-Xpartial-linkage` deprecated) |
| `-Xccall-mode=direct` | 2.3.20 | Native | New C/Obj-C interop mode (KT-83218) |
| `-Xbinary=preCodegenInlineThreshold=40` | 2.1.20 | Native | Pre-codegen inlining pass |
| `-Xoverride-konan-properties=minVersion.ios=…` | 2.3.0 | Native | Override raised Apple minimum targets (unsupported) |
| `-Xbackend-threads=N` | 1.6.20 | JVM | Parallel compilation of one module |
| `-Xdebug` | 1.8.0 | JVM | Disable optimizations for debugging |
| `-Xstring-concat=indy-with-constants\|indy\|inline` | 1.4.20 | JVM | String concatenation strategy (indy default since 1.5.20) |
| `-Xsam-conversions=class` | 1.5.0 | JVM | Legacy SAM adapter generation |
| `-Xjspecify-annotations=strict` | 1.5.20 | JVM | JSpecify severity (strict default since 2.1.0 via `-Xnullability-annotations`) |

## Opt-in annotations for experimental stdlib APIs

| Opt-in | API | Status |
|---|---|---|
| `@OptIn(ExperimentalUuidApi::class)` | `kotlin.uuid.Uuid` | core Stable in 2.4.0; V4/V7 generators still Experimental |
| `@OptIn(ExperimentalAtomicApi::class)` | `kotlin.concurrent.atomics` (2.1.20+), `update()`/`fetchAndUpdate()` (2.2.20+) | Experimental |
| `@OptIn(ExperimentalTime::class)` | `kotlin.time.Clock` / `Instant` (2.1.20) | Stable in 2.3.0 |
| `@OptIn(ExperimentalStdlibApi::class)` | `copyOf {}` (2.2.20), `Map.Entry.copy()` (2.3.20), map fallback functions (2.4.0), `enumEntries<T>()` (pre-2.0) | varies |
| `@OptIn(ExperimentalStdlibCoroutineSupportApi::class)` | `StackTraceRecoverable` (2.4.20-Beta1) | Experimental |
| `@OptIn(ExperimentalContracts::class)` | contracts; extended via `ExperimentalExtendedContracts` (2.2.20) | Experimental |
| `@OptIn(ExperimentalVersionOverloading::class)` | `@IntroducedAt` (2.4.0) | Experimental |
| `@OptIn(ExperimentalEncodingApi::class)` | Base64 | Stable in 2.2.0 — opt-in obsolete |
| `@OptIn(ExperimentalPathApi::class)` | `copyToRecursively()`, `deleteRecursively()` | still Experimental |
| `@OptIn(ExperimentalForeignApi::class)` / `@OptIn(BetaInteropApi::class)` | `kotlinx.cinterop` | cinterop Beta since 2.3.0 |

Machine-readable schema of all compiler options: `org.jetbrains.kotlin:kotlin-compiler-arguments-description`
(since 2.2.20). Current CLI reference: <https://kotlinlang.org/docs/compiler-reference.html>.
