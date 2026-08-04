package com.shreeai.os.platform.brain.executive;

import com.shreeai.os.platform.cognition.MetaThought;
import com.shreeai.os.platform.context.ConversationContext;
import org.springframework.stereotype.Component;

@Component
public class ExecutiveControlEngine {

    public ExecutiveDecision decide(String input,
                                    ConversationContext context,
                                    MetaThought meta) {

        String text = input.toLowerCase();

        // teaching detection
        if (text.contains("learn")) {
            return new ExecutiveDecision(
                    ExecutiveDecision.Action.START_TEACHING,
                    "User wants learning mode");
        }

        // confusion detection
        if (meta != null && !meta.isSuccessful()) {
            return new ExecutiveDecision(
                    ExecutiveDecision.Action.ASK_CLARIFICATION,
                    "Previous response ineffective");
        }

        // short unclear input
        if (text.length() < 3) {
            return new ExecutiveDecision(
                    ExecutiveDecision.Action.ASK_CLARIFICATION,
                    "Input unclear");
        }

        return new ExecutiveDecision(
                ExecutiveDecision.Action.RESPOND,
                "Normal conversation");
    }
}