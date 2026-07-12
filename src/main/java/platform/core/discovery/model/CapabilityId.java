package platform.core.discovery.model;

import java.util.Objects;

/**
 * <b>CapabilityId</b>
 *
 * <p>Represents the unique identity of a platform capability within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides a type-safe identifier for platform capabilities.</li>
 *   <li>Enables kernels to request capabilities without knowing implementation details.</li>
 *   <li>Supports the Discovery Service's capability resolution mechanism.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> The underlying identifier string SHALL NOT be null or blank.</p>
 *
 * <p><b>Constitutional Authority:</b> KERNEL-006</p>
 *
 * @see platform.core.discovery.model.DiscoveryResult
 */
public final class CapabilityId {

    private final String id;

    /**
     * Constructs a new {@code CapabilityId} with the given identifier string.
     *
     * @param id the unique identifier string for this capability
     * @throws IllegalArgumentException if {@code id} is {@code null} or blank
     */
    public CapabilityId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("CapabilityId must not be null or blank");
        }
        this.id = id;
    }

    /**
     * Returns the underlying identifier string.
     *
     * @return the capability identifier string
     */
    public String value() {
        return id;
    }

    /**
     * Compares this {@code CapabilityId} to the specified object for equality.
     *
     * @param o the object to compare to
     * @return {@code true} if the given object is a {@code CapabilityId} with the same identifier
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CapabilityId that = (CapabilityId) o;
        return id.equals(that.id);
    }

    /**
     * Returns the hash code for this {@code CapabilityId}.
     *
     * @return the hash code based on the identifier string
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Returns a string representation of this {@code CapabilityId}.
     *
     * @return the identifier string
     */
    @Override
    public String toString() {
        return id;
    }
}