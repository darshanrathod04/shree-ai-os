package com.shreeai.os.platform.runtime.execution;

import java.util.Locale;

/**
 * <b>ExecutionCapability</b>
 *
 * <p>Enumerates the autonomous execution capabilities that the Runtime can
 * dispatch to the appropriate kernel. Each capability represents a logical
 * unit of work that the SDK can request without knowing which kernel
 * actually owns the execution.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides a stable, type-safe contract for capability-based dispatch.</li>
 *   <li>Decouples the SDK from the concrete kernel implementations.</li>
 *   <li>Enables the Runtime to own routing decisions.</li>
 *   <li>Contains no execution logic.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Runtime Kernel</p>
 * <p><b>Version:</b> 2.1</p>
 *
 * @since 2.1
 */
public enum ExecutionCapability {

    /** Project planning capability delegated to the Planning Kernel. */
    PROJECT_PLANNING("PROJECT_PLANNING"),

    /** Workout planning capability delegated to the Planning Kernel. */
    WORKOUT_PLANNING("WORKOUT_PLANNING"),

    /** Knowledge search capability delegated to the Knowledge Kernel. */
    KNOWLEDGE_SEARCH("KNOWLEDGE_SEARCH"),

    /** Memory recall capability delegated to the Memory Kernel. */
    MEMORY_RECALL("MEMORY_RECALL"),

    /** Task execution capability delegated to the Execution Kernel. */
    TASK_EXECUTION("TASK_EXECUTION");

    private final String value;

    ExecutionCapability(String value) {
        this.value = value;
    }

    /**
     * Returns the canonical string value of this capability.
     *
     * @return the canonical value (never null)
     */
    public String value() {
        return value;
    }

    /**
     * Resolves an {@code ExecutionCapability} from its canonical string value.
     *
     * <p>Matching is case-insensitive and tolerant of surrounding whitespace.</p>
     *
     * @param value the capability string (may be null or blank)
     * @return the matching capability, or empty when unresolvable
     */
    public static java.util.Optional<ExecutionCapability> fromValue(String value) {
        if (value == null || value.isBlank()) {
            return java.util.Optional.empty();
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT).replace("-", "_");
        for (ExecutionCapability capability : values()) {
            if (capability.value.equals(normalized)) {
                return java.util.Optional.of(capability);
            }
        }
        return java.util.Optional.empty();
    }
}
