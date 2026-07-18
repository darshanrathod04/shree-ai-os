package platform.kernels.planning.api;

import platform.kernels.planning.model.TaskRequirements;

/**
 * <b>TaskPlanningService</b>
 *
 * <p>Defines contracts for task planning operations within the Planning Kernel.
 * This interface provides the contractual framework for task generation, sequencing,
 * dependency description, grouping, and refinement.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines contracts for task generation and management.</li>
 *   <li>Specifies task sequencing interfaces.</li>
 *   <li>Provides task dependency description contracts.</li>
 *   <li>Defines task grouping and organization.</li>
 *   <li>Establishes task refinement contracts.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Interface-only — no implementation logic.</li>
 *   <li>Technology-agnostic — no framework dependencies.</li>
 *   <li>Contract-focused — exposes only task planning contracts.</li>
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
 * @see SchedulingService
 */
public interface TaskPlanningService {

    /**
     * Generates tasks from a goal.
     *
     * <p>This operation creates executable tasks from a planning goal,
     * defining the work required to achieve the goal.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param taskGenerationRequest the task generation parameters (must not be {@code null})
     * @return a task generation result identifier
     * @throws IllegalArgumentException if taskGenerationRequest is {@code null}
     */
    String generateTasks(TaskGenerationRequest taskGenerationRequest);

    /**
     * Sequences tasks in execution order.
     *
     * <p>This operation determines the optimal execution sequence for tasks
     * based on dependencies and constraints.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param taskSequencingRequest the sequencing parameters (must not be {@code null})
     * @return a sequencing result identifier
     * @throws IllegalArgumentException if taskSequencingRequest is {@code null}
     */
    String sequenceTasks(TaskSequencingRequest taskSequencingRequest);

    /**
     * Describes dependencies between tasks.
     *
     * <p>This operation defines the dependency relationships between tasks,
     * including prerequisites, blockers, and coordination requirements.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param dependencyDescriptionRequest the dependency parameters (must not be {@code null})
     * @return a dependency description identifier
     * @throws IllegalArgumentException if dependencyDescriptionRequest is {@code null}
     */
    String describeTaskDependencies(DependencyDescriptionRequest dependencyDescriptionRequest);

    /**
     * Groups tasks into logical units.
     *
     * <p>This operation organizes tasks into groups based on common
     * characteristics, objectives, or execution contexts.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param taskGroupingRequest the grouping parameters (must not be {@code null})
     * @return a task grouping identifier
     * @throws IllegalArgumentException if taskGroupingRequest is {@code null}
     */
    String groupTasks(TaskGroupingRequest taskGroupingRequest);

    /**
     * Refines a task based on new information or constraints.
     *
     * <p>This operation updates a task's definition while maintaining
     * its core objectives and requirements.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param taskRefinementRequest the refinement parameters (must not be {@code null})
     * @return a refined task identifier
     * @throws IllegalArgumentException if taskRefinementRequest is {@code null}
     */
    String refineTask(TaskRefinementRequest taskRefinementRequest);

    /**
     * Represents a task generation request.
     *
     * @param goalId          the goal identifier (must not be {@code null} or empty)
     * @param generationStrategy the generation strategy (must not be {@code null})
     * @param taskConstraints the task constraints (must not be {@code null})
     */
    record TaskGenerationRequest(String goalId,
                                 PlanningTypes.TaskGenerationStrategy generationStrategy,
                                 PlanningTypes.TaskConstraints taskConstraints) {
    }

    /**
     * Represents a task sequencing request.
     *
     * @param taskIds         the task identifiers (must not be {@code null})
     * @param sequencingCriteria the sequencing criteria (must not be {@code null})
     */
    record TaskSequencingRequest(java.util.List<String> taskIds,
                                 PlanningTypes.SequencingCriteria sequencingCriteria) {
    }

    /**
     * Represents a dependency description request.
     *
     * @param taskId          the task identifier (must not be {@code null} or empty)
     * @param dependencies    the task dependencies (must not be {@code null})
     */
    record DependencyDescriptionRequest(String taskId,
                                        PlanningTypes.TaskDependencies dependencies) {
    }

    /**
     * Represents a task grouping request.
     *
     * @param taskIds         the task identifiers (must not be {@code null})
     * @param groupingCriteria the grouping criteria (must not be {@code null})
     */
    record TaskGroupingRequest(java.util.List<String> taskIds,
                               PlanningTypes.GroupingCriteria groupingCriteria) {
    }

    /**
     * Represents a task refinement request.
     *
     * @param taskId          the task identifier (must not be {@code null} or empty)
     * @param refinementContext the refinement context (must not be {@code null})
     * @param updatedRequirements the updated requirements (must not be {@code null})
     */
    record TaskRefinementRequest(String taskId,
                                 PlanningTypes.RefinementContext refinementContext,
                                 TaskRequirements updatedRequirements) {
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
         * Temporal constraints (time-based).
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
}