package com.shreeai.os.platform.kernels.cognitive.engine;

import com.shreeai.os.platform.kernels.planning.engine.PlanningIntelligenceEngine;
import com.shreeai.os.platform.kernels.planning.model.PlanningObjective;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GoalPlanningIntelligenceBridgeTest {

    @Test
    void shouldConvertGoalIntelligenceIntoPlanningObjective() {

        GoalPlanningIntelligenceBridge bridge =
                new GoalPlanningIntelligenceBridge();

        PlanningObjective objective =
                bridge.toPlanningObjective(
                        "goal-build-platform",
                        "Build a reliable student management platform",
                        "COMPREHENSIVE",
                        Map.of(
                                "priority", "HIGH",
                                "feasibility", "HIGH",
                                "progress", "0.0",
                                "blockers", "NONE",
                                "confidence", "0.91"
                        )
                );

        assertNotNull(objective);

        assertEquals(
                "goal-build-platform",
                objective.planningId().value()
        );

        assertEquals(
                "Build a reliable student management platform",
                objective.description()
        );

        assertEquals(
                "COMPREHENSIVE",
                objective.scope()
        );

        assertEquals(
                "HIGH",
                objective.metadata().get("priority")
        );

        assertEquals(
                "GoalIntelligenceEngine",
                objective.metadata().get("goalIntelligenceSource")
        );

        assertEquals(
                "goal-build-platform",
                objective.metadata().get("goalId")
        );
    }

    @Test
    void shouldPreserveGoalIntelligenceDuringPlanning() {

        GoalPlanningIntelligenceBridge bridge =
                new GoalPlanningIntelligenceBridge();

        PlanningIntelligenceEngine.PlanningAnalysis analysis =
                bridge.analyzeGoal(
                        "goal-student-app",
                        "Build and test a student management application",
                        "COMPREHENSIVE",
                        Map.of(
                                "priority", "HIGH",
                                "feasibility", "HIGH",
                                "confidence", "0.90"
                        )
                );

        assertNotNull(analysis);
        assertNotNull(analysis.goal());
        assertNotNull(analysis.tasks());
        assertNotNull(analysis.schedule());
        assertNotNull(analysis.quality());

        assertFalse(
                analysis.tasks().isEmpty(),
                "Goal-aware planning should produce tasks"
        );

        assertEquals(
                "goal-student-app",
                analysis.goal()
                        .objective()
                        .planningId()
                        .value()
        );
    }

    @Test
    void shouldRejectNullGoalIntelligence() {

        GoalPlanningIntelligenceBridge bridge =
                new GoalPlanningIntelligenceBridge();

        assertThrows(
                NullPointerException.class,
                () -> bridge.toPlanningObjective(
                        "goal-1",
                        "Build application",
                        "STANDARD",
                        null
                )
        );
    }

    @Test
    void shouldRejectBlankGoalId() {

        GoalPlanningIntelligenceBridge bridge =
                new GoalPlanningIntelligenceBridge();

        assertThrows(
                IllegalArgumentException.class,
                () -> bridge.toPlanningObjective(
                        " ",
                        "Build application",
                        "STANDARD",
                        Map.of()
                )
        );
    }
}