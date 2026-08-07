package com.shreeai.os.platform.planner;

import com.shreeai.os.platform.context.ConversationContext;
import org.springframework.stereotype.Component;

@Component
public class PlannerBrain {

    public Plan createPlan(String intent, String input, ConversationContext context) {

        if ("PLAN_DAY".equals(intent)) {
            Plan plan = new Plan();

            // Explicitly create PlanStep objects
            plan.addStep(new PlanStep("Check calendar"));
            plan.addStep(new PlanStep("Create task list"));
            plan.addStep(new PlanStep("Prioritize tasks"));

            return plan;
        }

        return null;
    }
}