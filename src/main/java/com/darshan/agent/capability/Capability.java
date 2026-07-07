package com.darshan.agent.capability;

import java.util.List;

/**
 * Capability interface — describes a capability WITHOUT executing it.
 * Implementations declare what they CAN do, not HOW they do it.
 *
 * This is SHADOW MODE — the registry only predicts which capability
 * should execute. Production execution remains in AgentBrain switch().
 */
public interface Capability {

    /**
     * Unique capability identifier.
     */
    String getName();

    /**
     * Human-readable description.
     */
    String getDescription();

    /**
     * Priority when multiple capabilities match (higher = preferred).
     */
    int getPriority();

    /**
     * Intents this capability can handle.
     */
    List<String> getSupportedIntents();

    /**
     * Current health status.
     */
    HealthStatus getHealthStatus();

    /**
     * Whether this capability is currently enabled.
     */
    boolean isEnabled();

    /**
     * Version string for compatibility tracking.
     */
    String getVersion();

    /**
     * Execution type hint (for future use).
     */
    ExecutionType getExecutionType();

    enum HealthStatus {
        HEALTHY,
        DEGRADED,
        UNHEALTHY,
        UNKNOWN
    }

    enum ExecutionType {
        DETERMINISTIC,  // No LLM (quiz, progress, etc.)
        HYBRID,         // LLM + deterministic (lessons)
        LLM,            // Full LLM (chat, general)
        STREAMING,      // LLM with streaming
        BACKGROUND      // Async execution
    }
}