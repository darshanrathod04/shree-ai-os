package platform.kernels.knowledge.api;

/**
 * <b>KnowledgeExtractionService</b>
 *
 * <p>Defines the contract for concept extraction and structured knowledge generation
 * within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the contract for extracting concepts from unstructured content.</li>
 *   <li>Defines the contract for generating structured knowledge from raw data.</li>
 *   <li>Exposes extraction operations without implementing extraction logic.</li>
 *   <li>Provides a stable contract for downstream kernels to request extraction.</li>
 * </ul>
 *
 * <p><b>Extraction Responsibilities:</b></p>
 * <ul>
 *   <li>Defining contracts for concept extraction</li>
 *   <li>Defining contracts for structured knowledge generation</li>
 *   <li>Exposing extraction operations</li>
 * </ul>
 *
 * <p><b>Thread Safety:</b> Implementations MUST be thread-safe. Multiple kernels
 * may concurrently request extraction operations.</p>
 *
 * <p><b>Immutability:</b> All returned extraction results MUST be immutable.
 * Consumers MUST NOT modify returned objects.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-KNW-101, EIO-ARCH-001</p>
 *
 * @see platform.kernels.knowledge.api.KnowledgeService
 * @see platform.kernels.knowledge.api.KnowledgeQueryService
 * @see platform.kernels.knowledge.api.KnowledgeGraphService
 */
public interface KnowledgeExtractionService {

    /**
     * Extracts concepts from the provided content.
     *
     * <p>This operation analyzes the given content and extracts meaningful concepts
     * as structured knowledge entities. The extraction process SHALL identify
     * key entities, terms, and relationships present in the content.</p>
     *
     * <p>The implementation SHALL determine the extraction algorithm, including
     * any natural language processing or pattern recognition techniques.</p>
     *
     * @param content the source content from which to extract concepts
     *                (must not be {@code null} or empty)
     * @return an array of extracted concept objects (never {@code null},
     *         may be empty if no concepts could be extracted)
     * @throws IllegalArgumentException if {@code content} is {@code null} or empty
     */
    Object[] extractConcepts(String content);

    /**
     * Generates structured knowledge from the provided content.
     *
     * <p>This operation transforms the given content into structured knowledge
     * entities suitable for storage and query within the knowledge base. The
     * generation process SHALL produce well-formed knowledge entities with
     * identifiable attributes.</p>
     *
     * <p>The implementation SHALL determine the knowledge schema and structure
     * of the generated entities.</p>
     *
     * @param content the source content from which to generate structured knowledge
     *                (must not be {@code null} or empty)
     * @return an array of generated structured knowledge entities (never
     *         {@code null}, may be empty if no knowledge could be generated)
     * @throws IllegalArgumentException if {@code content} is {@code null} or empty
     */
    Object[] generateStructuredKnowledge(String content);

    /**
     * Extracts semantic relationships from the provided content.
     *
     * <p>This operation analyzes the given content to identify semantic
     * relationships between known knowledge entities. The extraction process
     * SHALL produce relationship descriptors that can be added to the knowledge
     * graph.</p>
     *
     * <p>The implementation SHALL determine which entities are involved and the
     * nature of their relationships.</p>
     *
     * @param content the source content from which to extract relationships
     *                (must not be {@code null} or empty)
     * @return an array of extracted relationship descriptors (never {@code null},
     *         may be empty if no relationships could be extracted)
     * @throws IllegalArgumentException if {@code content} is {@code null} or empty
     */
    Object[] extractRelationships(String content);

    /**
     * Classifies the provided content into known knowledge categories.
     *
     * <p>This operation analyzes the given content and classifies it into
     * predefined knowledge categories within the platform's ontology. The
     * classification process SHALL assign one or more categories to the content.</p>
     *
     * <p>The implementation SHALL determine the classification scheme and
     * ontology used for categorization.</p>
     *
     * @param content the source content to classify (must not be {@code null}
     *                or empty)
     * @return an array of category identifiers or classification labels (never
     *         {@code null}, may be empty if classification is not possible)
     * @throws IllegalArgumentException if {@code content} is {@code null} or empty
     */
    String[] classifyContent(String content);
}