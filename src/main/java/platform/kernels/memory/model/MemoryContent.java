package platform.kernels.memory.model;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * <b>MemoryContent</b>
 *
 * <p>Represents the content of a Memory within the platform.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the contract for Memory content data.</li>
 *   <li>Encapsulates the actual information stored in a Memory.</li>
 *   <li>Provides a stable API contract independent of implementation.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Memory Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> This is an immutable data contract with no business logic.</p>
 *
 * <p><b>Constitutional Authority:</b> ADD-201</p>
 *
 * @param text the textual content of the memory
 * @param embedding optional vector embedding for similarity search
 * @param metadata additional metadata as key-value pairs
 * @param createdAt when the content was created
 */
public record MemoryContent(
    String text,
    double[] embedding,
    Map<String, Object> metadata,
    Instant createdAt
) {
    /**
     * Creates a new MemoryContent with null validation and defensive copying.
     *
     * @param text the textual content of the memory
     * @param embedding optional vector embedding for similarity search
     * @param metadata additional metadata as key-value pairs
     * @param createdAt when the content was created
     * @throws NullPointerException if any required parameter is {@code null}
     */
    public MemoryContent {
        Objects.requireNonNull(text, "text must not be null");
        Objects.requireNonNull(metadata, "metadata must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        if (embedding != null) {
            embedding = embedding.clone();
        }
        metadata = Collections.unmodifiableMap(Map.copyOf(metadata));
    }
}