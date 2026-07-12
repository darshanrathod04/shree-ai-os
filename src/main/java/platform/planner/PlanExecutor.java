package platform.planner;

import platform.context.ConversationContext;
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

