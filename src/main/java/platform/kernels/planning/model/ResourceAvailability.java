package platform.kernels.planning.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>ResourceAvailability</b>
 *
 * <p>Represents immutable resource availability information within the Planning Kernel.
 * This model captures available resources, capacity, availability windows,
 * and associated metadata without performing any allocation algorithm.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the availability of resources for planning.</li>
 *   <li>Captures resource identifiers, capacity, and availability windows.</li>
 *   <li>Holds associated metadata.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Immutable — all fields are final with no setters.</li>
 *   <li>Constructor validation — rejects {@code null} arguments.</li>
 *   <li>Defensive copying — collections are copied on construction.</li>
 *   <li>Value-based equality — implements {@link #equals(Object)} and {@link #hashCode()}.</li>
 *   <li>Data-only — contains no allocation algorithm.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Planning Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-PLAN-102, EIO-ARCH-001</p>
 */
public final class ResourceAvailability {

    private final List<String> availableResources;
    private final Map<String, String> capacity;
    private final Map<String, String> availabilityWindows;
    private final Map<String, String> metadata;

    /**
     * Constructs a {@code ResourceAvailability} with the specified parameters.
     *
     * @param availableResources the list of available resource identifiers (must not be {@code null})
     * @param capacity           the capacity information (must not be {@code null})
     * @param availabilityWindows the availability windows (must not be {@code null})
     * @param metadata           additional metadata (must not be {@code null})
     * @throws NullPointerException if any argument is {@code null}
     */
    public ResourceAvailability(List<String> availableResources,
                                Map<String, String> capacity,
                                Map<String, String> availabilityWindows,
                                Map<String, String> metadata) {
        this.availableResources = List.copyOf(
                Objects.requireNonNull(availableResources, "availableResources must not be null"));
        this.capacity = Collections.unmodifiableMap(
                Objects.requireNonNull(capacity, "capacity must not be null"));
        this.availabilityWindows = Collections.unmodifiableMap(
                Objects.requireNonNull(availabilityWindows, "availabilityWindows must not be null"));
        this.metadata = Collections.unmodifiableMap(
                Objects.requireNonNull(metadata, "metadata must not be null"));
    }

    /**
     * Returns an unmodifiable view of the available resource identifiers.
     *
     * @return the list of available resources
     */
    public List<String> availableResources() {
        return availableResources;
    }

    /**
     * Returns an unmodifiable view of the capacity information.
     *
     * @return the capacity map
     */
    public Map<String, String> capacity() {
        return capacity;
    }

    /**
     * Returns an unmodifiable view of the availability windows.
     *
     * @return the availability windows map
     */
    public Map<String, String> availabilityWindows() {
        return availabilityWindows;
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
        if (!(o instanceof ResourceAvailability that)) return false;
        return availableResources.equals(that.availableResources)
                && capacity.equals(that.capacity)
                && availabilityWindows.equals(that.availabilityWindows)
                && metadata.equals(that.metadata);
    }

    @Override
    public int hashCode() {
        int result = availableResources.hashCode();
        result = 31 * result + capacity.hashCode();
        result = 31 * result + availabilityWindows.hashCode();
        result = 31 * result + metadata.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ResourceAvailability{"
                + "availableResources=" + availableResources
                + ", capacity=" + capacity
                + ", availabilityWindows=" + availabilityWindows
                + ", metadata=" + metadata
                + '}';
    }
}