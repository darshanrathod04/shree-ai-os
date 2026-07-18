package platform.kernels.knowledge.api;

/**
 * <b>KnowledgeService</b>
 *
 * <p>Primary API for knowledge lifecycle management within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the contract for creating, updating, removing, and retrieving knowledge entities.</li>
 *   <li>Encapsulates all operations that manage structured knowledge within the platform.</li>
 *   <li>Provides a stable API contract independent of implementation.</li>
 *   <li>Enforces the principle that knowledge lifecycle operations flow through this interface.</li>
 * </ul>
 *
 * <p><b>Knowledge Responsibilities:</b></p>
 * <ul>
 *   <li>Managing structured knowledge entities</li>
 *   <li>Updating existing knowledge records</li>
 *   <li>Removing knowledge from the system</li>
 *   <li>Retrieving knowledge by identifier</li>
 * </ul>
 *
 * <p><b>Thread Safety:</b> Implementations MUST be thread-safe. Multiple kernels
 * may concurrently access and modify knowledge data.</p>
 *
 * <p><b>Immutability:</b> All returned knowledge objects MUST be immutable.
 * Consumers MUST NOT modify returned objects.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-KNW-101, EIO-ARCH-001</p>
 *
 * @see platform.kernels.knowledge.api.KnowledgeQueryService
 * @see platform.kernels.knowledge.api.KnowledgeGraphService
 * @see platform.kernels.knowledge.api.KnowledgeExtractionService
 */
public interface KnowledgeService {

    /**
     * Creates a new knowledge entity within the platform.
     *
     * <p>This operation requests the creation of a new structured knowledge entity
     * with the specified attributes. The knowledge entity becomes part of the
     * platform's semantic model upon successful creation.</p>
     *
     * <p>The created knowledge entity SHALL be assigned a unique identifier that
     * remains stable for the lifetime of the entity.</p>
     *
     * @param entity the knowledge entity content to create (must not be {@code null})
     * @return the unique identifier assigned to the newly created knowledge entity
     * @throws IllegalArgumentException if {@code entity} is {@code null} or invalid
     */
    String createKnowledge(Object entity);

    /**
     * Updates an existing knowledge entity.
     *
     * <p>This operation requests an update to the attributes of an existing
     * knowledge entity. Updates preserve the entity's identity while allowing
     * attribute modification.</p>
     *
     * <p>The knowledge entity's history SHALL record this update event.</p>
     *
     * @param knowledgeId the unique identifier of the knowledge entity to update
     *                    (must not be {@code null})
     * @param entity      the updated knowledge entity content (must not be {@code null})
     * @return {@code true} if the update was accepted, {@code false} if the
     *         knowledge entity was not found
     * @throws IllegalArgumentException if {@code knowledgeId} or {@code entity} is {@code null}
     */
    boolean updateKnowledge(String knowledgeId, Object entity);

    /**
     * Removes a knowledge entity from the platform.
     *
     * <p>This operation requests the removal of the identified knowledge entity.
     * Once removed, the entity SHALL no longer be available through query or
     * retrieval operations.</p>
     *
     * <p>Removal is permanent. Implementations MAY archive removed entities for
     * audit purposes, but they SHALL NOT be returned in standard queries.</p>
     *
     * @param knowledgeId the unique identifier of the knowledge entity to remove
     *                    (must not be {@code null})
     * @return {@code true} if the knowledge entity was removed, {@code false} if
     *         the entity was not found
     * @throws IllegalArgumentException if {@code knowledgeId} is {@code null}
     */
    boolean removeKnowledge(String knowledgeId);

    /**
     * Retrieves a knowledge entity by its unique identifier.
     *
     * <p>This operation retrieves the knowledge entity associated with the
     * specified identifier. If the entity does not exist, an empty result
     * SHALL be returned.</p>
     *
     * @param knowledgeId the unique identifier of the knowledge entity to retrieve
     *                    (must not be {@code null})
     * @return the knowledge entity if found, or {@code null} if not found
     * @throws IllegalArgumentException if {@code knowledgeId} is {@code null}
     */
    Object getKnowledge(String knowledgeId);
}