package platform.core.health.error;

import platform.core.health.model.HealthComponent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * <b>InvalidHealthComponentException</b>
 *
 * <p>Thrown when a health component fails validation.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Signals that a health component is invalid.</li>
 *   <li>Provides structured error information via {@link HealthError}.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see HealthException
 * @see HealthError
 * @see HealthErrorCode#HEALTH_INVALID_COMPONENT
 */
public class InvalidHealthComponentException extends HealthException {

    /**
     * Constructs a new {@code InvalidHealthComponentException} for the given component.
     *
     * @param component the invalid health component (must not be null)
     * @param reason    the reason for invalidity (must not be null or blank)
     */
    public InvalidHealthComponentException(HealthComponent component, String reason) {
        this(component, reason, Collections.emptyMap());
    }

    /**
     * Constructs a new {@code InvalidHealthComponentException} for the given component with details.
     *
     * @param component the invalid health component (must not be null)
     * @param reason    the reason for invalidity (must not be null or blank)
     * @param details   additional error details (must not be null)
     */
    public InvalidHealthComponentException(HealthComponent component, String reason, Map<String, Object> details) {
        super(createError(component, reason, details));
    }

    private static HealthError createError(HealthComponent component, String reason, Map<String, Object> details) {
        String message = "Invalid health component: " + component.name() + " - " + reason;
        Map<String, Object> errorDetails = new HashMap<>(details != null ? details : new HashMap<>());
        errorDetails.put("componentId", component.id().value());
        errorDetails.put("componentName", component.name());
        errorDetails.put("componentCategory", component.category());
        errorDetails.put("reason", reason);
        return new HealthError(
                HealthErrorCode.HEALTH_INVALID_COMPONENT,
                message,
                java.time.Instant.now(),
                errorDetails
        );
    }
}