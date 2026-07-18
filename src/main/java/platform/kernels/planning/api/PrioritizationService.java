package platform.kernels.planning.api;

/**
 * <b>PrioritizationService</b>
 *
 * <p>Defines contracts for prioritization operations within the Planning Kernel.
 * This interface provides the contractual framework for priority assignment,
 * ordering, urgency classification, and prioritization policies.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines contracts for priority assignment requests.</li>
 *   <li>Specifies priority ordering interfaces.</li>
 *   <li>Provides urgency classification contracts.</li>
 *   <li>Defines importance evaluation request interfaces.</li>
 *   <li>Establishes prioritization policy contracts.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Interface-only — no implementation logic.</li>
 *   <li>Technology-agnostic — no framework dependencies.</li>
 *   <li>Contract-focused — exposes only prioritization contracts.</li>
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
 * @see PlanValidationService
 */
public interface PrioritizationService {

    /**
     * Assigns priorities to goals or tasks.
     *
     * <p>This operation evaluates and assigns priority levels to planning
     * elements based on specified criteria and policies.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param priorityAssignmentRequest the priority assignment parameters (must not be {@code null})
     * @return a priority assignment result identifier
     * @throws IllegalArgumentException if priorityAssignmentRequest is {@code null}
     */
    String assignPriorities(PriorityAssignmentRequest priorityAssignmentRequest);

    /**
     * Orders elements by priority.
     *
     * <p>This operation sorts planning elements according to their
     * assigned priorities and specified ordering criteria.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param priorityOrderingRequest the ordering parameters (must not be {@code null})
     * @return an ordering result identifier
     * @throws IllegalArgumentException if priorityOrderingRequest is {@code null}
     */
    String orderByPriority(PriorityOrderingRequest priorityOrderingRequest);

    /**
     * Classifies urgency of planning elements.
     *
     * <p>This operation categorizes planning elements based on their
     * urgency levels and time sensitivity.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param urgencyClassificationRequest the classification parameters (must not be {@code null})
     * @return a classification result identifier
     * @throws IllegalArgumentException if urgencyClassificationRequest is {@code null}
     */
    String classifyUrgency(UrgencyClassificationRequest urgencyClassificationRequest);

    /**
     * Evaluates importance of planning elements.
     *
     * <p>This operation assesses the relative importance of planning
     * elements based on strategic value and impact.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param importanceEvaluationRequest the evaluation parameters (must not be {@code null})
     * @return an evaluation result identifier
     * @throws IllegalArgumentException if importanceEvaluationRequest is {@code null}
     */
    String evaluateImportance(ImportanceEvaluationRequest importanceEvaluationRequest);

    /**
     * Applies prioritization policies.
     *
     * <p>This operation enforces specified prioritization policies across
     * planning elements to ensure consistent priority management.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param policyApplicationRequest the policy application parameters (must not be {@code null})
     * @return a policy application result identifier
     * @throws IllegalArgumentException if policyApplicationRequest is {@code null}
     */
    String applyPrioritizationPolicy(PolicyApplicationRequest policyApplicationRequest);

    /**
     * Represents a priority assignment request.
     *
     * @param elementIds      the element identifiers (must not be {@code null})
     * @param assignmentCriteria the assignment criteria (must not be {@code null})
     * @param priorityScale   the priority scale to use (must not be {@code null})
     */
    record PriorityAssignmentRequest(java.util.List<String> elementIds,
                                     PlanningTypes.AssignmentCriteria assignmentCriteria,
                                     PlanningTypes.PriorityScale priorityScale) {
    }

    /**
     * Represents a priority ordering request.
     *
     * @param elementIds      the element identifiers (must not be {@code null})
     * @param orderingCriteria the ordering criteria (must not be {@code null})
     */
    record PriorityOrderingRequest(java.util.List<String> elementIds,
                                   PlanningTypes.OrderingCriteria orderingCriteria) {
    }

    /**
     * Represents an urgency classification request.
     *
     * @param elementIds      the element identifiers (must not be {@code null})
     * @param classificationCriteria the classification criteria (must not be {@code null})
     */
    record UrgencyClassificationRequest(java.util.List<String> elementIds,
                                        PlanningTypes.ClassificationCriteria classificationCriteria) {
    }

    /**
     * Represents an importance evaluation request.
     *
     * @param elementIds      the element identifiers (must not be {@code null})
     * @param evaluationCriteria the evaluation criteria (must not be {@code null})
     */
    record ImportanceEvaluationRequest(java.util.List<String> elementIds,
                                       PlanningTypes.EvaluationCriteria evaluationCriteria) {
    }

    /**
     * Represents a policy application request.
     *
     * @param elementIds      the element identifiers (must not be {@code null})
     * @param policies        the prioritization policies (must not be {@code null})
     */
    record PolicyApplicationRequest(java.util.List<String> elementIds,
                                    PlanningTypes.PrioritizationPolicies policies) {
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