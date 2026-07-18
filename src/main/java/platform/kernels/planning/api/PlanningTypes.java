package platform.kernels.planning.api;

/**
 * <b>PlanningTypes</b>
 *
 * <p>Provides supporting types for the Planning Kernel API contracts.
 * This file contains request/response type definitions used across
 * all planning service interfaces.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines common planning request types.</li>
 *   <li>Provides criteria and constraint definitions.</li>
 *   <li>Establishes type safety for planning contracts.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Immutable — all records are immutable value objects.</li>
 *   <li>Type-safe — provides compile-time type checking.</li>
 *   <li>Minimal — contains only type definitions, no logic.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Planning Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-PLAN-101, EIO-ARCH-001, EIO-PLAN-102</p>
 *
 * @see platform.kernels.planning.model.PlanningConstraints
 * @see platform.kernels.planning.model.PlanningObjective
 * @see platform.kernels.planning.model.GoalConstraints
 * @see platform.kernels.planning.model.ValidationCriteria
 * @see platform.kernels.planning.model.SchedulingConstraints
 * @see platform.kernels.planning.model.ResourceAvailability
 * @see platform.kernels.planning.model.TaskRequirements
 */
public final class PlanningTypes {

    /**
     * Private constructor to prevent instantiation.
     */
    private PlanningTypes() {
        // Utility class — no instantiation
    }

    /**
     * Represents a refinement context.
     *
     * @param contextId   the context identifier (must not be {@code null} or empty)
     * @param description the context description (must not be {@code null} or empty)
     */
    public record RefinementContext(String contextId, String description) {
    }

    /**
     * Represents completeness criteria.
     *
     * @param criteriaId   the criteria identifier (must not be {@code null} or empty)
     * @param description  the criteria description (must not be {@code null} or empty)
     */
    public record CompletenessCriteria(String criteriaId, String description) {
    }

    /**
     * Represents assignment criteria.
     *
     * @param criteriaId   the criteria identifier (must not be {@code null} or empty)
     * @param description  the criteria description (must not be {@code null} or empty)
     */
    public record AssignmentCriteria(String criteriaId, String description) {
    }

    /**
     * Represents ordering criteria.
     *
     * @param criteriaId   the criteria identifier (must not be {@code null} or empty)
     * @param description  the criteria description (must not be {@code null} or empty)
     */
    public record OrderingCriteria(String criteriaId, String description) {
    }

    /**
     * Represents classification criteria.
     *
     * @param criteriaId   the criteria identifier (must not be {@code null} or empty)
     * @param description  the criteria description (must not be {@code null} or empty)
     */
    public record ClassificationCriteria(String criteriaId, String description) {
    }

    /**
     * Represents evaluation criteria.
     *
     * @param criteriaId   the criteria identifier (must not be {@code null} or empty)
     * @param description  the criteria description (must not be {@code null} or empty)
     */
    public record EvaluationCriteria(String criteriaId, String description) {
    }

    /**
     * Represents prioritization policies.
     *
     * @param policyId     the policy identifier (must not be {@code null} or empty)
     * @param description  the policy description (must not be {@code null} or empty)
     */
    public record PrioritizationPolicies(String policyId, String description) {
    }

    /**
     * Represents a time horizon.
     *
     * @param horizonId    the horizon identifier (must not be {@code null} or empty)
     * @param description  the horizon description (must not be {@code null} or empty)
     */
    public record TimeHorizon(String horizonId, String description) {
    }

    /**
     * Represents optimization criteria.
     *
     * @param criteriaId   the criteria identifier (must not be {@code null} or empty)
     * @param description  the criteria description (must not be {@code null} or empty)
     */
    public record OptimizationCriteria(String criteriaId, String description) {
    }

    /**
     * Represents timeline objectives.
     *
     * @param objectiveId   the objective identifier (must not be {@code null} or empty)
     * @param description   the objective description (must not be {@code null} or empty)
     */
    public record TimelineObjectives(String objectiveId, String description) {
    }

    /**
     * Represents task constraints.
     *
     * @param description the constraint description (must not be {@code null} or empty)
     */
    public record TaskConstraints(String description) {
    }

    /**
     * Represents sequencing criteria.
     *
     * @param criteriaId   the criteria identifier (must not be {@code null} or empty)
     * @param description  the criteria description (must not be {@code null} or empty)
     */
    public record SequencingCriteria(String criteriaId, String description) {
    }

    /**
     * Represents task dependencies.
     *
     * @param description the dependency description (must not be {@code null} or empty)
     */
    public record TaskDependencies(String description) {
    }

    /**
     * Represents grouping criteria.
     *
     * @param criteriaId   the criteria identifier (must not be {@code null} or empty)
     * @param description  the criteria description (must not be {@code null} or empty)
     */
    public record GroupingCriteria(String criteriaId, String description) {
    }

    // Enums used in request types
    public enum PlanningScope {
        SURFACE, STANDARD, DEEP, COMPREHENSIVE
    }

    public enum DecompositionStrategy {
        HIERARCHICAL, SEQUENTIAL, PARALLEL, CONDITIONAL
    }

    public enum DecompositionDepth {
        SINGLE, MODERATE, DEEP, EXHAUSTIVE
    }

    public enum TaskGenerationStrategy {
        HIERARCHICAL, SEQUENTIAL, PARALLEL, TEMPLATE_BASED
    }

    public enum SchedulingStrategy {
        EARLY_START, LATE_START, CRITICAL_PATH, RESOURCE_OPTIMIZED
    }

    public enum PriorityScale {
        BINARY, THREE_LEVEL, FIVE_LEVEL, NUMERIC
    }

    public enum ValidationScope {
        STRUCTURE, DEPENDENCIES, CONSTRAINTS, COMPLETENESS, COMPREHENSIVE
    }

    public enum DependencyType {
        SEQUENTIAL, PARALLEL, CONDITIONAL, RESOURCE
    }

    public enum ConstraintType {
        TEMPORAL, RESOURCE, LOGICAL, BUSINESS
    }
}