package platform.kernels.memory.model;

import java.util.Objects;

/**
 * <b>MemoryResult</b>
 *
 * <p>Result object for Memory operations.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Encapsulates the result of a Memory operation.</li>
 *   <li>Provides a consistent pattern for success/failure indication.</li>
 *   <li>Enables type-safe handling of Memory operation results.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Memory Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> This is an immutable data contract with no business logic.</p>
 *
 * <p><b>Constitutional Authority:</b> ADD-201</p>
 *
 * @param success whether the operation succeeded
 * @param memory the Memory if applicable, null otherwise
 * @param failureMessage the reason for failure if not successful
 */
public record MemoryResult(
    boolean success,
    Memory memory,
    String failureMessage
) {
    /**
     * Creates a successful result with a Memory.
     *
     * @param memory the Memory
     * @return a successful MemoryResult
     * @throws NullPointerException if {@code memory} is {@code null}
     */
    public static MemoryResult success(Memory memory) {
        Objects.requireNonNull(memory, "memory must not be null");
        return new MemoryResult(true, memory, null);
    }

    /**
     * Creates a failed result with a failure message.
     *
     * @param failureMessage the reason for failure
     * @return a failed MemoryResult
     * @throws NullPointerException if {@code failureMessage} is {@code null}
     */
    public static MemoryResult failure(String failureMessage) {
        Objects.requireNonNull(failureMessage, "failureMessage must not be null");
        return new MemoryResult(false, null, failureMessage);
    }
}