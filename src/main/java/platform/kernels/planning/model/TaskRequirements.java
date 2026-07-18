package platform.kernels.planning.model;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * <b>TaskRequirements</b>
 *
 * <p>Represents immutable task requirements within the Planning Kernel.
 * This model captures prerequisites, required capabilities, required resources,
 * and associated metadata without performing any validation or allocation logic.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the requirements that a task must satisfy.</li>
 *   <li>Captures prerequisites, capabilities, and resource needs.</li>
 *   <li>Holds associated metadata.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Immutable — all fields are final with no setters.</li>
 *   <li>Constructor validation — rejects {@code null} arguments.</li>
 *   <li>Value-based equality — implements {@link #equals(Object)} and {@link #hashCode()}.</li>
 *   <li>Data-only — contains no validation or allocation logic.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Planning Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-PLAN-102, EIO-ARCH-001</p>
 */
public final class TaskRequirements {

    private final Map<String, String> prerequisites;
    private final Map<String, String> capabilities;
    private final Map<String, String> resources;
    private final Map<String, String> metadata;

    /**
     * Constructs a {@code TaskRequirements} with the specified parameters.
     *
     * @param prerequisites the prerequisite requirements (must not be {@code null})
     * @param capabilities  the required capabilities (must not be {@code null})
     * @param resources     the required resources (must not be {@code null})
     * @param metadata      additional metadata (must not be {@code null})
     * @throws NullPointerException if any argument is {@code null}
     */
    public TaskRequirements(Map<String, String> prerequisites,
                            Map<String, String> capabilities,
                            Map<String, String> resources,
                            Map<String, String> metadata) {
        this.prerequisites = Collections.unmodifiableMap(
                Objects.requireNonNull(prerequisites, "prerequisites must not be null"));
        this.capabilities = Collections.unmodifiableMap(
                Objects.requireNonNull(capabilities, "capabilities must not be null"));
        this.resources = Collections.unmodifiableMap(
                Objects.requireNonNull(resources, "resources must not be null"));
        this.metadata = Collections.unmodifiableMap(
                Objects.requireNonNull(metadata, "metadata must not be null"));
    }

    /**
     * Returns an unmodifiable view of the prerequisites.
     *
     * @return the prerequisites map
     */
    public Map<String, String> prerequisites() {
        return prerequisites;
    }

    /**
     * Returns an unmodifiable view of the required capabilities.
     *
     * @return the capabilities map
     */
    public Map<String, String> capabilities() {
        return capabilities;
    }

    /**
     * Returns an unmodifiable view of the required resources.
     *
     * @return the resources map
     */
    public Map<String, String> resources() {
        return resources;
    }

    /**
     * Returns an unmodifiable view of the metadata.
     *
     * @return the metadata map
     */
    public Map<String, String> metadata() {
        return metadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TaskRequirements that)) return false;
        return prerequisites.equals(that.prerequisites)
                && capabilities.equals(that.capabilities)
                && resources.equals(that.resources)
                && metadata.equals(that.metadata);
    }

    @Override
    public int hashCode() {
        int result = prerequisites.hashCode();
        result = 31 * result + capabilities.hashCode();
        result = 31 * result + resources.hashCode();
        result = 31 * result + metadata.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "TaskRequirements{"
                + "prerequisites=" + prerequisites
                + ", capabilities=" + capabilities
                + ", resources=" + resources
                + ", metadata=" + metadata
                + '}';
    }
}