package com.shreeai.os.platform.kernels.acquisition.model;

/**
 * <b>RequirementPriority</b>
 *
 * <p>Defines the locked priority ranking of a discovered knowledge requirement
 * within the Knowledge Acquisition Kernel.</p>
 *
 * <p><b>Locked priority assignment rules (K0.6.1):</b></p>
 * <ul>
 *   <li>{@link #CRITICAL} - domain topics (the request's primary domain must
 *       be understood).</li>
 *   <li>{@link #HIGH} - goal topics (required to achieve the identified
 *       goal).</li>
 *   <li>{@link #MEDIUM} - constraint topics (shaped by explicit user
 *       constraints).</li>
 *   <li>{@link #LOW} - optional enrichment topics (nice-to-have knowledge,
 *       e.g. the general-domain fallback).</li>
 * </ul>
 *
 * <p><b>Thread Safety:</b> Enums are inherently thread-safe.</p>
 *
 * <p><b>Ownership:</b> Knowledge Acquisition Kernel - K0.6.1 Source Discovery</p>
 * <p><b>Version:</b> 1.0</p>
 */
public enum RequirementPriority {
    /** Optional enrichment knowledge (lowest priority). */
    LOW,

    /** Knowledge required by an explicit user constraint. */
    MEDIUM,

    /** Knowledge required to achieve the identified goal. */
    HIGH,

    /** Knowledge required by the primary domain (highest priority). */
    CRITICAL
}
