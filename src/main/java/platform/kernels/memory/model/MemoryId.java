package platform.kernels.memory.model;

import java.util.Objects;

/**
 * <b>MemoryId</b>
 *
 * <p>Represents the unique identifier for a Memory within the platform.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides a stable, immutable identifier for Memory entities.</li>
 *   <li>Ensures type-safe memory references across the platform.</li>
 *   <li>Encapsulates memory identification logic.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Memory Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> This is an immutable value object with no business logic.</p>
 *
 * <p><b>Constitutional Authority:</b> ADD-201</p>
 *
 * @param value the unique identifier value
 */
public record MemoryId(String value) {
    /**
     * Creates a new MemoryId with null validation.
     *
     * @param value the unique identifier value
     * @throws NullPointerException if {@code value} is {@code null}
     */
    public MemoryId {
        Objects.requireNonNull(value, "value must not be null");
    }
}