package com.shreeai.os.platform.capability;

import java.util.List;

/**
 * RoadmapCapability — describes the Roadmap/Planning capability.
 * NO business logic. NO execution. Only metadata.
 */
public class RoadmapCapability implements Capability {

    @Override
    public String getName() {
        return "roadmap";
    }

    @Override
    public String getDescription() {
        return "Roadmap creation, planning, and task management";
    }

    @Override
    public int getPriority() {
        return 10; // High priority — roadmap intents should route here
    }

    @Override
    public List<String> getSupportedIntents() {
        return List.of(
                "PLAN", "ROADMAP_REQUEST", "NEXT_STEP", "COMPLETE_TASK",
                "PROGRESS", "CURRENT_TASK"
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
        return ExecutionType.DETERMINISTIC; // No LLM for roadmap logic
    }
}