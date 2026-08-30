package com.shreeai.os.platform.intelligence.agent;

/**
 * <b>AgentStatus</b>
 *
 * <p>Lifecycle status of an {@link Agent} within the {@link AgentRegistry}.</p>
 *
 * <p><b>Ownership:</b> Intelligence — Agent Registry</p>
 * <p><b>Version:</b> 3.0</p>
 *
 * @since 3.0
 */
public enum AgentStatus {

    /** Registered but not yet eligible for dispatch. */
    REGISTERED,

    /** Eligible for dispatch by the runtime. */
    ACTIVE,

    /** Temporarily suspended from dispatch. */
    SUSPENDED,

    /** Permanently retired and removed from dispatch consideration. */
    RETIRED
}
