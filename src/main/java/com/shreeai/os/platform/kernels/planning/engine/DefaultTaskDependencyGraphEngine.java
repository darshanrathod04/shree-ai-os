package com.shreeai.os.platform.kernels.planning.engine;

import com.shreeai.os.platform.kernels.planning.model.PlanBlueprint;
import com.shreeai.os.platform.kernels.planning.model.PlanMilestone;
import com.shreeai.os.platform.kernels.planning.model.PlanningTask;
import com.shreeai.os.platform.kernels.planning.model.TaskDependency;
import com.shreeai.os.platform.kernels.planning.model.TaskGraph;
import com.shreeai.os.platform.kernels.planning.model.TaskType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * <b>DefaultTaskDependencyGraphEngine</b>
 *
 * <p>Stateless, deterministic, thread-safe implementation of
 * {@link TaskDependencyGraphEngine}.</p>
 *
 * <p><b>Template expansion:</b> each milestone is converted into an
 * ordered row of {@link PlanningTask} objects using a fixed, locale-free
 * keyword table. No LLM, no ML, no randomness. The table is ordered from the
 * most specific keyword bundle to the most generic, so the expansion is
 * fully reproducible.</p>
 *
 * <p><b>Edge policy:</b> intra-milestone tasks are linked with
 * FINISH_TO_START; cross-milestone links connect the last task of milestone
 * N to the first task of milestone N+1, also FINISH_TO_START. The
 * START_TO_START type is modelled by the domain but is not emitted by this
 * deterministic expansion.</p>
 *
 * <p><b>Determinism:</b> the same blueprint always yields the identical
 * graph - identical task identifiers, identical edges, identical ordering.</p>
 *
 * <p><b>Ownership:</b> Planning Kernel - P2.2 Task Dependency Graph</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @since P2.2
 */
public final class DefaultTaskDependencyGraphEngine implements TaskDependencyGraphEngine {

    /** A fixed template row: a title paired with its deterministic type. */
    public record TaskTemplate(String title, TaskType type) {
        public TaskTemplate {
            if (title == null || title.trim().isEmpty()) {
                throw new IllegalArgumentException("TaskTemplate title must not be blank");
            }
            if (type == null) {
                throw new IllegalArgumentException("TaskTemplate type must not be null");
            }
        }
    }

    /**
     * A deterministic expansion rule: a lower-cased keyword predicate and the
     * ordered templates it expands to. {@code anyOf} matches if any keyword
     * occurs; {@code allOf} requires every keyword to be present.
     */
    public record ExpansionRule(
            List<String> allOf,
            List<String> anyOf,
            List<TaskTemplate> templates) {

        public ExpansionRule {
            allOf = allOf == null ? List.of() : List.copyOf(allOf);
            anyOf = anyOf == null ? List.of() : List.copyOf(anyOf);
            templates = List.copyOf(templates);
            if (templates.isEmpty()) {
                throw new IllegalArgumentException("ExpansionRule templates must not be empty");
            }
        }

        /**
         * Reports whether this rule fires for the given lower-cased milestone
         * name.
         */
        public boolean matches(String lowerCasedMilestoneName) {
            for (String keyword : allOf) {
                if (!lowerCasedMilestoneName.contains(keyword)) {
                    return false;
                }
            }
            for (String keyword : anyOf) {
                if (lowerCasedMilestoneName.contains(keyword)) {
                    return true;
                }
            }
            return false;
        }
    }

    /** The deterministic template table: specific to generic, default last. */
    public static final List<ExpansionRule> RULES = loadRules();

    private static List<ExpansionRule> loadRules() {
        List<ExpansionRule> rules = new ArrayList<>();
        rules.add(rule(List.of(), List.of("javascript"), ts(
                t("Syntax Fundamentals", TaskType.LEARNING),
                t("Functions & Scope", TaskType.LEARNING),
                t("DOM Manipulation", TaskType.IMPLEMENTATION))));
        rules.add(rule(List.of("java"), List.of("basic", "fundamental", "foundation", "core"), ts(
                t("Variables", TaskType.LEARNING),
                t("Loops", TaskType.LEARNING),
                t("Methods", TaskType.LEARNING))));
        rules.add(rule(List.of(), List.of("oop", "object oriented"), ts(
                t("Class", TaskType.LEARNING),
                t("Object", TaskType.LEARNING),
                t("Inheritance", TaskType.LEARNING))));
        rules.add(rule(List.of(), List.of("spring"), ts(
                t("Spring Core", TaskType.LEARNING),
                t("Spring MVC", TaskType.IMPLEMENTATION),
                t("Spring Boot Application", TaskType.PROJECT))));
        rules.add(rule(List.of(), List.of("api", "rest", "endpoint", "microservice"), ts(
                t("REST Design", TaskType.LEARNING),
                t("Endpoint Implementation", TaskType.IMPLEMENTATION),
                t("API Testing", TaskType.ASSESSMENT))));
        rules.add(rule(List.of(), List.of("database", "sql", "persistence", "data model"), ts(
                t("Data Modeling", TaskType.LEARNING),
                t("Query Implementation", TaskType.IMPLEMENTATION),
                t("Data Validation", TaskType.ASSESSMENT))));
        rules.add(rule(List.of(), List.of("market", "customer", "interview", "discovery", "research"), ts(
                t("Customer Research", TaskType.LEARNING),
                t("Validation Interviews", TaskType.IMPLEMENTATION),
                t("Findings Report", TaskType.REVIEW))));
        rules.add(rule(List.of(), List.of("test", "testing", "quality", "qa"), ts(
                t("Test Plan", TaskType.LEARNING),
                t("Test Execution", TaskType.IMPLEMENTATION),
                t("Quality Report", TaskType.ASSESSMENT))));
        rules.add(rule(List.of(), List.of("design", "architecture", "requirement", "strategy"), ts(
                t("Requirements Analysis", TaskType.LEARNING),
                t("Design Draft", TaskType.IMPLEMENTATION),
                t("Design Review", TaskType.REVIEW))));
        rules.add(rule(List.of(), List.of("launch", "release", "deploy", "deployment", "production", "go-live", "ship"), ts(
                t("Release Checklist", TaskType.IMPLEMENTATION),
                t("Production Deployment", TaskType.IMPLEMENTATION),
                t("Post-Launch Review", TaskType.REVIEW))));
        rules.add(rule(List.of(), List.of("project", "portfolio", "capstone", "build"), ts(
                t("Scope Definition", TaskType.LEARNING),
                t("Project Implementation", TaskType.IMPLEMENTATION),
                t("Project Review", TaskType.REVIEW))));
        rules.add(rule(List.of(), List.of("review", "assessment", "evaluation", "mastery", "checkpoint"), ts(
                t("Self Assessment", TaskType.ASSESSMENT),
                t("Gap Analysis", TaskType.ASSESSMENT),
                t("Remediation Plan", TaskType.REVIEW))));
        rules.add(rule(List.of(), List.of("foundation", "fundamental", "basic", "core", "intro", "onboarding", "getting started"), ts(
                t("Core Concepts", TaskType.LEARNING),
                t("Guided Practice", TaskType.IMPLEMENTATION),
                t("Progress Review", TaskType.REVIEW))));
        rules.add(rule(List.of(), List.of("fitness", "health", "workout", "training", "nutrition", "diet"), ts(
                t("Baseline Assessment", TaskType.ASSESSMENT),
                t("Training Block", TaskType.IMPLEMENTATION),
                t("Progress Review", TaskType.REVIEW))));
        // Default fallback: always matches.
        rules.add(rule(List.of(), List.of(), ts(
                t("Learn Core Ideas", TaskType.LEARNING),
                t("Apply Concepts", TaskType.IMPLEMENTATION),
                t("Review Progress", TaskType.REVIEW))));
        return List.copyOf(rules);
    }

    private static ExpansionRule rule(List<String> allOf, List<String> anyOf, List<TaskTemplate> templates) {
        return new ExpansionRule(allOf, anyOf, templates);
    }

    private static TaskTemplate t(String title, TaskType type) {
        return new TaskTemplate(title, type);
    }

    private static List<TaskTemplate> ts(TaskTemplate... templates) {
        return List.of(templates);
    }

    // ── Pipeline public API ─────────────────────────────────────────────

    @Override
    public TaskGraph buildTaskGraph(PlanBlueprint blueprint) {
        if (blueprint == null) {
            return TaskGraph.empty();
        }
        return buildTaskGraph(blueprint.milestones());
    }

    /**
     * Converts an ordered list of milestones into a validated task graph.
     *
     * @param milestones the milestones in execution order (may be null)
     * @return a validated, canonically ordered task graph (never null)
     */
    public TaskGraph buildTaskGraph(List<PlanMilestone> milestones) {
        List<PlanMilestone> ordered = milestones == null
                ? List.of()
                : milestones.stream().filter(Objects::nonNull).toList();
        if (ordered.isEmpty()) {
            return TaskGraph.empty();
        }
        // Stage 1 + Stage 2 + Stage 3.
        List<PlanningTask> tasks = new ArrayList<>();
        List<TaskDependency> dependencies = new ArrayList<>();
        List<PlanningTask> previousMilestoneTasks = List.of();
        int nextOrder = PlanningTask.MIN_ORDER;
        for (PlanMilestone milestone : ordered) {
            List<PlanningTask> milestoneTasks = expandMilestone(milestone, nextOrder);
            nextOrder += milestoneTasks.size();
            tasks.addAll(milestoneTasks);
            // Stage 2 - intra-milestone FINISH_TO_START chain.
            dependencies.addAll(chainDependencies(milestoneTasks));
            // Stage 3 - cross-milestone link to the previous milestone.
            if (!previousMilestoneTasks.isEmpty()) {
                dependencies.add(crossMilestoneDependency(previousMilestoneTasks, milestoneTasks));
            }
            previousMilestoneTasks = milestoneTasks;
        }
        // Stage 4 (constructor invariant) + Stage 5 (canonical ordering).
                TaskGraph raw = new TaskGraph(tasks, dependencies);
        return new TaskGraph(raw.tasksInCanonicalOrder(),
                raw.dependenciesInCanonicalOrder());
    }

    // ── Stage 1: expand milestones ──────────────────────────────────────

    /**
     * Expands a milestone into an ordered row of tasks using the
     * deterministic {@link #RULES} table.
     *
     * @param milestone  the milestone to expand (must not be null)
     * @param firstOrder the starting composite order for the row (must be
     *                   {@code >= 1})
     * @return the ordered task row, each task with a deterministic
     *         identifier (never null, never empty)
     */
    public List<PlanningTask> expandMilestone(PlanMilestone milestone, int firstOrder) {
        Objects.requireNonNull(milestone, "milestone must not be null");
        if (firstOrder < PlanningTask.MIN_ORDER) {
            throw new IllegalArgumentException(
                    "firstOrder must be >= " + PlanningTask.MIN_ORDER + ": " + firstOrder);
        }
        List<TaskTemplate> templates = templatesFor(milestone);
        List<PlanningTask> tasks = new ArrayList<>(templates.size());
        int order = firstOrder;
        for (TaskTemplate template : templates) {
            tasks.add(PlanningTask.create(
                    milestone.name(), template.title(), template.type(), order));
            order++;
        }
        return List.copyOf(tasks);
    }

    /**
     * Selects the deterministic template row for a milestone.
     *
     * @param milestone the milestone (must not be null)
     * @return the first matching template row; the default fallback always
     *         matches, so the result is never empty
     */
    public List<TaskTemplate> templatesFor(PlanMilestone milestone) {
        Objects.requireNonNull(milestone, "milestone must not be null");
        String lower = normalize(milestone.name());
        for (ExpansionRule rule : RULES) {
            if (rule.matches(lower)) {
                return rule.templates();
            }
        }
        return RULES.get(RULES.size() - 1).templates();
    }

    // ── Stage 2: intra-milestone dependencies ──────────────────────────

    /**
     * Builds the FINISH_TO_START chain within a single milestone row:
     * {@code task0 -> task1 -> ... -> taskN-1}.
     */
    public List<TaskDependency> chainDependencies(List<PlanningTask> milestoneTasks) {
        Objects.requireNonNull(milestoneTasks, "milestoneTasks must not be null");
        List<TaskDependency> edges = new ArrayList<>(Math.max(0, milestoneTasks.size() - 1));
        for (int i = 1; i < milestoneTasks.size(); i++) {
            PlanningTask previous = Objects.requireNonNull(milestoneTasks.get(i - 1), "milestoneTasks[" + (i - 1) + "] must not be null");
            PlanningTask next = Objects.requireNonNull(milestoneTasks.get(i), "milestoneTasks[" + i + "] must not be null");
            edges.add(TaskDependency.finishToStart(previous.taskId(), next.taskId()));
        }
        return List.copyOf(edges);
    }

    // ── Stage 3: cross-milestone links ───────────────────────────────────

    /**
     * Builds the FINISH_TO_START link from the last task of the previous
     * milestone row to the first task of the next milestone row.
     */
    public TaskDependency crossMilestoneDependency(
            List<PlanningTask> previousMilestoneTasks,
            List<PlanningTask> nextMilestoneTasks) {
        Objects.requireNonNull(previousMilestoneTasks, "previousMilestoneTasks must not be null");
        Objects.requireNonNull(nextMilestoneTasks, "nextMilestoneTasks must not be null");
        if (previousMilestoneTasks.isEmpty()) {
            throw new IllegalArgumentException("previousMilestoneTasks must not be empty");
        }
        if (nextMilestoneTasks.isEmpty()) {
            throw new IllegalArgumentException("nextMilestoneTasks must not be empty");
        }
        PlanningTask previousLast = previousMilestoneTasks.get(previousMilestoneTasks.size() - 1);
        PlanningTask nextFirst = nextMilestoneTasks.get(0);
        return TaskDependency.finishToStart(previousLast.taskId(), nextFirst.taskId());
    }

    /**
     * Canonical, locale-independent normalisation of a descriptor for
     * keyword matching: trim, collapse whitespace, lower-case with
     * {@link Locale#ROOT}.
     */
    static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}

