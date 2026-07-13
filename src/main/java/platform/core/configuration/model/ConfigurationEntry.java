package platform.core.configuration.model;

import java.time.Instant;
import java.util.Objects;

/**
 * <b>ConfigurationEntry</b>
 *
 * <p>Represents a configuration entry within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides an immutable representation of a configuration entry.</li>
 *   <li>Encapsulates configuration key, value, namespace, type, and metadata.</li>
 *   <li>Enables type-safe configuration management across Platform components.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> All required fields are non-null. Value may be null.</p>
 */
public final class ConfigurationEntry {

    private final ConfigurationKey key;
    private final ConfigurationNamespace namespace;
    private final ConfigurationType type;
    private final Object value;
    private final String description;
    private final boolean readOnly;
    private final Instant createdAt;

    /**
     * Constructs a new {@code ConfigurationEntry} with the given parameters.
     *
     * @param key        the configuration key (must not be null)
     * @param namespace  the configuration namespace (must not be null)
     * @param type       the configuration type (must not be null)
     * @param value      the configuration value (may be null)
     * @param description the configuration description (must not be null)
     * @param readOnly   whether the configuration is read-only
     * @param createdAt  the creation timestamp (must not be null)
     * @throws IllegalArgumentException if any required parameter is null
     */
    public ConfigurationEntry(ConfigurationKey key,
                              ConfigurationNamespace namespace,
                              ConfigurationType type,
                              Object value,
                              String description,
                              boolean readOnly,
                              Instant createdAt) {
        this.key = Objects.requireNonNull(key, "ConfigurationKey must not be null");
        this.namespace = Objects.requireNonNull(namespace, "ConfigurationNamespace must not be null");
        this.type = Objects.requireNonNull(type, "ConfigurationType must not be null");
        this.value = value;
        this.description = Objects.requireNonNull(description, "Description must not be null");
        this.readOnly = readOnly;
        this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt must not be null");
    }

    /**
     * Returns the configuration key.
     *
     * @return the configuration key
     */
    public ConfigurationKey key() {
        return key;
    }

    /**
     * Returns the configuration namespace.
     *
     * @return the configuration namespace
     */
    public ConfigurationNamespace namespace() {
        return namespace;
    }

    /**
     * Returns the configuration type.
     *
     * @return the configuration type
     */
    public ConfigurationType type() {
        return type;
    }

    /**
     * Returns the configuration value.
     *
     * @return the configuration value (may be null)
     */
    public Object value() {
        return value;
    }

    /**
     * Returns the configuration description.
     *
     * @return the configuration description
     */
    public String description() {
        return description;
    }

    /**
     * Returns whether the configuration is read-only.
     *
     * @return {@code true} if read-only, {@code false} otherwise
     */
    public boolean readOnly() {
        return readOnly;
    }

    /**
     * Returns the creation timestamp.
     *
     * @return the creation timestamp
     */
    public Instant createdAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigurationEntry that = (ConfigurationEntry) o;
        return readOnly == that.readOnly
                && key.equals(that.key)
                && namespace.equals(that.namespace)
                && type == that.type
                && Objects.equals(value, that.value)
                && description.equals(that.description)
                && createdAt.equals(that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, namespace, type, value, description, readOnly, createdAt);
    }

    @Override
    public String toString() {
        return "ConfigurationEntry{"
                + "key=" + key
                + ", namespace=" + namespace
                + ", type=" + type
                + ", value=" + value
                + ", description='" + description + '\''
                + ", readOnly=" + readOnly
                + ", createdAt=" + createdAt
                + '}';
    }
}