package com.shreeai.os.platform.kernels.planning.error;

/**
 * <b>SchedulingException</b>
 *
 * <p>Represents failures associated with scheduling operations.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Classifies scheduling failures.</li>
 *   <li>Encapsulates {@link PlanningError} for consistent reporting.</li>
 *   <li>Preserves the original cause where applicable.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Planning Kernel — Error Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> This exception classifies only. It never modifies planning objects,
 * evaluates schedule quality, or performs scheduling operations.</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-PLAN-104, EIO-ARCH-001</p>
 *
 * <p><b>Classification:</b></p>
 * <p>This exception is used for:</p>
 * <ul>
 *   <li>Schedule generation failures.</li>
 *   <li>Schedule optimization failures.</li>
 *   <li>Timeline planning failures.</li>
 *   <li>Constraint-based scheduling failures.</li>
 *   <li>Schedule evaluation failures.</li>
 * </ul>
 *
 * <p><b>What This Exception Does NOT Do:</b></p>
 * <ul>
 *   <li>Does not modify planning objects.</li>
 *   <li>Does not evaluate schedule quality.</li>
 *   <li>Does not optimize schedules.</li>
 *   <li>Does not execute scheduling operations.</li>
 * </ul>
 *
 * @since 1.0
 * @see PlanningException
 * @see PlanningError
 */
public class SchedulingException extends PlanningException {

    /**
     * Creates a new {@code SchedulingException} with the specified error.
     *
     * @param error the planning error (must not be {@code null})
     * @throws IllegalArgumentException if error is {@code null}
     */
    public SchedulingException(PlanningError error) {
        super(error);
    }

    /**
     * Creates a new {@code SchedulingException} with the specified error and cause.
     *
     * @param error the planning error (must not be {@code null})
     * @param cause the original cause (may be {@code null})
     * @throws IllegalArgumentException if error is {@code null}
     */
    public SchedulingException(PlanningError error, Throwable cause) {
        super(error, cause);
    }

    /**
     * Returns a string representation of this exception.
     *
     * @return a string representation
     */
    @Override
    public String toString() {
        return "SchedulingException{"
                + "error=" + error()
                + '}';
    }
}