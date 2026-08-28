package com.shreeai.os.platform.legacy.agents;

import com.shreeai.os.platform.legacy.memory.AgentCommunicationMemory;

public abstract class BaseAgent {

    protected final AgentCommunicationMemory memory;

    protected BaseAgent(AgentCommunicationMemory memory) {
        this.memory = memory;
    }

    protected void say(String from,
                       String to,
                       String msg) {

        memory.send(from, to, msg);
    }
}
