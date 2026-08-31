package com.shreeai.os.platform.runtime.storage;

import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeNode;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeRelationship;

import java.util.List;
import java.util.Optional;

/**
 * <b>KnowledgeGraphStore</b>
 *
 * <p>SPI behind the frozen {@code KnowledgeGraphService} abstraction. The
 * knowledge kernel keeps operating on the domain model; this port decides
 * <i>where</i> the graph lives. Implementations are pluggable adapters
 * (in-JVM, Neo4j, ...) selected via {@link KnowledgeGraphStores}; no kernel
 * references a concrete store and no provider is hard-coded.</p>
 *
 * <p><b>Contract:</b></p>
 * <ul>
 *   <li>Implementations MUST be thread-safe.</li>
 *   <li>{@link #saveNode} and {@link #saveRelationship} are upserts keyed by id.</li>
 *   <li>Domain objects are immutable; stores MUST NOT mutate them.</li>
 *   <li>Failures MUST be translated into {@link StorageRuntimeException}.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Runtime — Storage</p>
 * <p><b>Version:</b> 1.0</p>
 * <p><b>Constitutional Authority:</b> PHASE-1-ARCH-001</p>
 */
public interface KnowledgeGraphStore {

    /**
     * Stores (upserts) a knowledge node.
     *
     * @param node the node to persist (must not be null)
     * @throws StorageRuntimeException if persistence fails
     */
    void saveNode(KnowledgeNode node);

    /**
     * Finds a node by id.
     *
     * @param nodeId the node id (must not be null or blank)
     * @return the node, or empty when unknown
     * @throws StorageRuntimeException if lookup fails
     */
    Optional<KnowledgeNode> findNodeById(String nodeId);

    /**
     * Returns all persisted nodes.
     *
     * @return immutable list of nodes (never null; may be empty)
     * @throws StorageRuntimeException if the scan fails
     */
    List<KnowledgeNode> allNodes();

    /**
     * Removes a node.
     *
     * @param nodeId the node id (must not be null or blank)
     * @return {@code true} when a node was removed
     * @throws StorageRuntimeException if removal fails
     */
    boolean removeNode(String nodeId);

    /**
     * Stores (upserts) a knowledge relationship.
     *
     * @param relationship the relationship to persist (must not be null)
     * @throws StorageRuntimeException if persistence fails
     */
    void saveRelationship(KnowledgeRelationship relationship);

    /**
     * Returns all persisted relationships.
     *
     * @return immutable list of relationships (never null; may be empty)
     * @throws StorageRuntimeException if the scan fails
     */
    List<KnowledgeRelationship> allRelationships();

    /**
     * Removes a relationship.
     *
     * @param relationshipId the relationship id (must not be null or blank)
     * @return {@code true} when a relationship was removed
     * @throws StorageRuntimeException if removal fails
     */
    boolean removeRelationship(String relationshipId);
}
