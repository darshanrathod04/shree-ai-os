package com.shreeai.os.platform.kernels.knowledge.api;

/**
 * <b>KnowledgeQueryService</b>
 *
 * <p>Defines the contract for querying structured and semantic knowledge within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the contract for querying structured knowledge entities.</li>
 *   <li>Defines the contract for searching semantic knowledge.</li>
 *   <li>Provides retrieval of knowledge by identifier.</li>
 *   <li>Supports filtering knowledge using platform-defined criteria.</li>
 *   <li>Enforces separation between query operations and mutation operations.</li>
 * </ul>
 *
 * <p><b>Knowledge Responsibilities:</b></p>
 * <ul>
 *   <li>Querying structured knowledge</li>
 *   <li>Searching semantic knowledge</li>
 *   <li>Retrieving knowledge by identifier</li>
 *   <li>Filtering knowledge using platform-defined criteria</li>
 * </ul>
 *
 * <p><b>Thread Safety:</b> Implementations MUST be thread-safe. Multiple kernels
 * may concurrently query knowledge data.</p>
 *
 * <p><b>Immutability:</b> All returned knowledge objects MUST be immutable.
 * Consumers MUST NOT modify returned objects.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-KNW-101, EIO-ARCH-001</p>
 *
 * @see KnowledgeService
 * @see KnowledgeGraphService
 * @see KnowledgeExtractionService
 */
public interface KnowledgeQueryService {

    /**
     * Queries structured knowledge using the specified query criteria.
     *
     * <p>This operation searches the knowledge base for entities matching the
     * provided query parameters. The query format and supported operators
     * SHALL be defined by the implementation.</p>
     *
     * <p>Results SHALL be returned as an array of matching knowledge entities.
     * If no entities match the query, an empty array SHALL be returned.</p>
     *
     * @param query the query criteria defining the search parameters
     *              (must not be {@code null})
     * @return an array of matching knowledge entities (never {@code null},
     *         may be empty)
     * @throws IllegalArgumentException if {@code query} is {@code null}
     */
    Object[] queryKnowledge(Object query);

    /**
     * Searches semantic knowledge using the specified search term.
     *
     * <p>This operation performs a semantic search across the knowledge base,
     * returning entities that are semantically related to the provided search
     * term. Semantic relevance SHALL be determined by the implementation.</p>
     *
     * <p>Results SHALL be ordered by relevance, with the most relevant results
     * appearing first.</p>
     *
     * @param searchTerm the semantic search term (must not be {@code null} or empty)
     * @return an array of semantically matching knowledge entities (never
     *         {@code null}, may be empty)
     * @throws IllegalArgumentException if {@code searchTerm} is {@code null} or empty
     */
    Object[] searchSemantic(String searchTerm);

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
    Object getById(String knowledgeId);

    /**
     * Filters knowledge entities using platform-defined criteria.
     *
     * <p>This operation filters the knowledge base according to the specified
     * criteria. The supported filter keys and their semantics SHALL be defined
     * by the implementation.</p>
     *
     * <p>Results SHALL be returned as an array of matching knowledge entities.
     * If no entities match the filter criteria, an empty array SHALL be returned.</p>
     *
     * @param criteria the filter criteria as key-value pairs (must not be
     *                 {@code null})
     * @return an array of filtered knowledge entities (never {@code null},
     *         may be empty)
     * @throws IllegalArgumentException if {@code criteria} is {@code null}
     */
    Object[] filterKnowledge(Object criteria);
}