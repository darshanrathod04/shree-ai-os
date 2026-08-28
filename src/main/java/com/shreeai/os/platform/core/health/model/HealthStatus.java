package com.shreeai.os.platform.core.health.model;

/**
 * <b>HealthStatus</b>
 *
 * <p>Represents the overall health state of a platform component within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the possible health states for platform components.</li>
 *   <li>Provides a type-safe enumeration for health check results.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 */
public enum HealthStatus {

    /**
     * Component is healthy and operating normally.
     */
    HEALTHY,

    /**
     * Component is degraded but still operational.
     */
    DEGRADED,

    /**
     * Component is unhealthy and not operating correctly.
     */
    UNHEALTHY,

    /**
     * Component health is unknown or has not been checked.
     */
    UNKNOWN
}