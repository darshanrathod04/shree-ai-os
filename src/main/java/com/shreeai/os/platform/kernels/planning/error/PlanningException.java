package com.shreeai.os.platform.kernels.planning.error;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * <b>PlanningException</b>
 *
 * <p>Root exception for the Planning Kernel.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Encapsulates a {@link PlanningError} for consistent failure reporting.</li>
 *   <li>Preserves the original cause where applicable.</li>
 *   <li>Provides standard exception constructors.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Planning Kernel — Error Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> This is an immutable exception wrapper. The {@link PlanningError}
 * reference is immutable and never modified after construction.</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-PLAN-104, EIO-ARCH-001</p>
 *
 * <p><b>Exception Hierarchy:</b></p>
 * <pre>
 * RuntimeException
 *     │
 *     ▼
 * PlanningException
 *     │
 *     ├── GoalPlanningException
 *     ├── TaskPlanningException
 *     ├── SchedulingException
 *     ├── PriorityException
 *     └── PlanValidationException
 * </pre>
 *
 * @since 1.0
 */
public class PlanningException extends RuntimeException {

    private final PlanningError error;

    /**
     * Creates a new {@code PlanningException} with the specified error.
     *
     * <p>The exception encapsulates the {@link PlanningError} and preserves immutable
     * error information for consistent failure reporting.</p>
     *
     * @param error the planning error (must not be {@code null})
     * @throws IllegalArgumentException if error is {@code null}
     */
    public PlanningException(PlanningError error) {
        super(error.message());
        Objects.requireNonNull(error, "PlanningException error must not be null");
        this.error = error;
    }

    /**
     * Creates a new {@code PlanningException} with the specified error and cause.
     *
     * <p>The exception encapsulates the {@link PlanningError} and preserves the original
     * cause for debugging purposes.</p>
     *
     * @param error the planning error (must not be {@code null})
     * @param cause the original cause (may be {@code null})
     * @throws IllegalArgumentException if error is {@code null}
     */
    public PlanningException(PlanningError error, Throwable cause) {
        super(error.message(), cause);
        Objects.requireNonNull(error, "PlanningException error must not be null");
        this.error = error;
    }

    /**
     * Returns the encapsulated {@link PlanningError}.
     *
     * <p>The returned error is immutable and safe to share.</p>
     *
     * @return the planning error
     */
    public PlanningError error() {
        return error;
    }

    /**
     * Returns the error code from the encapsulated {@link PlanningError}.
     *
     * @return the {@link PlanningErrorCode}
     */
    public PlanningErrorCode errorCode() {
        return error.code();
    }

    /**
     * Returns the error message from the encapsulated {@link PlanningError}.
     *
     * @return the error message
     */
    @Override
    public String getMessage() {
        return error.message();
    }

    /**
     * Returns the occurrence timestamp from the encapsulated {@link PlanningError}.
     *
     * @return the occurrence {@link Instant}
     */
    public Instant occurredAt() {
        return error.occurredAt();
    }

    /**
     * Returns an unmodifiable view of the error metadata from the encapsulated {@link PlanningError}.
     *
     * <p>The returned map is a defensive copy to preserve immutability.</p>
     *
     * @return an unmodifiable view of the metadata
     */
    public Map<String, Object> metadata() {
        return error.metadata();
    }

    /**
     * Returns a string representation of this exception.
     *
     * <p>Includes the error code, message, and occurrence timestamp.</p>
     *
     * @return a string representation
     */
    @Override
    public String toString() {
        return "PlanningException{"
                + "error=" + error
                + '}';
    }
}