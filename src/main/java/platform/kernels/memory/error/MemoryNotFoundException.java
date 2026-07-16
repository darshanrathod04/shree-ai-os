package platform.kernels.memory.error;

import platform.kernels.memory.model.MemoryId;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * <b>MemoryNotFoundException</b>
 *
 * <p>Thrown when a requested memory is not found in the Memory Kernel.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Signals that a memory lookup failed.</li>
 *   <li>Provides structured error information via {@link MemoryError}.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Memory Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see MemoryException
 * @see MemoryError
 * @see MemoryErrorCode#MEMORY_NOT_FOUND
 */
public class MemoryNotFoundException extends MemoryException {

    /**
     * Constructs a new {@code MemoryNotFoundException} for the given memory ID.
     *
     * @param memoryId the memory that was not found (must not be null)
     */
    public MemoryNotFoundException(MemoryId memoryId) {
        this(memoryId, Collections.emptyMap());
    }

    /**
     * Constructs a new {@code MemoryNotFoundException} for the given memory ID with details.
     *
     * @param memoryId the memory that was not found (must not be null)
     * @param details  additional error details (must not be null)
     */
    public MemoryNotFoundException(MemoryId memoryId, Map<String, Object> details) {
        super(createError(memoryId, details));
    }

    private static MemoryError createError(MemoryId memoryId, Map<String, Object> details) {
        String message = "Memory not found: " + memoryId.value();
        Map<String, Object> errorDetails = new HashMap<>(details != null ? details : new HashMap<>());
        errorDetails.put("memoryId", memoryId.value());
        return new MemoryError(
                MemoryErrorCode.MEMORY_NOT_FOUND,
                message,
                java.time.Instant.now(),
                errorDetails
        );
    }
}