package platform.kernels.planning.api;

import platform.kernels.planning.model.ValidationCriteria;

/**
 * <b>PlanValidationService</b>
 *
 * <p>Defines contracts for validating plans within the Planning Kernel.
 * This interface provides the contractual framework for plan validation,
 * dependency verification, constraint checking, and completeness assessment.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines contracts for plan validation requests.</li>
 *   <li>Specifies dependency validation interfaces.</li>
 *   <li>Provides constraint verification contracts.</li>
 *   <li>Defines completeness verification interfaces.</li>
 *   <li>Establishes validation criteria contracts.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Interface-only — no implementation logic.</li>
 *   <li>Technology-agnostic — no framework dependencies.</li>
 *   <li>Contract-focused — exposes only validation contracts.</li>
 *   <li>Stateless — no mutable state.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Planning Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-PLAN-101, EIO-ARCH-001, EIO-PLAN-102</p>
 *
 * @see PlanningService
 * @see GoalPlanningService
 * @see TaskPlanningService
 */
public interface PlanValidationService {

    /**
     * Validates a plan for completeness and consistency.
     *
     * <p>This operation performs comprehensive validation of a plan including
     * structural integrity, dependency verification, and completeness assessment.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param planValidationRequest the validation parameters (must not be {@code null})
     * @return a validation result identifier
     * @throws IllegalArgumentException if planValidationRequest is {@code null}
     */
    String validatePlan(PlanValidationRequest planValidationRequest);

    /**
     * Validates dependencies within a plan.
     *
     * <p>This operation verifies that all task and goal dependencies
     * are properly defined and consistent.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param dependencyValidationRequest the dependency validation parameters (must not be {@code null})
     * @return a dependency validation result identifier
     * @throws IllegalArgumentException if dependencyValidationRequest is {@code null}
     */
    String validateDependencies(DependencyValidationRequest dependencyValidationRequest);

    /**
     * Verifies constraints within a plan.
     *
     * <p>This operation checks that a plan satisfies all specified
     * constraints including temporal, resource, and logical constraints.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param constraintVerificationRequest the constraint verification parameters (must not be {@code null})
     * @return a constraint verification result identifier
     * @throws IllegalArgumentException if constraintVerificationRequest is {@code null}
     */
    String verifyConstraints(ConstraintVerificationRequest constraintVerificationRequest);

    /**
     * Verifies completeness of a plan.
     *
     * <p>This operation assesses whether a plan is complete and addresses
     * all required objectives and requirements.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param completenessVerificationRequest the completeness verification parameters (must not be {@code null})
     * @return a completeness verification result identifier
     * @throws IllegalArgumentException if completenessVerificationRequest is {@code null}
     */
    String verifyCompleteness(CompletenessVerificationRequest completenessVerificationRequest);

    /**
     * Represents a plan validation request.
     *
     * @param planId           the plan identifier (must not be {@code null} or empty)
     * @param validationScope   the scope of validation (must not be {@code null})
     * @param validationCriteria the validation criteria (must not be {@code null})
     */
    record PlanValidationRequest(String planId,
                                 PlanningTypes.ValidationScope validationScope,
                                 ValidationCriteria validationCriteria) {
    }

    /**
     * Represents a dependency validation request.
     *
     * @param planId           the plan identifier (must not be {@code null} or empty)
     * @param dependencyTypes   the types of dependencies to validate (must not be {@code null})
     */
    record DependencyValidationRequest(String planId,
                                       java.util.List<PlanningTypes.DependencyType> dependencyTypes) {
    }

    /**
     * Represents a constraint verification request.
     *
     * @param planId           the plan identifier (must not be {@code null} or empty)
     * @param constraintTypes   the types of constraints to verify (must not be {@code null})
     */
    record ConstraintVerificationRequest(String planId,
                                         java.util.List<PlanningTypes.ConstraintType> constraintTypes) {
    }

    /**
     * Represents a completeness verification request.
     *
     * @param planId           the plan identifier (must not be {@code null} or empty)
     * @param completenessCriteria the completeness criteria (must not be {@code null})
     */
    record CompletenessVerificationRequest(String planId,
                                           PlanningTypes.CompletenessCriteria completenessCriteria) {
    }

    /**
     * Defines validation scope.
     */
    enum ValidationScope {
        /**
         * Validate only the plan structure.
         */
        STRUCTURE,

        /**
         * Validate plan dependencies.
         */
        DEPENDENCIES,

        /**
         * Validate plan constraints.
         */
        CONSTRAINTS,

        /**
         * Validate plan completeness.
         */
        COMPLETENESS,

        /**
         * Perform comprehensive validation of all aspects.
         */
        COMPREHENSIVE
    }

    /**
     * Defines dependency types.
     */
    enum DependencyType {
        /**
         * Sequential dependencies (task B depends on task A).
         */
        SEQUENTIAL,

        /**
         * Parallel dependencies (tasks can execute concurrently).
         */
        PARALLEL,

        /**
         * Conditional dependencies (dependency based on conditions).
         */
        CONDITIONAL,

        /**
         * Resource dependencies (tasks share resources).
         */
        RESOURCE
    }

    /**
     * Defines constraint types.
     */
    enum ConstraintType {
        /**
         * Temporary constraints (time-based).
         */
        TEMPORAL,

        /**
         * Resource constraints (availability-based).
         */
        RESOURCE,

        /**
         * Logical constraints (logic-based).
         */
        LOGICAL,

        /**
         * Business constraints (policy-based).
         */
        BUSINESS
    }

    /**
     * Defines scheduling strategies.
     */
    enum SchedulingStrategy {
        /**
         * Schedule tasks as early as possible.
         */
        EARLY_START,

        /**
         * Schedule tasks as late as possible.
         */
        LATE_START,

        /**
         * Schedule tasks with critical path priority.
         */
        CRITICAL_PATH,

        /**
         * Schedule tasks to optimize resource utilization.
         */
        RESOURCE_OPTIMIZED
    }

    /**
     * Defines priority scales.
     */
    enum PriorityScale {
        /**
         * Binary priority: high or low.
         */
        BINARY,

        /**
         * Three-level priority: high, medium, low.
         */
        THREE_LEVEL,

        /**
         * Five-level priority scale.
         */
        FIVE_LEVEL,

        /**
         * Numeric priority scale (1-10).
         */
        NUMERIC
    }
}