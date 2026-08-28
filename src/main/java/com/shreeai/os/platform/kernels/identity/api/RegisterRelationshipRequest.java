package com.shreeai.os.platform.kernels.identity.api;

import java.time.Instant;

/**
 * <b>RegisterRelationshipRequest</b>
 *
 * <p>Request object for registering a relationship between two Identities.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the contract for relationship registration requests.</li>
 *   <li>Encapsulates source, target, and relationship type.</li>
 *   <li>Provides a stable API contract independent of implementation.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Identity Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> This is a pure data contract with no business logic.</p>
 *
 * <p><b>Constitutional Authority:</b> ADD-105</p>
 *
 * @param sourceIdentityId the unique identifier of the source Identity
 * @param targetIdentityId the unique identifier of the target Identity
 * @param relationshipType the type of relationship (e.g., PARENT_CHILD, OWNS, EMPLOYS)
 * @param createdAt the timestamp when the relationship was created
 */
public record RegisterRelationshipRequest(
    String sourceIdentityId,
    String targetIdentityId,
    String relationshipType,
    Instant createdAt
) {
    /**
     * Creates a new RegisterRelationshipRequest with validation.
     *
     * @param sourceIdentityId the unique identifier of the source Identity
     * @param targetIdentityId the unique identifier of the target Identity
     * @param relationshipType the type of relationship
     * @param createdAt the timestamp when the relationship was created
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public RegisterRelationshipRequest {
        if (sourceIdentityId == null || sourceIdentityId.isBlank()) {
            throw new IllegalArgumentException("sourceIdentityId cannot be null or blank");
        }
        if (targetIdentityId == null || targetIdentityId.isBlank()) {
            throw new IllegalArgumentException("targetIdentityId cannot be null or blank");
        }
        if (relationshipType == null || relationshipType.isBlank()) {
            throw new IllegalArgumentException("relationshipType cannot be null or blank");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("createdAt cannot be null");
        }
    }
}