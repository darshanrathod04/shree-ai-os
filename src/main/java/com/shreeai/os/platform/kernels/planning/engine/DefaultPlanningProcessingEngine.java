package com.shreeai.os.platform.kernels.planning.engine;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.shreeai.os.platform.kernels.planning.analyzer.PlanningAnalyzer;
import com.shreeai.os.platform.kernels.planning.engine.planners.DomainPlannerRegistry;
import com.shreeai.os.platform.kernels.planning.model.Goal;
import com.shreeai.os.platform.kernels.planning.model.GoalConstraints;
import com.shreeai.os.platform.kernels.planning.model.PlanBlueprint;
import com.shreeai.os.platform.kernels.planning.model.PlanningAnalysisResult;
import com.shreeai.os.platform.kernels.planning.model.PlanningId;
import com.shreeai.os.platform.kernels.planning.model.PlanningObjective;
import com.shreeai.os.platform.kernels.planning.model.Priority;
import com.shreeai.os.platform.kernels.planning.model.Schedule;
import com.shreeai.os.platform.kernels.planning.model.SchedulingConstraints;
import com.shreeai.os.platform.kernels.planning.model.Task;
import com.shreeai.os.platform.kernels.planning.model.TaskRequirements;
import com.shreeai.os.platform.kernels.planning.model.ValidationCriteria;

/**
 * <b>DefaultPlanningProcessingEngine</b>
 *
 * <p>Default implementation of the PlanningProcessingEngine.
 * Performs deterministic planning computation on validated Planning domain models.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Executes deterministic planning computations.</li>
 *   <li>Transforms validated Planning models into immutable results.</li>
 *   <li>Constructs immutable PlanningProcessingResult instances.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Planning Kernel — Engine Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> This class is stateless, thread-safe, deterministic, and read-only.
 * It maintains no mutable instance state and performs no orchestration, validation,
 * or exception translation.</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-PLAN-106, EIO-ARCH-001</p>
 *
 * <p><b>Processing Philosophy:</b></p>
 * <p>The engine performs deterministic transformations of validated inputs.
 * It does not evaluate plan quality, optimize schedules, or make decisions.
 * It transforms structure only.</p>
 *
 * @since 1.0
 */
public final class DefaultPlanningProcessingEngine implements PlanningProcessingEngine {

    /** Sprint-11 domain planner registry. */
    private final DomainPlannerRegistry plannerRegistry = new DomainPlannerRegistry();

    /** Sprint-11 planning intent analyzer. */
    private final PlanningAnalyzer analyzer = new PlanningAnalyzer();

    /**
     * Creates a new {@code DefaultPlanningProcessingEngine}.
     *
     * <p>This engine is stateless and requires no dependencies.</p>
     */
    public DefaultPlanningProcessingEngine() {
    }

    /**
     * Processes a goal planning operation.
     *
     * <p>Executes deterministic goal planning computation.
     * Transforms the planning objective into goal structures.</p>
     *
     * <p><b>Processing Responsibilities:</b></p>
     * <ul>
     *   <li>Transform objective into goal structures.</li>
     *   <li>Generate immutable goal instances.</li>
     *   <li>Return deterministic results.</li>
     * </ul>
     *
     * <p><b>What This Method Does NOT Do:</b></p>
     * <ul>
     *   <li>Does not validate inputs.</li>
     *   <li>Does not decompose goals.</li>
     *   <li>Does not evaluate goal quality.</li>
     * </ul>
     *
     * @param objective the planning objective (must not be {@code null})
     * @return a list of processed {@link Goal} instances
     */
    @Override
    public List<Goal> processGoalPlanning(PlanningObjective objective) {
        Objects.requireNonNull(
                objective,
                "PlanningObjective must not be null"
        );

        PlanningIntelligenceEngine intelligenceEngine =
                new PlanningIntelligenceEngine();

        PlanningIntelligenceEngine.PlanningAnalysis analysis =
                intelligenceEngine.analyze(
                        objective
                );

        return analysis.goal() == null
                ? List.of()
                : List.of(
                analysis.goal()
        );
    }

    /**
     * Processes a task planning operation.
     *
     * <p>Executes deterministic task planning computation.
     * Transforms goals into task structures.</p>
     *
     * <p><b>Processing Responsibilities:</b></p>
     * <ul>
     *   <li>Transform goals into task structures.</li>
     *   <li>Generate immutable task instances.</li>
     *   <li>Return deterministic results.</li>
     * </ul>
     *
     * <p><b>What This Method Does NOT Do:</b></p>
     * <ul>
     *   <li>Does not validate inputs.</li>
     *   <li>Does not generate or sequence tasks.</li>
     *   <li>Does not evaluate task dependencies.</li>
     * </ul>
     *
     * @param goals the list of goals to plan tasks for (must not be {@code null})
     * @return a list of processed {@link Task} instances
     */
    @Override
    public List<Task> processTaskPlanning(List<Goal> goals) {
        Objects.requireNonNull(
                goals,
                "Goals must not be null"
        );

        PlanningIntelligenceEngine intelligenceEngine =
                new PlanningIntelligenceEngine();

        return intelligenceEngine
                .analyzeGoals(
                        goals
                )
                .tasks();
    }

    /**
     * Processes a scheduling operation.
     *
     * <p>Executes deterministic scheduling computation.
     * Transforms tasks into a schedule structure.</p>
     *
     * <p><b>Processing Responsibilities:</b></p>
     * <ul>
     *   <li>Transform tasks into schedule structure.</li>
     *   <li>Generate immutable schedule instance.</li>
     *   <li>Return deterministic results.</li>
     * </ul>
     *
     * <p><b>What This Method Does NOT Do:</b></p>
     * <ul>
     *   <li>Does not validate inputs.</li>
     *   <li>Does not optimize schedules.</li>
     *   <li>Does not evaluate scheduling quality.</li>
     * </ul>
     *
     * @param tasks the list of tasks to schedule (must not be {@code null})
     * @return a processed {@link Schedule} instance
     */
    @Override
    public Schedule processScheduling(List<Task> tasks) {
        Objects.requireNonNull(
                tasks,
                "Tasks must not be null"
        );

        PlanningIntelligenceEngine intelligenceEngine =
                new PlanningIntelligenceEngine();

        return intelligenceEngine.schedule(
                tasks
        );
    }

    /**
     * Processes a prioritization operation.
     *
     * <p>Executes deterministic prioritization computation.
     * Transforms tasks into priority structures.</p>
     *
     * <p><b>Processing Responsibilities:</b></p>
     * <ul>
     *   <li>Transform tasks into priority structures.</li>
     *   <li>Generate immutable priority instances.</li>
     *   <li>Return deterministic results.</li>
     * </ul>
     *
     * <p><b>What This Method Does NOT Do:</b></p>
     * <ul>
     *   <li>Does not validate inputs.</li>
     *   <li>Does not compute priorities.</li>
     *   <li>Does not evaluate priority correctness.</li>
     * </ul>
     *
     * @param tasks the list of tasks to prioritize (must not be {@code null})
     * @return a list of processed {@link Priority} instances
     */
    @Override
    public List<Priority> processPrioritization(List<Task> tasks) {
        Objects.requireNonNull(
                tasks,
                "Tasks must not be null"
        );

        PlanningIntelligenceEngine intelligenceEngine =
                new PlanningIntelligenceEngine();

        return intelligenceEngine.prioritize(
                tasks
        );
    }

    /**
     * Processes a plan validation operation.
     *
     * <p>Executes deterministic plan validation computation.
     * Transforms validation criteria into a processing result.</p>
     *
     * <p><b>Processing Responsibilities:</b></p>
     * <ul>
     *   <li>Transform validation criteria into result structure.</li>
     *   <li>Generate immutable processing result.</li>
     *   <li>Return deterministic results.</li>
     * </ul>
     *
     * <p><b>What This Method Does NOT Do:</b></p>
     * <ul>
     *   <li>Does not validate inputs.</li>
     *   <li>Does not validate plans semantically.</li>
     *   <li>Does not evaluate plan quality.</li>
     * </ul>
     *
     * @param criteria the validation criteria (must not be {@code null})
     * @return a validation result
     */
    @Override
    public Object processPlanValidation(ValidationCriteria criteria) {
        Objects.requireNonNull(criteria, "ValidationCriteria must not be null");

        // Deterministic transformation: create processing result from criteria
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("validationRulesCount", criteria.validationRules().size());
        metadata.put("requiredConditionsCount", criteria.requiredConditions().size());
        metadata.put("completenessRequirementsCount", criteria.completenessRequirements().size());

        return new PlanningProcessingResult(
                true,
                Instant.now(),
                metadata,
                null,
                null,
                null,
                null,
                null
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Sprint-11 — Domain-Aware Planning
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Processes a domain-aware planning operation (Sprint-11).
     *
     * <ol>
     *   <li>Analyzes the objective text with {@link PlanningAnalyzer}</li>
     *   <li>Selects the appropriate {@link com.shreeai.os.platform.kernels.planning.engine.planners.DomainPlanner}</li>
     *   <li>Builds a rich {@link PlanBlueprint} with phases, milestones, risks, and metrics</li>
     * </ol>
     *
     * @param objective the planning objective (must not be {@code null})
     * @return a fully-populated {@link PlanBlueprint}
     */
    @Override
    public PlanBlueprint processDomainPlanning(PlanningObjective objective) {
        Objects.requireNonNull(objective, "PlanningObjective must not be null");

        // Extract the objective description from the canonical description field,
        // falling back to the objectiveId for legacy callers.
        String text = objective.description() != null && !objective.description().isBlank()
                ? objective.description()
                : objective.planningId().value();

        // Step 1: Analyze the input deterministically
        PlanningAnalysisResult analysis = analyzer.analyze(text);

        // Step 2: Select planner and build the blueprint
        return plannerRegistry.buildPlan(analysis);
    }
}