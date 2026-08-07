package com.shreeai.os.platform.core.registry.model;

import com.shreeai.os.platform.core.registry.api.KernelRegistry;

import java.util.Objects;

/**
 * <b>RegisteredKernel</b>
 *
 * <p>Represents a registered Kernel within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Composes the identity, version, and metadata of a registered kernel into a single value object.</li>
 *   <li>Serves as the registration entry type for the {@link KernelRegistry}.</li>
 *   <li>Contains no behavior — it is a pure data carrier representing the registration state.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> All fields SHALL be non-null and validated at construction time.</p>
 *
 * <p><b>Constitutional Authority:</b> KERNEL-005</p>
 *
 * @see KernelId
 * @see KernelVersion
 * @see KernelMetadata
 */
public final class RegisteredKernel {

    private final KernelId kernelId;
    private final KernelVersion version;
    private final KernelMetadata metadata;

    /**
     * Constructs a new {@code RegisteredKernel} with the given identity, version, and metadata.
     *
     * @param kernelId the unique kernel identity (must not be null)
     * @param version  the kernel version (must not be null)
     * @param metadata the kernel metadata (must not be null)
     * @throws NullPointerException if any parameter is null
     */
    public RegisteredKernel(KernelId kernelId, KernelVersion version, KernelMetadata metadata) {
        this.kernelId = Objects.requireNonNull(kernelId, "KernelId must not be null");
        this.version = Objects.requireNonNull(version, "KernelVersion must not be null");
        this.metadata = Objects.requireNonNull(metadata, "KernelMetadata must not be null");
    }

    /**
     * Returns the unique identity of this registered kernel.
     *
     * @return the kernel identity
     */
    public KernelId kernelId() {
        return kernelId;
    }

    /**
     * Returns the version of this registered kernel.
     *
     * @return the kernel version
     */
    public KernelVersion version() {
        return version;
    }

    /**
     * Returns the descriptive metadata of this registered kernel.
     *
     * @return the kernel metadata
     */
    public KernelMetadata metadata() {
        return metadata;
    }

    /**
     * Compares this {@code RegisteredKernel} to the specified object for equality.
     *
     * @param o the object to compare to
     * @return {@code true} if the given object is a {@code RegisteredKernel} with the same
     *         identity, version, and metadata
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegisteredKernel that = (RegisteredKernel) o;
        return kernelId.equals(that.kernelId)
                && version.equals(that.version)
                && metadata.equals(that.metadata);
    }

    /**
     * Returns the hash code for this {@code RegisteredKernel}.
     *
     * @return the hash code based on identity, version, and metadata
     */
    @Override
    public int hashCode() {
        return Objects.hash(kernelId, version, metadata);
    }

    /**
     * Returns a string representation of this {@code RegisteredKernel}.
     *
     * @return a string containing the kernel identity and version
     */
    @Override
    public String toString() {
        return "RegisteredKernel{"
                + "kernelId=" + kernelId
                + ", version=" + version
                + ", name=" + metadata.name()
                + '}';
    }
}