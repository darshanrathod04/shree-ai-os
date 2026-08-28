package com.shreeai.os.platform.kernels.planning.engine;

import com.shreeai.os.platform.kernels.planning.model.Goal;
import com.shreeai.os.platform.kernels.planning.model.GoalConstraints;
import com.shreeai.os.platform.kernels.planning.model.PlanningId;
import com.shreeai.os.platform.kernels.planning.model.PlanningObjective;
import com.shreeai.os.platform.kernels.planning.model.Priority;
import com.shreeai.os.platform.kernels.planning.model.Schedule;
import com.shreeai.os.platform.kernels.planning.model.SchedulingConstraints;
import com.shreeai.os.platform.kernels.planning.model.Task;
import com.shreeai.os.platform.kernels.planning.model.TaskRequirements;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Advanced deterministic planning intelligence for the Planning Kernel.
 *
 * <p>This engine converts validated planning intent into an explicit,
 * inspectable planning graph. It performs goal decomposition support,
 * task generation, dependency construction, prioritization, scheduling,
 * plan-quality analysis and uncertainty reporting.</p>
 *
 * <p>The engine does not execute tasks, persist state, call an AI provider,
 * or depend on the legacy package. All inference is deterministic and
 * traceable to the supplied objective metadata.</p>
 *
 * <p>When structured task information is supplied, it is preferred over
 * generic decomposition. When it is absent, the engine uses conservative
 * deterministic decomposition and explicitly marks generated steps as
 * inferred rather than pretending they were supplied by the developer.</p>
 *
 * <p><b>Ownership:</b> Planning Kernel — Intelligence Engine</p>
 * <p><b>Version:</b> 1.0</p>
 * <p><b>Legacy dependency:</b> None</p>
 */
public final class PlanningIntelligenceEngine {

    private static final Pattern SENTENCE_PATTERN =
            Pattern.compile("(?<=[.!?])\\s+|\\r?\\n+");

    private static final Pattern TOKEN_PATTERN =
            Pattern.compile("[\\p{L}\\p{N}][\\p{L}\\p{N}_'-]*");

    private static final Set<String> ACTION_TERMS = Set.of(
            "analyze", "analyse", "build", "create", "design", "develop",
            "define", "determine", "identify", "implement", "integrate",
            "prepare", "plan", "test", "validate", "verify", "configure",
            "deploy", "document", "review", "compare", "evaluate", "solve",
            "research", "generate", "establish", "migrate", "refactor",
            "implementing", "developing", "building", "creating", "testing"
    );

    private static final Set<String> URGENT_TERMS = Set.of(
            "urgent", "critical", "immediately", "deadline", "blocking",
            "blocker", "asap", "emergency", "today", "now"
    );

    private static final Set<String> IMPORTANT_TERMS = Set.of(
            "important", "required", "must", "core", "essential",
            "security", "safety", "reliability", "production"
    );

    /**
     * Creates a stateless planning intelligence engine.
     */
    public PlanningIntelligenceEngine() {
    }

    /**
     * Builds an advanced plan from a validated objective.
     *
     * @param objective validated planning objective
     * @return immutable planning intelligence result
     */
    public PlanningAnalysis analyze(PlanningObjective objective) {
        Objects.requireNonNull(
                objective,
                "PlanningObjective must not be null"
        );

        List<TaskBlueprint> blueprints =
                deriveTaskBlueprints(objective);

        List<TaskBlueprint> normalized =
                normalizeBlueprints(
                        blueprints
                );

        List<TaskBlueprint> dependencyAware =
                applyDependencies(
                        normalized
                );

        List<Task> tasks =
                materializeTasks(
                        objective,
                        dependencyAware
                );

        Goal goal =
                materializeGoal(
                        objective,
                        tasks
                );

        List<Priority> priorities =
                tasks.stream()
                        .map(Task::priority)
                        .toList();

        Schedule schedule =
                buildSchedule(
                        tasks
                );

        PlanQuality quality =
                evaluatePlan(
                        objective,
                        tasks,
                        schedule
                );

        Map<String, Object> metadata =
                buildMetadata(
                        objective,
                        tasks,
                        schedule,
                        quality
                );

        return new PlanningAnalysis(
                goal,
                tasks,
                schedule,
                priorities,
                quality,
                metadata
        );
    }

    /**
     * Reconstructs planning intelligence from an existing goal set.
     *
     * <p>This method is useful for the public task-planning and scheduling
     * operations when those operations are invoked independently of
     * createPlan().</p>
     */
    public PlanningAnalysis analyzeGoals(
            List<Goal> goals) {

        Objects.requireNonNull(
                goals,
                "Goals must not be null"
        );

        if (goals.isEmpty()) {
            return new PlanningAnalysis(
                    null,
                    List.of(),
                    buildSchedule(List.of()),
                    List.of(),
                    PlanQuality.empty(),
                    Map.of(
                            "planningIntelligenceVersion",
                            "1.0",
                            "goalCount",
                            0,
                            "taskCount",
                            0,
                            "status",
                            "NO_GOALS"
                    )
            );
        }

        Goal primary =
                goals.get(0);

        List<TaskBlueprint> blueprints =
                deriveTaskBlueprints(
                        primary.objective()
                );

        List<TaskBlueprint> normalized =
                applyDependencies(
                        normalizeBlueprints(
                                blueprints
                        )
                );

        List<Task> tasks =
                materializeTasks(
                        primary.objective(),
                        normalized
                );

        Schedule schedule =
                buildSchedule(
                        tasks
                );

        PlanQuality quality =
                evaluatePlan(
                        primary.objective(),
                        tasks,
                        schedule
                );

        List<Priority> priorities =
                tasks.stream()
                        .map(Task::priority)
                        .toList();

        return new PlanningAnalysis(
                primary,
                tasks,
                schedule,
                priorities,
                quality,
                buildMetadata(
                        primary.objective(),
                        tasks,
                        schedule,
                        quality
                )
        );
    }

    /**
     * Schedules an existing task collection using dependency metadata.
     */
    public Schedule schedule(
            List<Task> tasks) {

        Objects.requireNonNull(
                tasks,
                "Tasks must not be null"
        );

        return buildSchedule(
                tasks
        );
    }

    /**
     * Computes priority information without mutating tasks.
     */
    public List<Priority> prioritize(
            List<Task> tasks) {

        Objects.requireNonNull(
                tasks,
                "Tasks must not be null"
        );

        return tasks.stream()
                .sorted(
                        Comparator
                                .comparingInt(
                                        this::priorityRank
                                )
                                .reversed()
                                .thenComparing(
                                        task ->
                                                task.planningId().value()
                                )
                )
                .map(
                        Task::priority
                )
                .toList();
    }

    private List<TaskBlueprint> deriveTaskBlueprints(
            PlanningObjective objective) {

        Map<String, Object> metadata = objective.metadata();

        List<TaskBlueprint> explicit =
                parseExplicitTasks(
                        metadata
                );

        if (!explicit.isEmpty()) {
            return explicit;
        }

        String requestText =
                safe(
                        metadata.get("requestText")
                );

        String reasoningConclusion =
                safe(
                        metadata.get("reasoningConclusion")
                );

        String objectiveDescription =
                safe(
                        objective.description()
                );

        LinkedHashSet<String> candidateTexts =
                new LinkedHashSet<>();

        extractActionSentences(
                requestText,
                candidateTexts
        );

        extractActionSentences(
                reasoningConclusion,
                candidateTexts
        );

        extractActionSentences(
                objectiveDescription,
                candidateTexts
        );

        List<TaskBlueprint> inferred =
                new ArrayList<>();

        int index = 1;

        for (String candidate :
                candidateTexts) {

            if (candidate.length() < 5) {
                continue;
            }

            inferred.add(
                    new TaskBlueprint(
                            "step-" + index++,
                            candidate,
                            Set.of(),
                            "INFERRED"
                    )
            );

            if (inferred.size() >= 8) {
                break;
            }
        }

        if (!inferred.isEmpty()) {
            return inferred;
        }

        return fallbackBlueprints(
                objective
        );
    }

    private List<TaskBlueprint> parseExplicitTasks(
            Map<String, Object> metadata) {

        String raw =
                firstNonBlank(
                        metadata.get("tasks"),
                        metadata.get("taskList"),
                        metadata.get("planningTasks")
                );

        if (raw == null) {
            return List.of();
        }

        List<TaskBlueprint> result =
                new ArrayList<>();

        String[] entries =
                raw.split(
                        "\\r?\\n|\\s*\\|\\s*|\\s*;\\s*"
                );

        int index = 1;

        for (String entry :
                entries) {

            String task =
                    normalizeText(
                            entry
                    );

            if (task.isBlank()) {
                continue;
            }

            result.add(
                    new TaskBlueprint(
                            "step-" + index++,
                            task,
                            Set.of(),
                            "EXPLICIT"
                    )
            );
        }

        return result;
    }

    private void extractActionSentences(
            String source,
            Set<String> output) {

        if (source.isBlank()) {
            return;
        }

        String[] sentences =
                SENTENCE_PATTERN.split(
                        source
                );

        for (String sentence :
                sentences) {

            String normalized =
                    normalizeText(
                            sentence
                    );

            if (normalized.isBlank()) {
                continue;
            }

            Set<String> terms =
                    terms(
                            normalized
                    );

            boolean actionable =
                    terms.stream()
                            .anyMatch(
                                    ACTION_TERMS::contains
                            );

            if (actionable) {
                output.add(
                        normalized
                );
            }
        }
    }

    private List<TaskBlueprint> fallbackBlueprints(
            PlanningObjective objective) {

        String scope =
                safe(
                        objective.scope()
                )
                        .toUpperCase(
                                Locale.ROOT
                        );

        List<TaskBlueprint> result =
                new ArrayList<>();

        result.add(
                new TaskBlueprint(
                        "step-1",
                        "Understand and structure the planning objective",
                        Set.of(),
                        "INFERRED"
                )
        );

        result.add(
                new TaskBlueprint(
                        "step-2",
                        "Identify requirements, assumptions, and constraints",
                        Set.of("step-1"),
                        "INFERRED"
                )
        );

        result.add(
                new TaskBlueprint(
                        "step-3",
                        "Develop the objective-aligned solution approach",
                        Set.of("step-2"),
                        "INFERRED"
                )
        );

        if (scope.contains("DEEP")
                || scope.contains("COMPREHENSIVE")) {

            result.add(
                    new TaskBlueprint(
                            "step-4",
                            "Analyze dependencies, risks, and alternative approaches",
                            Set.of("step-3"),
                            "INFERRED"
                    )
            );

            result.add(
                    new TaskBlueprint(
                            "step-5",
                            "Validate plan completeness and consistency",
                            Set.of("step-4"),
                            "INFERRED"
                    )
            );

            result.add(
                    new TaskBlueprint(
                            "step-6",
                            "Prepare the plan for execution handoff",
                            Set.of("step-5"),
                            "INFERRED"
                    )
            );

        } else {

            result.add(
                    new TaskBlueprint(
                            "step-4",
                            "Validate the resulting plan",
                            Set.of("step-3"),
                            "INFERRED"
                    )
            );
        }

        return result;
    }

    private List<TaskBlueprint> normalizeBlueprints(
            List<TaskBlueprint> input) {

        LinkedHashMap<String, TaskBlueprint> unique =
                new LinkedHashMap<>();

        int index = 1;

        for (TaskBlueprint blueprint :
                input) {

            String description =
                    normalizeText(
                            blueprint.description()
                    );

            if (description.isBlank()) {
                continue;
            }

            String id =
                    safe(
                            blueprint.id()
                    );

            if (id.isBlank()) {
                id =
                        "step-" + index;
            }

            String uniqueId =
                    id;

            int suffix = 2;

            while (unique.containsKey(
                    uniqueId
            )) {

                uniqueId =
                        id + "-" + suffix++;
            }

            unique.put(
                    uniqueId,
                    new TaskBlueprint(
                            uniqueId,
                            description,
                            blueprint.dependencies(),
                            blueprint.origin()
                    )
            );

            index++;
        }

        return List.copyOf(
                unique.values()
        );
    }

    private List<TaskBlueprint> applyDependencies(
            List<TaskBlueprint> input) {

        if (input.isEmpty()) {
            return List.of();
        }

        Set<String> knownIds =
                input.stream()
                        .map(
                                TaskBlueprint::id
                        )
                        .collect(
                                java.util.stream.Collectors.toCollection(
                                        LinkedHashSet::new
                                )
                        );

        List<TaskBlueprint> result =
                new ArrayList<>();

        for (int i = 0;
             i < input.size();
             i++) {

            TaskBlueprint current =
                    input.get(i);

            LinkedHashSet<String> dependencies =
                    new LinkedHashSet<>();

            for (String dependency :
                    current.dependencies()) {

                if (knownIds.contains(
                        dependency
                )
                        && !dependency.equals(
                        current.id()
                )) {

                    dependencies.add(
                            dependency
                    );
                }
            }

            /*
             * Conservative default:
             * when no dependency is supplied, preserve source order by
             * depending on the immediately preceding step. This prevents
             * the engine from inventing unsafe parallelism.
             */
            if (dependencies.isEmpty()
                    && i > 0) {

                dependencies.add(
                        input.get(i - 1).id()
                );
            }

            result.add(
                    new TaskBlueprint(
                            current.id(),
                            current.description(),
                            Set.copyOf(
                                    dependencies
                            ),
                            current.origin()
                    )
            );
        }

        return List.copyOf(
                result
        );
    }

    private List<Task> materializeTasks(
            PlanningObjective objective,
            List<TaskBlueprint> blueprints) {

        List<Task> tasks =
                new ArrayList<>();

        for (int i = 0;
             i < blueprints.size();
             i++) {

            TaskBlueprint blueprint =
                    blueprints.get(i);

            Priority priority =
                    derivePriority(
                            blueprint.description(),
                            i,
                            blueprints.size()
                    );

            Map<String, String> taskMetadata =
                    new LinkedHashMap<>();

            taskMetadata.put(
                    "origin",
                    blueprint.origin()
            );

            taskMetadata.put(
                    "sourceObjective",
                    objective.planningId().value()
            );

            taskMetadata.put(
                    "stepIndex",
                    String.valueOf(
                            i + 1
                    )
            );

            taskMetadata.put(
                    "dependencyCount",
                    String.valueOf(
                            blueprint.dependencies().size()
                    )
            );

            taskMetadata.put(
                    "dependsOn",
                    String.join(
                            ",",
                            blueprint.dependencies()
                    )
            );

            taskMetadata.put(
                    "planningIntelligenceVersion",
                    "1.0"
            );

            TaskRequirements requirements =
                    new TaskRequirements(
                            dependencyMap(
                                    blueprint.dependencies()
                            ),
                            Map.of(),
                            Map.of(),
                            Map.of(
                                    "sourceObjective",
                                    objective.planningId().value()
                            )
                    );

            tasks.add(
                    new Task(
                            new PlanningId(
                                    "task-"
                                            + objective.planningId().value()
                                            + "-"
                                            + (i + 1)
                            ),
                            blueprint.description(),
                            requirements,
                            priority,
                            taskMetadata
                    )
            );
        }

        return List.copyOf(
                tasks
        );
    }

    private Goal materializeGoal(
            PlanningObjective objective,
            List<Task> tasks) {

        Map<String, Object> metadata =
                new LinkedHashMap<>(objective.metadata());

        metadata.put(
                "planningIntelligenceVersion",
                "1.0"
        );

        metadata.put(
                "generatedTaskCount",
                String.valueOf(
                        tasks.size()
                )
        );

        metadata.put(
                "decompositionMode",
                "DETERMINISTIC_EVIDENCE_AWARE"
        );

        return new Goal(
                new PlanningId(
                        "goal-"
                                + objective.planningId().value()
                ),
                objective,
                new GoalConstraints(
                        Map.of(
                                "taskCount",
                                String.valueOf(
                                        tasks.size()
                                )
                        ),
                        dependencyMap(
                                tasks.stream()
                                        .map(
                                                Task::planningId
                                        )
                                        .map(
                                                PlanningId::value
                                        )
                                        .toList()
                        ),
                        Map.of(),
                        Map.of(
                                "planningIntelligenceVersion",
                                "1.0"
                        )
                ),
                metadata
        );
    }

    private Schedule buildSchedule(
            List<Task> tasks) {

        List<Task> ordered =
                topologicalOrder(
                        tasks
                );

        Map<String, String> metadata =
                new LinkedHashMap<>();

        metadata.put(
                "planningIntelligenceVersion",
                "1.0"
        );

        metadata.put(
                "taskCount",
                String.valueOf(
                        ordered.size()
                )
        );

        metadata.put(
                "scheduleStrategy",
                "DEPENDENCY_AWARE_STABLE_ORDER"
        );

        metadata.put(
                "criticalPathLength",
                String.valueOf(
                        criticalPathLength(
                                ordered
                        )
                )
        );

        return new Schedule(
                ordered,
                new SchedulingConstraints(
                        Map.of(),
                        Map.of(
                                "strategy",
                                "DEPENDENCY_AWARE"
                        ),
                        dependencyMetadata(
                                ordered
                        ),
                        metadata
                ),
                ordered.stream()
                        .map(
                                Task::planningId
                        )
                        .map(
                                PlanningId::value
                        )
                        .toList(),
                metadata
        );
    }

    private List<Task> topologicalOrder(
            List<Task> tasks) {

        if (tasks.isEmpty()) {
            return List.of();
        }

        Map<String, Task> byId =
                new LinkedHashMap<>();

        for (Task task :
                tasks) {

            byId.put(
                    task.planningId().value(),
                    task
            );
        }

        List<Task> ordered =
                new ArrayList<>();

        Set<String> completed =
                new LinkedHashSet<>();

        List<Task> remaining =
                new ArrayList<>(
                        tasks
                );

        while (!remaining.isEmpty()) {

            Task candidate =
                    null;

            for (Task task :
                    remaining) {

                Set<String> dependencies =
                        dependenciesOf(
                                task
                        );

                if (completed.containsAll(
                        dependencies
                )) {

                    candidate =
                            task;

                    break;
                }
            }

            if (candidate == null) {

                /*
                 * A cycle exists. Do not silently produce a false schedule.
                 * Preserve stable source order and expose the issue through
                 * schedule metadata later.
                 */
                remaining.sort(
                        Comparator.comparing(
                                task ->
                                        task.planningId().value()
                        )
                );

                ordered.addAll(
                        remaining
                );

                break;
            }

            ordered.add(
                    candidate
            );

            completed.add(
                    candidate.planningId().value()
            );

            remaining.remove(
                    candidate
            );
        }

        return List.copyOf(
                ordered
        );
    }

    private Priority derivePriority(
            String description,
            int index,
            int total) {

        Set<String> terms =
                terms(
                        description
                );

        boolean urgent =
                terms.stream()
                        .anyMatch(
                                URGENT_TERMS::contains
                        );

        boolean important =
                terms.stream()
                        .anyMatch(
                                IMPORTANT_TERMS::contains
                        );

        String level;
        String urgency;
        String importance;

        if (urgent) {

            level = "CRITICAL";
            urgency = "HIGH";
            importance = "HIGH";

        } else if (important) {

            level = "HIGH";
            urgency = "MEDIUM";
            importance = "HIGH";

        } else if (index == 0) {

            level = "HIGH";
            urgency = "MEDIUM";
            importance = "HIGH";

        } else if (index == total - 1) {

            level = "MEDIUM";
            urgency = "LOW";
            importance = "HIGH";

        } else {

            level = "MEDIUM";
            urgency = "MEDIUM";
            importance = "MEDIUM";
        }

        return new Priority(
                level,
                urgency,
                importance,
                Map.of(
                        "derivedBy",
                        "PlanningIntelligenceEngine",
                        "position",
                        String.valueOf(
                                index + 1
                        )
                )
        );
    }

    private PlanQuality evaluatePlan(
            PlanningObjective objective,
            List<Task> tasks,
            Schedule schedule) {

        if (tasks.isEmpty()) {
            return PlanQuality.empty();
        }

        double specificity =
                tasks.stream()
                        .mapToDouble(
                                task ->
                                        specificity(
                                                task.description()
                                        )
                        )
                        .average()
                        .orElse(
                                0.0
                        );

        double dependencyIntegrity =
                dependencyIntegrity(
                        tasks
                );

        double orderingQuality =
                orderingQuality(
                        tasks,
                        schedule
                );

        double sourceGrounding =
                objective.metadata()
                        .containsKey(
                                "requestText"
                        )
                        || objective.metadata()
                        .containsKey(
                                "reasoningConclusion"
                        )
                        ? 1.0
                        : 0.60;

        double confidence =
                clamp(
                        specificity * 0.30
                                + dependencyIntegrity * 0.30
                                + orderingQuality * 0.20
                                + sourceGrounding * 0.20,
                        0.0,
                        1.0
                );

        return new PlanQuality(
                specificity,
                dependencyIntegrity,
                orderingQuality,
                sourceGrounding,
                confidence,
                confidence >= 0.80
                        ? "HIGH"
                        : confidence >= 0.60
                        ? "MODERATE"
                        : "LIMITED"
        );
    }

    private double specificity(
            String description) {

        Set<String> terms =
                terms(
                        description
                );

        double lengthScore =
                clamp(
                        terms.size()
                                / 10.0,
                        0.0,
                        1.0
                );

        boolean actionable =
                terms.stream()
                        .anyMatch(
                                ACTION_TERMS::contains
                        );

        return clamp(
                lengthScore * 0.60
                        + (actionable ? 0.40 : 0.10),
                0.0,
                1.0
        );
    }

    private double dependencyIntegrity(
            List<Task> tasks) {

        Set<String> ids =
                tasks.stream()
                        .map(
                                task ->
                                        task.planningId().value()
                        )
                        .collect(
                                java.util.stream.Collectors.toSet()
                        );

        int references = 0;
        int valid = 0;

        for (Task task :
                tasks) {

            for (String dependency :
                    dependenciesOf(
                            task
                    )) {

                references++;

                if (ids.contains(
                        dependency
                )
                        && !dependency.equals(
                        task.planningId().value()
                )) {

                    valid++;
                }
            }
        }

        if (references == 0) {
            return tasks.size() <= 1
                    ? 1.0
                    : 0.70;
        }

        return (double) valid
                / references;
    }

    private double orderingQuality(
            List<Task> tasks,
            Schedule schedule) {

        if (tasks.size()
                != schedule.plannedSequence().size()) {

            return 0.0;
        }

        return schedule.plannedSequence()
                .equals(
                        topologicalOrder(
                                tasks
                        )
                )
                ? 1.0
                : 0.50;
    }

    private int criticalPathLength(
            List<Task> tasks) {

        if (tasks.isEmpty()) {
            return 0;
        }

        Map<String, Integer> depth =
                new LinkedHashMap<>();

        for (Task task :
                tasks) {

            int currentDepth = 1;

            for (String dependency :
                    dependenciesOf(
                            task
                    )) {

                currentDepth =
                        Math.max(
                                currentDepth,
                                depth.getOrDefault(
                                        dependency,
                                        0
                                ) + 1
                        );
            }

            depth.put(
                    task.planningId().value(),
                    currentDepth
            );
        }

        return depth.values()
                .stream()
                .max(
                        Integer::compareTo
                )
                .orElse(0);
    }

    private Map<String, String> dependencyMap(
            Set<String> dependencies) {

        Map<String, String> result =
                new LinkedHashMap<>();

        int index = 1;

        for (String dependency :
                dependencies) {

            result.put(
                    "dependency-" + index++,
                    dependency
            );
        }

        return result;
    }

    private Map<String, String> dependencyMap(
            List<String> dependencies) {

        return dependencyMap(
                new LinkedHashSet<>(
                        dependencies
                )
        );
    }

    private Map<String, String> dependencyMetadata(
            List<Task> tasks) {

        Map<String, String> result =
                new LinkedHashMap<>();

        for (Task task :
                tasks) {

            result.put(
                    task.planningId().value(),
                    String.join(
                            ",",
                            dependenciesOf(
                                    task
                            )
                    )
            );
        }

        return result;
    }

    private Set<String> dependenciesOf(
            Task task) {

        String raw =
                task.metadata().get(
                        "dependsOn"
                );

        if (raw == null
                || raw.isBlank()) {

            return Set.of();
        }

        return java.util.Arrays.stream(
                        raw.split(",")
                )
                .map(
                        String::trim
                )
                .filter(
                        value ->
                                !value.isBlank()
                )
                .collect(
                        java.util.stream.Collectors.toCollection(
                                LinkedHashSet::new
                        )
                );
    }

    private Map<String, Object> buildMetadata(
            PlanningObjective objective,
            List<Task> tasks,
            Schedule schedule,
            PlanQuality quality) {

        Map<String, Object> metadata =
                new LinkedHashMap<>();

        metadata.put(
                "planningIntelligenceVersion",
                "1.0"
        );

        metadata.put(
                "status",
                "ANALYZED"
        );

        metadata.put(
                "objectiveId",
                objective.planningId().value()
        );

        metadata.put(
                "planningScope",
                objective.scope()
        );

        metadata.put(
                "taskCount",
                tasks.size()
        );

        metadata.put(
                "criticalPathLength",
                criticalPathLength(
                        tasks
                )
        );

        metadata.put(
                "scheduleStrategy",
                "DEPENDENCY_AWARE_STABLE_ORDER"
        );

        metadata.put(
                "quality",
                quality
        );

        metadata.put(
                "explicitTaskEvidence",
                tasks.stream()
                        .filter(
                                task ->
                                        "EXPLICIT".equals(
                                                task.metadata()
                                                        .get(
                                                                "origin"
                                                        )
                                        )
                        )
                        .count()
        );

        metadata.put(
                "inferredTaskCount",
                tasks.stream()
                        .filter(
                                task ->
                                        "INFERRED".equals(
                                                task.metadata()
                                                        .get(
                                                                "origin"
                                                        )
                                        )
                        )
                        .count()
        );

        return Map.copyOf(
                metadata
        );
    }

    private String firstNonBlank(
            String... values) {

        for (String value :
                values) {

            if (value != null
                    && !value.isBlank()) {

                return value;
            }
        }

        return null;
    }

    private String safe(
            String value) {

        return value == null
                ? ""
                : value.trim();
    }

    private String normalizeText(
            String value) {

        return safe(
                value
        )
                .replaceAll(
                        "\\s+",
                        " "
                )
                .trim();
    }

    private Set<String> terms(
            String text) {

        if (text == null
                || text.isBlank()) {

            return Set.of();
        }

        Set<String> result =
                new LinkedHashSet<>();

        var matcher =
                TOKEN_PATTERN.matcher(
                        text.toLowerCase(
                                Locale.ROOT
                        )
                );

        while (matcher.find()) {

            String token =
                    matcher.group();

            if (token.length() >= 2) {
                result.add(
                        token
                );
            }
        }

        return result;
    }

    private int priorityRank(
            Task task) {

        return switch (
                task.priority()
                        .level()
                        .toUpperCase(
                                Locale.ROOT
                        )) {

            case "CRITICAL" -> 4;
            case "HIGH" -> 3;
            case "MEDIUM" -> 2;
            case "LOW" -> 1;
            default -> 0;
        };
    }

    private double clamp(
            double value,
            double minimum,
            double maximum) {

        return Math.max(
                minimum,
                Math.min(
                        maximum,
                        value
                )
        );
    }

    /**
     * Immutable planning-quality assessment.
     */
    public record PlanQuality(
            double specificity,
            double dependencyIntegrity,
            double orderingQuality,
            double sourceGrounding,
            double confidence,
            String confidenceBand) {

        static PlanQuality empty() {
            return new PlanQuality(
                    0.0,
                    0.0,
                    0.0,
                    0.0,
                    0.05,
                    "VERY_LOW"
            );
        }
    }

    /**
     * Immutable complete planning analysis.
     */
    public record PlanningAnalysis(
            Goal goal,
            List<Task> tasks,
            Schedule schedule,
            List<Priority> priorities,
            PlanQuality quality,
            Map<String, Object> metadata) {

        public PlanningAnalysis {
            tasks =
                    tasks == null
                            ? List.of()
                            : List.copyOf(tasks);

            priorities =
                    priorities == null
                            ? List.of()
                            : List.copyOf(priorities);

            metadata =
                    metadata == null
                            ? Map.of()
                            : Map.copyOf(metadata);
        }
    }

    private record TaskBlueprint(
            String id,
            String description,
            Set<String> dependencies,
            String origin) {
    }
}