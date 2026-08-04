package com.shreeai.os.platform.core.health.error;

import com.shreeai.os.platform.core.health.model.HealthComponent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * <b>HealthCheckFailedException</b>
 *
 * <p>Thrown when a health check fails to complete successfully.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Signals that a health check operation failed.</li>
 *   <li>Provides structured error information via {@link HealthError}.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see HealthException
 * @see HealthError
 * @see HealthErrorCode#HEALTH_CHECK_FAILED
 */
public class HealthCheckFailedException extends HealthException {

    /**
     * Constructs a new {@code HealthCheckFailedException} for the given component.
     *
     * @param component the health component that failed the check (must not be null)
     * @param reason    the reason for the failure (must not be null or blank)
     */
    public HealthCheckFailedException(HealthComponent component, String reason) {
        this(component, reason, Collections.emptyMap());
    }

    /**
     * Constructs a new {@code HealthCheckFailedException} for the given component with details.
     *
     * @param component the health component that failed the check (must not be null)
     * @param reason    the reason for the failure (must not be null or blank)
     * @param details   additional error details (must not be null)
     */
    public HealthCheckFailedException(HealthComponent component, String reason, Map<String, Object> details) {
        super(createError(component, reason, details));
    }

    private static HealthError createError(HealthComponent component, String reason, Map<String, Object> details) {
        String message = "Health check failed for component: " + component.name() + " - " + reason;
        Map<String, Object> errorDetails = new HashMap<>(details != null ? details : new HashMap<>());
        errorDetails.put("componentId", component.id().value());
        errorDetails.put("componentName", component.name());
        errorDetails.put("componentCategory", component.category());
        errorDetails.put("reason", reason);
        return new HealthError(
                HealthErrorCode.HEALTH_CHECK_FAILED,
                message,
                java.time.Instant.now(),
                errorDetails
        );
    }
}