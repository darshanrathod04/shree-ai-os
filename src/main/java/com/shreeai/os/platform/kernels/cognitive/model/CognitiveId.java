package com.shreeai.os.platform.kernels.cognitive.model;

/**
 * <b>CognitiveId</b>
 *
 * <p>Represents the unique identifier for a cognitive entity within the platform.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides a stable, immutable identifier for cognitive entities.</li>
 *   <li>Ensures type-safe identity references across the platform.</li>
 *   <li>Encapsulates cognitive entity identification logic.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Cognitive Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> This is an immutable value object with no business logic.</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-COG-102, EIO-ARCH-001</p>
 *
 * @param value the unique identifier value (must not be {@code null} or empty)
 */
public record CognitiveId(String value) {

    /**
     * Creates a new CognitiveId with the specified value.
     *
     * <p>Performs defensive validation to ensure the identifier is non-null and non-empty.</p>
     *
     * @param value the unique identifier value (must not be {@code null} or empty)
     * @throws IllegalArgumentException if value is {@code null} or empty
     */
    public CognitiveId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("CognitiveId value must not be null or empty");
        }
    }
}