# Contributing to Shree AI OS

Thank you for your interest in contributing to **Shree AI OS**!

Shree AI OS is an enterprise-grade, JVM-native cognitive operating system runtime. Our engineering standards emphasize deterministic architecture, rigorous concurrency safety, fail-closed security, and zero regressions.

---

## 1. Prerequisites

Ensure your development environment meets the following specifications:

- **Java Development Kit (JDK):** **Java 21 LTS** or later (Eclipse Temurin / Adoptium recommended).
- **Build Tool:** Bundled Maven Wrapper (`./mvnw` on Linux/macOS, `mvnw.cmd` on Windows) or Maven 3.9+.
- **Version Control:** Git 2.40+.
- **Container Runtime (Optional):** Docker 24+ (required only if running full PostgreSQL + pgvector integration tests via `docker compose`).

Verify your environment:
```bash
java -version
# openjdk version "21.0.x" ...

./mvnw -version
# Apache Maven 3.9.x ...
```

---

## 2. Mandatory Verification & Test Execution

Before submitting any pull request or proposing changes, you **MUST** run the mandatory regression verification suite:

### Mandatory Core Regression Command:

```bash
# Linux / macOS:
./mvnw test -Dtest=Phase5StaticRemediationVerificationTest,ProductionBugSweepVerificationTest,PlatformAdversarialChaosIntegrationTest

# Windows:
mvnw.cmd test -Dtest=Phase5StaticRemediationVerificationTest,ProductionBugSweepVerificationTest,PlatformAdversarialChaosIntegrationTest
```

### Full Verification Suite:

```bash
# Run all 56+ verification test suites:
./mvnw clean test
```

> [!IMPORTANT]
> **Zero Tolerance for Regressions:** Pull requests that fail any verification tests, introduce compiler warnings, or exhibit flaky concurrency behavior under heavy load will not be merged.

---

## 3. Engineering & Hygiene Standards

All code contributions must strictly adhere to the following architectural and code hygiene rules:

### A. Concurrency & Thread Safety Standards
- **Atomic State Mutations:** Never use non-atomic volatile compound operations (e.g., `volatile int count; count++;`) in multithreaded or shared service components. Use `AtomicInteger`, `AtomicLong`, `LongAdder`, or explicit synchronization blocks (`synchronized (lock)`).
- **Thread-Safe Iteration:** When reading or iterating over mutable collections accessed by multiple threads, synchronize the access block or return an immutable snapshot (`synchronized (collection) { return List.copyOf(collection); }`) to prevent `ConcurrentModificationException`.
- **Lock-Free Concurrency Under Load:** Design shared tracking and diagnostic metrics to scale under at least 1,000 concurrent threads without deadlock or throughput degradation.

### B. Stream & Subprocess Hygiene
- **Explicit UTF-8 Enforcement:** **Never rely on platform-default charset encoding.** Always specify `StandardCharsets.UTF_8` explicitly when instantiating `InputStreamReader`, calling `String.getBytes()`, or writing files.
- **Subprocess Stream Deadlock Prevention:** When executing external processes via `ProcessBuilder`:
  - Never read `stdout` and `stderr` sequentially on a single thread. If an external command floods `stderr`, the OS pipe buffer will fill and permanently deadlock the JVM thread waiting on `stdout`.
  - Always consume `stdout` and `stderr` asynchronously using separate dedicated threads, worker pools, or `CompletableFuture.runAsync()`.
  - Always read both streams to exhaustion before waiting for `process.waitFor()`.

### C. Fail-Closed Security & Authorization Gates
- **Fail-Closed by Default:** All authorization gates, permission policies, and security checks must default to `DENY` (`PermissionDecision.DENY`).
- If an authorization check encounters a `NullPointerException`, `IllegalArgumentException`, unmapped capability, or unexpected runtime condition, it must catch the exception and immediately deny access. Fail-open behavior is strictly prohibited.

### D. Exception Serialization Safety
- Any subsystem exception extending `java.lang.Exception` or `java.lang.RuntimeException` must declare an explicit `private static final long serialVersionUID = 1L;`.
- Ensure all custom exception fields are either primitive, serializable, or marked `transient`.

### E. Memory & Object Lifecycle Hygiene
- **No Inner Class Capture:** Avoid double-brace initialization (`new HashMap<>() {{ put(...); }}`) as it creates an anonymous inner class capturing the outer enclosing instance, leading to memory leaks and serialization failures. Use standard instantiation or `Map.of()` / `new HashMap<>()`.
- **Constructor Null Guards:** Public SDK classes and kernel service constructors must strictly validate required parameters with `Objects.requireNonNull(param, "param must not be null")`.

---

## 4. Repository Layout

```text
src/main/java/com/shreeai/os/
platform/
 ├── core/          # Kernel registry, lifecycle, discovery, event bus
 ├── kernels/       # Identity, Memory, Knowledge, Planning, Reflection, Developer
 ├── runtime/       # 11-stage cognitive pipeline, pgvector store, agents
 ├── llm/           # Model providers (Gemini, OpenAI, Ollama) and LlmRouter
 └── sdk/           # Public 10-SDK developer facade
application/
 └── shree-playground/ # Spring Boot 3/4 reference application (port 7070)
docs/
 └── developer/     # Canonical architecture, capabilities, and quickstart guides
```

---

## 5. Pull Request Checklist

Before submitting your pull request, verify that:

- [ ] All code compiles under Java 21 LTS: `./mvnw clean compile -DskipTests`.
- [ ] Mandatory regression suite passes: `./mvnw test -Dtest=Phase5StaticRemediationVerificationTest,ProductionBugSweepVerificationTest,PlatformAdversarialChaosIntegrationTest`.
- [ ] Full test suite passes: `./mvnw test`.
- [ ] Explicit `StandardCharsets.UTF_8` is used for all stream and string byte conversions.
- [ ] Subprocess streams are read asynchronously without deadlock risk.
- [ ] Shared counters and state mutations are thread-safe (`AtomicInteger`, `LongAdder`).
- [ ] Security gates maintain fail-closed semantics (`DENY` on fault).
- [ ] Relevant documentation in `docs/developer/` and `CHANGELOG.md` is updated.

---

**Project:** Shree AI OS  
**Target Version:** `1.0.6-developer-preview`  
**License:** Proprietary  
