package com.shreeai.os.platform.core.configuration.model;

/**
 * <b>ConfigurationNamespace</b>
 *
 * <p>Represents a namespace for organizing configuration entries within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides a type-safe namespace for configuration entries.</li>
 *   <li>Ensures namespaces are non-null and non-blank.</li>
 *   <li>Enables value-based equality for namespace grouping.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> Value is non-null and non-blank.</p>
 */
public final class ConfigurationNamespace {

    private final String value;

    /**
     * Constructs a new {@code ConfigurationNamespace} with the given value.
     *
     * @param value the namespace value (must not be null or blank)
     * @throws IllegalArgumentException if {@code value} is null or blank
     */
    public ConfigurationNamespace(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("ConfigurationNamespace value must not be null or blank");
        }
        this.value = value;
    }

    /**
     * Returns the namespace value.
     *
     * @return the namespace value
     */
    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigurationNamespace that = (ConfigurationNamespace) o;
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