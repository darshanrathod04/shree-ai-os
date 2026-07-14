package platform.core.health.model;

import java.util.Objects;

/**
 * <b>HealthIndicator</b>
 *
 * <p>Represents a single health observation for a platform component within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides a single health observation with name, status, severity, and message.</li>
 *   <li>Enables granular health reporting (e.g., CPU, Memory, Database, Event Bus).</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> All fields are non-null.</p>
 */
public final class HealthIndicator {

    private final String name;
    private final HealthStatus status;
    private final HealthSeverity severity;
    private final String message;

    /**
     * Constructs a new {@code HealthIndicator} with the given parameters.
     *
     * @param name     the indicator name (must not be null)
     * @param status   the health status (must not be null)
     * @param severity the severity level (must not be null)
     * @param message  the indicator message (must not be null)
     * @throws IllegalArgumentException if any required parameter is null
     */
    public HealthIndicator(String name, HealthStatus status, HealthSeverity severity, String message) {
        this.name = Objects.requireNonNull(name, "Indicator name must not be null");
        this.status = Objects.requireNonNull(status, "HealthStatus must not be null");
        this.severity = Objects.requireNonNull(severity, "HealthSeverity must not be null");
        this.message = Objects.requireNonNull(message, "Message must not be null");
    }

    /**
     * Returns the indicator name.
     *
     * @return the indicator name
     */
    public String name() {
        return name;
    }

    /**
     * Returns the health status.
     *
     * @return the health status
     */
    public HealthStatus status() {
        return status;
    }

    /**
     * Returns the severity level.
     *
     * @return the severity level
     */
    public HealthSeverity severity() {
        return severity;
    }

    /**
     * Returns the indicator message.
     *
     * @return the indicator message
     */
    public String message() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HealthIndicator that = (HealthIndicator) o;
        return name.equals(that.name)
                && status == that.status
                && severity == that.severity
                && message.equals(that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, status, severity, message);
    }

    @Override
    public String toString() {
        return "HealthIndicator{"
                + "name='" + name + '\''
                + ", status=" + status
                + ", severity=" + severity
                + ", message='" + message + '\''
                + '}';
    }
}