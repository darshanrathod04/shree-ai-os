package com.shreeai.os.platform.core.health.error;

/**
 * <b>HealthErrorCode</b>
 *
 * <p>Enumeration of all possible Health subsystem error conditions within Shree AI OS.</p>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 */
public enum HealthErrorCode {

    /**
     * The requested health component was not found.
     */
    HEALTH_COMPONENT_NOT_FOUND,

    /**
     * A health check failed to complete successfully.
     */
    HEALTH_CHECK_FAILED,

    /**
     * The health component is invalid.
     */
    HEALTH_INVALID_COMPONENT,

    /**
     * Health validation failed.
     */
    HEALTH_VALIDATION_FAILED,

    /**
     * The health component is already registered.
     */
    HEALTH_ALREADY_REGISTERED,

    /**
     * The health component is not registered.
     */
    HEALTH_NOT_REGISTERED,

    /**
     * The health engine encountered a failure.
     */
    HEALTH_ENGINE_FAILURE
}