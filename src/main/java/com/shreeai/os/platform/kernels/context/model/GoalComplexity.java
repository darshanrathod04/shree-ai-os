package com.shreeai.os.platform.kernels.context.model;

/**
 * <b>GoalComplexity</b>
 *
 * <p>Defines the complexity level of an identified goal.</p>
 *
 * <p><b>Ownership:</b> Context Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 */
public enum GoalComplexity {
    /**
     * Simple goal - single, straightforward objective.
     */
    SIMPLE,

    /**
     * Moderate goal - requires some planning or multiple steps.
     */
    MODERATE,

    /**
     * Complex goal - requires significant planning, multiple sub-goals.
     */
    COMPLEX
}