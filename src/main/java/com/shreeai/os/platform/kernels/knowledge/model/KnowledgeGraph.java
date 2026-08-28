package com.shreeai.os.platform.kernels.knowledge.model;

import java.util.List;
import java.util.Objects;

/**
 * <b>KnowledgeGraph</b>
 *
 * <p>Represents the semantic graph that aggregates knowledge nodes and relationships within the platform.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Aggregates {@link KnowledgeNode} instances as the vertices of the semantic graph.</li>
 *   <li>Aggregates {@link KnowledgeRelationship} instances as the edges of the semantic graph.</li>
 *   <li>Represents the complete semantic structure of a knowledge domain.</li>
 *   <li>Does not implement graph algorithms — it is a domain model only.</li>
 * </ul>
 *
 * <p><b>Immutability:</b> This class is immutable. All fields are final
 * and set via constructor. Collections are defensively copied.</p>
 *
 * <p><b>Thread Safety:</b> This class is thread-safe. Immutable objects
 * can be safely shared across threads.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-KNW-101, EIO-KNW-102</p>
 *
 * @see KnowledgeNode
 * @see KnowledgeRelationship
 */
public final class KnowledgeGraph {

    private final List<KnowledgeNode> nodes;
    private final List<KnowledgeRelationship> relationships;

    private KnowledgeGraph(List<KnowledgeNode> nodes, List<KnowledgeRelationship> relationships) {
        this.nodes = nodes;
        this.relationships = relationships;
    }

    /**
     * Creates a new KnowledgeGraph with null validation and defensive copying.
     *
     * <p>All parameters are validated for null. The node and relationship lists
     * are defensively copied to ensure immutability.</p>
     *
     * @param nodes         the list of knowledge nodes (must not be null, will be defensively copied)
     * @param relationships the list of knowledge relationships (must not be null, will be defensively copied)
     * @return a new KnowledgeGraph instance
     * @throws NullPointerException if any parameter is null
     */
    public static KnowledgeGraph of(List<KnowledgeNode> nodes, List<KnowledgeRelationship> relationships) {
        Objects.requireNonNull(nodes, "nodes must not be null");
        Objects.requireNonNull(relationships, "relationships must not be null");

        List<KnowledgeNode> nodesCopy = List.copyOf(nodes);
        List<KnowledgeRelationship> relationshipsCopy = List.copyOf(relationships);

        return new KnowledgeGraph(nodesCopy, relationshipsCopy);
    }

    /**
     * Creates an empty KnowledgeGraph with no nodes or relationships.
     *
     * @return an empty KnowledgeGraph instance
     */
    public static KnowledgeGraph empty() {
        return new KnowledgeGraph(List.of(), List.of());
    }

    /**
     * Returns an unmodifiable list of all knowledge nodes in this graph.
     *
     * @return the unmodifiable list of nodes (never null)
     */
    public List<KnowledgeNode> getNodes() {
        return nodes;
    }

    /**
     * Returns an unmodifiable list of all knowledge relationships in this graph.
     *
     * @return the unmodifiable list of relationships (never null)
     */
    public List<KnowledgeRelationship> getRelationships() {
        return relationships;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * <p>Two KnowledgeGraph instances are equal if they have the same nodes
     * and relationships.</p>
     *
     * @param o the reference object with which to compare
     * @return {@code true} if this object is the same as the {@code o} argument;
     *         {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KnowledgeGraph that = (KnowledgeGraph) o;
        return nodes.equals(that.nodes) && relationships.equals(that.relationships);
    }

    /**
     * Returns a hash code value for this object.
     *
     * @return a hash code based on the nodes and relationships
     */
    @Override
    public int hashCode() {
        return Objects.hash(nodes, relationships);
    }

    /**
     * Returns a string representation of this knowledge graph.
     *
     * @return a string containing the node and relationship counts
     */
    @Override
    public String toString() {
        return "KnowledgeGraph{" +
                "nodeCount=" + nodes.size() +
                ", relationshipCount=" + relationships.size() +
                '}';
    }
}