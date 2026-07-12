package platform.agents;

import platform.memory.AgentCommunicationMemory;

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
