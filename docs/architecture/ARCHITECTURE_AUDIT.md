# Shree AI OS - Constitutional Architecture Audit

**Status:** RATIFIED - governing document
**Authority:** Chief Architect
**Date:** 2026-08-27

---

## 1. Executive Summary

The codebase is a transitional artifact: a well-structured canonical platform
(core, kernels, runtime, sdk; ~720 classes) coexists with a fully LIVE legacy
monolith (227 classes, 125 active Spring beans). Legacy is not isolated: it
serves the only working REST API, owns the only working LLM client
(`legacy.llm.OllamaClient`), leaks 6 of its types into the canonical runtime
contract, and self-schedules background work at startup.

**Verdict:** strong skeleton, hollow organs, live parasite.

## 2. Verified Facts

| Layer | Reality |
|---|---|
| Build | Maven single module, Java 21, Spring Boot 4.0.2, OkHttp 4.12, Lombok |
| Core | 7 subsystems (registry, discovery, lifecycle, eventbus, configuration, health, plugin), concurrency-tested, but NOT wired into the runtime |
| Kernels | 12 kernels; memory/ranking/reasoning/response real; knowledge kernel 9+ stubs returning empty arrays |
| Runtime | 10-stage pipeline, session API, observable events; depends on 6 legacy types |
| SDK | ShreeAI builder + client + domain SDKs + events + streaming; auto-creates runtime |
| LLM | ZERO canonical LLM integration; only model client is legacy OllamaClient |
| Persistence | None canonical; legacy MemoryStore/VectorMemoryStore write to filesystem |
| REST | 10 live controllers, all in legacy.controller |
| Autonomy | legacy AutonomousScheduler, @Scheduled(15s), gated by shree.scheduler.enabled |
| Hygiene | Fixed in Phase 0: zips in source tree, ~100 root report files, no README |

## 3. Findings

- **F1 (critical):** legacy is live, not quarantined. 125 legacy beans start with
  every boot; the public REST contract is legacy; legacy @Configuration classes
  shape the running app; 125-bean dead weight per deployment.
- **F2 (critical):** runtime-to-legacy dependency leak. 43 canonical files import
  legacy; PipelineContext/PipelineResult/PipelineExecutionState/DefaultRuntimeService
  and 9 validation rule files plus 2 DecisionValidators depend on legacy types.
- **F3:** duplicate contracts: two ExecutionPipeline interfaces, two
  DecisionValidators, two validation frameworks, two memory systems, two planning
  systems, PlatformServiceLocator vs Spring DI.
- **F4:** core subsystems are decorative (built and tested, unused by runtime).
- **F5:** intelligence chain broken mid-flight: knowledge stubs, no LLM provider,
  placeholder chief/multiagent, fake word-chunk streaming, IntelligenceContext
  smuggled through metadata Map.
- **F6:** production risks: in-memory only state, synchronous blocking pipeline,
  unbounded common-FJ-pool async, no auth/rate limiting/multi-tenancy, no CI.

## 4. Constitutional Layers (mandatory order)

1. Runtime Engine  2. Memory Kernel  3. Knowledge Kernel  4. Reasoning Kernel
5. Planning Kernel  6. Response Synthesizer  7. Tool & Plugin Framework
8. Permission System  9. Multi-Agent Kernel  10. Production Platform

Post-audit mandated order: Response Synthesizer, Tool Framework, Permission,
Multi-Agent.

## 5. Phase Roadmap

- **Phase 0 - Stabilize & baseline:** hygiene, baseline commit, constitutional
  docs, R1 enforcement test, green build. DONE with this commit series.
- **Phase 1 - Make the brain real:** LLM Provider SPI (+ migrated OllamaClient),
  knowledge kernel implementation, wire memory/knowledge into runtime, true
  token streaming, typed IntelligenceContext on ExecutionRequest.
- **Phase 2 - De-contaminate the contract:** promote-and-delegate migration of
  ExecutionMetadata, Thought, CognitiveDecision, ResolvedContext,
  ConversationSession; unify duplicate pipeline/validator contracts; re-point
  16 legacy-coupled tests. Exit: zero canonical-to-legacy imports.
- **Phase 3 - Persistence & core activation:** MemoryStore/KnowledgeStore SPI
  (embedded SQLite default, migrated legacy vector store as impl), wire core
  registry/eventbus/health into runtime, retire root JSON state files.
- **Phase 4 - Layers 7-9:** Tool & Plugin Framework (from core.plugin + migrated
  ToolRegistry/skills), Permission System (from legacy.approval + chief),
  Multi-Agent kernel (absorbing agents/planner/debate concepts, parity tests
  before removal). Controllers re-pointed through Runtime.submit().
- **Phase 5 - Production platform:** auth, multi-tenancy, backpressure,
  observability, CI with coverage gate, reference application + Getting Started,
  gated REMOVES (debate/society/learning extraction decision).

## 6. Compatibility Guarantees

- REST routes and DTOs frozen until Phase 5.
- Legacy FQNs keep compiling via deprecated delegating adapters.
- Legacy-coupled tests migrate rather than die.
- ShreeAI public API never changes shape.

## 7. Enforcement

- R1 test: `platform/architecture/CanonicalIsolationTest` (see
  LEGACY_MIGRATION_REPORT.md for gates R1-R5).
- Every phase ends green: `mvnw clean test` plus parity suites.
