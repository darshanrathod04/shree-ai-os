package platform.core.health.model;

import java.util.Objects;

/**
 * <b>HealthCheck</b>
 *
 * <p>Represents a health check request for a platform component within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Encapsulates a health check request for a specific component.</li>
 *   <li>Provides a type-safe request for health check operations.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> Component is non-null.</p>
 */
public final class HealthCheck {

    private final HealthComponent component;
    private final boolean deep;

    /**
     * Constructs a new {@code HealthCheck} for the given component.
     *
     * @param component the health component to check (must not be null)
     * @param deep      whether to perform a deep health check
     * @throws IllegalArgumentException if {@code component} is null
     */
    public HealthCheck(HealthComponent component, boolean deep) {
        this.component = Objects.requireNonNull(component, "HealthComponent must not be null");
        this.deep = deep;
    }

    /**
     * Returns the health component to check.
     *
     * @return the health component
     */
    public HealthComponent component() {
        return component;
    }

    /**
     * Returns whether this is a deep health check.
     *
     * @return {@code true} if deep check, {@code false} otherwise
     */
    public boolean deep() {
        return deep;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HealthCheck that = (HealthCheck) o;
        return deep == that.deep && component.equals(that.component);
    }

    @Override
    public int hashCode() {
        return Objects.hash(component, deep);
    }

    @Override
    public String toString() {
        return "HealthCheck{"
                + "component=" + component
                + ", deep=" + deep
                + '}';
    }
}