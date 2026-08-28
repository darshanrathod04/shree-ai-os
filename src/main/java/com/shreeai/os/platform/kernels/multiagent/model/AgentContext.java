package com.shreeai.os.platform.kernels.multiagent.model;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Shared intelligence package passed to every agent.
 */
public final class AgentContext {

    private final String objective;
    private final List<String> memory;
    private final List<String> knowledge;
    private final Map<String, Object> metadata;

    public AgentContext(
            String objective,
            List<String> memory,
            List<String> knowledge,
            Map<String, Object> metadata
    ) {
        this.objective = Objects.requireNonNullElse(objective, "");
        this.memory = List.copyOf(memory == null ? List.of() : memory);
        this.knowledge = List.copyOf(knowledge == null ? List.of() : knowledge);
        this.metadata = Map.copyOf(metadata == null ? Map.of() : metadata);
    }

    public String objective() {
        return objective;
    }

    public List<String> memory() {
        return memory;
    }

    public List<String> knowledge() {
        return knowledge;
    }

    public Map<String, Object> metadata() {
        return metadata;
    }
}