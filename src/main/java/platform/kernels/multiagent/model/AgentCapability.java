package platform.kernels.multiagent.model;

import java.util.Map;
import java.util.Objects;

/**
 * <b>AgentCapability</b>
 *
 * <p>Represents one advertised capability of an agent.</p>
 *
 * <p><b>Ownership:</b> Multi-Agent Kernel — Domain Model</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> MAGENT-102, EIO-ARCH-001</p>
 *
 * <p>AgentCapability is metadata only. It contains no executable behavior.</p>
 *
 * @param name         the capability name (must not be {@code null} or blank)
 * @param version      the capability version (must not be {@code null} or blank)
 * @param metadata     additional capability metadata (must not be {@code null})
 *
 * @since 1.0
 */
public final class AgentCapability {
    private final String name;
    private final String version;
    private final Map<String, Object> metadata;

    /**
     * Creates a new AgentCapability with the specified parameters.
     *
     * @param name     the capability name (must not be {@code null} or blank)
     * @param version  the capability version (must not be {@code null} or blank)
     * @param metadata additional capability metadata (must not be {@code null})
     * @throws NullPointerException     if any parameter is {@code null}
     * @throws IllegalArgumentException if name or version is blank
     * @since 1.0
     */
    public AgentCapability(String name, String version, Map<String, Object> metadata) {
        this.name = validateName(name);
        this.version = validateVersion(version);
        this.metadata = Map.copyOf(Objects.requireNonNull(metadata, "AgentCapability metadata must not be null"));
    }

    private static String validateName(String name) {
        Objects.requireNonNull(name, "AgentCapability name must not be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("AgentCapability name must not be blank");
        }
        return name;
    }

    private static String validateVersion(String version) {
        Objects.requireNonNull(version, "AgentCapability version must not be null");
        if (version.isBlank()) {
            throw new IllegalArgumentException("AgentCapability version must not be blank");
        }
        return version;
    }

    /**
     * Returns the capability name.
     *
     * @return the capability name
     * @since 1.0
     */
    public String name() {
        return name;
    }

    /**
     * Returns the capability version.
     *
     * @return the capability version
     * @since 1.0
     */
    public String version() {
        return version;
    }

    /**
     * Returns the capability metadata.
     *
     * @return an unmodifiable view of the capability metadata
     * @since 1.0
     */
    public Map<String, Object> metadata() {
        return metadata;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * Two AgentCapabilities are equal if they have the same name, version, and metadata.
     *
     * @param obj the reference object with which to compare
     * @return {@code true} if this object is equal to the obj argument
     * @since 1.0
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AgentCapability that = (AgentCapability) obj;
        return name.equals(that.name) &&
               version.equals(that.version) &&
               metadata.equals(that.metadata);
    }

    /**
     * Returns a hash code value for the AgentCapability.
     *
     * @return a hash code value
     * @since 1.0
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, version, metadata);
    }

    /**
     * Returns a string representation of the AgentCapability.
     *
     * @return a string representation
     * @since 1.0
     */
    @Override
    public String toString() {
        return "AgentCapability{" +
                "name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", metadata=" + metadata +
                '}';
    }
}