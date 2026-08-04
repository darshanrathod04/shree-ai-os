package com.shreeai.os.platform.kernels.chief.model;

import java.util.Objects;

/**
 * <b>ChiefId</b>
 *
 * <p>Represents the unique identity of an orchestration instance.
 * This is an immutable value object that serves as the canonical identity
 * for Chief aggregate roots.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides unique identity for orchestration instances.</li>
 *   <li>Ensures consistent identity representation across the platform.</li>
 *   <li>Maintains value semantics for identity comparison.</li>
 *   <li>Architecturally consistent with IdentityId, MemoryId, ContextId, KnowledgeId, CognitiveId, PlanningId, and ExecutionId.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Immutable — all fields are final.</li>
 *   <li>Constructor validation — rejects null or empty identifiers.</li>
 *   <li>Value-based equality — implements equals, hashCode, toString.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Chief Kernel — Domain Model</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-CHIEF-102, EIO-ARCH-001</p>
 *
 * @param value the unique orchestration identifier (must not be {@code null} or empty)
 *
 * @since 1.0
 */
public final class ChiefId {

    private final String value;

    /**
     * Constructs a {@code ChiefId} with the specified identifier value.
     *
     * @param value the unique orchestration identifier (must not be {@code null} or empty)
     * @throws IllegalArgumentException if value is {@code null} or empty
     */
    public ChiefId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("ChiefId value must not be null or empty");
        }
        this.value = value;
    }

    /**
     * Returns the orchestration identifier value.
     *
     * @return the orchestration identifier value
     */
    public String value() {
        return value;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * <p>Two {@code ChiefId} instances are equal if they have the same
     * identifier value.</p>
     *
     * @param obj the reference object with which to compare
     * @return {@code true} if this object is equal to the {@code obj} argument
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ChiefId that = (ChiefId) obj;
        return Objects.equals(value, that.value);
    }

    /**
     * Returns a hash code value for this {@code ChiefId}.
     *
     * @return a hash code value
     */
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    /**
     * Returns a string representation of this {@code ChiefId}.
     *
     * @return a string representation
     */
    @Override
    public String toString() {
        return "ChiefId{" +
                "value='" + value + '\'' +
                '}';
    }
}