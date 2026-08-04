package com.shreeai.os.platform.core.discovery.model;

import java.util.Objects;

/**
 * <b>ContractId</b>
 *
 * <p>Represents a discoverable platform contract within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides a type-safe identifier for platform contracts.</li>
 *   <li>Enables kernels to discover other kernels by contract type.</li>
 *   <li>Supports version compatibility resolution (KD-004).</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> The underlying identifier string SHALL NOT be null or blank.</p>
 *
 * <p><b>Constitutional Authority:</b> KERNEL-006</p>
 *
 * @see DiscoveryResult
 */
public final class ContractId {

    private final String id;

    /**
     * Constructs a new {@code ContractId} with the given identifier string.
     *
     * @param id the unique identifier string for this contract
     * @throws IllegalArgumentException if {@code id} is {@code null} or blank
     */
    public ContractId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("ContractId must not be null or blank");
        }
        this.id = id;
    }

    /**
     * Returns the underlying identifier string.
     *
     * @return the contract identifier string
     */
    public String value() {
        return id;
    }

    /**
     * Compares this {@code ContractId} to the specified object for equality.
     *
     * @param o the object to compare to
     * @return {@code true} if the given object is a {@code ContractId} with the same identifier
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContractId that = (ContractId) o;
        return id.equals(that.id);
    }

    /**
     * Returns the hash code for this {@code ContractId}.
     *
     * @return the hash code based on the identifier string
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Returns a string representation of this {@code ContractId}.
     *
     * @return the identifier string
     */
    @Override
    public String toString() {
        return id;
    }
}