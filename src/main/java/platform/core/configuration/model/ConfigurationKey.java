package platform.core.configuration.model;

import java.util.Objects;

/**
 * <b>ConfigurationKey</b>
 *
 * <p>Represents a unique key for identifying configuration entries within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides a type-safe identifier for configuration entries.</li>
 *   <li>Ensures keys are non-null and non-blank.</li>
 *   <li>Enables value-based equality for configuration lookups.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> Value is non-null and non-blank.</p>
 */
public final class ConfigurationKey {

    private final String value;

    /**
     * Constructs a new {@code ConfigurationKey} with the given value.
     *
     * @param value the key value (must not be null or blank)
     * @throws IllegalArgumentException if {@code value} is null or blank
     */
    public ConfigurationKey(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("ConfigurationKey value must not be null or blank");
        }
        this.value = value;
    }

    /**
     * Returns the key value.
     *
     * @return the key value
     */
    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigurationKey that = (ConfigurationKey) o;
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