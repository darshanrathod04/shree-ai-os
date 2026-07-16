package platform.kernels.identity.api;

import java.time.Instant;

/**
 * <b>IdentityRelationship</b>
 *
 * <p>Represents a relationship between two Identities within the platform.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the contract for Identity relationship data.</li>
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
 * @param sourceIdentity the source Identity in the relationship
 * @param targetIdentity the target Identity in the relationship
 * @param relationshipType the type of relationship
 * @param createdAt the timestamp when the relationship was created
 */
public record IdentityRelationship(
    Identity sourceIdentity,
    Identity targetIdentity,
    String relationshipType,
    Instant createdAt
) {
    /**
     * Creates a new IdentityRelationship with validation.
     *
     * @param sourceIdentity the source Identity in the relationship
     * @param targetIdentity the target Identity in the relationship
     * @param relationshipType the type of relationship
     * @param createdAt the timestamp when the relationship was created
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public IdentityRelationship {
        if (sourceIdentity == null) {
            throw new IllegalArgumentException("sourceIdentity cannot be null");
        }
        if (targetIdentity == null) {
            throw new IllegalArgumentException("targetIdentity cannot be null");
        }
        if (relationshipType == null || relationshipType.isBlank()) {
            throw new IllegalArgumentException("relationshipType cannot be null or blank");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("createdAt cannot be null");
        }
    }
}