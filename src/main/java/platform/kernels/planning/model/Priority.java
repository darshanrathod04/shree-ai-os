package platform.kernels.planning.model;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * <b>Priority</b>
 *
 * <p>Represents immutable priority information within the Planning Kernel.
 * This model captures priority level, urgency, importance, and associated
 * metadata without performing any ranking algorithm.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the priority of a planning element.</li>
 *   <li>Captures priority level, urgency, and importance.</li>
 *   <li>Holds associated metadata.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Immutable — all fields are final with no setters.</li>
 *   <li>Constructor validation — rejects {@code null} arguments.</li>
 *   <li>Value-based equality — implements {@link #equals(Object)} and {@link #hashCode()}.</li>
 *   <li>Data-only — contains no ranking algorithm.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Planning Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-PLAN-102, EIO-ARCH-001</p>
 */
public final class Priority {

    private final String level;
    private final String urgency;
    private final String importance;
    private final Map<String, String> metadata;

    /**
     * Constructs a {@code Priority} with the specified parameters.
     *
     * @param level      the priority level (must not be {@code null})
     * @param urgency    the urgency classification (must not be {@code null})
     * @param importance the importance classification (must not be {@code null})
     * @param metadata   additional metadata (must not be {@code null})
     * @throws NullPointerException if any argument is {@code null}
     */
    public Priority(String level,
                    String urgency,
                    String importance,
                    Map<String, String> metadata) {
        this.level = Objects.requireNonNull(level, "level must not be null");
        this.urgency = Objects.requireNonNull(urgency, "urgency must not be null");
        this.importance = Objects.requireNonNull(importance, "importance must not be null");
        this.metadata = Collections.unmodifiableMap(
                Objects.requireNonNull(metadata, "metadata must not be null"));
    }

    /**
     * Returns the priority level.
     *
     * @return the priority level
     */
    public String level() {
        return level;
    }

    /**
     * Returns the urgency classification.
     *
     * @return the urgency
     */
    public String urgency() {
        return urgency;
    }

    /**
     * Returns the importance classification.
     *
     * @return the importance
     */
    public String importance() {
        return importance;
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
        if (!(o instanceof Priority priority)) return false;
        return level.equals(priority.level)
                && urgency.equals(priority.urgency)
                && importance.equals(priority.importance)
                && metadata.equals(priority.metadata);
    }

    @Override
    public int hashCode() {
        int result = level.hashCode();
        result = 31 * result + urgency.hashCode();
        result = 31 * result + importance.hashCode();
        result = 31 * result + metadata.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Priority{"
                + "level='" + level + '\''
                + ", urgency='" + urgency + '\''
                + ", importance='" + importance + '\''
                + ", metadata=" + metadata
                + '}';
    }
}