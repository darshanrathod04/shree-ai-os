package com.shreeai.os.platform.legacy.agents;

import com.shreeai.os.platform.legacy.context.ConversationContext;
import com.shreeai.os.platform.legacy.memory.AgentCommunicationMemory;
import org.springframework.stereotype.Component;

@Component
public final class ReviewerAgent extends BaseAgent {

    public ReviewerAgent(AgentCommunicationMemory memory) {
        super(memory);
    }

    public String act(ConversationContext ctx) {

        var inbox = memory.inbox("REVIEWER");

        if (inbox.isEmpty())
            return "Nothing to review.";

        String result =
                inbox.get(inbox.size()-1)
                        .getContent();

        return "Review OK ✅ : " + result;
    }
}
