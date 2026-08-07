package com.shreeai.os.platform.planner;

import com.shreeai.os.platform.context.ConversationContext;
import org.springframework.stereotype.Component;

@Component
public class PlanExecutor {

    public String execute(Plan plan, ConversationContext context) {

        StringBuilder result = new StringBuilder();

        for (PlanStep step : plan.getSteps()) {

            result.append("✅ ")
                    .append(step.getAction())
                    .append("\n");
        }

        return result.toString();
    }
}

