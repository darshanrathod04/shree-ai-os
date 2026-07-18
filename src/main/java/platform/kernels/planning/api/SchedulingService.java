package platform.kernels.planning.api;

/**
 * <b>SchedulingService</b>
 *
 * <p>Defines contracts for scheduling operations within the Planning Kernel.
 * This interface provides the contractual framework for schedule generation,
 * optimization, timeline planning, and constraint scheduling.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines contracts for schedule generation.</li>
 *   <li>Specifies schedule optimization request interfaces.</li>
 *   <li>Provides timeline planning contracts.</li>
 *   <li>Defines constraint scheduling interfaces.</li>
 *   <li>Establishes schedule evaluation request contracts.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Interface-only — no implementation logic.</li>
 *   <li>Technology-agnostic — no framework dependencies.</li>
 *   <li>Contract-focused — exposes only scheduling contracts.</li>
 *   <li>Stateless — no mutable state.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Planning Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-PLAN-101, EIO-ARCH-001</p>
 *
 * @see PlanningService
 * @see GoalPlanningService
 * @see TaskPlanningService
 * @see PrioritizationService
 */
public interface SchedulingService {

    /**
     * Generates a schedule for tasks.
     *
     * <p>This operation creates a timeline-based schedule for executing
     * tasks within specified constraints and objectives.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param scheduleGenerationRequest the schedule generation parameters (must not be {@code null})
     * @return a schedule identifier
     * @throws IllegalArgumentException if scheduleGenerationRequest is {@code null}
     */
    String generateSchedule(ScheduleGenerationRequest scheduleGenerationRequest);

    /**
     * Requests optimization of an existing schedule.
     *
     * <p>This operation evaluates and optimizes a schedule based on
     * specified criteria and constraints.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param scheduleOptimizationRequest the optimization parameters (must not be {@code null})
     * @return an optimization result identifier
     * @throws IllegalArgumentException if scheduleOptimizationRequest is {@code null}
     */
    String optimizeSchedule(ScheduleOptimizationRequest scheduleOptimizationRequest);

    /**
     * Plans a timeline for goal achievement.
     *
     * <p>This operation creates a timeline that maps out when goals
     * and tasks will be executed over time.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param timelinePlanningRequest the timeline planning parameters (must not be {@code null})
     * @return a timeline identifier
     * @throws IllegalArgumentException if timelinePlanningRequest is {@code null}
     */
    String planTimeline(TimelinePlanningRequest timelinePlanningRequest);

    /**
     * Performs constraint-based scheduling.
     *
     * <p>This operation schedules tasks while respecting specified
     * constraints such as deadlines, resources, and dependencies.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param constraintSchedulingRequest the constraint scheduling parameters (must not be {@code null})
     * @return a constraint-based schedule identifier
     * @throws IllegalArgumentException if constraintSchedulingRequest is {@code null}
     */
    String scheduleWithConstraints(ConstraintSchedulingRequest constraintSchedulingRequest);

    /**
     * Requests evaluation of a schedule.
     *
     * <p>This operation evaluates a schedule against specified criteria
     * to assess its quality and feasibility.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param scheduleEvaluationRequest the evaluation parameters (must not be {@code null})
     * @return an evaluation result identifier
     * @throws IllegalArgumentException if scheduleEvaluationRequest is {@code null}
     */
    String evaluateSchedule(ScheduleEvaluationRequest scheduleEvaluationRequest);

    /**
     * Represents a schedule generation request.
     *
     * @param planId          the plan identifier (must not be {@code null} or empty)
     * @param schedulingStrategy the scheduling strategy (must not be {@code null})
     * @param timeHorizon     the time horizon for scheduling (must not be {@code null})
     */
    record ScheduleGenerationRequest(String planId,
                                     PlanningTypes.SchedulingStrategy schedulingStrategy,
                                     PlanningTypes.TimeHorizon timeHorizon) {
    }

    /**
     * Represents a schedule optimization request.
     *
     * @param scheduleId      the schedule identifier (must not be {@code null} or empty)
     * @param optimizationCriteria the optimization criteria (must not be {@code null})
     * @param constraints     the optimization constraints (must not be {@code null})
     */
    record ScheduleOptimizationRequest(String scheduleId,
                                       PlanningTypes.OptimizationCriteria optimizationCriteria,
                                       PlanningTypes.SchedulingConstraints constraints) {
    }

    /**
     * Represents a timeline planning request.
     *
     * @param planId          the plan identifier (must not be {@code null} or empty)
     * @param timelineObjectives the timeline objectives (must not be {@code null})
     * @param milestones      the required milestones (must not be {@code null})
     */
    record TimelinePlanningRequest(String planId,
                                   PlanningTypes.TimelineObjectives timelineObjectives,
                                   java.util.List<String> milestones) {
    }

    /**
     * Represents a constraint scheduling request.
     *
     * @param planId          the plan identifier (must not be {@code null} or empty)
     * @param constraints     the scheduling constraints (must not be {@code null})
     * @param resources       the available resources (must not be {@code null})
     */
    record ConstraintSchedulingRequest(String planId,
                                       PlanningTypes.SchedulingConstraints constraints,
                                       PlanningTypes.ResourceAvailability resources) {
    }

    /**
     * Represents a schedule evaluation request.
     *
     * @param scheduleId      the schedule identifier (must not be {@code null} or empty)
     * @param evaluationCriteria the evaluation criteria (must not be {@code null})
     */
    record ScheduleEvaluationRequest(String scheduleId,
                                     PlanningTypes.EvaluationCriteria evaluationCriteria) {
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
}