package com.shreeai.os.platform.core.registry.model;

import java.util.Objects;

/**
 * <b>KernelId</b>
 *
 * <p>Represents the unique identity of a Kernel within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides a type-safe identifier for kernel registration and discovery.</li>
 *   <li>Ensures every kernel has exactly one immutable identity (KR-001).</li>
 *   <li>Kernel IDs are immutable and cannot be changed after creation (KR-002).</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> The underlying identifier string SHALL NOT be null or blank.</p>
 *
 * <p><b>Constitutional Authority:</b> KERNEL-005</p>
 *
 * @see RegisteredKernel
 */
public final class KernelId {

    private final String id;

    /**
     * Constructs a new {@code KernelId} with the given identifier string.
     *
     * @param id the unique identifier string for this kernel
     * @throws IllegalArgumentException if {@code id} is {@code null} or blank
     */
    public KernelId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("KernelId must not be null or blank");
        }
        this.id = id;
    }

    /**
     * Returns the underlying identifier string.
     *
     * @return the kernel identifier string
     */
    public String value() {
        return id;
    }

    /**
     * Compares this {@code KernelId} to the specified object for equality.
     *
     * @param o the object to compare to
     * @return {@code true} if the given object is a {@code KernelId} with the same identifier
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KernelId kernelId = (KernelId) o;
        return id.equals(kernelId.id);
    }

    /**
     * Returns the hash code for this {@code KernelId}.
     *
     * @return the hash code based on the identifier string
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Returns a string representation of this {@code KernelId}.
     *
     * @return the identifier string
     */
    @Override
    public String toString() {
        return id;
    }
}