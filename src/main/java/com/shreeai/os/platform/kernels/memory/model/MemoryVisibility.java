package com.shreeai.os.platform.kernels.memory.model;

/**
 * <b>MemoryVisibility</b>
 *
 * <p>Defines the visibility scope of a Memory within the platform.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides a stable enumeration of Memory visibility levels.</li>
 *   <li>Enables type-safe access control.</li>
 *   <li>Supports memory privacy and sharing policies.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Memory Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> This is a pure enumeration with no business logic.</p>
 *
 * <p><b>Constitutional Authority:</b> ADD-201</p>
 */
public enum MemoryVisibility {
    /** Memory is private to the owner */
    PRIVATE,
    /** Memory is shared with specific kernels */
    SHARED,
    /** Memory is accessible to all kernels */
    PUBLIC,
    /** Memory is restricted to system operations */
    SYSTEM
}