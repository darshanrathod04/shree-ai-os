package com.shreeai.os.platform.legacy.cognition;

import com.shreeai.os.platform.legacy.personality.AgentPersonality;
import org.springframework.stereotype.Component;

@Component
public class ResponseComposer {

    private final AgentPersonality personality;

    public ResponseComposer(AgentPersonality personality) {
        this.personality = personality;
    }

    public String compose(String rawResponse) {

        // apply personality tone
        return personality.prefix()
                + rawResponse.trim()
                + personality.suffix();
    }
}
