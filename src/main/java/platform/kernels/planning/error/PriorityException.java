package platform.kernels.planning.error;

/**
 * <b>PriorityException</b>
 *
 * <p>Represents failures associated with prioritization operations.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Classifies prioritization failures.</li>
 *   <li>Encapsulates {@link PlanningError} for consistent reporting.</li>
 *   <li>Preserves the original cause where applicable.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Planning Kernel — Error Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> This exception classifies only. It never modifies planning objects,
 * evaluates priority correctness, or performs prioritization operations.</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-PLAN-104, EIO-ARCH-001</p>
 *
 * <p><b>Classification:</b></p>
 * <p>This exception is used for:</p>
 * <ul>
 *   <li>Priority assignment failures.</li>
 *   <li>Priority ordering failures.</li>
 *   <li>Urgency classification failures.</li>
 *   <li>Importance evaluation failures.</li>
 *   <li>Prioritization policy application failures.</li>
 * </ul>
 *
 * <p><b>What This Exception Does NOT Do:</b></p>
 * <ul>
 *   <li>Does not modify planning objects.</li>
 *   <li>Does not evaluate priority correctness.</li>
 *   <li>Does not compute priorities.</li>
 *   <li>Does not execute prioritization operations.</li>
 * </ul>
 *
 * @since 1.0
 * @see PlanningException
 * @see PlanningError
 */
public class PriorityException extends PlanningException {

    /**
     * Creates a new {@code PriorityException} with the specified error.
     *
     * @param error the planning error (must not be {@code null})
     * @throws IllegalArgumentException if error is {@code null}
     */
    public PriorityException(PlanningError error) {
        super(error);
    }

    /**
     * Creates a new {@code PriorityException} with the specified error and cause.
     *
     * @param error the planning error (must not be {@code null})
     * @param cause the original cause (may be {@code null})
     * @throws IllegalArgumentException if error is {@code null}
     */
    public PriorityException(PlanningError error, Throwable cause) {
        super(error, cause);
    }

    /**
     * Returns a string representation of this exception.
     *
     * @return a string representation
     */
    @Override
    public String toString() {
        return "PriorityException{"
                + "error=" + error()
                + '}';
    }
}