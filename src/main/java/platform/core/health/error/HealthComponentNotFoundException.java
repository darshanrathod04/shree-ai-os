package platform.core.health.error;

import platform.core.health.model.HealthComponent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * <b>HealthComponentNotFoundException</b>
 *
 * <p>Thrown when a requested health component is not found in the Health registry.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Signals that a health component lookup failed.</li>
 *   <li>Provides structured error information via {@link HealthError}.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see HealthException
 * @see HealthError
 * @see HealthErrorCode#HEALTH_COMPONENT_NOT_FOUND
 */
public class HealthComponentNotFoundException extends HealthException {

    /**
     * Constructs a new {@code HealthComponentNotFoundException} for the given component.
     *
     * @param component the health component that was not found (must not be null)
     */
    public HealthComponentNotFoundException(HealthComponent component) {
        this(component, Collections.emptyMap());
    }

    /**
     * Constructs a new {@code HealthComponentNotFoundException} for the given component with details.
     *
     * @param component the health component that was not found (must not be null)
     * @param details   additional error details (must not be null)
     */
    public HealthComponentNotFoundException(HealthComponent component, Map<String, Object> details) {
        super(createError(component, details));
    }

    private static HealthError createError(HealthComponent component, Map<String, Object> details) {
        String message = "Health component not found: " + component.name();
        Map<String, Object> errorDetails = new HashMap<>(details != null ? details : new HashMap<>());
        errorDetails.put("componentId", component.id().value());
        errorDetails.put("componentName", component.name());
        errorDetails.put("componentCategory", component.category());
        return new HealthError(
                HealthErrorCode.HEALTH_COMPONENT_NOT_FOUND,
                message,
                java.time.Instant.now(),
                errorDetails
        );
    }
}