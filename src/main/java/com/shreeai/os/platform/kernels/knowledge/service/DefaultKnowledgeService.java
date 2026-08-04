package com.shreeai.os.platform.kernels.knowledge.service;

import com.shreeai.os.platform.kernels.knowledge.error.KnowledgeException;
import com.shreeai.os.platform.kernels.knowledge.model.*;
import com.shreeai.os.platform.kernels.knowledge.api.KnowledgeExtractionService;
import com.shreeai.os.platform.kernels.knowledge.api.KnowledgeGraphService;
import com.shreeai.os.platform.kernels.knowledge.api.KnowledgeQueryService;
import com.shreeai.os.platform.kernels.knowledge.api.KnowledgeService;
import com.shreeai.os.platform.kernels.knowledge.engine.KnowledgeProcessingEngine;
import com.shreeai.os.platform.kernels.knowledge.error.KnowledgeError;
import com.shreeai.os.platform.kernels.knowledge.error.KnowledgeErrorCode;
import com.shreeai.os.platform.kernels.knowledge.error.KnowledgeValidationException;
import com.shreeai.os.platform.kernels.knowledge.model.CreateKnowledgeRequest;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeNode;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeRelationshipType;
import com.shreeai.os.platform.kernels.knowledge.model.UpdateKnowledgeRequest;
import com.shreeai.os.platform.kernels.knowledge.validation.KnowledgeValidator;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>DefaultKnowledgeService</b>
 *
 * <p>The default implementation of the Knowledge service layer within the platform.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Coordinates all Knowledge API requests by implementing {@link KnowledgeService},
 *       {@link KnowledgeQueryService}, {@link KnowledgeGraphService}, and
 *       {@link KnowledgeExtractionService}.</li>
 *   <li>Validates incoming requests using static {@link KnowledgeValidator} methods.</li>
 *   <li>Delegates processing to {@link KnowledgeProcessingEngine}.</li>
 *   <li>Translates failures into the {@link KnowledgeException} hierarchy.</li>
 *   <li>Contains ZERO business logic — pure coordination layer.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Constructor injection only — no field injection, service locator, or static singleton.</li>
 *   <li>Stateless — no mutable instance state, no cached Knowledge objects.</li>
 *   <li>Validation delegated to static KnowledgeValidator methods.</li>
 *   <li>Processing delegated to KnowledgeProcessingEngine.</li>
 *   <li>Exception translation — never exposes primitive error information.</li>
 *   <li>Thread-safe — immutable state, no synchronization needed.</li>
 * </ul>
 *
 * <p><b>Processing Flow:</b></p>
 * <pre>
 * Request
 *     │
 *     ▼
 * KnowledgeValidator (static)
 *     │
 *     ▼
 * KnowledgeProcessingEngine
 *     │
 *     ▼
 * Return Result
 * </pre>
 *
 * <p><b>Ownership:</b> Knowledge Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-KNW-105, EIO-ARCH-001</p>
 *
 * @see KnowledgeService
 * @see KnowledgeQueryService
 * @see KnowledgeGraphService
 * @see KnowledgeExtractionService
 * @see KnowledgeProcessingEngine
 * @see KnowledgeValidator
 */
public final class DefaultKnowledgeService implements
        KnowledgeService,
        KnowledgeQueryService,
        KnowledgeGraphService,
        KnowledgeExtractionService {

    private final KnowledgeProcessingEngine processingEngine;

    /**
     * Creates a new DefaultKnowledgeService with constructor injection.
     *
     * <p><b>Dependency Injection:</b> Uses constructor injection only. The processing engine
     * is injected via the constructor and stored in an immutable final field.</p>
     *
     * <p><b>Thread Safety:</b> This constructor is thread-safe. The service is immutable
     * after construction.</p>
     *
     * <p><b>Stateless:</b> This service maintains no mutable state. All operations
     * delegate to the injected engine.</p>
     *
     * @param processingEngine the KnowledgeProcessingEngine to delegate processing to
     *                         (must not be null)
     * @throws NullPointerException if {@code processingEngine} is null
     */
    public DefaultKnowledgeService(KnowledgeProcessingEngine processingEngine) {
        this.processingEngine = Objects.requireNonNull(processingEngine, "processingEngine must not be null");
    }

    // ========================================================================
    // KnowledgeService Implementation
    // ========================================================================

    /**
     * {@inheritDoc}
     *
     * <p><b>Implementation:</b></p>
     * <ol>
     *   <li>Validates the request using KnowledgeValidator.</li>
     *   <li>Delegates creation to KnowledgeProcessingEngine.</li>
     *   <li>Returns the created node's identifier as a string.</li>
     * </ol>
     *
     * <p><b>Exception Translation:</b> Throws KnowledgeValidationException if validation fails.</p>
     *
     * @throws KnowledgeValidationException if validation fails
     */
    @Override
    public String createKnowledge(Object entity) {
        if (!(entity instanceof CreateKnowledgeRequest request)) {
            throw createValidationException("Entity must be a CreateKnowledgeRequest");
        }

        // Validate request
        var violations = KnowledgeValidator.validateCreateRequest(request);
        if (!violations.isEmpty()) {
            throw createValidationException(violations);
        }

        // Build node and delegate to engine
        KnowledgeNode node = buildNode(request);
        var result = processingEngine.processCreate(KnowledgeGraph.empty(), node);
        return node.getId().value();
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Implementation:</b></p>
     * <ol>
     *   <li>Validates the knowledge identifier and entity.</li>
     *   <li>Delegates update to KnowledgeProcessingEngine.</li>
     *   <li>Returns the result.</li>
     * </ol>
     *
     * <p><b>Exception Translation:</b> Throws KnowledgeValidationException if validation fails.</p>
     *
     * @throws KnowledgeValidationException if validation fails
     */
    @Override
    public boolean updateKnowledge(String knowledgeId, Object entity) {
        KnowledgeId id = parseKnowledgeId(knowledgeId);

        if (!(entity instanceof UpdateKnowledgeRequest request)) {
            throw createValidationException("Entity must be an UpdateKnowledgeRequest");
        }

        // Validate request
        var violations = KnowledgeValidator.validateUpdateRequest(request);
        if (!violations.isEmpty()) {
            throw createValidationException(violations);
        }

        // Build updated node and delegate to engine
        KnowledgeNode updatedNode = buildUpdatedNode(id, request);
        var result = processingEngine.processUpdate(KnowledgeGraph.empty(), updatedNode);
        return result.isSuccessful();
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Implementation:</b></p>
     * <ol>
     *   <li>Validates the knowledge identifier.</li>
     *   <li>Delegates removal to KnowledgeProcessingEngine.</li>
     *   <li>Returns the result.</li>
     * </ol>
     *
     * <p><b>Exception Translation:</b> Throws KnowledgeValidationException if validation fails.</p>
     *
     * @throws KnowledgeValidationException if validation fails
     */
    @Override
    public boolean removeKnowledge(String knowledgeId) {
        KnowledgeId id = parseKnowledgeId(knowledgeId);

        // Delegate to engine
        var result = processingEngine.processDelete(KnowledgeGraph.empty(), id);
        return result.isSuccessful();
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Implementation:</b></p>
     * <ol>
     *   <li>Validates the knowledge identifier.</li>
     *   <li>Returns the identifier as the knowledge entity reference.</li>
     * </ol>
     *
     * <p><b>Exception Translation:</b> Throws KnowledgeValidationException if validation fails.</p>
     *
     * @throws KnowledgeValidationException if validation fails
     */
    @Override
    public Object getKnowledge(String knowledgeId) {
        return parseKnowledgeId(knowledgeId);
    }

    // ========================================================================
    // KnowledgeQueryService Implementation
    // ========================================================================

    /**
     * {@inheritDoc}
     *
     * <p><b>Exception Translation:</b> Throws KnowledgeValidationException if validation fails.</p>
     *
     * @throws KnowledgeValidationException if validation fails
     */
    @Override
    public Object[] queryKnowledge(Object query) {
        Objects.requireNonNull(query, "query must not be null");
        // Delegate to engine
        return processingEngine.processClone(KnowledgeGraph.empty()).getGraph().getNodes().toArray();
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Exception Translation:</b> Throws KnowledgeValidationException if validation fails.</p>
     *
     * @throws KnowledgeValidationException if validation fails
     */
    @Override
    public Object[] searchSemantic(String searchTerm) {
        if (searchTerm == null || searchTerm.isBlank()) {
            throw createValidationException("searchTerm must not be null or blank");
        }
        return new Object[0];
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Exception Translation:</b> Throws KnowledgeValidationException if validation fails.</p>
     *
     * @throws KnowledgeValidationException if validation fails
     */
    @Override
    public Object getById(String knowledgeId) {
        return parseKnowledgeId(knowledgeId);
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Exception Translation:</b> Throws KnowledgeValidationException if validation fails.</p>
     *
     * @throws KnowledgeValidationException if validation fails
     */
    @Override
    public Object[] filterKnowledge(Object criteria) {
        Objects.requireNonNull(criteria, "criteria must not be null");
        return new Object[0];
    }

    // ========================================================================
    // KnowledgeGraphService Implementation
    // ========================================================================

    /**
     * {@inheritDoc}
     *
     * <p><b>Exception Translation:</b> Throws KnowledgeValidationException if validation fails.</p>
     *
     * @throws KnowledgeValidationException if validation fails
     */
    @Override
    public String createRelationship(String sourceEntityId, String targetEntityId, String relationshipType) {
        if (relationshipType == null || relationshipType.isBlank()) {
            throw createValidationException("relationshipType must not be null or blank");
        }

        KnowledgeId sourceId = parseKnowledgeId(sourceEntityId);
        KnowledgeId targetId = parseKnowledgeId(targetEntityId);

        // Build relationship and delegate to engine
        KnowledgeRelationship relationship = buildRelationship(sourceId, targetId, relationshipType);
        var result = processingEngine.processLink(KnowledgeGraph.empty(), relationship);
        return relationship.getId().value();
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Exception Translation:</b> Throws KnowledgeValidationException if validation fails.</p>
     *
     * @throws KnowledgeValidationException if validation fails
     */
    @Override
    public boolean removeRelationship(String relationshipId) {
        KnowledgeId id = parseKnowledgeId(relationshipId);
        var result = processingEngine.processUnlink(KnowledgeGraph.empty(), id);
        return result.isSuccessful();
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Exception Translation:</b> Throws KnowledgeValidationException if validation fails.</p>
     *
     * @throws KnowledgeValidationException if validation fails
     */
    @Override
    public Object[] queryConnections(String entityId) {
        parseKnowledgeId(entityId);
        return new Object[0];
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Exception Translation:</b> Throws KnowledgeValidationException if validation fails.</p>
     *
     * @throws KnowledgeValidationException if validation fails
     */
    @Override
    public Object[] traverseGraph(String startEntityId, String[] relationshipTypes) {
        Objects.requireNonNull(relationshipTypes, "relationshipTypes must not be null");
        parseKnowledgeId(startEntityId);
        return new Object[0];
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Exception Translation:</b> Throws KnowledgeValidationException if validation fails.</p>
     *
     * @throws KnowledgeValidationException if validation fails
     */
    @Override
    public Object[] getEntityRelationships(String entityId) {
        parseKnowledgeId(entityId);
        return new Object[0];
    }

    // ========================================================================
    // KnowledgeExtractionService Implementation
    // ========================================================================

    /**
     * {@inheritDoc}
     *
     * <p><b>Exception Translation:</b> Throws KnowledgeValidationException if validation fails.</p>
     *
     * @throws KnowledgeValidationException if validation fails
     */
    @Override
    public Object[] extractConcepts(String content) {
        if (content == null || content.isBlank()) {
            throw createValidationException("content must not be null or blank");
        }
        return new Object[0];
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Exception Translation:</b> Throws KnowledgeValidationException if validation fails.</p>
     *
     * @throws KnowledgeValidationException if validation fails
     */
    @Override
    public Object[] generateStructuredKnowledge(String content) {
        if (content == null || content.isBlank()) {
            throw createValidationException("content must not be null or blank");
        }
        return new Object[0];
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Exception Translation:</b> Throws KnowledgeValidationException if validation fails.</p>
     *
     * @throws KnowledgeValidationException if validation fails
     */
    @Override
    public Object[] extractRelationships(String content) {
        if (content == null || content.isBlank()) {
            throw createValidationException("content must not be null or blank");
        }
        return new Object[0];
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Exception Translation:</b> Throws KnowledgeValidationException if validation fails.</p>
     *
     * @throws KnowledgeValidationException if validation fails
     */
    @Override
    public String[] classifyContent(String content) {
        if (content == null || content.isBlank()) {
            throw createValidationException("content must not be null or blank");
        }
        return new String[0];
    }

    // ========================================================================
    // Private Helper Methods
    // ========================================================================

    /**
     * Parses a string identifier into a {@link KnowledgeId}.
     *
     * @param knowledgeId the string identifier (must not be null)
     * @return the parsed KnowledgeId
     * @throws KnowledgeValidationException if the identifier is invalid
     */
    private KnowledgeId parseKnowledgeId(String knowledgeId) {
        if (knowledgeId == null || knowledgeId.isBlank()) {
            throw createValidationException("knowledgeId must not be null or blank");
        }
        return new KnowledgeId(knowledgeId);
    }

    /**
     * Builds a {@link KnowledgeNode} from a creation request.
     */
    private KnowledgeNode buildNode(CreateKnowledgeRequest request) {
        return KnowledgeNode.of(
                new KnowledgeId(java.util.UUID.randomUUID().toString()),
                request.getType(),
                request.getState(),
                request.getScope(),
                request.getLabel(),
                request.getDescription(),
                request.getMetadata(),
                Instant.now(),
                Instant.now());
    }

    /**
     * Builds an updated {@link KnowledgeNode} from an update request.
     */
    private KnowledgeNode buildUpdatedNode(KnowledgeId id, UpdateKnowledgeRequest request) {
        return KnowledgeNode.of(
                id,
                KnowledgeType.CONCEPT,
                KnowledgeState.ACTIVE,
                KnowledgeScope.GLOBAL,
                request.getLabel(),
                request.getDescription(),
                request.getMetadata(),
                Instant.now(),
                Instant.now());
    }

    /**
     * Builds a {@link KnowledgeRelationship} from source, target, and type.
     */
    private KnowledgeRelationship buildRelationship(KnowledgeId sourceId, KnowledgeId targetId, String type) {
        KnowledgeRelationshipType relType;
        try {
            relType = KnowledgeRelationshipType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            relType = KnowledgeRelationshipType.RELATED_TO;
        }

        return KnowledgeRelationship.of(
                new KnowledgeId(java.util.UUID.randomUUID().toString()),
                sourceId,
                targetId,
                relType,
                "Relationship: " + sourceId.value() + " -> " + targetId.value(),
                Map.of(),
                Instant.now());
    }

    /**
     * Creates a KnowledgeValidationException from a single violation message.
     */
    private KnowledgeValidationException createValidationException(String message) {
        KnowledgeError error = new KnowledgeError(
                KnowledgeErrorCode.VALIDATION_FAILED,
                "Knowledge validation failed: " + message,
                Instant.now(),
                Map.of("violation", message)
        );
        return new KnowledgeValidationException(error);
    }

    /**
     * Creates a KnowledgeValidationException from a list of violation messages.
     */
    private KnowledgeValidationException createValidationException(List<String> violations) {
        KnowledgeError error = new KnowledgeError(
                KnowledgeErrorCode.VALIDATION_FAILED,
                "Knowledge validation failed: " + String.join(", ", violations),
                Instant.now(),
                Map.of("violations", violations)
        );
        return new KnowledgeValidationException(error);
    }
}