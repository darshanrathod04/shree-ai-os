package com.shreeai.os.platform.kernels.knowledge.api;

import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeNode;

import java.util.List;

/**
 * <b>KnowledgeSearchService</b>
 *
 * <p>Defines the contract for searching knowledge within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the contract for searching knowledge entities.</li>
 *   <li>Supports keyword, topic, concept, and tag-based search.</li>
 *   <li>Returns search results as lists of KnowledgeNode.</li>
 *   <li>Enforces separation between search and mutation operations.</li>
 * </ul>
 *
 * <p><b>Thread Safety:</b> Implementations MUST be thread-safe. Multiple kernels
 * may concurrently search knowledge data.</p>
 *
 * <p><b>Immutability:</b> All returned KnowledgeNode objects MUST be immutable.
 * Consumers MUST NOT modify returned objects.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-KNW-101, EIO-ARCH-001</p>
 *
 * @see KnowledgeQueryService
 * @see KnowledgeService
 */
public interface KnowledgeSearchService {

    /**
     * Searches knowledge by keyword.
     *
     * <p>Performs a full-text search across knowledge labels and descriptions.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * @param keyword the search keyword (must not be null or empty)
     * @return an immutable list of matching knowledge nodes (never null, may be empty)
     * @throws IllegalArgumentException if {@code keyword} is null or empty
     */
    List<KnowledgeNode> search(String keyword);

    /**
     * Searches knowledge by topic.
     *
     * <p>Returns knowledge nodes associated with the specified topic.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * @param topic the topic to search for (must not be null or empty)
     * @return an immutable list of matching knowledge nodes (never null, may be empty)
     * @throws IllegalArgumentException if {@code topic} is null or empty
     */
    List<KnowledgeNode> searchByTopic(String topic);

    /**
     * Searches knowledge by concept.
     *
     * <p>Returns knowledge nodes associated with the specified concept.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * @param concept the concept to search for (must not be null or empty)
     * @return an immutable list of matching knowledge nodes (never null, may be empty)
     * @throws IllegalArgumentException if {@code concept} is null or empty
     */
    List<KnowledgeNode> searchByConcept(String concept);

    /**
     * Searches knowledge by tags.
     *
     * <p>Returns knowledge nodes that have any of the specified tags.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * @param tags the tags to search for (must not be null)
     * @return an immutable list of matching knowledge nodes (never null, may be empty)
     * @throws IllegalArgumentException if {@code tags} is null
     */
    List<KnowledgeNode> searchByTags(Iterable<String> tags);

    /**
     * Searches knowledge by similarity to text.
     *
     * <p>Returns knowledge nodes that are semantically similar to the provided text.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * @param text the reference text (must not be null or empty)
     * @return an immutable list of similar knowledge nodes (never null, may be empty)
     * @throws IllegalArgumentException if {@code text} is null or empty
     */
    List<KnowledgeNode> searchBySimilarity(String text);
}