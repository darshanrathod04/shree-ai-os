package com.darshan.agent.capability;

import java.util.List;

/**
 * LearningCapability — describes the Learning capability.
 * NO business logic. NO execution. Only metadata.
 */
public class LearningCapability implements Capability {

    @Override
    public String getName() {
        return "learning";
    }

    @Override
    public String getDescription() {
        return "Course learning, lesson navigation, and teaching";
    }

    @Override
    public int getPriority() {
        return 10; // High priority — learning intents should route here
    }

    @Override
    public List<String> getSupportedIntents() {
        return List.of(
                "START_COURSE", "CONTINUE_LESSON", "COMPLETE_LESSON",
                "CURRENT_LESSON", "TEACH_TOPIC", "REPEAT_LESSON",
                "EXIT_COURSE", "LESSON_PROGRESS", "LEARN"
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
        return ExecutionType.HYBRID; // LLM + deterministic
    }
}