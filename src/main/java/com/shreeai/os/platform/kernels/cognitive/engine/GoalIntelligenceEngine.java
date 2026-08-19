package com.shreeai.os.platform.kernels.cognitive.engine;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Goal Intelligence Engine.
 *
 * <p>Provides deterministic, evidence-aware analysis of goals without
 * directly executing, mutating, or scheduling work.</p>
 *
 * <p>The engine is responsible for understanding a goal as a cognitive
 * object and producing structured intelligence that downstream planning,
 * decision, execution, reflection, learning, adaptation, and replanning
 * layers can consume.</p>
 *
 * <p>Core responsibilities:</p>
 * <ul>
 *     <li>Goal normalization and understanding</li>
 *     <li>Goal decomposition</li>
 *     <li>Dependency identification</li>
 *     <li>Priority assessment</li>
 *     <li>Progress assessment</li>
 *     <li>Conflict detection</li>
 *     <li>Goal feasibility assessment</li>
 *     <li>Goal evolution signals</li>
 *     <li>Evidence and confidence tracking</li>
 * </ul>
 *
 * <p>This engine does not execute actions and does not mutate external
 * planning or execution state.</p>
 *
 * @author Shree AI OS Team
 * @version 1.0
 */
public final class GoalIntelligenceEngine {

    private static final String ENGINE_NAME = "GoalIntelligenceEngine";
    private static final String VERSION = "1.0";

    /**
     * Analyze a goal using the supplied request.
     *
     * @param request goal analysis request
     * @return immutable goal intelligence analysis
     */
    public GoalAnalysis analyze(GoalRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        String normalizedGoal = normalizeGoal(request.goal());

        List<String> evidence =
                normalizeList(request.evidence());

        List<String> constraints =
                normalizeList(request.constraints());

        List<String> knownProgress =
                normalizeList(request.completedWork());

        List<String> blockers =
                normalizeList(request.blockers());

        List<String> dependencies =
                normalizeList(request.dependencies());

        List<String> conflicts =
                detectConflicts(
                        normalizedGoal,
                        constraints,
                        request.relatedGoals()
                );

        List<String> subtasks =
                decomposeGoal(normalizedGoal);

        double progress =
                calculateProgress(
                        subtasks,
                        knownProgress,
                        request.progress()
                );

        Priority priority =
                assessPriority(
                        request.urgency(),
                        request.importance(),
                        request.impact()
                );

        Feasibility feasibility =
                assessFeasibility(
                        blockers,
                        conflicts,
                        dependencies,
                        request.feasibility()
                );

        GoalStatus status =
                determineStatus(
                        progress,
                        blockers,
                        feasibility
                );

        List<String> requiredInformation =
                determineMissingInformation(
                        evidence,
                        constraints,
                        dependencies,
                        blockers,
                        feasibility
                );

        List<String> evolutionSignals =
                determineEvolutionSignals(
                        progress,
                        blockers,
                        conflicts,
                        request.goalStability()
                );

        boolean decompositionRequired =
                subtasks.size() > 1 || request.forceDecomposition();

        boolean replanningRelevant =
                !blockers.isEmpty()
                        || !conflicts.isEmpty()
                        || !evolutionSignals.isEmpty()
                        || status == GoalStatus.BLOCKED
                        || status == GoalStatus.AT_RISK;

        double confidence =
                calculateConfidence(
                        evidence,
                        progress,
                        feasibility,
                        requiredInformation
                );

        ConfidenceBand confidenceBand =
                confidenceBand(confidence);

        List<String> recommendations =
                generateRecommendations(
                        status,
                        priority,
                        feasibility,
                        decompositionRequired,
                        replanningRelevant,
                        requiredInformation
                );

        Map<String, Object> metadata =
                buildMetadata(
                        request,
                        normalizedGoal,
                        status,
                        priority,
                        feasibility,
                        progress,
                        confidence
                );

        return new GoalAnalysis(
                request.analysisId(),
                normalizedGoal,
                status,
                priority,
                feasibility,
                progress,
                confidence,
                confidenceBand,
                decompositionRequired,
                replanningRelevant,
                subtasks,
                dependencies,
                blockers,
                conflicts,
                requiredInformation,
                evolutionSignals,
                recommendations,
                evidence,
                constraints,
                metadata,
                Instant.now()
        );
    }

    /**
     * Normalize goal text while preserving semantic content.
     */
    private String normalizeGoal(String goal) {

        if (goal == null || goal.isBlank()) {
            return "Undefined goal";
        }

        return goal
                .trim()
                .replaceAll("\\s+", " ");
    }

    /**
     * Performs conservative deterministic decomposition.
     *
     * <p>This deliberately does not pretend to perform LLM-level semantic
     * decomposition. It extracts explicit task boundaries when they are
     * observable in the goal text and otherwise keeps the goal intact.</p>
     */
    private List<String> decomposeGoal(String goal) {

        if (goal == null || goal.isBlank()) {
            return List.of();
        }

        String normalized = goal.trim();

        String[] separators = {
                "\\s+and\\s+",
                "\\s+then\\s+",
                "\\s+followed by\\s+",
                "\\s*,\\s*"
        };

        for (String separator : separators) {

            String[] parts = normalized.split(separator);

            if (parts.length > 1) {

                LinkedHashSet<String> unique =
                        new LinkedHashSet<>();

                for (String part : parts) {

                    String value = part.trim();

                    if (!value.isBlank()) {
                        unique.add(value);
                    }
                }

                if (unique.size() > 1) {
                    return List.copyOf(unique);
                }
            }
        }

        return List.of(normalized);
    }

    /**
     * Detect obvious goal/constraint conflicts.
     */
    private List<String> detectConflicts(
            String goal,
            List<String> constraints,
            List<String> relatedGoals) {

        List<String> conflicts =
                new ArrayList<>();

        String lowerGoal =
                goal.toLowerCase();

        for (String constraint : constraints) {

            String lowerConstraint =
                    constraint.toLowerCase();

            if (containsContradiction(
                    lowerGoal,
                    lowerConstraint)) {

                conflicts.add(
                        "Goal may conflict with constraint: "
                                + constraint
                );
            }
        }

        if (relatedGoals != null) {

            for (String related : relatedGoals) {

                if (related == null || related.isBlank()) {
                    continue;
                }

                String lowerRelated =
                        related.toLowerCase();

                if (semanticOpposition(
                        lowerGoal,
                        lowerRelated)) {

                    conflicts.add(
                            "Potential conflict with related goal: "
                                    + related
                    );
                }
            }
        }

        return List.copyOf(conflicts);
    }

    private boolean containsContradiction(
            String goal,
            String constraint) {

        if (constraint.contains("cannot")
                || constraint.contains("must not")
                || constraint.contains("forbidden")) {

            String[] terms =
                    constraint
                            .replace(
                                    "cannot",
                                    "")
                            .replace(
                                    "must not",
                                    "")
                            .replace(
                                    "forbidden",
                                    "")
                            .trim()
                            .split("\\s+");

            int matches = 0;

            for (String term : terms) {

                if (term.length() > 3
                        && goal.contains(term)) {
                    matches++;
                }
            }

            return matches >= 1;
        }

        return false;
    }

    private boolean semanticOpposition(
            String goal,
            String related) {

        return (goal.contains("remove")
                && related.contains("add"))
                || (goal.contains("delete")
                && related.contains("preserve"))
                || (goal.contains("minimize")
                && related.contains("maximize"));
    }

    /**
     * Calculate progress using explicit progress when available,
     * otherwise derive a conservative value from completed subtasks.
     */
    private double calculateProgress(
            List<String> subtasks,
            List<String> completedWork,
            Double explicitProgress) {

        if (explicitProgress != null) {
            return clamp(explicitProgress);
        }

        if (subtasks.isEmpty()
                || completedWork.isEmpty()) {
            return 0.0;
        }

        int completed = 0;

        for (String task : subtasks) {

            for (String completedTask : completedWork) {

                if (similar(
                        task,
                        completedTask)) {

                    completed++;
                    break;
                }
            }
        }

        return clamp(
                (double) completed
                        / (double) subtasks.size()
        );
    }

    private boolean similar(
            String first,
            String second) {

        if (first == null || second == null) {
            return false;
        }

        String a =
                first
                        .toLowerCase()
                        .trim();

        String b =
                second
                        .toLowerCase()
                        .trim();

        return a.equals(b)
                || a.contains(b)
                || b.contains(a);
    }

    /**
     * Assess priority from urgency, importance, and impact.
     */
    private Priority assessPriority(
            int urgency,
            int importance,
            int impact) {

        double score =
                (clampScore(urgency)
                        + clampScore(importance)
                        + clampScore(impact))
                        / 3.0;

        if (score >= 8.0) {
            return Priority.CRITICAL;
        }

        if (score >= 6.5) {
            return Priority.HIGH;
        }

        if (score >= 4.0) {
            return Priority.MEDIUM;
        }

        return Priority.LOW;
    }

    private int clampScore(int value) {
        return Math.max(0, Math.min(10, value));
    }

    /**
     * Conservative feasibility assessment.
     */
    private Feasibility assessFeasibility(
            List<String> blockers,
            List<String> conflicts,
            List<String> dependencies,
            Feasibility requested) {

        if (!blockers.isEmpty()) {
            return Feasibility.BLOCKED;
        }

        if (!conflicts.isEmpty()) {
            return Feasibility.UNCERTAIN;
        }

        if (!dependencies.isEmpty()) {
            return Feasibility.CONDITIONAL;
        }

        if (requested != null) {
            return requested;
        }

        return Feasibility.PLAUSIBLE;
    }

    private GoalStatus determineStatus(
            double progress,
            List<String> blockers,
            Feasibility feasibility) {

        if (!blockers.isEmpty()
                || feasibility == Feasibility.BLOCKED) {
            return GoalStatus.BLOCKED;
        }

        if (progress >= 0.999) {
            return GoalStatus.COMPLETED;
        }

        if (feasibility == Feasibility.UNCERTAIN) {
            return GoalStatus.AT_RISK;
        }

        if (progress > 0.0) {
            return GoalStatus.IN_PROGRESS;
        }

        return GoalStatus.NOT_STARTED;
    }

    private List<String> determineMissingInformation(
            List<String> evidence,
            List<String> constraints,
            List<String> dependencies,
            List<String> blockers,
            Feasibility feasibility) {

        List<String> missing =
                new ArrayList<>();

        if (evidence.isEmpty()) {
            missing.add(
                    "Evidence supporting the current goal state"
            );
        }

        if (constraints.isEmpty()) {
            missing.add(
                    "Explicit goal constraints"
            );
        }

        if (feasibility == Feasibility.CONDITIONAL
                && dependencies.isEmpty()) {

            missing.add(
                    "Dependency details required for conditional feasibility"
            );
        }

        if (!blockers.isEmpty()) {
            missing.add(
                    "Resolution information for active blockers"
            );
        }

        return List.copyOf(missing);
    }

    private List<String> determineEvolutionSignals(
            double progress,
            List<String> blockers,
            List<String> conflicts,
            GoalStability stability) {

        List<String> signals =
                new ArrayList<>();

        if (!blockers.isEmpty()) {
            signals.add("BLOCKER_CHANGED_GOAL_FEASIBILITY");
        }

        if (!conflicts.isEmpty()) {
            signals.add("CONFLICT_MAY_REQUIRE_GOAL_REVISION");
        }

        if (progress < 0.25
                && stability == GoalStability.UNSTABLE) {

            signals.add("GOAL_MAY_REQUIRE_RESCOPING");
        }

        if (progress >= 0.75
                && stability == GoalStability.UNSTABLE) {

            signals.add("GOAL_COMPLETION_CRITERIA_MAY_HAVE_CHANGED");
        }

        return List.copyOf(signals);
    }

    private double calculateConfidence(
            List<String> evidence,
            double progress,
            Feasibility feasibility,
            List<String> missingInformation) {

        double confidence = 0.25;

        if (!evidence.isEmpty()) {
            confidence += 0.25;
        }

        if (progress > 0.0) {
            confidence += 0.15;
        }

        if (feasibility == Feasibility.PLAUSIBLE) {
            confidence += 0.20;
        } else if (feasibility == Feasibility.CONDITIONAL) {
            confidence += 0.10;
        }

        if (missingInformation.isEmpty()) {
            confidence += 0.15;
        }

        if (feasibility == Feasibility.BLOCKED) {
            confidence *= 0.5;
        }

        return clamp(confidence);
    }

    private ConfidenceBand confidenceBand(
            double confidence) {

        if (confidence >= 0.80) {
            return ConfidenceBand.HIGH;
        }

        if (confidence >= 0.55) {
            return ConfidenceBand.MEDIUM;
        }

        if (confidence >= 0.30) {
            return ConfidenceBand.LOW;
        }

        return ConfidenceBand.MINIMAL;
    }

    private List<String> generateRecommendations(
            GoalStatus status,
            Priority priority,
            Feasibility feasibility,
            boolean decompositionRequired,
            boolean replanningRelevant,
            List<String> missingInformation) {

        List<String> recommendations =
                new ArrayList<>();

        if (decompositionRequired) {
            recommendations.add(
                    "Decompose the goal into independently trackable objectives"
            );
        }

        if (!missingInformation.isEmpty()) {
            recommendations.add(
                    "Acquire missing goal information before high-confidence planning"
            );
        }

        if (feasibility == Feasibility.BLOCKED) {
            recommendations.add(
                    "Resolve blockers before execution"
            );
        }

        if (feasibility == Feasibility.UNCERTAIN) {
            recommendations.add(
                    "Validate conflicting assumptions before committing to execution"
            );
        }

        if (replanningRelevant) {
            recommendations.add(
                    "Evaluate whether the current plan remains aligned with the goal"
            );
        }

        if (priority == Priority.CRITICAL
                && status != GoalStatus.COMPLETED) {
            recommendations.add(
                    "Maintain high-priority monitoring of goal progress"
            );
        }

        if (recommendations.isEmpty()) {
            recommendations.add(
                    "Continue with the current goal-directed plan"
            );
        }

        return List.copyOf(
                new LinkedHashSet<>(recommendations)
        );
    }

    private Map<String, Object> buildMetadata(
            GoalRequest request,
            String normalizedGoal,
            GoalStatus status,
            Priority priority,
            Feasibility feasibility,
            double progress,
            double confidence) {

        Map<String, Object> metadata =
                new LinkedHashMap<>();

        metadata.put("engine", ENGINE_NAME);
        metadata.put("version", VERSION);
        metadata.put("analysisId", request.analysisId());
        metadata.put("goal", normalizedGoal);
        metadata.put("status", status.name());
        metadata.put("priority", priority.name());
        metadata.put("feasibility", feasibility.name());
        metadata.put("progress", progress);
        metadata.put("confidence", confidence);
        metadata.put("executionStarted", false);
        metadata.put("planMutationPerformed", false);
        metadata.put("goalMutationPerformed", false);
        metadata.put("timestamp", Instant.now());

        return Collections.unmodifiableMap(
                new LinkedHashMap<>(metadata)
        );
    }

    private List<String> normalizeList(
            List<String> values) {

        if (values == null || values.isEmpty()) {
            return List.of();
        }

        LinkedHashSet<String> normalized =
                new LinkedHashSet<>();

        for (String value : values) {

            if (value == null) {
                continue;
            }

            String cleaned =
                    value.trim()
                            .replaceAll("\\s+", " ");

            if (!cleaned.isBlank()) {
                normalized.add(cleaned);
            }
        }

        return List.copyOf(normalized);
    }

    private double clamp(double value) {
        return Math.max(
                0.0,
                Math.min(1.0, value)
        );
    }

    // ---------------------------------------------------------------------
    // Request
    // ---------------------------------------------------------------------

    public record GoalRequest(
            String analysisId,
            String goal,
            List<String> evidence,
            List<String> constraints,
            List<String> completedWork,
            List<String> blockers,
            List<String> dependencies,
            List<String> relatedGoals,
            Double progress,
            int urgency,
            int importance,
            int impact,
            Feasibility feasibility,
            GoalStability goalStability,
            boolean forceDecomposition
    ) {

        public GoalRequest {
            analysisId =
                    analysisId == null || analysisId.isBlank()
                            ? UUID.randomUUID().toString()
                            : analysisId;

            evidence =
                    evidence == null
                            ? List.of()
                            : List.copyOf(evidence);

            constraints =
                    constraints == null
                            ? List.of()
                            : List.copyOf(constraints);

            completedWork =
                    completedWork == null
                            ? List.of()
                            : List.copyOf(completedWork);

            blockers =
                    blockers == null
                            ? List.of()
                            : List.copyOf(blockers);

            dependencies =
                    dependencies == null
                            ? List.of()
                            : List.copyOf(dependencies);

            relatedGoals =
                    relatedGoals == null
                            ? List.of()
                            : List.copyOf(relatedGoals);

            goalStability =
                    goalStability == null
                            ? GoalStability.STABLE
                            : goalStability;
        }

        public static GoalRequest of(String goal) {
            return new GoalRequest(
                    UUID.randomUUID().toString(),
                    goal,
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of(),
                    null,
                    5,
                    5,
                    5,
                    null,
                    GoalStability.STABLE,
                    false
            );
        }
    }

    // ---------------------------------------------------------------------
    // Analysis
    // ---------------------------------------------------------------------

    public record GoalAnalysis(
            String analysisId,
            String normalizedGoal,
            GoalStatus status,
            Priority priority,
            Feasibility feasibility,
            double progress,
            double confidence,
            ConfidenceBand confidenceBand,
            boolean decompositionRequired,
            boolean replanningRelevant,
            List<String> subtasks,
            List<String> dependencies,
            List<String> blockers,
            List<String> conflicts,
            List<String> requiredInformation,
            List<String> evolutionSignals,
            List<String> recommendations,
            List<String> evidence,
            List<String> constraints,
            Map<String, Object> metadata,
            Instant analyzedAt
    ) {

        public GoalAnalysis {
            subtasks =
                    immutable(subtasks);

            dependencies =
                    immutable(dependencies);

            blockers =
                    immutable(blockers);

            conflicts =
                    immutable(conflicts);

            requiredInformation =
                    immutable(requiredInformation);

            evolutionSignals =
                    immutable(evolutionSignals);

            recommendations =
                    immutable(recommendations);

            evidence =
                    immutable(evidence);

            constraints =
                    immutable(constraints);

            metadata =
                    metadata == null
                            ? Map.of()
                            : Collections.unmodifiableMap(
                            new LinkedHashMap<>(metadata)
                    );
        }

        private static List<String> immutable(
                List<String> values) {

            return values == null
                    ? List.of()
                    : List.copyOf(values);
        }
    }

    // ---------------------------------------------------------------------
    // Enums
    // ---------------------------------------------------------------------

    public enum GoalStatus {
        NOT_STARTED,
        IN_PROGRESS,
        AT_RISK,
        BLOCKED,
        COMPLETED
    }

    public enum Priority {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    public enum Feasibility {
        PLAUSIBLE,
        CONDITIONAL,
        UNCERTAIN,
        BLOCKED
    }

    public enum ConfidenceBand {
        MINIMAL,
        LOW,
        MEDIUM,
        HIGH
    }

    public enum GoalStability {
        STABLE,
        EVOLVING,
        UNSTABLE
    }
}