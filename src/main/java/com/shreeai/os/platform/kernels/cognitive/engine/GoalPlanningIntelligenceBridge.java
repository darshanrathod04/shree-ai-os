package com.shreeai.os.platform.kernels.cognitive.engine;

import com.shreeai.os.platform.kernels.planning.engine.PlanningIntelligenceEngine;
import com.shreeai.os.platform.kernels.planning.model.PlanningId;
import com.shreeai.os.platform.kernels.planning.model.PlanningObjective;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Bridges Goal Intelligence output into the Planning Kernel.
 *
 * <p>This class does not perform planning itself. It converts the
 * structured output of Goal Intelligence into the immutable
 * {@link PlanningObjective} contract consumed by the Planning Kernel.</p>
 *
 * <p>The bridge preserves goal intelligence as explicit metadata so that
 * downstream planning remains evidence-aware and inspectable.</p>
 *
 * <p>Architectural flow:</p>
 *
 * <pre>
 * Goal Intelligence
 *        |
 *        v
 * GoalAnalysis
 *        |
 *        v
 * GoalPlanningIntelligenceBridge
 *        |
 *        v
 * PlanningObjective
 *        |
 *        v
 * PlanningIntelligenceEngine
 * </pre>
 *
 * <p>No execution, persistence, provider calls, or mutation are performed.</p>
 */
public final class GoalPlanningIntelligenceBridge {

    private static final String BRIDGE_VERSION = "1.0";

    private final PlanningIntelligenceEngine planningEngine;

    /**
     * Creates a bridge using the supplied Planning Intelligence engine.
     *
     * @param planningEngine planning intelligence engine
     */
    public GoalPlanningIntelligenceBridge(
            PlanningIntelligenceEngine planningEngine) {

        this.planningEngine = Objects.requireNonNull(
                planningEngine,
                "planningEngine must not be null"
        );
    }

    /**
     * Creates a bridge with the default deterministic Planning Intelligence engine.
     */
    public GoalPlanningIntelligenceBridge() {
        this(new PlanningIntelligenceEngine());
    }

    /**
     * Converts Goal Intelligence metadata into a PlanningObjective.
     *
     * <p>The supplied metadata is copied defensively and enriched with
     * explicit goal-intelligence provenance.</p>
     *
     * @param goalId unique goal identifier
     * @param goalDescription goal description
     * @param planningScope planning scope
     * @param goalIntelligence goal intelligence metadata
     * @return immutable planning objective
     */
    public PlanningObjective toPlanningObjective(
            String goalId,
            String goalDescription,
            String planningScope,
            Map<String, ?> goalIntelligence) {

        Objects.requireNonNull(
                goalId,
                "goalId must not be null"
        );

        Objects.requireNonNull(
                goalDescription,
                "goalDescription must not be null"
        );

        Objects.requireNonNull(
                planningScope,
                "planningScope must not be null"
        );

        Objects.requireNonNull(
                goalIntelligence,
                "goalIntelligence must not be null"
        );

        if (goalId.isBlank()) {
            throw new IllegalArgumentException(
                    "goalId must not be blank"
            );
        }

        if (goalDescription.isBlank()) {
            throw new IllegalArgumentException(
                    "goalDescription must not be blank"
            );
        }

        if (planningScope.isBlank()) {
            throw new IllegalArgumentException(
                    "planningScope must not be blank"
            );
        }

        Map<String, String> metadata =
                new LinkedHashMap<>();

        /*
         * Preserve the complete Goal Intelligence output using
         * deterministic string representation because PlanningObjective
         * intentionally exposes Map<String, String>.
         */
        for (Map.Entry<String, ?> entry :
                goalIntelligence.entrySet()) {

            if (entry.getKey() == null
                    || entry.getKey().isBlank()) {
                continue;
            }

            Object value = entry.getValue();

            if (value != null) {
                metadata.put(
                        entry.getKey(),
                        String.valueOf(value)
                );
            }
        }

        /*
         * Explicit provenance.
         */
        metadata.put(
                "goalIntelligenceSource",
                "GoalIntelligenceEngine"
        );

        metadata.put(
                "goalIntelligenceBridgeVersion",
                BRIDGE_VERSION
        );

        metadata.put(
                "goalId",
                goalId
        );

        metadata.put(
                "goalDescription",
                goalDescription
        );

        metadata.put(
                "planningScope",
                planningScope
        );

        /*
         * PlanningIntelligenceEngine already understands these metadata
         * fields and can use them as evidence when deriving tasks.
         */
        metadata.putIfAbsent(
                "requestText",
                goalDescription
        );

        metadata.putIfAbsent(
                "reasoningConclusion",
                goalDescription
        );

        return new PlanningObjective(
                new PlanningId(goalId),
                goalDescription,
                planningScope,
                metadata
        );
    }

    /**
     * Converts Goal Intelligence output and immediately produces a
     * deterministic planning analysis.
     *
     * @param goalId unique goal identifier
     * @param goalDescription goal description
     * @param planningScope planning scope
     * @param goalIntelligence goal intelligence metadata
     * @return planning analysis
     */
    public PlanningIntelligenceEngine.PlanningAnalysis analyzeGoal(
            String goalId,
            String goalDescription,
            String planningScope,
            Map<String, ?> goalIntelligence) {

        PlanningObjective objective =
                toPlanningObjective(
                        goalId,
                        goalDescription,
                        planningScope,
                        goalIntelligence
                );

        return planningEngine.analyze(
                objective
        );
    }

    /**
     * Returns the Planning Intelligence engine used by this bridge.
     *
     * @return planning intelligence engine
     */
    public PlanningIntelligenceEngine planningEngine() {
        return planningEngine;
    }
}