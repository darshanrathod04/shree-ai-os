package com.shreeai.os.platform.kernels.memory.error;

/**
 * <b>MemoryException</b>
 *
 * <p>The base runtime exception for all Memory errors within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides the base exception type for all Memory errors.</li>
 *   <li>Wraps a {@link MemoryError} to provide structured error information.</li>
 *   <li>Enables consistent exception handling across the Memory Kernel.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Memory Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Note:</b> This SHALL become the ONLY base exception for the Memory subsystem.
 * All concrete exceptions extend this class.</p>
 *
 * @see MemoryError
 * @see MemoryNotFoundException
 * @see DuplicateMemoryException
 * @see InvalidMemoryException
 */
public class MemoryException extends RuntimeException {

    private final MemoryError error;

    /**
     * Constructs a new {@code MemoryException} with the given error.
     *
     * @param error the memory error (must not be null)
     * @throws IllegalArgumentException if {@code error} is {@code null}
     */
    public MemoryException(MemoryError error) {
        super(error.message());
        this.error = error;
    }

    /**
     * Returns the memory error associated with this exception.
     *
     * @return the memory error
     */
    public MemoryError error() {
        return error;
    }

    /**
     * Returns the error code.
     *
     * @return the error code
     */
    public MemoryErrorCode code() {
        return error.code();
    }

    /**
     * Returns the error message.
     *
     * @return the error message
     */
    @Override
    public String getMessage() {
        return error.message();
    }
}