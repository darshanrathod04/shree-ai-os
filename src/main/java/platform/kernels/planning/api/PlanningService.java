package platform.kernels.planning.api;

/**
 * <b>PlanningService</b>
 *
 * <p>Primary façade for the Planning Kernel, providing high-level planning
 * operations and coordinating planning-related contracts.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Exposes high-level planning operations for the platform.</li>
 *   <li>Coordinates planning-related service contracts.</li>
 *   <li>Provides stable API boundaries for planning capabilities.</li>
 *   <li>Delegates specialized planning tasks to subordinate services.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Interface-only — no implementation logic.</li>
 *   <li>Technology-agnostic — no framework dependencies.</li>
 *   <li>Contract-focused — exposes only planning contracts.</li>
 *   <li>Stateless — no mutable state.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Planning Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-PLAN-101, EIO-ARCH-001</p>
 *
 * @see GoalPlanningService
 * @see TaskPlanningService
 * @see SchedulingService
 * @see PrioritizationService
 * @see PlanValidationService
 */
public interface PlanningService {

    /**
     * Creates a comprehensive plan from cognitive intent.
     *
     * <p>This operation transforms cognitive intent into a structured plan
     * by coordinating goal decomposition, task planning, scheduling,
     * prioritization, and validation.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param planningRequest the planning request parameters (must not be {@code null})
     * @return a plan identifier
     * @throws IllegalArgumentException if planningRequest is {@code null}
     */
    String createPlan(PlanningRequest planningRequest);

    /**
     * Refines an existing plan based on new information or constraints.
     *
     * <p>This operation updates a plan while maintaining its overall structure
     * and objectives.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param planRefinementRequest the plan refinement parameters (must not be {@code null})
     * @return a refined plan identifier
     * @throws IllegalArgumentException if planRefinementRequest is {@code null}
     */
    String refinePlan(PlanRefinementRequest planRefinementRequest);

    /**
     * Validates a plan for completeness and consistency.
     *
     * <p>This operation performs comprehensive validation of a plan including
     * dependency verification, constraint checking, and completeness assessment.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param planValidationRequest the validation request parameters (must not be {@code null})
     * @return a validation result identifier
     * @throws IllegalArgumentException if planValidationRequest is {@code null}
     */
    String validatePlan(PlanValidationRequest planValidationRequest);

    /**
     * Represents a planning request.
     *
     * @param objectiveId      the objective identifier (must not be {@code null} or empty)
     * @param planningScope    the scope of planning (must not be {@code null})
     * @param constraints      the planning constraints (must not be {@code null})
     */
    record PlanningRequest(String objectiveId,
                           PlanningTypes.PlanningScope planningScope,
                           PlanningTypes.PlanningConstraints constraints) {
    }

    /**
     * Represents a plan refinement request.
     *
     * @param planId           the plan identifier (must not be {@code null} or empty)
     * @param refinementContext the refinement context (must not be {@code null})
     * @param updatedConstraints the updated constraints (must not be {@code null})
     */
    record PlanRefinementRequest(String planId,
                                 PlanningTypes.RefinementContext refinementContext,
                                 PlanningTypes.PlanningConstraints updatedConstraints) {
    }

    /**
     * Represents a plan validation request.
     *
     * @param planId           the plan identifier (must not be {@code null} or empty)
     * @param validationCriteria the validation criteria (must not be {@code null})
     */
    record PlanValidationRequest(String planId,
                                 PlanningTypes.ValidationCriteria validationCriteria) {
    }

    /**
     * Defines the scope of planning.
     */
    enum PlanningScope {
        /**
         * Surface-level planning focusing on immediate goals.
         */
        SURFACE,

        /**
         * Standard planning with moderate depth.
         */
        STANDARD,

        /**
         * Deep planning with comprehensive decomposition.
         */
        DEEP,

        /**
         * Comprehensive planning with full dependency analysis.
         */
        COMPREHENSIVE
    }
}