package platform.kernels.identity.api;

import java.time.Instant;
import java.util.Map;

/**
 * <b>IdentityProfile</b>
 *
 * <p>Represents the profile of an Identity within the platform.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the contract for Identity profile data.</li>
 *   <li>Encapsulates display name, description, and attributes.</li>
 *   <li>Provides a stable API contract independent of implementation.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Identity Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> This is a pure data contract with no business logic.</p>
 *
 * <p><b>Constitutional Authority:</b> ADD-104</p>
 *
 * @param identityId the unique identifier of the Identity
 * @param displayName the display name of the Identity
 * @param description a description of the Identity
 * @param attributes additional attributes as key-value pairs
 * @param updatedAt the timestamp when the profile was last updated
 */
public record IdentityProfile(
    String identityId,
    String displayName,
    String description,
    Map<String, Object> attributes,
    Instant updatedAt
) {
    /**
     * Creates a new IdentityProfile with validation.
     *
     * @param identityId the unique identifier of the Identity
     * @param displayName the display name of the Identity
     * @param description a description of the Identity
     * @param attributes additional attributes as key-value pairs
     * @param updatedAt the timestamp when the profile was last updated
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public IdentityProfile {
        if (identityId == null || identityId.isBlank()) {
            throw new IllegalArgumentException("identityId cannot be null or blank");
        }
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("displayName cannot be null or blank");
        }
        if (attributes == null) {
            throw new IllegalArgumentException("attributes cannot be null");
        }
        if (updatedAt == null) {
            throw new IllegalArgumentException("updatedAt cannot be null");
        }
    }
}