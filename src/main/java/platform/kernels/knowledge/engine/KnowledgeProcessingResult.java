package platform.kernels.knowledge.engine;

import platform.kernels.knowledge.model.KnowledgeGraph;
import platform.kernels.knowledge.model.KnowledgeSnapshot;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <b>KnowledgeProcessingResult</b>
 *
 * <p>An immutable value object representing the result of a Knowledge processing operation.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Encapsulates the outcome of a deterministic semantic graph transformation.</li>
 *   <li>Carries the resulting {@link KnowledgeGraph} or {@link KnowledgeSnapshot}.</li>
 *   <li>Provides metadata about the processing operation for audit and debugging.</li>
 *   <li>Serves as the sole return type for all engine processing operations.</li>
 * </ul>
 *
 * <p><b>Immutability:</b> This class is immutable. All fields are final
 * and set via constructor. Collections are defensively copied to ensure immutability.</p>
 *
 * <p><b>Thread Safety:</b> This class is thread-safe. Immutable objects
 * can be safely shared across threads.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-KNW-106, EIO-ARCH-001</p>
 */
public final class KnowledgeProcessingResult {

    private final boolean successful;
    private final KnowledgeGraph graph;
    private final KnowledgeSnapshot snapshot;
    private final Instant processedAt;
    private final Map<String, Object> metadata;

    private KnowledgeProcessingResult(
            boolean successful,
            KnowledgeGraph graph,
            KnowledgeSnapshot snapshot,
            Instant processedAt,
            Map<String, Object> metadata) {
        this.successful = successful;
        this.graph = graph;
        this.snapshot = snapshot;
        this.processedAt = processedAt;
        this.metadata = metadata;
    }

    /**
     * Creates a new KnowledgeProcessingResult for a graph-transforming operation.
     *
     * <p>All parameters are validated for null. The metadata map is defensively
     * copied to ensure immutability.</p>
     *
     * @param successful  whether the processing was successful
     * @param graph       the resulting knowledge graph (must not be null)
     * @param processedAt when the processing was performed (must not be null)
     * @param metadata    additional processing metadata (must not be null, will be defensively copied)
     * @return a new KnowledgeProcessingResult instance
     * @throws NullPointerException if graph, processedAt, or metadata is null
     */
    public static KnowledgeProcessingResult ofGraph(
            boolean successful,
            KnowledgeGraph graph,
            Instant processedAt,
            Map<String, Object> metadata) {
        Objects.requireNonNull(graph, "graph must not be null");
        Objects.requireNonNull(processedAt, "processedAt must not be null");
        Objects.requireNonNull(metadata, "metadata must not be null");

        Map<String, Object> unmodifiableMetadata = Collections.unmodifiableMap(new HashMap<>(metadata));

        return new KnowledgeProcessingResult(successful, graph, null, processedAt, unmodifiableMetadata);
    }

    /**
     * Creates a new KnowledgeProcessingResult for a snapshot-producing operation.
     *
     * <p>All parameters are validated for null. The metadata map is defensively
     * copied to ensure immutability.</p>
     *
     * @param successful  whether the processing was successful
     * @param snapshot    the resulting knowledge snapshot (must not be null)
     * @param processedAt when the processing was performed (must not be null)
     * @param metadata    additional processing metadata (must not be null, will be defensively copied)
     * @return a new KnowledgeProcessingResult instance
     * @throws NullPointerException if snapshot, processedAt, or metadata is null
     */
    public static KnowledgeProcessingResult ofSnapshot(
            boolean successful,
            KnowledgeSnapshot snapshot,
            Instant processedAt,
            Map<String, Object> metadata) {
        Objects.requireNonNull(snapshot, "snapshot must not be null");
        Objects.requireNonNull(processedAt, "processedAt must not be null");
        Objects.requireNonNull(metadata, "metadata must not be null");

        Map<String, Object> unmodifiableMetadata = Collections.unmodifiableMap(new HashMap<>(metadata));

        return new KnowledgeProcessingResult(successful, null, snapshot, processedAt, unmodifiableMetadata);
    }

    /**
     * Returns whether the processing was successful.
     *
     * @return true if successful, false otherwise
     */
    public boolean isSuccessful() {
        return successful;
    }

    /**
     * Returns the resulting knowledge graph, if this result was produced by a
     * graph-transforming operation.
     *
     * @return the knowledge graph, or null if this is a snapshot result
     */
    public KnowledgeGraph getGraph() {
        return graph;
    }

    /**
     * Returns the resulting knowledge snapshot, if this result was produced by a
     * snapshot-producing operation.
     *
     * @return the knowledge snapshot, or null if this is a graph result
     */
    public KnowledgeSnapshot getSnapshot() {
        return snapshot;
    }

    /**
     * Returns when the processing was performed.
     *
     * @return the processing timestamp
     */
    public Instant getProcessedAt() {
        return processedAt;
    }

    /**
     * Returns an unmodifiable map of processing metadata.
     *
     * <p>This method ensures that the internal metadata map cannot be modified
     * by callers, preserving the immutability contract.</p>
     *
     * @return an unmodifiable map of metadata
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * <p>Two KnowledgeProcessingResult objects are equal if they have the same
     * successful flag, graph, snapshot, processedAt timestamp, and metadata.</p>
     *
     * @param o the reference object with which to compare
     * @return true if this object is the same as the obj argument; false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        KnowledgeProcessingResult that = (KnowledgeProcessingResult) o;
        return successful == that.successful
                && Objects.equals(graph, that.graph)
                && Objects.equals(snapshot, that.snapshot)
                && Objects.equals(processedAt, that.processedAt)
                && Objects.equals(metadata, that.metadata);
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value
     */
    @Override
    public int hashCode() {
        return Objects.hash(successful, graph, snapshot, processedAt, metadata);
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation
     */
    @Override
    public String toString() {
        return "KnowledgeProcessingResult{" +
                "successful=" + successful +
                ", hasGraph=" + (graph != null) +
                ", hasSnapshot=" + (snapshot != null) +
                ", processedAt=" + processedAt +
                ", metadata=" + metadata +
                '}';
    }
}