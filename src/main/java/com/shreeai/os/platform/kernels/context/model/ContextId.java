package com.shreeai.os.platform.kernels.context.model;

import java.util.Objects;

/**
 * <b>ContextId</b>
 *
 * <p>Represents the unique identifier for a Context within the platform.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides a unique identifier for Context instances.</li>
 *   <li>Ensures type safety for Context references.</li>
 *   <li>Immutable value object.</li>
 * </ul>
 *
 * <p><b>Immutability:</b> This class is immutable. The value field is final
 * and set via constructor.</p>
 *
 * <p><b>Thread Safety:</b> This class is thread-safe. Immutable objects
 * can be safely shared across threads.</p>
 *
 * <p><b>Ownership:</b> Context Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-CTX-101</p>
 *
 * @param value the unique identifier value (must not be null or blank)
 */
public record ContextId(String value) {
    /**
     * Creates a new ContextId with null validation.
     *
     * @param value the unique identifier value (must not be null or blank)
     * @throws NullPointerException if {@code value} is null
     * @throws IllegalArgumentException if {@code value} is blank
     */
    public ContextId {
        Objects.requireNonNull(value, "value must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }
    }
}