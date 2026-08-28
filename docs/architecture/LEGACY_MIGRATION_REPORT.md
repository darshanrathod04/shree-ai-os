# Legacy to Runtime Migration Report

**Status:** RATIFIED - governing document
**Authority:** Chief Architect
**Scope:** 227 files across 34 packages in `platform/legacy`

---

## Method

Every legacy package was analyzed for: inbound references from canonical code,
Spring bean liveness, test coupling, and unique functionality absent from the
canonical platform. Classification categories: KEEP, BRIDGE, MIGRATE,
DEPRECATE, REMOVE.

## Inbound Coupling Map (canonical imports legacy - 43 import sites, 14 files)

| Legacy type | Canonical consumers |
|---|---|
| legacy.execution.ExecutionMetadata | PipelineContext, PipelineResult, PipelineExecutionState, DefaultRuntimeService |
| legacy.cognition.CognitiveDecision, legacy.cognition.Thought | PipelineContext, 9 validation rules, 2 DecisionValidators |
| legacy.production.ResolvedContext | PipelineContext, 9 validation rules |
| legacy.context.ConversationSession | 9 validation rules |
| legacy.capability.Capability, CapabilityRegistry | 9 validation rules |

16 test classes import legacy and migrate in lockstep.

## Per-Package Ledger

| Package | Files | Verdict | Rationale and target |
|---|---|---|---|
| legacy.execution | 8 | MIGRATE (P1) | ExecutionMetadata is the de-facto runtime contract; promote to runtime.execution, deprecated delegating alias stays |
| legacy.cognition | 15 | MIGRATE core types (Thought, CognitiveDecision) to platform cognition; DEPRECATE rest incl. uqc |
| legacy.production | 5 | MIGRATE (ResolvedContext to runtime execution model) |
| legacy.context | 11 | BRIDGE then MIGRATE ConversationSession; DEPRECATE managers |
| legacy.capability | 10 | BRIDGE canonical Capability API over CapabilityRegistry, then MIGRATE into core/registry |
| legacy.controller | 10 | BRIDGE (P1) | current public API; freeze routes/DTOs, delegate to Runtime.submit() via adapters |
| legacy.llm | 1 | MIGRATE (P1) | OllamaClient becomes first canonical LlmProvider |
| legacy.memory (+episodic, semantic) | 25 | MIGRATE mechanics into Memory/Kernel Store SPI; DEPRECATE facade beans |
| legacy.autonomy | 8 | BRIDGE then MIGRATE loop logic into Planning/MultiAgent; DEPRECATE scheduler |
| legacy.brain | 9 | DEPRECATE after controller bridging |
| legacy.agents, orchestrator, planner, planning | 19 | MIGRATE concepts into Planning/MultiAgent kernels; DEPRECATE classes |
| legacy.tools (ToolRegistry) | 1 | MIGRATE to Tool and Plugin Framework |
| legacy.skills | 8 | MIGRATE skill concept to plugin framework; DEPRECATE beans |
| legacy.graph | 5 | MIGRATE graph model to Knowledge kernel |
| legacy.resolver, router, rules, intent | 9 | DEPRECATE; CapabilityResolver bridged until validation rules migrate |
| legacy.debate, debate.swarm, society | 19 | DEPRECATE now; REMOVE only with MultiAgent parity tests |
| legacy.learning (+adaptive, curriculum, quiz) | 33 | DEPRECATE; candidate for extraction to separate app |
| legacy.personality, self, state, config | 9 | DEPRECATE |
| legacy.dto | 4 | KEEP (AgentRequest/AgentResponse) - REST API compatibility |
| legacy.approval, legacy.chief | 3 | BRIDGE into Permission System seed |

**Totals:** MIGRATE ~85, BRIDGE ~15, KEEP ~2, DEPRECATE ~107,
REMOVE-candidate ~19 (gated). Zero removals before adapters land.

## Gates

- **R1:** no canonical code imports legacy. Enforced by
  `com.shreeai.os.platform.architecture.CanonicalIsolationTest` with an explicit
  shrinking allowlist (14 files today, empty at end of Phase 2).
- **R2:** migrated types move via promote-and-delegate: canonical type is the
  implementation; legacy FQN becomes a deprecated delegating wrapper. No logic
  duplication; runtime is the single source of truth.
- **R3:** controllers keep routes and DTOs frozen; only delegation changes;
  contract tests pin request/response JSON before any rewiring.
- **R4:** a package flips DEPRECATE to REMOVE only with: zero canonical imports,
  zero test dependencies (or migrated tests), and a parity test proving behavior
  through the canonical path.
- **R5:** each phase ends green: `mvnw clean test` plus all parity suites.

## Allowlist Inventory (R1, at ratification)

    platform/kernels/chief/validation/DecisionValidator.java
    platform/runtime/pipeline/PipelineContext.java
    platform/runtime/pipeline/PipelineExecutionState.java
    platform/runtime/pipeline/PipelineResult.java
    platform/runtime/service/DefaultRuntimeService.java
    platform/validation/DecisionValidator.java
    platform/validation/ValidationRule.java
    platform/validation/rules/CapabilityRule.java
    platform/validation/rules/ConfidenceRule.java
    platform/validation/rules/ContextRule.java
    platform/validation/rules/DecisionExistsRule.java
    platform/validation/rules/ExecutionModeRule.java
    platform/validation/rules/RiskRule.java
    platform/validation/rules/SessionRule.java

## Phase Exit Criteria

- Phase 1: pipeline executes real LLM-backed inference; knowledge search returns
  real results; streaming emits provider deltas; R1 allowlist unchanged or smaller.
- Phase 2: R1 allowlist empty; duplicate contracts unified; all 16 legacy-coupled
  tests re-pointed; REST contract tests green.
- Phase 3: restart preserves memory/knowledge; root JSON state files retired;
  core registry/eventbus/health used by runtime.
- Phase 4: Tools, Permission, Multi-Agent live in that order; every REST route
  served through Runtime.submit(); parity tests green for migrated features.
- Phase 5: gated REMOVES executed; coverage gate active in CI; reference
  application published.
