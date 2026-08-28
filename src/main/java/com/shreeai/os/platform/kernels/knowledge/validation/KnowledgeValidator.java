package com.shreeai.os.platform.kernels.knowledge.validation;

import com.shreeai.os.platform.kernels.knowledge.model.CreateKnowledgeRequest;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeConcept;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeGraph;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeId;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeNode;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeRelationship;
import com.shreeai.os.platform.kernels.knowledge.model.UpdateKnowledgeRequest;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>KnowledgeValidator</b>
 *
 * <p>Primary validation coordinator for the Knowledge Kernel.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Coordinates validation across all specialized validators.</li>
 *   <li>Validates request models ({@link CreateKnowledgeRequest}, {@link UpdateKnowledgeRequest}).</li>
 *   <li>Validates {@link KnowledgeId} values.</li>
 *   <li>Validates enumerations for recognized values.</li>
 *   <li>Validates metadata structure.</li>
 *   <li>Validates timestamps for temporal consistency.</li>
 *   <li>Delegates specialized validation to {@link KnowledgeNodeValidator},
 *       {@link KnowledgeConceptValidator}, {@link KnowledgeRelationshipValidator},
 *       and {@link KnowledgeGraphValidator}.</li>
 *   <li>Aggregates validation results into a single {@link KnowledgeValidationResult}.</li>
 *   <li>Does not perform graph mutation.</li>
 * </ul>
 *
 * <p><b>Validator Rules:</b></p>
 * <ul>
 *   <li>Static methods only</li>
 *   <li>Stateless</li>
 *   <li>Pure validation</li>
 *   <li>Thread-safe</li>
 *   <li>Deterministic</li>
 *   <li>No business logic</li>
 *   <li>No side effects</li>
 *   <li>No persistence</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Knowledge Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-KNW-103, EIO-ARCH-001</p>
 *
 * @see KnowledgeNodeValidator
 * @see KnowledgeConceptValidator
 * @see KnowledgeRelationshipValidator
 * @see KnowledgeGraphValidator
 * @see KnowledgeValidationResult
 */
public final class KnowledgeValidator {

    private KnowledgeValidator() {
        // Utility class — prevent instantiation
    }

    /**
     * Validates a {@link KnowledgeId}.
     *
     * <p>Checks that the identifier is not null and its value is not blank.</p>
     *
     * @param knowledgeId the knowledge identifier to validate (must not be null)
     * @return a list of violation messages (empty if valid)
     * @throws NullPointerException if {@code knowledgeId} is null
     */
    public static List<String> validateKnowledgeId(KnowledgeId knowledgeId) {
        Objects.requireNonNull(knowledgeId, "knowledgeId must not be null");

        List<String> violations = new ArrayList<>();
        if (knowledgeId.value() == null || knowledgeId.value().isBlank()) {
            violations.add("KnowledgeId value must not be null or blank");
        }
        return violations;
    }

    /**
     * Validates a {@link CreateKnowledgeRequest}.
     *
     * <p>Checks that the request has a valid type, state, scope, non-blank label
     * and description, and non-null metadata.</p>
     *
     * @param request the create request to validate (must not be null)
     * @return a list of violation messages (empty if the request is structurally valid)
     * @throws NullPointerException if {@code request} is null
     */
    public static List<String> validateCreateRequest(CreateKnowledgeRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        List<String> violations = new ArrayList<>();

        if (request.getType() == null) {
            violations.add("CreateKnowledgeRequest type must not be null");
        }
        if (request.getState() == null) {
            violations.add("CreateKnowledgeRequest state must not be null");
        }
        if (request.getScope() == null) {
            violations.add("CreateKnowledgeRequest scope must not be null");
        }
        if (request.getLabel() == null || request.getLabel().isBlank()) {
            violations.add("CreateKnowledgeRequest label must not be null or blank");
        }
        if (request.getDescription() == null || request.getDescription().isBlank()) {
            violations.add("CreateKnowledgeRequest description must not be null or blank");
        }
        if (request.getMetadata() == null) {
            violations.add("CreateKnowledgeRequest metadata must not be null");
        }

        return violations;
    }

    /**
     * Validates an {@link UpdateKnowledgeRequest}.
     *
     * <p>Checks that the request has a valid knowledge identifier, non-blank label
     * and description, and non-null metadata.</p>
     *
     * @param request the update request to validate (must not be null)
     * @return a list of violation messages (empty if the request is structurally valid)
     * @throws NullPointerException if {@code request} is null
     */
    public static List<String> validateUpdateRequest(UpdateKnowledgeRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        List<String> violations = new ArrayList<>();

        if (request.getKnowledgeId() == null) {
            violations.add("UpdateKnowledgeRequest knowledgeId must not be null");
        }
        if (request.getLabel() == null || request.getLabel().isBlank()) {
            violations.add("UpdateKnowledgeRequest label must not be null or blank");
        }
        if (request.getDescription() == null || request.getDescription().isBlank()) {
            violations.add("UpdateKnowledgeRequest description must not be null or blank");
        }
        if (request.getMetadata() == null) {
            violations.add("UpdateKnowledgeRequest metadata must not be null");
        }

        return violations;
    }

    /**
     * Validates a {@link KnowledgeNode} by delegating to {@link KnowledgeNodeValidator}.
     *
     * @param node the knowledge node to validate (must not be null)
     * @return a list of violation messages (empty if the node is structurally valid)
     * @throws NullPointerException if {@code node} is null
     */
    public static List<String> validateNode(KnowledgeNode node) {
        return KnowledgeNodeValidator.validate(node);
    }

    /**
     * Validates a {@link KnowledgeConcept} by delegating to {@link KnowledgeConceptValidator}.
     *
     * @param concept the knowledge concept to validate (must not be null)
     * @return a list of violation messages (empty if the concept is structurally valid)
     * @throws NullPointerException if {@code concept} is null
     */
    public static List<String> validateConcept(KnowledgeConcept concept) {
        return KnowledgeConceptValidator.validate(concept);
    }

    /**
     * Validates a {@link KnowledgeRelationship} by delegating to {@link KnowledgeRelationshipValidator}.
     *
     * @param relationship the knowledge relationship to validate (must not be null)
     * @return a list of violation messages (empty if the relationship is structurally valid)
     * @throws NullPointerException if {@code relationship} is null
     */
    public static List<String> validateRelationship(KnowledgeRelationship relationship) {
        return KnowledgeRelationshipValidator.validate(relationship);
    }

    /**
     * Validates a {@link KnowledgeGraph} by delegating to {@link KnowledgeGraphValidator}.
     *
     * @param graph the knowledge graph to validate (must not be null)
     * @return a list of violation messages (empty if the graph is structurally valid)
     * @throws NullPointerException if {@code graph} is null
     */
    public static List<String> validateGraph(KnowledgeGraph graph) {
        return KnowledgeGraphValidator.validate(graph);
    }

    /**
     * Performs a complete validation of a {@link KnowledgeNode} and returns a
     * {@link KnowledgeValidationResult}.
     *
     * <p>Aggregates all violations and produces a result with validation metadata.</p>
     *
     * @param node the knowledge node to validate (must not be null)
     * @return a {@link KnowledgeValidationResult} containing the validation outcome
     * @throws NullPointerException if {@code node} is null
     */
    public static KnowledgeValidationResult validateNodeWithResult(KnowledgeNode node) {
        Objects.requireNonNull(node, "node must not be null");

        List<String> violations = validateNode(node);
        return buildResult(violations, "KnowledgeNode");
    }

    /**
     * Performs a complete validation of a {@link KnowledgeConcept} and returns a
     * {@link KnowledgeValidationResult}.
     *
     * <p>Aggregates all violations and produces a result with validation metadata.</p>
     *
     * @param concept the knowledge concept to validate (must not be null)
     * @return a {@link KnowledgeValidationResult} containing the validation outcome
     * @throws NullPointerException if {@code concept} is null
     */
    public static KnowledgeValidationResult validateConceptWithResult(KnowledgeConcept concept) {
        Objects.requireNonNull(concept, "concept must not be null");

        List<String> violations = validateConcept(concept);
        return buildResult(violations, "KnowledgeConcept");
    }

    /**
     * Performs a complete validation of a {@link KnowledgeRelationship} and returns a
     * {@link KnowledgeValidationResult}.
     *
     * <p>Aggregates all violations and produces a result with validation metadata.</p>
     *
     * @param relationship the knowledge relationship to validate (must not be null)
     * @return a {@link KnowledgeValidationResult} containing the validation outcome
     * @throws NullPointerException if {@code relationship} is null
     */
    public static KnowledgeValidationResult validateRelationshipWithResult(KnowledgeRelationship relationship) {
        Objects.requireNonNull(relationship, "relationship must not be null");

        List<String> violations = validateRelationship(relationship);
        return buildResult(violations, "KnowledgeRelationship");
    }

    /**
     * Performs a complete validation of a {@link KnowledgeGraph} and returns a
     * {@link KnowledgeValidationResult}.
     *
     * <p>Aggregates all violations and produces a result with validation metadata.</p>
     *
     * @param graph the knowledge graph to validate (must not be null)
     * @return a {@link KnowledgeValidationResult} containing the validation outcome
     * @throws NullPointerException if {@code graph} is null
     */
    public static KnowledgeValidationResult validateGraphWithResult(KnowledgeGraph graph) {
        Objects.requireNonNull(graph, "graph must not be null");

        List<String> violations = validateGraph(graph);
        return buildResult(violations, "KnowledgeGraph");
    }

    /**
     * Performs a complete validation of a {@link CreateKnowledgeRequest} and returns a
     * {@link KnowledgeValidationResult}.
     *
     * <p>Aggregates all violations and produces a result with validation metadata.</p>
     *
     * @param request the create request to validate (must not be null)
     * @return a {@link KnowledgeValidationResult} containing the validation outcome
     * @throws NullPointerException if {@code request} is null
     */
    public static KnowledgeValidationResult validateCreateRequestWithResult(CreateKnowledgeRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        List<String> violations = validateCreateRequest(request);
        return buildResult(violations, "CreateKnowledgeRequest");
    }

    /**
     * Performs a complete validation of an {@link UpdateKnowledgeRequest} and returns a
     * {@link KnowledgeValidationResult}.
     *
     * <p>Aggregates all violations and produces a result with validation metadata.</p>
     *
     * @param request the update request to validate (must not be null)
     * @return a {@link KnowledgeValidationResult} containing the validation outcome
     * @throws NullPointerException if {@code request} is null
     */
    public static KnowledgeValidationResult validateUpdateRequestWithResult(UpdateKnowledgeRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        List<String> violations = validateUpdateRequest(request);
        return buildResult(violations, "UpdateKnowledgeRequest");
    }

    /**
     * Builds a {@link KnowledgeValidationResult} from a list of violations.
     *
     * @param violations the list of violation messages
     * @param entityType the type of entity that was validated
     * @return a new KnowledgeValidationResult
     */
    private static KnowledgeValidationResult buildResult(List<String> violations, String entityType) {
        boolean valid = violations.isEmpty();
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("entityType", entityType);
        metadata.put("violationCount", violations.size());

        return new KnowledgeValidationResult(valid, violations, Instant.now(), metadata);
    }
}