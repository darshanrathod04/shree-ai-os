package com.shreeai.os.platform.core.registry.model;

import java.util.Objects;

/**
 * <b>KernelVersion</b>
 *
 * <p>Represents the semantic version of a Kernel within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides a type-safe version identifier for kernel registration and discovery.</li>
 *   <li>Enables version compatibility resolution during kernel discovery (KD-004).</li>
 *   <li>Supports the {@link Comparable} contract to enable version ordering and range queries.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> Major, minor, and patch version numbers SHALL be non-negative.</p>
 *
 * <p><b>Constitutional Authority:</b> KERNEL-005, KERNEL-006</p>
 *
 * @see RegisteredKernel
 */
public final class KernelVersion implements Comparable<KernelVersion> {

    private final int major;
    private final int minor;
    private final int patch;

    /**
     * Constructs a new {@code KernelVersion} with the given major, minor, and patch numbers.
     *
     * @param major the major version number
     * @param minor the minor version number
     * @param patch the patch version number
     * @throws IllegalArgumentException if any version number is negative
     */
    public KernelVersion(int major, int minor, int patch) {
        if (major < 0) {
            throw new IllegalArgumentException("Major version must be non-negative, got: " + major);
        }
        if (minor < 0) {
            throw new IllegalArgumentException("Minor version must be non-negative, got: " + minor);
        }
        if (patch < 0) {
            throw new IllegalArgumentException("Patch version must be non-negative, got: " + patch);
        }
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    /**
     * Returns the major version number.
     *
     * @return the major version
     */
    public int major() {
        return major;
    }

    /**
     * Returns the minor version number.
     *
     * @return the minor version
     */
    public int minor() {
        return minor;
    }

    /**
     * Returns the patch version number.
     *
     * @return the patch version
     */
    public int patch() {
        return patch;
    }

    /**
     * Compares this {@code KernelVersion} to another version for ordering.
     *
     * <p>Comparison is performed by evaluating major, then minor, then patch
     * in descending order of significance.</p>
     *
     * @param other the other version to compare to
     * @return a negative integer, zero, or a positive integer as this version
     *         is less than, equal to, or greater than the specified version
     */
    @Override
    public int compareTo(KernelVersion other) {
        int majorComparison = Integer.compare(this.major, other.major);
        if (majorComparison != 0) {
            return majorComparison;
        }
        int minorComparison = Integer.compare(this.minor, other.minor);
        if (minorComparison != 0) {
            return minorComparison;
        }
        return Integer.compare(this.patch, other.patch);
    }

    /**
     * Compares this {@code KernelVersion} to the specified object for equality.
     *
     * @param o the object to compare to
     * @return {@code true} if the given object is a {@code KernelVersion} with the same
     *         major, minor, and patch numbers
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KernelVersion that = (KernelVersion) o;
        return major == that.major && minor == that.minor && patch == that.patch;
    }

    /**
     * Returns the hash code for this {@code KernelVersion}.
     *
     * @return the hash code based on major, minor, and patch numbers
     */
    @Override
    public int hashCode() {
        return Objects.hash(major, minor, patch);
    }

    /**
     * Returns a string representation of this {@code KernelVersion} in {@code major.minor.patch} format.
     *
     * @return the version string (e.g. "1.2.3")
     */
    @Override
    public String toString() {
        return major + "." + minor + "." + patch;
    }
}