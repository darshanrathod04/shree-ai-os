package com.shreeai.os.platform.intelligence.agent;

/**
 * <b>AgentCapability</b>
 *
 * <p>Enumerates the logical capabilities an {@link Agent} may declare. These
 * mirror the platform's kernel capabilities so routing and orchestration can
 * match a request to the most suitable agent.</p>
 *
 * <p><b>Ownership:</b> Intelligence — Agent Registry</p>
 * <p><b>Version:</b> 3.0</p>
 *
 * @since 3.0
 */
public enum AgentCapability {

    /** Project/intent planning. */
    PLANNING("PLANNING"),

    /** Knowledge search and retrieval. */
    KNOWLEDGE("KNOWLEDGE"),

    /** Memory recall and persistence. */
    MEMORY("MEMORY"),

    /** Executing tools / actions. */
    TOOL_EXECUTION("TOOL_EXECUTION"),

    /** Multi-step reasoning. */
    REASONING("REASONING"),

    /** Coordinating sub-agents. */
    COORDINATION("COORDINATION");

    private final String value;

    AgentCapability(String value) {
        this.value = value;
    }

    /** @return the canonical string value */
    public String value() {
        return value;
    }
}
