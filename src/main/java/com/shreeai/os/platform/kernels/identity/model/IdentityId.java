package com.shreeai.os.platform.kernels.identity.model;

/**
 * <b>IdentityId</b>
 *
 * <p>Represents the unique identifier for an Identity within the platform.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides a stable, immutable identifier for Identity entities.</li>
 *   <li>Ensures type-safe identity references across the platform.</li>
 *   <li>Encapsulates identity identification logic.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Identity Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> This is an immutable value object with no business logic.</p>
 *
 * <p><b>Constitutional Authority:</b> ADD-104</p>
 *
 * @param value the unique identifier value
 */
public record IdentityId(String value) {
}