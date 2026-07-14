package platform.core.health.model;

import java.util.Objects;

/**
 * <b>HealthComponentId</b>
 *
 * <p>Represents a unique identifier for a health component within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides a type-safe identifier for health components.</li>
 *   <li>Ensures IDs are non-null and non-blank.</li>
 *   <li>Enables value-based equality for health component lookups.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> Value is non-null and non-blank.</p>
 */
public final class HealthComponentId {

    private final String value;

    /**
     * Constructs a new {@code HealthComponentId} with the given value.
     *
     * @param value the ID value (must not be null or blank)
     * @throws IllegalArgumentException if {@code value} is null or blank
     */
    public HealthComponentId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("HealthComponentId value must not be null or blank");
        }
        this.value = value;
    }

    /**
     * Returns the ID value.
     *
     * @return the ID value
     */
    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HealthComponentId that = (HealthComponentId) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}