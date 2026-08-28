package com.shreeai.os.platform.legacy.capability;

import java.util.List;

/**
 * ChatCapability — describes the Chat capability.
 * NO business logic. NO execution. Only metadata.
 */
public class ChatCapability implements Capability {

    @Override
    public String getName() {
        return "chat";
    }

    @Override
    public String getDescription() {
        return "General conversation, small talk, and chat responses";
    }

    @Override
    public int getPriority() {
        return 0; // Lowest priority — fallback capability
    }

    @Override
    public List<String> getSupportedIntents() {
        return List.of(
                "DEFAULT", "FOLLOW_UP", "SMALL_TALK", "CHAT",
                "GREETING", "WHO_AM_I", "TIME", "WEATHER", "REMINDER",
                "MEMORY_RECALL", "SYSTEM", "DEBUG_CODE", "CODE_GENERATION",
                "PROGRAMMING_QUERY", "COMPARISON", "CAREER_ADVICE"
        );
    }

    @Override
    public HealthStatus getHealthStatus() {
        return HealthStatus.HEALTHY;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public ExecutionType getExecutionType() {
        return ExecutionType.LLM;
    }
}