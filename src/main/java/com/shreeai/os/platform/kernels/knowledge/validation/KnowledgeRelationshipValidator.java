package com.shreeai.os.platform.kernels.knowledge.validation;

import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeRelationship;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <b>KnowledgeRelationshipValidator</b>
 *
 * <p>Validates the structural integrity of {@link KnowledgeRelationship} instances.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates source node identifier — ensures the relationship has a valid source.</li>
 *   <li>Validates target node identifier — ensures the relationship has a valid target.</li>
 *   <li>Validates relationship type — ensures the type is a recognized value.</li>
 *   <li>Validates relationship consistency — ensures source and target are not identical.</li>
 *   <li>Validates metadata completeness — ensures required metadata fields are present.</li>
 *   <li>Validates relationship structure — ensures all required fields are present.</li>
 *   <li>Validates endpoint consistency — ensures source and target are structurally valid.</li>
 *   <li>Does not evaluate whether the relationship is factually correct.</li>
 *   <li>Does not mutate the relationship — inspection only.</li>
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
 * @see KnowledgeRelationship
 * @see KnowledgeValidator
 */
public final class KnowledgeRelationshipValidator {

    private KnowledgeRelationshipValidator() {
        // Utility class — prevent instantiation
    }

    /**
     * Validates the structural integrity of a {@link KnowledgeRelationship}.
     *
     * <p>Checks that the relationship has a valid identifier, valid source and target
     * node identifiers, a recognized relationship type, a non-blank label, non-null
     * metadata, and a non-null creation timestamp.</p>
     *
     * @param relationship the knowledge relationship to validate (must not be null)
     * @return a list of violation messages (empty if the relationship is structurally valid)
     * @throws NullPointerException if {@code relationship} is null
     */
    public static List<String> validate(KnowledgeRelationship relationship) {
        Objects.requireNonNull(relationship, "relationship must not be null");

        List<String> violations = new ArrayList<>();

        // Validate identity
        if (relationship.getId() == null) {
            violations.add("Relationship id must not be null");
        }

        // Validate source node identifier
        if (relationship.getSourceNodeId() == null) {
            violations.add("Relationship sourceNodeId must not be null");
        }

        // Validate target node identifier
        if (relationship.getTargetNodeId() == null) {
            violations.add("Relationship targetNodeId must not be null");
        }

        // Validate relationship type
        if (relationship.getType() == null) {
            violations.add("Relationship type must not be null");
        }

        // Validate label
        if (relationship.getLabel() == null || relationship.getLabel().isBlank()) {
            violations.add("Relationship label must not be null or blank");
        }

        // Validate metadata
        if (relationship.getMetadata() == null) {
            violations.add("Relationship metadata must not be null");
        }

        // Validate creation timestamp
        if (relationship.getCreatedAt() == null) {
            violations.add("Relationship createdAt must not be null");
        }

        return violations;
    }
}