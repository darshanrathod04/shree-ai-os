package com.shreeai.os.platform.orchestrator;

import platform.agents.*;
import com.shreeai.os.platform.agents.ExecutorAgent;
import com.shreeai.os.platform.agents.PlannerAgent;
import com.shreeai.os.platform.agents.ReviewerAgent;
import com.shreeai.os.platform.context.ConversationContext;
import org.springframework.stereotype.Component;

@Component
public class AgentOrchestrator {

    private final PlannerAgent planner;
    private final ExecutorAgent executor;
    private final ReviewerAgent reviewer;

    public AgentOrchestrator(
            PlannerAgent planner,
            ExecutorAgent executor,
            ReviewerAgent reviewer) {

        this.planner = planner;
        this.executor = executor;
        this.reviewer = reviewer;
    }

    public String run(String goal,
                      ConversationContext context)
            throws Exception {

        planner.act(goal, context);
        executor.act(context);

        return reviewer.act(context);
    }

    public String runTask(String goal) throws Exception {
        return run(goal, new ConversationContext());
    }

}
