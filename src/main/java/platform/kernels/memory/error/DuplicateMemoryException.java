package platform.kernels.memory.error;

import platform.kernels.memory.model.MemoryId;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * <b>DuplicateMemoryException</b>
 *
 * <p>Thrown when a memory with the same identifier already exists in the Memory Kernel.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Signals that a memory creation or registration failed due to a duplicate.</li>
 *   <li>Provides structured error information via {@link MemoryError}.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Memory Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see MemoryException
 * @see MemoryError
 * @see MemoryErrorCode#MEMORY_DUPLICATE
 */
public class DuplicateMemoryException extends MemoryException {

    /**
     * Constructs a new {@code DuplicateMemoryException} for the given memory ID.
     *
     * @param memoryId the duplicate memory identifier (must not be null)
     */
    public DuplicateMemoryException(MemoryId memoryId) {
        this(memoryId, Collections.emptyMap());
    }

    /**
     * Constructs a new {@code DuplicateMemoryException} for the given memory ID with details.
     *
     * @param memoryId the duplicate memory identifier (must not be null)
     * @param details  additional error details (must not be null)
     */
    public DuplicateMemoryException(MemoryId memoryId, Map<String, Object> details) {
        super(createError(memoryId, details));
    }

    private static MemoryError createError(MemoryId memoryId, Map<String, Object> details) {
        String message = "Duplicate memory: " + memoryId.value();
        Map<String, Object> errorDetails = new HashMap<>(details != null ? details : new HashMap<>());
        errorDetails.put("memoryId", memoryId.value());
        return new MemoryError(
                MemoryErrorCode.MEMORY_DUPLICATE,
                message,
                java.time.Instant.now(),
                errorDetails
        );
    }
}