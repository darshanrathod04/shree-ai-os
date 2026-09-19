package com.shreeai.os.platform.kernels.context.model;

/**
 * <b>PrimaryIntent</b>
 *
 * <p>Defines the primary intent categories for user requests within the Context Kernel.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Enumerates the possible primary intents a user can express.</li>
 *   <li>Provides type safety for intent detection results.</li>
 *   <li>Immutable enum representing user objectives, not execution methods.</li>
 * </ul>
 *
 * <p><b>Intent Categories:</b></p>
 * <ul>
 *   <li>LEARN - User wants to learn or study a topic</li>
 *   <li>BUILD - User wants to create or build something</li>
 *   <li>DEBUG - User wants to debug or fix an issue</li>
 *   <li>EXPLAIN - User wants an explanation of a concept</li>
 *   <li>COMPARE - User wants to compare options or concepts</li>
 *   <li>ANALYZE - User wants analysis or insights</li>
 *   <li>PLAN - User wants to plan or strategize</li>
 *   <li>REVIEW - User wants a review or evaluation</li>
 *   <li>EXECUTE - User wants to execute a task</li>
 *   <li>UNKNOWN - Intent cannot be determined</li>
 * </ul>
 *
 * <p><b>Thread Safety:</b> Enums are inherently thread-safe.</p>
 *
 * <p><b>Ownership:</b> Context Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-CTX-201</p>
 */
public enum PrimaryIntent {
    /**
     * User wants to learn or study a topic.
     */
    LEARN,

    /**
     * User wants to create or build something.
     */
    BUILD,

    /**
     * User wants to debug or fix an issue.
     */
    DEBUG,

    /**
     * User wants an explanation of a concept.
     */
    EXPLAIN,

    /**
     * User wants to compare options or concepts.
     */
    COMPARE,

    /**
     * User wants analysis or insights.
     */
    ANALYZE,

    /**
     * User wants to plan or strategize.
     */
    PLAN,

    /**
     * User wants a review or evaluation.
     */
    REVIEW,

    /**
     * User wants to execute a task.
     */
    EXECUTE,

    /**
     * Intent cannot be determined.
     */
    UNKNOWN
}