# Shree AI OS

An autonomous AI Operating System: a production-grade platform on which developers
build **AI applications** and **non-AI software projects** on top of a canonical
Runtime, kernel stack, and SDK.

## Constitutional Layers

The platform is built, in this exact order, from these layers:

1. **Runtime Engine** - 10-stage canonical pipeline, sessions, observability
2. **Memory Kernel** - episodic + semantic storage behind a pluggable Store SPI
3. **Knowledge Kernel** - entities, relationships, graph traversal, search
4. **Reasoning Kernel** - evidence-grounded reasoning over memory + knowledge
5. **Planning Kernel** - goal decomposition into executable plans
6. **Response Synthesizer** - final answer composition
7. **Tool & Plugin Framework** - capability registration and execution
8. **Permission System** - approval and authorization guardrails
9. **Multi-Agent Kernel** - coordinated agent societies on the runtime
10. **Production Platform** - persistence, security, observability, scale

## Repository Map

    src/main/java/com/shreeai/os/
      ShreeAiOsApplication.java   Spring Boot entrypoint
      platform/core/              registry, discovery, lifecycle, eventbus,
                                  configuration, health, plugin
      platform/kernels/           identity, memory, context, knowledge,
                                  cognitive, inference, planning, execution,
                                  multiagent, chief, response
      platform/runtime/           canonical Runtime + 10-stage pipeline
      platform/sdk/               ShreeAI developer SDK (events, streaming)
      platform/legacy/            quarantined pre-V1 system (migration in
                                  progress, see LEGACY_MIGRATION_REPORT.md)

## Quickstart (SDK)

    ShreeAI ai = ShreeAI.builder().apiKey("local").build();
    SDKResponse response = ai.chat("Hello");

## Quickstart (build & test)

    mvnw clean test

## Constitutional Rules

- R1: no canonical code imports `platform.legacy` (enforced by
  `CanonicalIsolationTest`, allowlist shrinks to empty).
- R2: legacy types migrate by promote-and-delegate; the Runtime is the single
  source of truth; no logic is duplicated between legacy and runtime.
- R3: public API surfaces (REST routes/DTOs, SDK signatures) are frozen; only
  delegation changes until final removal.
- R4: a legacy component is removed only with zero canonical imports, zero test
  dependencies, and a parity test through the canonical path.
- R5: every phase ends green: `mvnw clean test` plus all parity suites.

See `docs/architecture/ARCHITECTURE_AUDIT.md` and
`docs/architecture/LEGACY_MIGRATION_REPORT.md` for the governing documents.
