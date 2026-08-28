package com.shreeai.os.platform.kernels.planning.response;

import com.shreeai.os.platform.kernels.cognitive.engine.GoalIntelligenceEngine;
import com.shreeai.os.platform.kernels.response.contracts.PlanningResponse;

import java.util.List;

public final class PlanningResponseBuilder {

    public PlanningResponse build(GoalIntelligenceEngine.GoalAnalysis analysis) {

        String goal = analysis.normalizedGoal();

        if (goal == null || goal.isBlank()) {
            goal = "Execution Plan";
        }

        return new PlanningResponse(
                goal,                                   // title
                goal,                                   // goal
                List.copyOf(analysis.subtasks()),
                List.copyOf(analysis.recommendations()),
                analysis.confidence()
        );
    }
}