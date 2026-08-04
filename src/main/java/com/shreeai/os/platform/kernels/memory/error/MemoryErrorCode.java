package com.shreeai.os.platform.kernels.memory.error;

/**
 * <b>MemoryErrorCode</b>
 *
 * <p>Enumeration of all possible Memory Kernel error conditions within Shree AI OS.</p>
 *
 * <p><b>Ownership:</b> Memory Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 */
public enum MemoryErrorCode {

    /**
     * The requested memory was not found.
     */
    MEMORY_NOT_FOUND,

    /**
     * A duplicate memory already exists.
     */
    MEMORY_DUPLICATE,

    /**
     * The memory is structurally invalid.
     */
    MEMORY_INVALID,

    /**
     * Memory validation failed.
     */
    MEMORY_VALIDATION_FAILED,

    /**
     * The memory already exists.
     */
    MEMORY_ALREADY_EXISTS,

    /**
     * Memory import operation failed.
     */
    MEMORY_IMPORT_FAILED,

    /**
     * Memory export operation failed.
     */
    MEMORY_EXPORT_FAILED
}