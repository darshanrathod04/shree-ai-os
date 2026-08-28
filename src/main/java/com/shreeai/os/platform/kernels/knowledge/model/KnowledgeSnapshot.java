package com.shreeai.os.platform.kernels.knowledge.model;

import java.time.Instant;
import java.util.Objects;

/**
 * <b>KnowledgeSnapshot</b>
 *
 * <p>Represents a read-only snapshot of the knowledge graph at a specific point in time.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Captures the complete state of a {@link KnowledgeGraph} at a point in time.</li>
 *   <li>Provides immutable, semantic-only snapshots for analysis and query.</li>
 *   <li>Is not runtime state — runtime state belongs in the Context Kernel.</li>
 *   <li>Is not historical memory storage — historical records belong in the Memory Kernel.</li>
 *   <li>Snapshots never replace Memory; they provide a read-only view of knowledge structure.</li>
 * </ul>
 *
 * <p><b>Immutability:</b> This class is immutable. All fields are final
 * and set via constructor. The internal graph is immutable by delegation.</p>
 *
 * <p><b>Thread Safety:</b> This class is thread-safe. Immutable objects
 * can be safely shared across threads.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-KNW-101, EIO-KNW-102</p>
 *
 * @see KnowledgeGraph
 */
public final class KnowledgeSnapshot {

    private final KnowledgeId snapshotId;
    private final KnowledgeGraph graph;
    private final Instant timestamp;
    private final String description;

    private KnowledgeSnapshot(KnowledgeId snapshotId, KnowledgeGraph graph, Instant timestamp, String description) {
        this.snapshotId = snapshotId;
        this.graph = graph;
        this.timestamp = timestamp;
        this.description = description;
    }

    /**
     * Creates a new KnowledgeSnapshot with null validation.
     *
     * <p>All parameters are validated for null. The graph is referenced directly
     * as it is already immutable.</p>
     *
     * @param snapshotId  the unique identifier for this snapshot (must not be null)
     * @param graph       the knowledge graph at the time of the snapshot (must not be null)
     * @param timestamp   when the snapshot was taken (must not be null)
     * @param description a description of the snapshot context (must not be null)
     * @return a new KnowledgeSnapshot instance
     * @throws NullPointerException if any required parameter is null
     */
    public static KnowledgeSnapshot of(KnowledgeId snapshotId, KnowledgeGraph graph, Instant timestamp, String description) {
        Objects.requireNonNull(snapshotId, "snapshotId must not be null");
        Objects.requireNonNull(graph, "graph must not be null");
        Objects.requireNonNull(timestamp, "timestamp must not be null");
        Objects.requireNonNull(description, "description must not be null");

        return new KnowledgeSnapshot(snapshotId, graph, timestamp, description);
    }

    /**
     * Returns the unique identifier of this snapshot.
     *
     * @return the snapshot identifier (never null)
     */
    public KnowledgeId getSnapshotId() {
        return snapshotId;
    }

    /**
     * Returns the knowledge graph at the time of this snapshot.
     *
     * @return the immutable knowledge graph (never null)
     */
    public KnowledgeGraph getGraph() {
        return graph;
    }

    /**
     * Returns when this snapshot was taken.
     *
     * @return the timestamp (never null)
     */
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Returns the description of this snapshot's context.
     *
     * @return the description (never null)
     */
    public String getDescription() {
        return description;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * <p>Two KnowledgeSnapshot instances are equal if they have the same snapshot identifier.</p>
     *
     * @param o the reference object with which to compare
     * @return {@code true} if this object is the same as the {@code o} argument;
     *         {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KnowledgeSnapshot that = (KnowledgeSnapshot) o;
        return snapshotId.equals(that.snapshotId);
    }

    /**
     * Returns a hash code value for this object.
     *
     * @return a hash code based on the snapshot identifier
     */
    @Override
    public int hashCode() {
        return snapshotId.hashCode();
    }

    /**
     * Returns a string representation of this snapshot.
     *
     * @return a string containing the identifier, timestamp, and description
     */
    @Override
    public String toString() {
        return "KnowledgeSnapshot{" +
                "snapshotId=" + snapshotId +
                ", timestamp=" + timestamp +
                ", description='" + description + '\'' +
                '}';
    }
}