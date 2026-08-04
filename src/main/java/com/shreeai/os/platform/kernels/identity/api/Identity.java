package com.shreeai.os.platform.kernels.identity.api;

import java.time.Instant;

/**
 * <b>Identity</b>
 *
 * <p>Represents an Identity within the platform.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the core Identity data contract.</li>
 *   <li>Encapsulates essential Identity attributes.</li>
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
 * @param id the unique identifier for the Identity
 * @param name the unique name of the Identity
 * @param type the type of Identity
 * @param createdAt the timestamp when the Identity was created
 */
public record Identity(
    String id,
    String name,
    IdentityType type,
    Instant createdAt
) {
    /**
     * Creates a new Identity with validation.
     *
     * @param id the unique identifier for the Identity
     * @param name the unique name of the Identity
     * @param type the type of Identity
     * @param createdAt the timestamp when the Identity was created
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public Identity {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id cannot be null or blank");
        }
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