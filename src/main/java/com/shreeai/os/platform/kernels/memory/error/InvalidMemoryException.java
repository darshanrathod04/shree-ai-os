package com.shreeai.os.platform.kernels.memory.error;

import com.shreeai.os.platform.kernels.memory.model.Memory;
import com.shreeai.os.platform.kernels.memory.model.MemoryId;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * <b>InvalidMemoryException</b>
 *
 * <p>Thrown when a memory fails structural validation.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Signals that a memory validation failed.</li>
 *   <li>Provides structured error information via {@link MemoryError}.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Memory Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see MemoryException
 * @see MemoryError
 * @see MemoryErrorCode#MEMORY_INVALID
 * @see MemoryErrorCode#MEMORY_VALIDATION_FAILED
 */
public class InvalidMemoryException extends MemoryException {

    /**
     * Constructs a new {@code InvalidMemoryException} for the given memory ID with a reason.
     *
     * @param memoryId the identifier of the invalid memory (must not be null)
     * @param reason   the validation failure reason (must not be null or blank)
     */
    public InvalidMemoryException(MemoryId memoryId, String reason) {
        this(memoryId, reason, Collections.emptyMap());
    }

    /**
     * Constructs a new {@code InvalidMemoryException} for the given memory ID with reason and details.
     *
     * @param memoryId the identifier of the invalid memory (must not be null)
     * @param reason   the validation failure reason (must not be null or blank)
     * @param details  additional error details (must not be null)
     */
    public InvalidMemoryException(MemoryId memoryId, String reason, Map<String, Object> details) {
        super(createError(memoryId, reason, details));
    }

    /**
     * Constructs a new {@code InvalidMemoryException} for the given memory with a reason.
     *
     * @param memory the memory that is invalid (must not be null)
     * @param reason the validation failure reason (must not be null or blank)
     */
    public InvalidMemoryException(Memory memory, String reason) {
        this(memory, reason, Collections.emptyMap());
    }

    /**
     * Constructs a new {@code InvalidMemoryException} for the given memory with reason and details.
     *
     * @param memory  the memory that is invalid (must not be null)
     * @param reason  the validation failure reason (must not be null or blank)
     * @param details additional error details (must not be null)
     */
    public InvalidMemoryException(Memory memory, String reason, Map<String, Object> details) {
        super(createError(memory.id(), reason, details));
    }

    private static MemoryError createError(MemoryId memoryId, String reason, Map<String, Object> details) {
        String message = "Invalid memory: " + reason;
        Map<String, Object> errorDetails = new HashMap<>(details != null ? details : new HashMap<>());
        errorDetails.put("memoryId", memoryId.value());
        errorDetails.put("reason", reason);
        return new MemoryError(
                MemoryErrorCode.MEMORY_INVALID,
                message,
                java.time.Instant.now(),
                errorDetails
        );
    }
}