package com.shreeai.os.platform.agents;

import com.shreeai.os.platform.context.ConversationContext;
import com.shreeai.os.platform.memory.AgentCommunicationMemory;
import org.springframework.stereotype.Component;

@Component
public class PlannerAgent extends BaseAgent {

    public PlannerAgent(AgentCommunicationMemory memory) {
        super(memory);
    }

    public String act(String goal,
                      ConversationContext ctx) {

        String plan =
                "Plan created for: " + goal;

        say("PLANNER", "EXECUTOR", plan);

        return plan;
    }
}
