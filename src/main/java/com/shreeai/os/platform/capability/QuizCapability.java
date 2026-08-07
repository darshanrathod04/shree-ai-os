package com.shreeai.os.platform.capability;

import java.util.List;

/**
 * QuizCapability — describes the Quiz capability.
 * NO business logic. NO execution. Only metadata.
 */
public class QuizCapability implements Capability {

    @Override
    public String getName() {
        return "quiz";
    }

    @Override
    public String getDescription() {
        return "Quiz creation, execution, and evaluation";
    }

    @Override
    public int getPriority() {
        return 15; // Higher priority — quiz should override most contexts
    }

    @Override
    public List<String> getSupportedIntents() {
        return List.of(
                "START_QUIZ", "CONTINUE_QUIZ", "SUBMIT_ANSWER",
                "FINISH_QUIZ", "QUIZ_RESULT"
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
        return ExecutionType.DETERMINISTIC; // No LLM for quiz logic
    }
}