package platform.kernels.execution.model;

import java.util.Objects;

/**
 * <b>ExecutionId</b>
 *
 * <p>Represents the unique identity of an execution instance.
 * This is an immutable value object that serves as the canonical identity
 * for Execution aggregate roots.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides unique identity for execution instances.</li>
 *   <li>Ensures consistent identity representation across the platform.</li>
 *   <li>Maintains value semantics for identity comparison.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Immutable — all fields are final.</li>
 *   <li>Constructor validation — rejects null or empty identifiers.</li>
 *   <li>Value-based equality — implements equals, hashCode, toString.</li>
 *   <li>Consistent with other kernel identity types (IdentityId, MemoryId, etc.).</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Execution Kernel — Domain Model</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-EXEC-102, EIO-ARCH-001</p>
 *
 * @param value the unique execution identifier (must not be {@code null} or empty)
 *
 * @since 1.0
 */
public final class ExecutionId {

    private final String value;

    /**
     * Constructs an {@code ExecutionId} with the specified identifier value.
     *
     * @param value the unique execution identifier (must not be {@code null} or empty)
     * @throws IllegalArgumentException if value is {@code null} or empty
     */
    public ExecutionId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("ExecutionId value must not be null or empty");
        }
        this.value = value;
    }

    /**
     * Returns the execution identifier value.
     *
     * @return the execution identifier value
     */
    public String value() {
        return value;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * <p>Two {@code ExecutionId} instances are equal if they have the same
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
        ExecutionId that = (ExecutionId) obj;
        return Objects.equals(value, that.value);
    }

    /**
     * Returns a hash code value for this {@code ExecutionId}.
     *
     * @return a hash code value
     */
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    /**
     * Returns a string representation of this {@code ExecutionId}.
     *
     * @return a string representation
     */
    @Override
    public String toString() {
        return "ExecutionId{" +
                "value='" + value + '\'' +
                '}';
    }
}