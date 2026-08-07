package com.shreeai.os.platform.kernels.planning.api;

import com.shreeai.os.platform.kernels.planning.model.PlanningObjective;
import com.shreeai.os.platform.kernels.planning.model.GoalConstraints;

/**
 * <b>GoalPlanningService</b>
 *
 * <p>Defines contracts for goal planning operations within the Planning Kernel.
 * This interface provides the contractual framework for goal creation, decomposition,
 * refinement, and hierarchy management.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines contracts for goal creation and management.</li>
 *   <li>Specifies goal decomposition interfaces.</li>
 *   <li>Provides goal refinement contracts.</li>
 *   <li>Defines goal hierarchy management.</li>
 *   <li>Establishes planning objective contracts.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Interface-only — no implementation logic.</li>
 *   <li>Technology-agnostic — no framework dependencies.</li>
 *   <li>Contract-focused — exposes only goal planning contracts.</li>
 *   <li>Stateless — no mutable state.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Planning Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-PLAN-101, EIO-ARCH-001, EIO-PLAN-102</p>
 *
 * @see PlanningService
 * @see TaskPlanningService
 * @see SchedulingService
 */
public interface GoalPlanningService {

    /**
     * Creates a new planning goal.
     *
     * <p>This operation defines a new goal with associated objectives,
     * constraints, and success criteria.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param goalCreationRequest the goal creation parameters (must not be {@code null})
     * @return a goal identifier
     * @throws IllegalArgumentException if goalCreationRequest is {@code null}
     */
    String createGoal(GoalCreationRequest goalCreationRequest);

    /**
     * Decomposes a goal into sub-goals.
     *
     * <p>This operation breaks down a high-level goal into manageable
     * sub-goals that can be planned and executed independently.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param goalDecompositionRequest the decomposition parameters (must not be {@code null})
     * @return a decomposition result identifier
     * @throws IllegalArgumentException if goalDecompositionRequest is {@code null}
     */
    String decomposeGoal(GoalDecompositionRequest goalDecompositionRequest);

    /**
     * Refines a goal based on new information or constraints.
     *
     * <p>This operation updates a goal's definition while maintaining
     * its core objectives and success criteria.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param goalRefinementRequest the refinement parameters (must not be {@code null})
     * @return a refined goal identifier
     * @throws IllegalArgumentException if goalRefinementRequest is {@code null}
     */
    String refineGoal(GoalRefinementRequest goalRefinementRequest);

    /**
     * Establishes a hierarchical relationship between goals.
     *
     * <p>This operation defines parent-child relationships between goals
     * to create a structured goal hierarchy.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param goalHierarchyRequest the hierarchy parameters (must not be {@code null})
     * @return a hierarchy result identifier
     * @throws IllegalArgumentException if goalHierarchyRequest is {@code null}
     */
    String establishGoalHierarchy(GoalHierarchyRequest goalHierarchyRequest);

    /**
     * Defines planning objectives for a goal.
     *
     * <p>This operation specifies the measurable objectives and success
     * criteria for a planning goal.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param objectiveDefinitionRequest the objective parameters (must not be {@code null})
     * @return an objective identifier
     * @throws IllegalArgumentException if objectiveDefinitionRequest is {@code null}
     */
    String definePlanningObjectives(ObjectiveDefinitionRequest objectiveDefinitionRequest);

    /**
     * Represents a goal creation request.
     *
     * @param goalDescription the goal description (must not be {@code null} or empty)
     * @param objective       the planning objective (must not be {@code null})
     * @param constraints     the goal constraints (must not be {@code null})
     */
    record GoalCreationRequest(String goalDescription,
                               PlanningObjective objective,
                               GoalConstraints constraints) {
    }

    /**
     * Represents a goal decomposition request.
     *
     * @param goalId          the goal identifier (must not be {@code null} or empty)
     * @param decompositionStrategy the decomposition strategy (must not be {@code null})
     * @param depth           the decomposition depth (must not be {@code null})
     */
    record GoalDecompositionRequest(String goalId,
                                    PlanningTypes.DecompositionStrategy decompositionStrategy,
                                    PlanningTypes.DecompositionDepth depth) {
    }

    /**
     * Represents a goal refinement request.
     *
     * @param goalId          the goal identifier (must not be {@code null} or empty)
     * @param refinementContext the refinement context (must not be {@code null})
     * @param updatedObjectives the updated objectives (must not be {@code null})
     */
    record GoalRefinementRequest(String goalId,
                                 PlanningTypes.RefinementContext refinementContext,
                                 java.util.List<PlanningObjective> updatedObjectives) {
    }

    /**
     * Represents a goal hierarchy request.
     *
     * @param parentGoalId    the parent goal identifier (must not be {@code null} or empty)
     * @param childGoalIds    the child goal identifiers (must not be {@code null})
     */
    record GoalHierarchyRequest(String parentGoalId,
                                java.util.List<String> childGoalIds) {
    }

    /**
     * Represents an objective definition request.
     *
     * @param goalId          the goal identifier (must not be {@code null} or empty)
     * @param objectives      the planning objectives (must not be {@code null})
     */
    record ObjectiveDefinitionRequest(String goalId,
                                      java.util.List<PlanningObjective> objectives) {
    }

    /**
     * Defines decomposition strategies.
     */
    enum DecompositionStrategy {
        /**
         * Hierarchical decomposition into sub-goals.
         */
        HIERARCHICAL,

        /**
         * Sequential decomposition into ordered sub-goals.
         */
        SEQUENTIAL,

        /**
         * Parallel decomposition into independent sub-goals.
         */
        PARALLEL,

        /**
         * Conditional decomposition based on decision points.
         */
        CONDITIONAL
    }

    /**
     * Defines decomposition depth.
     */
    enum DecompositionDepth {
        /**
         * Single-level decomposition.
         */
        SINGLE,

        /**
         * Multi-level decomposition with moderate depth.
         */
        MODERATE,

        /**
         * Deep decomposition with comprehensive breakdown.
         */
        DEEP,

        /**
         * Exhaustive decomposition to atomic tasks.
         */
        EXHAUSTIVE
    }

    /**
     * Defines task generation strategies.
     */
    enum TaskGenerationStrategy {
        /**
         * Generate tasks through hierarchical decomposition.
         */
        HIERARCHICAL,

        /**
         * Generate tasks through sequential breakdown.
         */
        SEQUENTIAL,

        /**
         * Generate tasks in parallel where possible.
         */
        PARALLEL,

        /**
         * Generate tasks based on templates.
         */
        TEMPLATE_BASED
    }
}