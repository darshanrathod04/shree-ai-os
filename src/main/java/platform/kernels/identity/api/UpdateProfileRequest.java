package platform.kernels.identity.api;

import java.time.Instant;

/**
 * <b>UpdateProfileRequest</b>
 *
 * <p>Request object for updating an Identity's profile.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the contract for Identity profile update requests.</li>
 *   <li>Encapsulates all attributes that can be updated in an Identity's profile.</li>
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
 * @param displayName the display name for the Identity
 * @param description a description of the Identity
 * @param updatedAt the timestamp when the profile was updated
 */
public record UpdateProfileRequest(
    String displayName,
    String description,
    Instant updatedAt
) {
    /**
     * Creates a new UpdateProfileRequest with validation.
     *
     * @param displayName the display name for the Identity
     * @param description a description of the Identity
     * @param updatedAt the timestamp when the profile was updated
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public UpdateProfileRequest {
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("displayName cannot be null or blank");
        }
        if (updatedAt == null) {
            throw new IllegalArgumentException("updatedAt cannot be null");
        }
    }
}