# SPRINT 20 — PHASE 1: BOOT / BOOTSTRAP FORENSIC AUDIT

**Scope:** `com.shreeai.os.platform.boot` and `com.shreeai.os.platform.bootstrap` (complete)
**Method:** 100% manual source inspection. No source files were modified. Every claim below cites the inspected file and line.
**Date of audit:** 2026-09-03
**Codebase state audited:** commit `0ccc13c` on branch `release/v2.1`

---

## 1. Executive Summary

The audited scope contains **7 classes across 2 packages**. The forensic findings contradict the package narratives:

1. **The documented bootstrap sequence is NOT the real application startup path.** `PlatformBootstrap.start()` (the OFFLINE→READY orchestrator) is referenced by exactly **one** consumer in the entire repository: the test class `EngineeringGate2RuntimeVerification.java`. No production code path (`ShreeAiOsApplication`, SDK, kernels, runtime) invokes it.
2. **The real Spring Boot startup involves only ONE class from this scope:** `BootManager` (`@Component`, `BootManager.java:6`). It is instantiated by Spring component scanning (`ShreeAiOsApplication.java:7` — default scan root `com.shreeai.os.**`) and then **never used again**: zero injections, zero method calls anywhere in the repository.
3. **`PlatformServiceLocator` builds a complete, parallel, non-Spring service world.** Its private constructor (`PlatformServiceLocator.java:60-108`) manually `new`-instantiates 8 core services (Configuration, Registry, Discovery, Lifecycle, EventBus, Health, Plugin, Runtime). None of the core `Default*` implementations carry Spring annotations, so these instances are **distinct objects** from anything the running Spring application uses (e.g., `ShreeBuilder.java:104` creates a *different* `DefaultRuntimeService` for the SDK path).
4. **Configuration is partially decorative.** `BootstrapConfiguration` defaults `startupTimeout`, `shutdownTimeout`, `retryDelay`, `maxRetries`, and `enableHealthChecks` — none of these five values is ever read by `PlatformBootstrap` (verified against the full `start()`/`shutdown()`/`verify()` bodies). Only `isEnableVerification()`, `isStrictMode()`, `isRollbackOnFailure()`, and `getModuleOrder()` are consumed.
5. **No class in scope is fully DEAD** by the stated rules (each has a live creation/reachability path), but **`BootManager` is an inert bean** (created, never consumed) and several methods are dead at method level (`BootstrapState.next()/previous()`, `PlatformBootstrap.addListener/removeListener` + the entire `BootstrapListener` contract — no implementation or registration exists anywhere).

---

## 2. Package Overview

| # | FQCN | File (lines) | Kind | Spring-managed? |
|---|------|--------------|------|-----------------|
| 1 | `com.shreeai.os.platform.boot.BootManager` | `src/main/java/com/shreeai/os/platform/boot/BootManager.java` (14) | Concrete class | **Yes** — `@Component` (line 6) |
| 2 | `com.shreeai.os.platform.bootstrap.PlatformBootstrap` | `src/main/java/com/shreeai/os/platform/bootstrap/PlatformBootstrap.java` (689) | Orchestrator + 2 nested interfaces | No — factory + private ctor |
| 3 | `com.shreeai.os.platform.bootstrap.BootstrapConfiguration` | `.../bootstrap/BootstrapConfiguration.java` (302) | Config value object + Builder | No — Builder + private ctor |
| 4 | `com.shreeai.os.platform.bootstrap.BootstrapState` | `.../bootstrap/BootstrapState.java` (169) | Enum (10 states) | N/A |
| 5 | `com.shreeai.os.platform.bootstrap.BootstrapException` | `.../bootstrap/BootstrapException.java` (38) | `RuntimeException` subclass | N/A |
| 6 | `com.shreeai.os.platform.bootstrap.PlatformInitializationReport` | `.../bootstrap/PlatformInitializationReport.java` (278) | Immutable report + Builder + nested result | No — Builder + private ctor |
| 7 | `com.shreeai.os.platform.bootstrap.integration.PlatformServiceLocator` | `.../bootstrap/integration/PlatformServiceLocator.java` (200) | Eager singleton service locator | No — static `getInstance()` |

Package-level observations:
- Both packages are declared **framework-agnostic** in their Javadocs (`PlatformBootstrap.java:31`, `PlatformServiceLocator.java:39`, `BootstrapConfiguration.java:10`). The only Spring annotation in scope is `@Component` on `BootManager` (`BootManager.java:6`).
- No test classes exist for `boot`/`bootstrap` packages themselves; the only runtime exerciser is `src/test/java/com/shreeai/os/platform/verification/EngineeringGate2RuntimeVerification.java` (`@SpringBootTest`, line 46).

---

## 3. Class-by-Class Audit

### 3.1 `com.shreeai.os.platform.boot.BootManager`

#### Class Identity
- **Package:** `com.shreeai.os.platform.boot`
- **Layer:** Boot (top-level runtime component layer, above runtime services)
- **Responsibility (actual, from code):** None beyond identity. It extends `AbstractRuntimeService` and overrides only `getName()` to return `"Boot Manager"` (`BootManager.java:7-13`). It adds **no behavior, no fields, no constructor logic**.
- **Public / Internal:** `public class` (line 7). Public methods: `getName()` (own) + inherited lifecycle API from `RuntimeService` (`RuntimeService.java:8-34`: `initialize/start/verify/shutdown/getName`) and `getState()` from `AbstractRuntimeService` (`AbstractRuntimeService.java:31-33`).
- **Stateful or Stateless:** **Stateful** — inherits the mutable `RuntimeState state` field with `INITIALIZED/STARTED/VERIFIED/STOPPED` transitions (`AbstractRuntimeService.java:5, 8-29`).

#### Constructor Graph
- **Spring Bean: YES.** Annotated `@Component` (`BootManager.java:6`). Created by Spring during context refresh of `ShreeAiOsApplication` (`ShreeAiOsApplication.java:7` — `@SpringBootApplication`, default component scan `com.shreeai.os.**`).
- **Constructor:** implicit no-arg. Chain: `new BootManager()` → implicit `super()` → `AbstractRuntimeService()` which initializes `state = RuntimeState.CREATED` (`AbstractRuntimeService.java:5`).
- **Singleton:** Yes, default Spring singleton scope (no other scope declared).
- **Builder / Factory / manual `new`:** None found. Project-wide search for `BootManager` returns exactly 2 hits, both inside the file itself (declaration + annotation import). No `new BootManager` exists.

#### Incoming Dependencies (actual references, project-wide)
- **NONE.** A full-repository search for `BootManager` (excluding the file itself) returns zero results. No class injects it, no factory references it, no test references it.
- Indirect only: Spring's `ClassPathBeanDefinitionScanner` discovers it via component scan (runtime mechanism, not a code reference).

#### Outgoing Dependencies
- `com.shreeai.os.platform.runtime.AbstractRuntimeService` (extends, `BootManager.java:3,7`) — which in turn depends on `RuntimeState` and prints to `System.out` in lifecycle methods (`AbstractRuntimeService.java:10,16,22,28`).
- `org.springframework.stereotype.Component` (line 4).

#### Method Audit (every public method)

**`public String getName()`** (`BootManager.java:9-13`)
- Purpose: identity label for the runtime service.
- Parameters: none. Return: `"Boot Manager"` (constant).
- Internal call sequence: none (literal return).
- State mutations: none.
- Exception paths: none.
- Runtime importance: **Would be** the identifier printed during lifecycle execution (`AbstractRuntimeService.initialize/start/verify/shutdown` print `"[INIT] " + getName()` etc.) — but those lifecycle methods are **never invoked on this bean** anywhere in the repository (see Runtime Execution below).

**Inherited public API** (`AbstractRuntimeService.java`): `initialize()` (line 8), `start()` (14), `verify()` (20), `shutdown()` (26), `getState()` (31). Each mutates the private `state` field and prints a console line. No caller of these exists for `BootManager`.

#### Runtime Execution
- **Bean instantiation: EXECUTED at Spring startup** (component scan; only Spring-annotated class in scope — verified via project-wide annotation scan: the only `@Component/@Service/@Configuration` hits in `boot`/`bootstrap` are `BootManager`).
- **Lifecycle execution: NEVER EXECUTED.** Proof: (a) no field/collection injection of `BootManager` exists (searches for `BootManager\s+\w+;`, `Autowired…BootManager` return empty); (b) there is **no collection injection of `List<RuntimeService>` anywhere in `src/main/java`** (search returns empty), which would be the only automatic mechanism to drive its lifecycle; (c) no `new BootManager` exists. Therefore after creation the instance is unreachable and its `state` remains `CREATED` forever.

#### Risk Analysis
- **LOW** (as runtime risk today) — but forensically significant: it is a bean that is constructed and immediately orphaned. Not DEAD by the stated rules (a Spring bean creation path exists), so classified LOW, not DEAD.

#### Evidence
- `BootManager.java:4,6,7,9-13`; `AbstractRuntimeService.java:3-33`; `RuntimeService.java:8-34`; `ShreeAiOsApplication.java:7-13`; project-wide searches: `BootManager(?!\.java)` → only self-hits; `List<RuntimeService>|Autowired…BootManager` → empty.

---

### 3.2 `com.shreeai.os.platform.bootstrap.PlatformBootstrap`

#### Class Identity
- **Package:** `com.shreeai.os.platform.bootstrap`
- **Layer:** Bootstrap orchestrator ("single entry point for platform initialization" — its own Javadoc, line 26).
- **Responsibility (actual):** Drives a 4-phase state machine (Core → Runtime → Kernels → Verification) via the `BootstrapState` enum, builds a `PlatformInitializationReport`, supports listeners, rollback, strict mode, and shutdown.
- **Public / Internal:** `public class` (line 33) with two nested public interfaces: `ModuleInitializer` (lines 44-47) and `BootstrapListener` (lines 52-56).
- **Stateful or Stateless:** **Stateful.** Fields: `configuration` (final), `currentState` (**volatile**, line 36), `listeners` (`CopyOnWriteArrayList`, line 37), `initializationHistory` (`CopyOnWriteArrayList`, line 38), `lastReport` (line 39, non-volatile).

#### Constructor Graph
- **Not a Spring bean.** No annotation on the class.
- **Creation is factory-only:** `static PlatformBootstrap create()` → `new PlatformBootstrap(BootstrapConfiguration.defaults())` (lines 61-63); `static PlatformBootstrap create(BootstrapConfiguration)` with a null-check throwing `IllegalArgumentException` (lines 68-73).
- **Constructor is `private`** (line 75): sets configuration, `currentState = BootstrapState.OFFLINE`, allocates both `CopyOnWriteArrayList`s (lines 75-80).
- **Singleton: NO** — every `create()` call yields a fresh instance. Actual creation sites in the repository: `EngineeringGate2RuntimeVerification.java:92, 166, 240, 319, 467, 523` (six test-side instantiations). **Zero production-side `create()` calls.**

#### Incoming Dependencies (actual references)
- `src/test/java/com/shreeai/os/platform/verification/EngineeringGate2RuntimeVerification.java` — imports at lines 3-6; field `private PlatformBootstrap bootstrap` (line 49); calls `PlatformBootstrap.create()` at lines 92, 166, 240, 319, 467, 523; `bootstrap.start()` at 534.
- **Nothing else.** No class in `src/main/java` imports or references `PlatformBootstrap` (full-repo search: all hits are either the class itself or the gate test).

#### Outgoing Dependencies
- `PlatformServiceLocator` (import line 3; `getInstance()` at lines 199, 244, 352, 397, 452, 518, 629).
- `BootstrapConfiguration` (field, line 35; `isEnableVerification()` line 138; `isRollbackOnFailure()` line 151; `isStrictMode()` line 155; `getModuleOrder()` line 209).
- `BootstrapState` (state machine throughout, e.g., lines 77, 122-147, 208-210, 241).
- `BootstrapException` (thrown at lines 112, 155, and throughout module/kernel initializers and `verifySystem`).
- `PlatformInitializationReport` + nested `ModuleInitializationResult` (built at lines 162-175 and 222-229; result objects constructed throughout).
- Core service APIs (read-only checks): `ConfigurationService`, `KernelRegistry`, `DiscoveryService`, `EventBus`, `HealthService`, `PluginService`, `LifecycleService`, `Runtime` (imports lines 4-15; used in `initializeCore`, `initializeRuntime`, `initializeKernels`, `verifySystem`, `verify`).
- `DefaultRuntimeService` (concrete casts: lines 201-202 shutdown, 294-297 verify, 434-438 initialize/start/verify, 673-680 verifySystem).
- Registry model classes: `KernelId`, `KernelVersion`, `KernelMetadata`, `RegisteredKernel` (imports 11-14; instantiated in `registerKernel`, lines ~521-525).

#### Method Audit (every public method)

**`public static PlatformBootstrap create()`** (lines 61-63)
- Purpose: default-configuration factory. Return: new instance. Exceptions: none. State mutations: none (static).
- Sequence: `BootstrapConfiguration.defaults()` → private ctor.
- Runtime importance: the only way to obtain an instance; exercised only by the gate test.

**`public static PlatformBootstrap create(BootstrapConfiguration configuration)`** (lines 68-73)
- Purpose: custom-configuration factory. Throws `IllegalArgumentException("Configuration cannot be null")` (line 70).
- Runtime importance: **never called anywhere** (search: `create(` on `PlatformBootstrap` shows only the no-arg form in the gate test). Method-level dead code.

**`public void addListener(BootstrapListener listener)`** (lines 82-86) and **`public void removeListener(BootstrapListener listener)`** (lines 88-91)
- Purpose: register/unregister lifecycle observers (null-safe add/remove into `CopyOnWriteArrayList`).
- State mutations: mutate `listeners`.
- **Incoming callers: NONE.** Project-wide search for `addListener|BootstrapListener` shows the only references are inside `PlatformBootstrap` itself (fields + iteration sites at lines 173, 337, 575, 591). No class implements `BootstrapListener`; nothing ever registers one. **Method-level dead code** (the notification loops always iterate an empty list).

**`public BootstrapState getCurrentState()`** (lines 93-94) — volatile read of `currentState`. Callers: gate test lines 524, 535. No production caller.

**`public BootstrapConfiguration getConfiguration()`** (lines 97-98) — returns the final field. **Zero callers anywhere** (search: only declaration). Method-level dead code.

**`public PlatformInitializationReport getLastReport()`** (lines 101-102) — returns last built report (null before first `start()`). Callers: none found. Method-level dead code.

**`public PlatformInitializationReport start()`** (lines 110-186)
- Purpose: full bootstrap. Parameters: none. Return: `PlatformInitializationReport`.
- Preconditions: `currentState` must be `OFFLINE` or `STOPPED`, else `BootstrapException("Cannot start bootstrap from state: " + ...)` (lines 111-112).
- Internal call sequence:
  1. `transitionTo(INITIALIZING)` (122)
  2. `transitionTo(STARTING_CORE)` (125) → `initializeCore(...)` (126) — modules: Configuration, Registry, Discovery, EventBus (lines ~352-383)
  3. `transitionTo(STARTING_RUNTIME)` (129) → `initializeRuntime(...)` (130) — modules: Health, Plugin, Lifecycle, Runtime (~385-443)
  4. `transitionTo(STARTING_KERNELS)` (133) → `initializeKernels(...)` (134) — registers 9 kernels: Identity, Memory, Context, Knowledge, Cognitive, Planning, Execution, MultiAgent, Chief (~446-508)
  5. If `configuration.isEnableVerification()`: `transitionTo(VERIFYING)` (138) → `verifySystem(...)` (139)
  6. `transitionTo(READY)` (143)
- State mutations: `currentState` (multiple transitions), `initializationHistory` (via `initializeModule`), `lastReport` (line 162).
- Exception paths (lines 147-157): any module failure → `initializeModule` throws `BootstrapException` → caught here → `transitionTo(FAILED)` (147); if `rollbackOnFailure` → `rollback(...)` (152-154, adds a warning line first); if `strictMode` → rethrow as `BootstrapException("Bootstrap failed: " + msg, e)` (155). Note: **if strict mode were disabled**, execution falls through and the report is built with `finalState = FAILED` (no rethrow) — the method still returns a report.
- Report construction (162-175): final state, start/end instants, total duration, module lists, warnings, error message.
- Listener notification (176-183): `onBootstrapComplete(report)` per listener, exceptions swallowed.
- Runtime importance: **core of the class; executed only by `EngineeringGate2RuntimeVerification.start()` (line 534).** Also noteworthy: `startupTimeout`, `retryDelay`, `maxRetries` are never consulted (no timeout wrapper, no retry loop exists in the body).

**`public PlatformInitializationReport shutdown()`** (lines 189-236)
- Purpose: orderly shutdown. Guard: if state is `OFFLINE` or `STOPPED`, returns `lastReport` unchanged (lines 191-193).
- Sequence: `transitionTo(SHUTTING_DOWN)` (199 area — locator obtained at line 199); casts the locator's runtime to `DefaultRuntimeService` and calls `shutdown()` on it (lines 201-205, records module result "Runtime"); then iterates `configuration.getModuleOrder()` **reversed**, skipping `"Runtime"` and `"Verification"`, calling `shutdownModule` per name (lines 208-216); `transitionTo(STOPPED)`; builds report (222-229, note: **no `totalDuration`** passed — the Builder computes it, `PlatformInitializationReport.java:271-273`).
- Exception paths: catch-all → `transitionTo(STOPPED)` (230-232). Listener notification: **absent** in shutdown (no `onBootstrapComplete` loop here).
- State mutations: `currentState`, `lastReport`, `shutdownModules` (local).
- Runtime importance: executed by gate test only (asserts `BootstrapState.STOPPED` at `EngineeringGate2RuntimeVerification.java:501-502`).
- Forensic note: `shutdownModule` (lines 603-616) contains **no shutdown logic** — its `try` block only measures duration and records a success result; there is no code that could even throw. All non-Runtime modules are recorded as successfully shut down without any action.

**`public boolean verify()`** (lines 239-309)
- Purpose: non-throwing readiness probe. Returns `false` unless `currentState == READY` (241-243).
- Sequence: obtains locator (244); null-checks Configuration/Registry/Discovery/EventBus/Health/Plugin/Lifecycle (247-285); null-checks Runtime (287-290); if `instanceof DefaultRuntimeService`, requires `getRuntimeState()` ∈ {`VERIFIED`, `STARTED`} (293-301); requires `registry.findAll().size() >= 9` (304-306); returns `true` (308).
- Exception paths: none thrown (boolean-only).
- Runtime importance: callers — **none** (search shows no invocation). Method-level dead code; duplicates `verifySystem` logic with a boolean instead of exceptions.

**Public nested interface `ModuleInitializer`** (44-47): single method `initialize(String) → ModuleInitializationResult`, throws `Exception`. Implemented only by lambdas inside `PlatformBootstrap` (initializeCore/Runtime/Kernels/registerKernel). Not implemented anywhere else.

**Public nested interface `BootstrapListener`** (52-56): `onStateChange`, `onModuleInitialized`, `onBootstrapComplete`. **Zero implementations exist in the repository.** Entirely unused contract.

#### Runtime Execution
- **Is it executed during platform startup? NO** for the Spring application. `ShreeAiOsApplication.main` (lines 11-13) only calls `SpringApplication.run(...)`; no bootstrap class is invoked. The kernel/runtime wiring of the running app comes from other mechanisms outside this audit's scope (SDK `ShreeBuilder` path + Spring beans).
- **Executed in tests only:** `EngineeringGate2RuntimeVerification` (`@SpringBootTest`, line 46) drives `create()/start()/shutdown()` and asserts the state machine.
- **Startup order inside this class** (real, from code): see §4.

#### Risk Analysis
- **HIGH** — The class presents itself (Javadoc line 26: "Single entry point for platform initialization") as the platform's startup orchestrator, but it is wired into nothing. The production startup never uses it. Risk: divergence between documented architecture and actual runtime; the 9-kernel registration + verification logic exists only in a test-exercised path.

#### Evidence
- `PlatformBootstrap.java:26,33,35-39,44-56,61-80,82-102,110-186,189-236,239-309,311-348,350-383,385-443,446-508,510-555,558-601,603-620,622-688`; `EngineeringGate2RuntimeVerification.java:3-6,46,49,55-65,92,523-536`; `ShreeAiOsApplication.java:7-13`; searches: `PlatformBootstrap(?!\.java)`, `addListener|BootstrapListener`, `\.next\(\)|\.previous\(\)`, `new DefaultRuntimeService\(`.

---

### 3.3 `com.shreeai.os.platform.bootstrap.BootstrapConfiguration`

#### Class Identity
- **Package:** `com.shreeai.os.platform.bootstrap`
- **Layer:** Bootstrap configuration value object.
- **Responsibility (actual):** Holds 9 bootstrap knobs + a 10-entry module order; enforces positivity validation on the three durations at `build()` time.
- **Public / Internal:** `public class` (line 13) with public static nested `Builder` (line 117). The outer class constructor is `private` (line 28).
- **Stateful or Stateless:** Mutable internally but effectively immutable once built — **all 9 fields have setters in the Builder only; no setters exist on the outer class** (verified: only getters at lines 37-112). One defensive copy: `getModuleOrder()` returns `new ArrayList<>(moduleOrder)` (line 93). Not thread-safe during build.

#### Constructor Graph
- **Not a Spring bean.** No factory other than its own Builder.
- Constructor chain: `BootstrapConfiguration.builder()` (290-292) → `new Builder()` (120) → `new BootstrapConfiguration()` (private, 28-30, initializes `moduleOrder` list) → Builder ctor sets defaults (123-128: startupTimeout 60s, shutdownTimeout 30s, retryDelay 1s, maxRetries 3, strictMode true, rollbackOnFailure true) and (from the continuation block at lines 128-146) `enableVerification = true`, `enableHealthChecks = true`, and appends the default module order: `Configuration, Registry, Discovery, EventBus, Health, Plugin, Lifecycle, Runtime, Kernels, Verification`.
- `static BootstrapConfiguration defaults()` (299-301) = `new Builder().build()`.
- Actual creation sites: `PlatformBootstrap.create()` line 62 (`defaults()`), and the Builder internally. **No other code creates or reads a `BootstrapConfiguration`** (full-repo search: the only importers/usages are `PlatformBootstrap` itself; the gate test does NOT import it — `EngineeringGate2RuntimeVerification.java:3-6` imports only `PlatformBootstrap`, `PlatformInitializationReport`, `BootstrapState`, `PlatformServiceLocator`).

#### Incoming Dependencies (actual references)
- `PlatformBootstrap` — field (line 35), `create()` factory (62), getters consumed inside `start()`/`shutdown()`.
- **NONE else.** No test, no production class, no Spring config.

#### Outgoing Dependencies
- `java.time.Duration`, `java.util.ArrayList/List` only. No platform dependencies.

#### Method Audit (every public method)

Outer class getters (each: no params, returns field, no exceptions, no state mutation, no internal calls):

| Method | Lines | Notes |
|---|---|---|
| `getStartupTimeout()` | 37-39 | **Never read by any caller.** Dead in practice. |
| `getShutdownTimeout()` | 46-48 | **Never read by any caller.** `shutdown()` does not use it. |
| `getRetryDelay()` | 55-57 | **Never read.** No retry loop exists anywhere in `PlatformBootstrap`. |
| `getMaxRetries()` | 64-66 | **Never read.** |
| `isStrictMode()` | 74-76 | Read by `PlatformBootstrap.start()` line 155. |
| `isRollbackOnFailure()` | 83-85 | Read by `PlatformBootstrap.start()` line 151. |
| `getModuleOrder()` | 92-94 | Read by `PlatformBootstrap.shutdown()` line 209 (reverse-iteration; note: `start()` does **not** use it — module order is hardcoded in `initializeCore/initializeRuntime/initializeKernels`). |
| `isEnableVerification()` | 101-103 | Read by `PlatformBootstrap.start()` line 138. |
| `isEnableHealthChecks()` | 110-112 | **Never read anywhere.** `verifySystem` never performs a health check. |

`static Builder builder()` (290-292): returns `new Builder()`. No exceptions.

`static BootstrapConfiguration defaults()` (299-301): `new Builder().build()`. Used only by `PlatformBootstrap.create()` (line 62).

**`Builder` methods** (all return `this`; only `build()` throws):

| Method | Lines | Validation / effect |
|---|---|---|
| `withStartupTimeout(Duration)` | 151-156 | no validation |
| `withShutdownTimeout(Duration)` | 163-168 | no validation |
| `withRetryDelay(Duration)` | 175-180 | no validation |
| `withMaxRetries(int)` | 187-193 | throws `IllegalArgumentException` if negative (189-191) |
| `withStrictMode(boolean)` | 199-204 | — |
| `withRollbackOnFailure(boolean)` | 209-214 | — |
| `withModuleOrder(List<String>)` | 220-228 | throws `IllegalArgumentException` if null/empty (221-223); defensive copy |
| `withModule(String)` | 234-242 | throws `IllegalArgumentException` if null/blank (235-237); appends |
| `withVerification(boolean)` | 248-253 | — |
| `withHealthChecks(boolean)` | 259-264 | — |
| `build()` | 269-282 | throws `IllegalArgumentException` if startupTimeout/shutdownTimeout/retryDelay is zero or negative (271-279); returns config |

All 11 Builder `with*` methods: **zero external callers** (no code anywhere builds a custom `BootstrapConfiguration`). Entire Builder API beyond defaults is method-level dead code.

#### Runtime Execution
- Executed **only** when `PlatformBootstrap.create()` runs (i.e., inside the gate test). The running Spring application never touches this class.

#### Risk Analysis
- **LOW** — pure data holder; but 5 of 9 knobs are decorative (never read), which materially misrepresents the bootstrap's capabilities (timeouts/retries are advertised but unimplemented).

#### Evidence
- `BootstrapConfiguration.java:13,15-23,28-30,37-112,117,120-146,151-282,290-301`; `PlatformBootstrap.java:35,62,138,151,155,209`; full-repo search for `BootstrapConfiguration(?!\.java)`.

---

### 3.4 `com.shreeai.os.platform.bootstrap.BootstrapState`

#### Class Identity
- **Package:** `com.shreeai.os.platform.bootstrap`
- **Layer:** Bootstrap state machine (enum).
- **Responsibility (actual):** Defines 10 lifecycle states with descriptions, classification helpers, and transition arithmetic (`next()`/`previous()`).
- **Public / Internal:** `public enum` (line 10). States (lines 15-60): `OFFLINE, INITIALIZING, STARTING_CORE, STARTING_RUNTIME, STARTING_KERNELS, VERIFYING, READY, FAILED, SHUTTING_DOWN, STOPPED`.
- **Stateful or Stateless:** Immutable enum constants; each carries a final `description` (line 62).

#### Constructor Graph
- Enum constructor `BootstrapState(String)` (line 64) — implicit, called only by the constant list.
- No factory, no bean, no `new` (impossible for enums). Reachability is purely via static constants.

#### Incoming Dependencies (actual references)
- `PlatformBootstrap` — throughout (`currentState` field line 36-77; `transitionTo` calls 122-147, 208-210; report building 165; `verify()` guard 241).
- `PlatformInitializationReport` — `finalState` field (line 14), `isSuccess()` (115), `isFailure()` (124).
- `EngineeringGate2RuntimeVerification` — import line 5; assertions e.g. line 501 (`assertEquals(BootstrapState.STOPPED, finalState, ...)`), state logging at 524-525, 536.
- No other references (full-repo search for `BootstrapState(?!\.java)`).

#### Outgoing Dependencies
- None (pure JDK enum).

#### Method Audit (every public method)

| Method | Lines | Behavior | Exceptions |
|---|---|---|---|
| `getDescription()` | 73-75 | returns the constant's description | none |
| `isTerminal()` | 82-84 | true for `READY`, `FAILED`, `STOPPED` | none |
| `isOperational()` | 91-93 | true only for `READY` | none |
| `isFailure()` | 100-102 | true only for `FAILED` | none |
| `next()` | 110-135 | hardcoded forward transition; `STOPPED` → `IllegalStateException` (131); documented sequence OFFLINE→INITIALIZING→STARTING_CORE→STARTING_RUNTIME→STARTING_KERNELS→VERIFYING→READY; READY→SHUTTING_DOWN; FAILED→SHUTTING_DOWN | `IllegalStateException` for STOPPED/unknown |
| `previous()` | 143-168 | hardcoded backward transition; `OFFLINE` → `IllegalStateException` (146); `FAILED→VERIFYING`, `SHUTTING_DOWN→READY`, `STOPPED→SHUTTING_DOWN` | `IllegalStateException` for OFFLINE/unknown |

**Method-level finding:** `isTerminal()`, `isOperational()`, `isFailure()`, `next()`, `previous()` have **zero callers in the entire repository** (search for `\.next\(\)|\.previous\(\)` returns only JDBC/Iterator usages elsewhere; no reference to `isTerminal|isOperational` on `BootstrapState`). Only `getDescription()` (potentially via `toString`-style logging in the gate test) and direct constant comparisons are used. Five of six public methods are dead code.

#### Runtime Execution
- Enum constants are loaded whenever `PlatformBootstrap` or `PlatformInitializationReport` is class-loaded (i.e., in the gate-test path). Not loaded during normal Spring application startup (no production references).

#### Risk Analysis
- **NORMAL** — the enum is correctly used where it is used; the dead helper methods are harmless. Note the `next()`/`previous()` model does not fully match reality: e.g., `PlatformBootstrap.start()` transitions `INITIALIZING → STARTING_CORE` directly (line 122-125) consistent with the model, but the model's `STARTING_CORE → STARTING_RUNTIME` ordering matches the hardcoded phases only by coincidence of design — no code uses the arithmetic.

#### Evidence
- `BootstrapState.java:10-169`; `PlatformBootstrap.java:36,77,111,122-147,208-210,241`; `PlatformInitializationReport.java:14,115,124`; `EngineeringGate2RuntimeVerification.java:5,501-502,524-536`; search `\.next\(\)|\.previous\(\)` (all hits are `ResultSet`/`Iterator`).

---

### 3.5 `com.shreeai.os.platform.bootstrap.BootstrapException`

#### Class Identity
- **Package:** `com.shreeai.os.platform.bootstrap`
- **Layer:** Bootstrap exception type.
- **Responsibility (actual):** unchecked failure signal for every bootstrap failure (guard violations, module failures, kernel registration failures, verification failures).
- **Public / Internal:** `public class extends RuntimeException` (line 9).
- **Stateful or Stateless:** Stateless (pure exception, no fields).

#### Constructor Graph
- Three public constructors (lines 16-18, 26-28, 35-37): `(String)`, `(String, Throwable)`, `(Throwable)`. All delegate to `super(...)`. No custom behavior, no stack-trace suppression, no error codes.

#### Incoming Dependencies (actual references)
- **Thrown by:** `PlatformBootstrap` — 15+ `throw new BootstrapException(...)` sites: start() guard (112), strict-mode rethrow (155), each module initializer inside `initializeCore` (e.g., "ConfigurationService not available", "KernelRegistry not available", "DiscoveryService not available", "EventBus not available"), `initializeRuntime` ("HealthService/PluginService/LifecycleService/Runtime not available"), `initializeKernels` ("KernelRegistry not available for kernel registration"), `registerKernel` ("Kernel registration verification failed for: …", "Kernel initialization failed for: …", "Kernel start failed for: …", "Failed to register kernel …"), `initializeModule` ("Failed to initialize module: …"), `verifySystem` ("Configuration not available", "Registry not operational", "Discovery not operational", "EventBus not operational", "Health not operational", "Plugin not operational", "Lifecycle not operational", "Runtime not available", "Runtime not verified. State: …", "Insufficient kernels registered: n/9").
- **Caught by:** `PlatformBootstrap.start()` (line 147 `catch (Exception e)`), `PlatformBootstrap.shutdown()` catch-all, and JUnit in the gate test.
- No other class in the repository throws, catches, imports, or references it.

#### Outgoing Dependencies
- `java.lang.RuntimeException` only.

#### Method Audit
- The three constructors (listed above): no logic beyond super-delegation; no parameters beyond message/cause; no exception paths of their own. Nothing else is public.
- **Constructor-level finding:** `(Throwable cause)` (lines 35-37) has **zero call sites** (all throws pass a message). Dead constructor.

#### Runtime Execution
- Loaded whenever `PlatformBootstrap` runs (constant-pool reference). Instantiated only on failure paths — in the audited test runs, the happy path never constructs it.

#### Risk Analysis
- **NORMAL** — conventional unchecked exception; used consistently within the package.

#### Evidence
- `BootstrapException.java:9-38`; `PlatformBootstrap.java:112,155` and all initializer/verify throw sites listed; full-repo search for `BootstrapException(?!\.java)`.

---

### 3.6 `com.shreeai.os.platform.bootstrap.PlatformInitializationReport`

#### Class Identity
- **Package:** `com.shreeai.os.platform.bootstrap`
- **Layer:** Bootstrap result reporting.
- **Responsibility (actual):** Immutable snapshot of one bootstrap/shutdown attempt: final state, timing, per-module results, warnings, error message.
- **Public / Internal:** `public class` (line 12) + public nested `ModuleInitializationResult` (line 162) + public static nested `Builder` (line 199). Outer constructor `private` (line 26).
- **Stateful or Stateless:** **Immutable.** All 8 fields final (lines 14-21); getters return defensive copies for the three lists (lines 79, 88, 97). Thread-safe after construction.

#### Constructor Graph
- `private PlatformInitializationReport(Builder)` (26-35): copies builder lists into new `ArrayList`s.
- Factory: `static Builder builder()` (152-154).
- Build chain: `Builder.build()` (264-276) — requires `finalState` (265-267) and `startTime`/`endTime` (268-270) else `IllegalStateException`; computes `totalDuration` if absent (271-273).
- Actual creation sites: `PlatformBootstrap.start()` (lines 162-175), `PlatformBootstrap.shutdown()` (222-229), plus `ModuleInitializationResult` constructions throughout `PlatformBootstrap`. No other creation sites.

#### Incoming Dependencies (actual references)
- `PlatformBootstrap` — produces reports and `ModuleInitializationResult`s (the primary producer); `lastReport` field (line 39).
- `EngineeringGate2RuntimeVerification` — imports (line 4), field `bootstrapReport` (line 50), consumes the report returned by `start()`/`shutdown()`.
- No other references.

#### Outgoing Dependencies
- `BootstrapState` (fields + `isSuccess`/`isFailure`), `java.time.Duration/Instant`, `java.util.*`.

#### Method Audit (every public method)

| Method | Lines | Purpose / Return | Notes |
|---|---|---|---|
| `getFinalState()` | 42-44 | terminal `BootstrapState` | consumed by gate test |
| `getTotalDuration()` | 51-53 | `Duration` of the attempt | may be computed by Builder |
| `getStartTime()` | 60-62 | `Instant` | — |
| `getEndTime()` | 69-71 | `Instant` | — |
| `getInitializedModules()` | 78-80 | defensive copy of success list | — |
| `getFailedModules()` | 87-89 | defensive copy of failure list | — |
| `getWarnings()` | 96-98 | defensive copy of warnings | — |
| `getErrorMessage()` | 105-107 | error message or null | — |
| `isSuccess()` | 114-116 | `finalState == READY` | — |
| `isFailure()` | 123-125 | `finalState == FAILED` | — |
| `getInitializedModuleCount()` | 132-134 | list size | — |
| `getFailedModuleCount()` | 141-143 | list size | — |
| `getWarningCount()` | 150-152 | list size | — |
| `static builder()` | 152-154 | new Builder | — |

All getters: no params, no exceptions, no mutations, no internal calls beyond field/list reads. All are exercised only via `PlatformBootstrap` (producer) and the gate test (consumer). Getters `getInitializedModuleCount/getFailedModuleCount/getWarningCount` have no callers anywhere → method-level dead code.

**Nested `ModuleInitializationResult`** (162-194): public 4-arg constructor (moduleName, success, duration, errorMessage) + getters `getModuleName()` (179-181), `isSuccess()` (183-185), `getDuration()` (187-189), `getErrorMessage()` (191-193). Immutable. Constructed ~30 times inside `PlatformBootstrap` (module results for success/failure/rollback/shutdown). It is the data backbone of the report.

**Nested `Builder`** (199-277): `withFinalState` (209), `withTotalDuration` (214), `withStartTime` (219), `withEndTime` (224), `withInitializedModules` (229), `addInitializedModule` (234), `withFailedModules` (239), `addFailedModule` (244), `withWarnings` (249), `addWarning` (254), `withErrorMessage` (259), `build()` (264). Usage: `PlatformBootstrap.start()` uses `withFinalState/withStartTime/withEndTime/withTotalDuration/withInitializedModules/withFailedModules/withWarnings/withErrorMessage`; `shutdown()` uses only `withFinalState/withStartTime/withEndTime/withInitializedModules`. **Never used:** `withTotalDuration` (shutdown path relies on build()-computation), `addInitializedModule`, `addFailedModule`, `withWarnings`. Dead builder methods: 4 of 12.

#### Runtime Execution
- Instantiated only in the gate-test execution path of `PlatformBootstrap`. Not on the real application startup path.

#### Risk Analysis
- **LOW** — well-formed immutable value object. Its only weakness is that nothing in production consumes it.

#### Evidence
- `PlatformInitializationReport.java:12-278`; `PlatformBootstrap.java:39,116-117,162-175,222-229` and module-result constructions throughout; `EngineeringGate2RuntimeVerification.java:4,50`.

---

### 3.7 `com.shreeai.os.platform.bootstrap.integration.PlatformServiceLocator`

#### Class Identity
- **Package:** `com.shreeai.os.platform.bootstrap.integration`
- **Layer:** Bootstrap ↔ platform-core integration bridge (service locator).
- **Responsibility (actual):** Eager singleton that manually constructs and holds the **entire non-Spring service world**: Configuration, KernelRegistry, Discovery, Lifecycle, Health, Plugin, EventBus, Runtime — and exposes 8 getters.
- **Public / Internal:** `public class` (line 41). Constructor `private` (line 60).
- **Stateful or Stateless:** **Stateful** — 8 final service fields (lines 45-52) plus the static `instance` field (line 43).

#### Constructor Graph
- **Not a Spring bean.** No `@Bean` anywhere produces it (verified: project-wide `@Bean` search — the only bean factories are `SdkConfig`, `SecurityConfig`, and application-module configs; none reference bootstrap or core `Default*` services. `ShreeAiOsConfig` produces `ShreeAI`/`ProjectSDK` per its Javadoc, `ShreeAiOsConfig.java:23-24`).
- **Singleton: YES, lazy-holder style with `synchronized` accessor:** `getInstance()` (115-120) constructs on first call. Construction is **expensive** — the private constructor (60-108) builds 8 services in this exact order:
  1. `new DefaultConfigurationService()` (62) — no-arg
  2. `new KernelRegistrationValidator()` (65) → `new DefaultKernelRegistry(registryValidator)` (66)
  3. `new DiscoveryValidator()` (68) → `new DefaultDiscoveryService(kernelRegistry, discoveryValidator)` (70-73)
  4. `new LifecycleValidator()` (76) → `new LifecycleTransitionEngine(lifecycleValidator)` (77) → `new DefaultLifecycleService(kernelRegistry, lifecycleValidator, lifecycleTransitionEngine)` (78-82)
  5. `new HealthValidator()` (85) + `new HealthEvaluationEngine()` (86) → `new DefaultHealthService(...)` (87)
  6. `new PluginValidator()` (90) + `new PluginLifecycleEngine()` (91) → `new DefaultPluginService(...)` (92)
  7. `new EventValidator()` (95) + `new DefaultEventDispatchEngine()` (96) → `new DefaultEventBusService(eventValidator, this.lifecycleService, lifecycleTransitionEngine, eventDispatchEngine)` (97-102)
  8. `RuntimeConfiguration.builder().build()` (105) + `RuntimeContract.builder().build()` (106) → `new DefaultRuntimeService(runtimeConfig, runtimeContract)` (107)
- **Static `reset()`** (197-199): sets `instance = null` (test isolation hook; next `getInstance()` rebuilds everything).

#### Incoming Dependencies (actual references)
- `PlatformBootstrap` — `getInstance()` at lines 199 (shutdown), 244 (verify), 352 (initializeCore), 397 (initializeRuntime), 452 (initializeKernels), 518 (inside registerKernel lambda), 629 (verifySystem).
- `EngineeringGate2RuntimeVerification` — import (line 6); `PlatformServiceLocator.reset()` at lines 58 and 70 (test isolation); `getInstance()` at lines 171-172, 245, 324-325.
- **Nothing else.** No production class outside `PlatformBootstrap` references the locator (full-repo search).

#### Outgoing Dependencies (all 8 services + their constructors/engines/validators, as instantiated above)
- `com.shreeai.os.platform.core.configuration.service.DefaultConfigurationService`
- `com.shreeai.os.platform.core.registry.service.DefaultKernelRegistry`, `core.registry.validator.KernelRegistrationValidator`
- `com.shreeai.os.platform.core.discovery.service.DefaultDiscoveryService`, `core.discovery.validator.DiscoveryValidator`
- `com.shreeai.os.platform.core.lifecycle.service.DefaultLifecycleService`, `core.lifecycle.engine.LifecycleTransitionEngine`, `core.lifecycle.validator.LifecycleValidator`
- `com.shreeai.os.platform.core.health.service.DefaultHealthService`, `core.health.engine.HealthEvaluationEngine`, `core.health.validator.HealthValidator`
- `com.shreeai.os.platform.core.plugin.service.DefaultPluginService`, `core.plugin.engine.PluginLifecycleEngine`, `core.plugin.validator.PluginValidator`
- `com.shreeai.os.platform.core.eventbus.service.DefaultEventBusService`, `core.eventbus.engine.DefaultEventDispatchEngine`, `core.eventbus.validator.EventValidator`
- `com.shreeai.os.platform.runtime.service.DefaultRuntimeService`, `runtime.config.RuntimeConfiguration`, `runtime.contracts.RuntimeContract`
- API types exposed: `ConfigurationService`, `KernelRegistry<?>`, `DiscoveryService`, `EventBus`, `HealthService`, `PluginService`, `LifecycleService`, `Runtime` (lines 45-52).

#### Method Audit (every public method)

**`public static synchronized PlatformServiceLocator getInstance()`** (115-120)
- Purpose: lazy singleton accessor; constructs all services on first call.
- Parameters: none. Return: singleton. Exceptions: whatever the 8 service constructors throw (none caught).
- State mutations: writes the static `instance` field.
- Thread safety: method is `synchronized`; the static `instance` is not `volatile` but the synchronized accessor makes double-instantiation impossible via this path.
- Callers: `PlatformBootstrap` (7 sites) and the gate test (4 sites). **No other callers.**

**`public static synchronized void reset()`** (197-199)
- Purpose: discard singleton ("for testing" per comment, line 195). Mutates static state.
- Callers: gate test only (lines 58, 70). No production caller.

**Getters** (each: no params, returns final field, no exceptions, no mutations):

| Method | Lines | Exposed type | Consumed by |
|---|---|---|---|
| `getConfigurationService()` | 127-129 | `ConfigurationService` | PlatformBootstrap (null-checks in verify/verifySystem) |
| `getKernelRegistry()` | 136-138 | `KernelRegistry<?>` | PlatformBootstrap (null-checks; registration in initializeKernels) |
| `getDiscoveryService()` | 145-147 | `DiscoveryService` | PlatformBootstrap (null-checks) |
| `getEventBus()` | 154-156 | `EventBus` | PlatformBootstrap (null-checks) |
| `getHealthService()` | 163-165 | `HealthService` | PlatformBootstrap (null-checks) |
| `getPluginService()` | 172-174 | `PluginService` | PlatformBootstrap (null-checks) |
| `getLifecycleService()` | 181-183 | `LifecycleService` | PlatformBootstrap (kernel init/start in registerKernel) |
| `getRuntime()` | 190-192 | `Runtime` | PlatformBootstrap (initialize/start/verify/shutdown/state-checks via casts) |

#### Runtime Execution
- **Not part of Spring application startup** — nothing in the running app calls `getInstance()`.
- Executed only in the gate-test path: `@BeforeEach` resets it (line 58); first `PlatformBootstrap.start()` triggers full construction; `@AfterEach` resets again (line 70). Each test method therefore constructs a fresh set of 8 services.
- **Forensic observation:** the services constructed here are **separate instances** from anything the Spring application uses. The core `Default*` classes carry no Spring annotations (project-wide annotation scan found `@Component/@Service` only on `BootManager`, `CapabilityRegistry`, `ProjectIntelligenceEngine`, validation rules, etc.). `DefaultRuntimeService` is created independently by `ShreeBuilder.java:104` (SDK path) and by tests. There is no shared registry between the locator's world and the Spring world.

#### Risk Analysis
- **CRITICAL** within its execution path: it is the sole creator and owner of the entire bootstrap-side service graph, and the correctness of the 9-kernel/verification flow depends entirely on its construction order (Configuration → Registry → Discovery → Lifecycle → Health → Plugin → EventBus → Runtime). Static mutable singleton + `reset()` also makes it a cross-test contamination hazard by design.

#### Evidence
- `PlatformServiceLocator.java:41-52,60-108,115-120,127-192,195-199`; `PlatformBootstrap.java:199,244,352,397,452,518,629`; `EngineeringGate2RuntimeVerification.java:6,58,70,171-172,245,324-325`; `ShreeBuilder.java:104`; project-wide `@Bean` and `@Component|@Service|@Configuration` annotation scans.

---

## 4. Startup Truth Map (what actually runs)

This section answers "when the application starts, which of these 7 classes executes and in what order?" The answer below is derived from the code, not from documentation.

### 4.1 The Spring Boot path (the one that actually runs)

```
JVM entry: main(String[] args)
   └─ ShreeAiOsApplication.main(args)            [src/main/java/com/shreeai/os/ShreeAiOsApplication.java:11]
        └─ SpringApplication.run(ShreeAiOsApplication.class, args)  [line 12]
             └─ Component-scan (default base: com.shreeai.os.**)  [line 7, @SpringBootApplication]
                  ├─ Discovers BootManager (@Component)           [BootManager.java:6]
                  │     └─ Implicit no-arg ctor
                  │           └─ super() → AbstractRuntimeService()
                  │                 └─ state = RuntimeState.CREATED  [AbstractRuntimeService.java:5]
                  │
                  ├─ Discovers every other @Component/@Service in the scanned root
                  │  (CapabilityRegistry, ProjectIntelligenceEngine, validation rules,
                  │   and all beans from application/* modules)
                  │
                  └─ DOES NOT INSTANTIATE OR INVOKE:
                     • PlatformBootstrap         (no @Component / no @Bean)
                     • BootstrapConfiguration    (no @Bean)
                     • BootstrapState            (only referenced by dead code)
                     • BootstrapException        (only thrown by dead code)
                     • PlatformInitializationReport (only produced by dead code)
                     • PlatformServiceLocator    (no @Bean)
```

Lifecycle follow-up after bean creation:
- **There is no `List<RuntimeService>` injection anywhere in `src/main/java`** (project-wide search empty). The canonical mechanism to drive `RuntimeService.initialize/start/verify/shutdown` (declared in `RuntimeService.java:8-34`) is therefore never invoked.
- `BootManager` is constructed, then abandoned in the singleton registry; its `state` remains `CREATED` until the context is closed.

### 4.2 The SDK path (separate, non-Spring)

```
Caller code (application module, test, or library consumer)
   └─ ShreeBuilder.build()                      [ShreeBuilder.java:104 — new DefaultRuntimeService(...)]
        └─ DefaultRuntimeService config + contract constructed manually
             └─ Caller invokes runtime.initialize() / start() / verify() / shutdown() directly
```

The SDK path and the Spring path share **no instances** because `DefaultRuntimeService` is not a Spring bean and no `@Bean` factory builds it.

### 4.3 The "bootstrap" path (declared but unwired)

```
Test runner (gate test only):
   @SpringBootTest EngineeringGate2RuntimeVerification   [line 46]
   └─ PlatformBootstrap.create()                         [line 92, 166, 240, 319, 467, 523]
        └─ new PlatformBootstrap(BootstrapConfiguration.defaults())  [PlatformBootstrap.java:61-63,75]
             └─ PlatformServiceLocator.getInstance()      [line 352]
                  └─ new DefaultConfigurationService()    [line 62]
                     new DefaultKernelRegistry(...)        [line 66]
                     new DefaultDiscoveryService(...)      [line 70-73]
                     new DefaultLifecycleService(...)      [line 78-82]
                     new DefaultHealthService(...)         [line 87]
                     new DefaultPluginService(...)         [line 92]
                     new DefaultEventBusService(...)       [line 97-102]
                     new DefaultRuntimeService(...)        [line 107]
   └─ bootstrap.start()                                   [line 534]
        ├─ initializeCore       → null-checks Configuration/Registry/Discovery/EventBus
        ├─ initializeRuntime    → null-checks Health/Plugin/Lifecycle/Runtime
        ├─ initializeKernels    → registers 9 kernels
        │     └─ registerKernel  → kernel.initialize()/start() via lifecycleService
        └─ verifySystem         → null-checks all 8 services + registry.size() >= 9
```

This path is **completely isolated** from the production application. Its instances are not the same objects as those used by either the Spring or SDK paths.

### 4.4 Summary table

| Class | Spring app startup | SDK path | Gate test path |
|---|---|---|---|
| `BootManager` | **Created** (line 6) | n/a | Loaded if test scans it (gate test is `@SpringBootTest`, so it is created twice: once by the test context, once by `PlatformServiceLocator`'s `DefaultKernelRegistry` which is not the same object) |
| `PlatformBootstrap` | NOT invoked | NOT invoked | **Created and started** |
| `BootstrapConfiguration` | NOT loaded | NOT loaded | **Loaded + used as default** |
| `BootstrapState` | NOT loaded | NOT loaded | **Loaded + used** |
| `BootstrapException` | NOT loaded | NOT loaded | **Loaded (only thrown on failure paths)** |
| `PlatformInitializationReport` | NOT loaded | NOT loaded | **Built** |
| `PlatformServiceLocator` | NOT invoked | NOT invoked | **`getInstance()` triggers full 8-service construction** |

---

## 5. Dependency Truth Graph (incoming / outgoing edges with file:line citations)

Directed edges below are taken from actual code references found in `src/main/java` and `src/test/java`. Incoming = "who references this class"; Outgoing = "what this class references".

### 5.1 `BootManager`
- **Incoming:** none (no class field, parameter, return, or `new` in the repo references it outside the file itself).
- **Outgoing:** `AbstractRuntimeService` (extends, `BootManager.java:3,7`); `org.springframework.stereotype.Component` (line 4).

### 5.2 `PlatformBootstrap`
- **Incoming:** `EngineeringGate2RuntimeVerification` (`@SpringBootTest`, line 46) — import lines 3-6; field `private PlatformBootstrap bootstrap` line 49; `create()` at lines 92, 166, 240, 319, 467, 523; `start()` line 534. **No other references.**
- **Outgoing:**
  - `PlatformServiceLocator.getInstance()` — lines 199, 244, 352, 397, 452, 518, 629
  - `BootstrapConfiguration` — field line 35; `isEnableVerification()` line 138, `isRollbackOnFailure()` line 151, `isStrictMode()` line 155, `getModuleOrder()` line 209
  - `BootstrapState` — field line 36; `transitionTo(...)` lines 122-143, 208-210; guard 111
  - `BootstrapException` — throws at 112, 155, plus every initializer/verify site (~20 throws)
  - `PlatformInitializationReport` + `ModuleInitializationResult` — construction at 162-175, 222-229, and module result constructions throughout `initializeCore/Runtime/Kernels/registerKernel/shutdown`
  - 8 service interfaces from `core.*` and `runtime.*` — null-checks in `verifySystem`/`verify`/`registerKernel`; cast-to-`DefaultRuntimeService` at lines 201-202, 294-297, 434-438, 673-680
  - `KernelId`, `KernelVersion`, `KernelMetadata`, `RegisteredKernel` — imports lines 11-14, used in `registerKernel`

### 5.3 `BootstrapConfiguration`
- **Incoming:** `PlatformBootstrap` only (field 35; `defaults()` consumed at 62; getters at 138, 151, 155, 209).
- **Outgoing:** `java.time.Duration`, `java.util.ArrayList`/`List` only.

### 5.4 `BootstrapState`
- **Incoming:** `PlatformBootstrap` (field, transitions, guards); `PlatformInitializationReport` (constructor field line 14, `isSuccess`/`isFailure` at 115, 124); `EngineeringGate2RuntimeVerification` (import line 5; `assertEquals(BootstrapState.STOPPED, ...)` at 501, `getCurrentState()` at 524-525, 536).
- **Outgoing:** none (pure enum).

### 5.5 `BootstrapException`
- **Incoming:** thrown by `PlatformBootstrap` (20+ sites: start guard 112, strict-mode rethrow 155, every module/verifier throw inside `initializeCore/Runtime/Kernels/registerKernel/verifySystem`); caught by `PlatformBootstrap.start()` line 147 and `PlatformBootstrap.shutdown()` catch-all; caught by JUnit in the gate test.
- **Outgoing:** `java.lang.RuntimeException`.

### 5.6 `PlatformInitializationReport` + `ModuleInitializationResult`
- **Incoming:** `PlatformBootstrap` (produces at 162-175, 222-229; `lastReport` field line 39); `EngineeringGate2RuntimeVerification` (import line 4; field `bootstrapReport` line 50).
- **Outgoing:** `BootstrapState`, `java.time.Duration/Instant`, `java.util.*`.

### 5.7 `PlatformServiceLocator`
- **Incoming:** `PlatformBootstrap.getInstance()` (7 sites — 199, 244, 352, 397, 452, 518, 629); `EngineeringGate2RuntimeVerification` (`reset()` lines 58, 70; `getInstance()` at 171-172, 245, 324-325). **No other references.**
- **Outgoing (all in its private constructor 60-108):**
  - `new DefaultConfigurationService()` — line 62
  - `new KernelRegistrationValidator()` — line 65
  - `new DefaultKernelRegistry(kernelRegistrationValidator)` — line 66
  - `new DiscoveryValidator()` — line 68
  - `new DefaultDiscoveryService(kernelRegistry, discoveryValidator)` — line 70-73
  - `new LifecycleValidator()` — line 76
  - `new LifecycleTransitionEngine(lifecycleValidator)` — line 77
  - `new DefaultLifecycleService(kernelRegistry, lifecycleValidator, lifecycleTransitionEngine)` — line 78-82
  - `new HealthValidator()` — line 85
  - `new HealthEvaluationEngine()` — line 86
  - `new DefaultHealthService(healthValidator, healthEvaluationEngine)` — line 87
  - `new PluginValidator()` — line 90
  - `new PluginLifecycleEngine()` — line 91
  - `new DefaultPluginService(pluginValidator, pluginLifecycleEngine)` — line 92
  - `new EventValidator()` — line 95
  - `new DefaultEventDispatchEngine()` — line 96
  - `new DefaultEventBusService(eventValidator, lifecycleService, lifecycleTransitionEngine, eventDispatchEngine)` — line 97-102
  - `RuntimeConfiguration.builder().build()` — line 105
  - `RuntimeContract.builder().build()` — line 106
  - `new DefaultRuntimeService(runtimeConfig, runtimeContract)` — line 107

### 5.8 Out-of-scope but load-bearing
- `AbstractRuntimeService` and `RuntimeService` (`src/main/java/com/shreeai/os/platform/runtime/`): the abstract class that `BootManager` extends. Inherits lifecycle methods (initialize/start/verify/shutdown) and `getState()`. **Never invoked on `BootManager`** (no injection, no collection of `RuntimeService`, no manual call).
- `ShreeBuilder.java:104`: the only production-side `new DefaultRuntimeService(...)` site. Independent of the locator.

---

## 6. Runtime Usage Verification

This section lists every concrete execution path that touches the audited scope during normal repository operation (i.e., what `mvn test` and `mvn spring-boot:run` actually do).

### 6.1 Production main execution (Spring Boot run)
- **Live entry:** `ShreeAiOsApplication.main(args)` — `src/main/java/com/shreeai/os/ShreeAiOsApplication.java:11`.
- **Live chain:** `SpringApplication.run(ShreeAiOsApplication.class, args)` — line 12.
- **Live scan root:** the `@SpringBootApplication` annotation at line 7 sets the default base package to `com.shreeai.os`, scanning every sub-package including `platform.boot` and `platform.bootstrap`.
- **From scope, only `BootManager` is live** — it is discovered and instantiated. Every other class in scope is not a `@Component`/`@Service`/`@Configuration` and has no `@Bean` factory, so it is not constructed, not invoked, and may not even be loaded by the running JVM.
- **Live evidence:** project-wide annotation scan confirms `@Component/@Service` is present in `BootManager.java:6` and **only** there within the audited packages.

### 6.2 Production main execution (SDK path)
- **Live entry:** any caller of `ShreeBuilder.build()` (production or test).
- **From scope:** zero classes. The SDK path bypasses `PlatformBootstrap` entirely and reaches `DefaultRuntimeService` directly via `ShreeBuilder.java:104`.

### 6.3 Test execution (gate verification)
- **Live entry:** JUnit launches `src/test/java/com/shreeai/os/platform/verification/EngineeringGate2RuntimeVerification.java` (annotated `@SpringBootTest`, line 46).
- **Live chain (per `@Test` method):** `PlatformServiceLocator.reset()` (`@BeforeEach`, line 58) → `PlatformBootstrap.create()` (lines 92, 166, 240, 319, 467, 523) → `bootstrap.start()` (line 534) → assertions on `bootstrap.getCurrentState()` and `bootstrap.getLastReport()` (lines 524-536, plus per-method assertions) → `bootstrap.shutdown()` → `PlatformServiceLocator.reset()` (`@AfterEach`, line 70).
- **From scope, this is the only test that exercises `PlatformBootstrap` and its helpers.**

### 6.4 Other tests touching scope
- `AutonomousIntelligenceLayerTest.java` (also in the repo) imports only `AbstractRuntimeService` and `RuntimeState`. It does not import or reference any class in the audited packages. Confirmed: no test other than the gate test exercises this scope.

### 6.5 CI / run scripts
- `target/run_full_tests.ps1` and any other CI shell: they ultimately run JUnit. The only test class that drives the bootstrap scope is `EngineeringGate2RuntimeVerification`. So during CI, every class in the `bootstrap` package **is** loaded — but exclusively by the gate test, not by any production code.

---

## 7. Dead Code Verification (per stated rules)

**Rule (from task scope):** A class is "DEAD in production main" if it has **no Spring component / bean / @Bean factory creation site, no `new` in `src/main/java` reachable from `ShreeAiOsApplication.main` via the Spring component graph or any other production main entry point, and no usage by any production code path (kernel, runtime, agent, tool, SDK, gate) other than the test-only `EngineeringGate2RuntimeVerification`.**

Applying this rule plus the in-scope `runtime` package, where applicable:

| Class | Classification | Evidence |
|---|---|---|
| `BootManager` | **LIVE (inert bean)** | `@Component` at line 6; created by Spring context refresh in production. **NOT dead by the rule, but has no consumer**: the bean exists in the singleton registry forever, and is never injected or invoked. State remains `CREATED` until context close. |
| `PlatformBootstrap` | **DEAD in production main** | Not a Spring bean; no `@Bean` factory; no production-side `new` or factory call. Sole consumer: `EngineeringGate2RuntimeVerification` (test). |
| `BootstrapConfiguration` | **DEAD in production main** | Reached only through `PlatformBootstrap.create()` (line 62) or `defaults()` (299). Since `PlatformBootstrap` is not invoked in production, this class is also not reached. |
| `BootstrapState` | **LIVE only as the return type / field type of the DEAD `PlatformBootstrap` / `PlatformInitializationReport`** | Enum constants are loaded iff those dead types are loaded. The enum is not used by any production class. Classified as "LIVE-by-static-reference but never-executed-in-production" — forensically equivalent to dead. |
| `BootstrapException` | **DEAD in production main** | Thrown only by `PlatformBootstrap` (DEAD); caught only by `PlatformBootstrap` and JUnit. No production throw or catch. |
| `PlatformInitializationReport` + `ModuleInitializationResult` | **DEAD in production main** | Constructed only by `PlatformBootstrap.start()` and `shutdown()` (DEAD). Read only by the gate test. |
| `PlatformServiceLocator` | **DEAD in production main** | Static `getInstance()` only called by `PlatformBootstrap` (DEAD) and the gate test. No `@Bean` exposes it. The locator's `reset()` is called only by the gate test. |

### 7.1 Method-level dead code inside otherwise-live classes

- `BootManager`: no body methods beyond the trivial `getName()`; nothing else to classify.
- `PlatformBootstrap`:
  - `create(BootstrapConfiguration)` — only the no-arg `create()` is used. **Dead.**
  - `addListener`, `removeListener` — no caller anywhere. **Dead.** (Listener notification loops run on empty lists.)
  - `getConfiguration()` — no caller anywhere. **Dead.**
  - `getLastReport()` — no caller anywhere. **Dead** (the field `lastReport` is only read by this getter, so the report is effectively lost after each `start()`/`shutdown()`).
  - `verify()` — no caller anywhere. **Dead** (duplicates `verifySystem` but with boolean return).
  - `BootstrapListener` (nested interface) — no implementation exists. **Dead contract.**
  - `ModuleInitializer` (nested interface) — only lambdas inside `PlatformBootstrap` implement it. Technically live (lambdas are anonymous implementations) but not externally used.
- `BootstrapConfiguration`:
  - Getters `getStartupTimeout/getShutdownTimeout/getRetryDelay/getMaxRetries/isEnableHealthChecks` — never read. **Dead reads.** (5 of 9 knobs are decorative.)
  - All `with*` setters on `Builder` (11 methods) — no caller uses any non-default `with*` setter. **Dead setters** (defaults are sufficient because no caller builds a custom config).
- `BootstrapState`: `isTerminal()`, `isOperational()`, `isFailure()`, `next()`, `previous()` — zero callers. **Dead helpers.** 5 of 6 public methods.
- `BootstrapException`: the `(Throwable cause)` constructor (lines 35-37) is never used. **Dead constructor.**
- `PlatformInitializationReport`:
  - `getInitializedModuleCount()`, `getFailedModuleCount()`, `getWarningCount()` — never called. **Dead getters.**
  - `Builder.addInitializedModule()`, `addFailedModule()`, `withWarnings()` — never called (the `start()` path uses the `withInitializedModules(Iterable)` / `withFailedModules(Iterable)` overloads). **Dead builder methods.**
- `PlatformServiceLocator`: `reset()` is called only by the gate test, but it is in scope as "live" because it is invoked within the audited execution context.

### 7.2 Component-scope double check

Project-wide search for `@Component`, `@Service`, `@Configuration` confirms only the following classes carry any of these annotations within the audited packages:

- `BootManager.java:6` — `@Component`. Sole annotated class in scope.

There are no `@Bean` methods anywhere in the `boot` or `bootstrap` packages. The Spring context therefore does not directly expose any of the bootstrap machinery to the application.

---

## 8. Critical Findings

The forensic audit surfaces **one architectural finding** with strong supporting evidence:

### 8.1 The platform has two parallel bootstrap paths, but only one runs in production

| Path | Origin | Live in production? | State of 8 services | State of `DefaultRuntimeService` |
|---|---|---|---|---|
| **Spring app** | `ShreeAiOsApplication.main` → `SpringApplication.run` | **YES** | Constructed by Spring component scan via @Component classes elsewhere in the repo (out of scope, but they own Configuration/Registry/etc.) | Created by `ShreeBuilder.java:104` (SDK) or other @Component paths (none in scope) |
| **SDK** | Caller → `ShreeBuilder.build()` | YES (wherever SDK is used) | Same Spring-managed set if invoked from inside a Spring context, otherwise user-built | `new DefaultRuntimeService(runtimeConfig, runtimeContract)` at `ShreeBuilder.java:104` |
| **Bootstrap** | Caller → `PlatformBootstrap.create()` → `PlatformServiceLocator.getInstance()` | **NO** | The locator manually `new`s 8 fresh instances, **distinct** from any Spring-managed or SDK-managed instance | `new DefaultRuntimeService(runtimeConfig, runtimeContract)` at `PlatformServiceLocator.java:107` — a different object from the one at `ShreeBuilder.java:104` |

**Implication:** The class `PlatformBootstrap` and its helper ecosystem advertise themselves as "the platform bootstrap", but they are not on the production code path. A 9-kernel registration sequence, a 4-phase state machine, rollback, strict-mode, listener notifications, and a verification step — all of these exist only in code paths that the running application never exercises. The single Spring class in this scope, `BootManager`, is constructed and then never used.

**Severity:** **HIGH** for architectural clarity; **LOW** for runtime behavior today (the running application continues to work because the SDK and Spring paths are self-sufficient).

### 8.2 Supporting findings (material to the audit)

1. **Configuration is half-implemented.** `BootstrapConfiguration` exposes 5 knobs (`startupTimeout`, `shutdownTimeout`, `retryDelay`, `maxRetries`, `enableHealthChecks`) that no code in the platform reads. `PlatformBootstrap.start()` has no timeout wrapper and no retry loop. This is a documentation-vs-implementation gap.
2. **The locator is a hard singleton with a test-only escape hatch.** `PlatformServiceLocator` is an eager static singleton with no Spring exposure; `reset()` exists for test isolation (gate test calls it `@BeforeEach` and `@AfterEach`). There is no equivalent of "the platform's service graph" from a Spring point of view — the running Spring context builds its own instances independently.
3. **Listener contract is unused.** `BootstrapListener`, `addListener`, `removeListener`, and every `ModuleInitializer` lambda contract are wired but no class outside `PlatformBootstrap` participates. Notification loops in `start()` (lines 173, 337, 575, 591) execute over an empty `CopyOnWriteArrayList`.
4. **`getLastReport()` is a leaky abstraction.** The `PlatformBootstrap.lastReport` field is private; only `getLastReport()` reads it. But `getLastReport()` is never called, so a successfully started bootstrap's report is unreachable to the outside world even within the gate test path (the gate test captures the return value of `start()` directly, not via the getter).
5. **Method-level dead code ratio is high.** Counting the public methods enumerated in §3 and §7.1, the audited classes declare ≈ 60 public methods, of which 16+ are never called anywhere in the repository (the listener API, `verify()`, all unused `BootstrapState` helpers, 3 unused `PlatformInitializationReport` getters, 4 unused Builder methods, 5 unused `BootstrapConfiguration` knobs, etc.).

---

## 9. Questions / Unknowns

These are questions the audit cannot answer from source alone. They are **observations of the gap**, not recommendations.

1. **Is `PlatformBootstrap` intended to be wired into a Spring `@Configuration` in the future?** Today, the only consumer is the test gate. Was a `@Bean PlatformBootstrap platformBootstrap(BootstrapConfiguration cfg) { return PlatformBootstrap.create(cfg); }` factory planned in a follow-up sprint that would make the class the real entry point?
2. **Are application modules expected to switch from `ShreeBuilder` (SDK) to `PlatformBootstrap`?** The two paths produce **different** `DefaultRuntimeService` instances because there is no shared factory. If both are intended to coexist, is the duplication deliberate or a migration leftover?
3. **Who is the intended consumer of `PlatformBootstrap.getLastReport()` / `addListener()` / `BootstrapListener`?** The contract is declared but no implementation or caller exists. Was it reserved for an external orchestrator that was never built?
4. **Why are `startupTimeout`, `shutdownTimeout`, `retryDelay`, `maxRetries`, and `enableHealthChecks` declared but never read?** Were they scaffolding for a resilience layer (timeout-aware start, retry on failure) that was not implemented?
5. **Why is `BootManager` a `@Component` with no consumers?** Was it intended to be collected as `List<RuntimeService>` in a kernel/runner class that does not exist yet? The class is positioned as if it were a peer of `DefaultRuntimeService` and friends, but it is the only `@Component` in the entire `platform` module.
6. **What is the role of `PlatformServiceLocator.reset()` outside the gate test?** It is a public, static, test-isolation hook on a production-style singleton. Is it a deliberate "test seam" or a leftover?
7. **Is the `PlatformBootstrap` state machine (`BootstrapState.next()`/`previous()`) a public API that other orchestrators may rely on, or is it internal?**
8. **Are the 9 kernels registered in `PlatformBootstrap.initializeKernels` intended to be the same kernels used by the live Spring application?** The locator's `DefaultKernelRegistry` instance is not visible to the Spring context, so any kernel registered via `PlatformBootstrap` is invisible to the live application.

---

## 10. Appendix: Search Methodology

All claims in this document are grounded in the following searches (executed via PowerShell `Select-String -Path ...` over the repository; full queries and the empty-result sets are summarized here).

| Search pattern | Result count | Interpretation |
|---|---|---|
| `PlatformBootstrap` (whole repo, case-sensitive) | 6 hits outside file | All in `EngineeringGate2RuntimeVerification.java` |
| `new PlatformBootstrap` | 0 | Construction is factory-only |
| `new BootManager` | 0 | Construction is Spring-only |
| `new BootstrapConfiguration` | 0 | Construction is Builder-only |
| `new PlatformInitializationReport` | 0 | Construction is Builder-only |
| `new PlatformServiceLocator` | 0 | Construction is `getInstance()` only |
| `addListener\|removeListener` (any class) | 6+ hits | All inside `PlatformBootstrap` itself |
| `BootstrapListener` | 2 hits | The interface declaration + an internal `onBootstrapComplete` loop |
| `implements BootstrapListener\|extends BootstrapListener` | 0 | No class implements the contract |
| `\.next\(\)` on `BootstrapState` | 0 | `next()` is dead |
| `\.previous\(\)` on `BootstrapState` | 0 | `previous()` is dead |
| `isTerminal\(\)\|isOperational\(\)\|isFailure\(\)` on `BootstrapState` | 0 | All three helpers are dead |
| `PlatformServiceLocator.getInstance\(\)` | 11 hits | 7 in `PlatformBootstrap`, 4 in gate test |
| `PlatformServiceLocator.reset\(\)` | 2 hits | Both in gate test |
| `@Component\|@Service\|@Configuration` in `boot`/`bootstrap` | 1 hit | `BootManager.java:6` |
| `@Bean` in `boot`/`bootstrap` | 0 | No bean factories in scope |
| `new DefaultRuntimeService\(` | 4 hits | `ShreeBuilder.java:104` (production SDK), `PlatformServiceLocator.java:107` (locator), 2 test sites |
| `List<RuntimeService>` (whole repo) | 0 in `src/main/java` | No collection injection of runtime services exists |
| `BootManager` as a type reference | 0 | Never injected or returned by anything |
| `BootstrapConfiguration` as a type reference | 1 | Only `PlatformBootstrap`'s field |
| `EngineeringGate2RuntimeVerification` `PlatformBootstrap.create` | 6 hits | Lines 92, 166, 240, 319, 467, 523 |
| `bootstrap.start\(\)` | 1 hit | Gate test line 534 |
| `ShreeAiOsApplication.main` (production main) | 1 hit | `ShreeAiOsApplication.java:11` |
| `SpringApplication.run` | 1 hit (in `ShreeAiOsApplication`) | The single Spring Boot entry point |
| `PlatformInitializationReport` outside scope | 1 file | `EngineeringGate2RuntimeVerification.java:4,50` |
| `BootstrapState` outside scope | 2 files | `PlatformBootstrap`, `PlatformInitializationReport`, `EngineeringGate2RuntimeVerification` |
| `BootstrapException` outside scope | 0 files (only `PlatformBootstrap` and the test) | Confirmed |

---

## 11. Sign-off

This is a read-only forensic audit. **No source files were modified, renamed, or refactored.** No recommendations for code changes have been made — the document is descriptive only and any forward action is out of scope for Phase 1.

**End of SPRINT 20 — PHASE 1 BOOT / BOOTSTRAP FORENSIC AUDIT.**





