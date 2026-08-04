package com.shreeai.os.platform.kernels.knowledge.api;

/**
 * <b>KnowledgeGraphService</b>
 *
 * <p>Defines the contract for managing semantic relationships and knowledge graph
 * operations within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the contract for managing semantic relationships between knowledge entities.</li>
 *   <li>Provides operations for creating and removing relationships.</li>
 *   <li>Supports querying graph connections between entities.</li>
 *   <li>Exposes graph-oriented operations for navigating the knowledge graph.</li>
 *   <li>Enforces the separation of graph operations from entity lifecycle operations.</li>
 * </ul>
 *
 * <p><b>Graph Operations:</b></p>
 * <ul>
 *   <li>Creating semantic relationships</li>
 *   <li>Removing semantic relationships</li>
 *   <li>Querying graph connections</li>
 *   <li>Exposing graph-oriented navigation</li>
 * </ul>
 *
 * <p><b>Thread Safety:</b> Implementations MUST be thread-safe. Multiple kernels
 * may concurrently access and modify the knowledge graph.</p>
 *
 * <p><b>Immutability:</b> All returned graph objects MUST be immutable.
 * Consumers MUST NOT modify returned objects.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-KNW-101, EIO-ARCH-001</p>
 *
 * @see KnowledgeService
 * @see KnowledgeQueryService
 * @see KnowledgeExtractionService
 */
public interface KnowledgeGraphService {

    /**
     * Creates a semantic relationship between two knowledge entities.
     *
     * <p>This operation establishes a directed semantic relationship from a source
     * entity to a target entity. The relationship type describes the semantic
     * nature of the connection.</p>
     *
     * <p>If the relationship already exists, the implementation SHALL determine
     * whether to update or reject based on its idempotency policy.</p>
     *
     * @param sourceEntityId the unique identifier of the source knowledge entity
     *                       (must not be {@code null})
     * @param targetEntityId the unique identifier of the target knowledge entity
     *                       (must not be {@code null})
     * @param relationshipType the semantic type describing the relationship
     *                         (must not be {@code null} or empty)
     * @return the unique identifier assigned to the newly created relationship
     * @throws IllegalArgumentException if any parameter is {@code null} or if
     *                                  {@code relationshipType} is empty
     */
    String createRelationship(String sourceEntityId, String targetEntityId, String relationshipType);

    /**
     * Removes a semantic relationship from the knowledge graph.
     *
     * <p>This operation removes the identified relationship from the knowledge
     * graph. Once removed, the relationship SHALL no longer be traversable
     * through graph operations.</p>
     *
     * <p>Removal is permanent. Implementations MAY archive removed relationships
     * for audit purposes.</p>
     *
     * @param relationshipId the unique identifier of the relationship to remove
     *                       (must not be {@code null})
     * @return {@code true} if the relationship was removed, {@code false} if the
     *         relationship was not found
     * @throws IllegalArgumentException if {@code relationshipId} is {@code null}
     */
    boolean removeRelationship(String relationshipId);

    /**
     * Queries the graph connections emanating from a knowledge entity.
     *
     * <p>This operation retrieves all relationships where the specified entity
     * is the source. The result includes both the relationship metadata and the
     * target entity identifiers.</p>
     *
     * <p>If the entity has no outgoing relationships, an empty array SHALL be
     * returned.</p>
     *
     * @param entityId the unique identifier of the source knowledge entity
     *                 (must not be {@code null})
     * @return an array of outgoing relationship objects (never {@code null},
     *         may be empty)
     * @throws IllegalArgumentException if {@code entityId} is {@code null}
     */
    Object[] queryConnections(String entityId);

    /**
     * Navigates the knowledge graph from a starting entity along specified
     * relationship types.
     *
     * <p>This operation traverses the knowledge graph starting from the given
     * entity, following relationships of the specified types. The depth of
     * traversal SHALL be controlled by the implementation.</p>
     *
     * <p>Results SHALL be returned as an array of reachable knowledge entities
     * or relationship paths.</p>
     *
     * @param startEntityId    the unique identifier of the starting knowledge entity
     *                         (must not be {@code null})
     * @param relationshipTypes the semantic relationship types to traverse
     *                          (must not be {@code null})
     * @return an array of reachable graph nodes or paths (never {@code null},
     *         may be empty)
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    Object[] traverseGraph(String startEntityId, String[] relationshipTypes);

    /**
     * Retrieves all relationships associated with a knowledge entity.
     *
     * <p>This operation retrieves both incoming and outgoing relationships for
     * the specified entity, providing a complete view of its position within
     * the knowledge graph.</p>
     *
     * <p>If the entity is not part of any relationships, an empty array SHALL
     * be returned.</p>
     *
     * @param entityId the unique identifier of the knowledge entity
     *                 (must not be {@code null})
     * @return an array of all relationship objects for the entity (never
     *         {@code null}, may be empty)
     * @throws IllegalArgumentException if {@code entityId} is {@code null}
     */
    Object[] getEntityRelationships(String entityId);
}