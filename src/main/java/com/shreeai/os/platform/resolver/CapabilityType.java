package com.shreeai.os.platform.resolver;

/**
 * <b>CapabilityType</b>
 *
 * <p>Canonical enumeration of the fourteen AGI OS platform capabilities.</p>
 *
 * <p>Each value describes a distinct capability the platform can provide for a
 * request. The {@link CapabilityResolver} decides WHICH capabilities are
 * required; it never executes them.</p>
 *
 * <p><b>Ownership:</b> Platform Resolver</p>
 * <p><b>Version:</b> 1.0</p>
 */
public enum CapabilityType {

    /** User and application identity resolution. */
    IDENTITY,

    /** Request and conversation context assembly. */
    CONTEXT,

    /** Long-term memory store and recall. */
    MEMORY,

    /** Knowledge base query and search. */
    KNOWLEDGE,

    /** Task planning and goal decomposition. */
    PLANNING,

    /** Reasoning and inference over available information. */
    REASONING,

    /** Action execution and task dispatch. */
    EXECUTION,

    /** Tool invocation. */
    TOOLS,

    /** Language and multimodal model invocation. */
    MODELS,

    /** Multi-agent coordination. */
    AGENTS,

    /** Request and response validation. */
    VALIDATION,

    /** Safety and policy enforcement. */
    SAFETY,

    /** Tracing, metrics, and auditability. */
    OBSERVABILITY,

    /** Orchestration of the required capability sequence. */
    ORCHESTRATION
}