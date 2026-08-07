package com.shreeai.os.platform.kernels.identity.api;

import java.time.Instant;

/**
 * <b>CreateIdentityRequest</b>
 *
 * <p>Request object for creating a new Identity within the platform.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the contract for Identity creation requests.</li>
 *   <li>Encapsulates all required attributes for Identity creation.</li>
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
 * @param name the unique name for the Identity
 * @param type the type of Identity (HUMAN, AGENT, ORGANIZATION, DEVICE, SERVICE, PLUGIN)
 * @param createdAt the timestamp when the Identity is created
 */
public record CreateIdentityRequest(
    String name,
    IdentityType type,
    Instant createdAt
) {
    /**
     * Creates a new CreateIdentityRequest with validation.
     *
     * @param name the unique name for the Identity
     * @param type the type of Identity
     * @param createdAt the timestamp when the Identity is created
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public CreateIdentityRequest {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name cannot be null or blank");
        }
        if (type == null) {
            throw new IllegalArgumentException("type cannot be null");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("createdAt cannot be null");
        }
    }
}