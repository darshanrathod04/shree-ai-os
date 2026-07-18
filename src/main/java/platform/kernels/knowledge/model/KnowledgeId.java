package platform.kernels.knowledge.model;

import java.util.Objects;

/**
 * <b>KnowledgeId</b>
 *
 * <p>Represents the unique identifier for a Knowledge entity within the platform.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides a unique identifier for Knowledge entities.</li>
 *   <li>Ensures type safety for Knowledge references.</li>
 *   <li>Immutable value object.</li>
 * </ul>
 *
 * <p><b>Immutability:</b> This record is immutable. The value field is final
 * and set via constructor.</p>
 *
 * <p><b>Thread Safety:</b> This class is thread-safe. Immutable objects
 * can be safely shared across threads.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-KNW-101, EIO-KNW-102</p>
 *
 * @param value the unique identifier value (must not be null or blank)
 */
public record KnowledgeId(String value) {
    /**
     * Creates a new KnowledgeId with null and blank validation.
     *
     * @param value the unique identifier value (must not be null or blank)
     * @throws NullPointerException if {@code value} is null
     * @throws IllegalArgumentException if {@code value} is blank
     */
    public KnowledgeId {
        Objects.requireNonNull(value, "value must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }
    }
}