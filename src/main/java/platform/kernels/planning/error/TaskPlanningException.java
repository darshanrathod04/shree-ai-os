package platform.kernels.planning.error;

/**
 * <b>TaskPlanningException</b>
 *
 * <p>Represents failures associated with task planning operations.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Classifies task planning failures.</li>
 *   <li>Encapsulates {@link PlanningError} for consistent reporting.</li>
 *   <li>Preserves the original cause where applicable.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Planning Kernel — Error Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> This exception classifies only. It never modifies planning objects,
 * evaluates plan correctness, or performs planning operations.</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-PLAN-104, EIO-ARCH-001</p>
 *
 * <p><b>Classification:</b></p>
 * <p>This exception is used for:</p>
 * <ul>
 *   <li>Task generation failures.</li>
 *   <li>Task sequencing failures.</li>
 *   <li>Task dependency description failures.</li>
 *   <li>Task grouping failures.</li>
 *   <li>Task refinement failures.</li>
 * </ul>
 *
 * <p><b>What This Exception Does NOT Do:</b></p>
 * <ul>
 *   <li>Does not modify planning objects.</li>
 *   <li>Does not evaluate plan correctness.</li>
 *   <li>Does not generate or sequence tasks.</li>
 *   <li>Does not execute planning operations.</li>
 * </ul>
 *
 * @since 1.0
 * @see PlanningException
 * @see PlanningError
 */
public class TaskPlanningException extends PlanningException {

    /**
     * Creates a new {@code TaskPlanningException} with the specified error.
     *
     * @param error the planning error (must not be {@code null})
     * @throws IllegalArgumentException if error is {@code null}
     */
    public TaskPlanningException(PlanningError error) {
        super(error);
    }

    /**
     * Creates a new {@code TaskPlanningException} with the specified error and cause.
     *
     * @param error the planning error (must not be {@code null})
     * @param cause the original cause (may be {@code null})
     * @throws IllegalArgumentException if error is {@code null}
     */
    public TaskPlanningException(PlanningError error, Throwable cause) {
        super(error, cause);
    }

    /**
     * Returns a string representation of this exception.
     *
     * @return a string representation
     */
    @Override
    public String toString() {
        return "TaskPlanningException{"
                + "error=" + error()
                + '}';
    }
}