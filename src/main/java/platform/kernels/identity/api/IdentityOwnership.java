package platform.kernels.identity.api;

import java.time.Instant;

/**
 * <b>IdentityOwnership</b>
 *
 * <p>Represents ownership of an asset by an Identity within the platform.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the contract for Identity ownership data.</li>
 *   <li>Encapsulates the relationship between an Identity and an owned asset.</li>
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
 * @param identityId the unique identifier of the owning Identity
 * @param resourceId the unique identifier of the owned asset
 * @param resourceType the type of resource
 * @param createdAt the timestamp when ownership was registered
 */
public record IdentityOwnership(
    String identityId,
    String resourceId,
    String resourceType,
    Instant createdAt
) {
    /**
     * Creates a new IdentityOwnership with validation.
     *
     * @param identityId the unique identifier of the owning Identity
     * @param resourceId the unique identifier of the owned asset
     * @param resourceType the type of resource
     * @param createdAt the timestamp when ownership was registered
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public IdentityOwnership {
        if (identityId == null || identityId.isBlank()) {
            throw new IllegalArgumentException("identityId cannot be null or blank");
        }
        if (resourceId == null || resourceId.isBlank()) {
            throw new IllegalArgumentException("resourceId cannot be null or blank");
        }
        if (resourceType == null || resourceType.isBlank()) {
            throw new IllegalArgumentException("resourceType cannot be null or blank");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("createdAt cannot be null");
        }
    }
}