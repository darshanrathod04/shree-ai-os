package platform.capability;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Immutable metadata for a Capability.
 * Describes what the capability does without any execution logic.
 */
public final class CapabilityMetadata {

    private final String name;
    private final String description;
    private final int priority;
    private final List<String> supportedIntents;
    private final String version;
    private final Capability.ExecutionType executionType;
    private final Capability.HealthStatus healthStatus;
    private final boolean enabled;

    public CapabilityMetadata(String name, String description, int priority,
                              List<String> supportedIntents, String version,
                              Capability.ExecutionType executionType,
                              Capability.HealthStatus healthStatus, boolean enabled) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.description = description != null ? description : "";
        this.priority = priority;
        this.supportedIntents = supportedIntents != null
                ? Collections.unmodifiableList(List.copyOf(supportedIntents))
                : List.of();
        this.version = version != null ? version : "1.0.0";
        this.executionType = executionType != null ? executionType : Capability.ExecutionType.LLM;
        this.healthStatus = healthStatus != null ? healthStatus : Capability.HealthStatus.UNKNOWN;
        this.enabled = enabled;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getPriority() { return priority; }
    public List<String> getSupportedIntents() { return supportedIntents; }
    public String getVersion() { return version; }
    public Capability.ExecutionType getExecutionType() { return executionType; }
    public Capability.HealthStatus getHealthStatus() { return healthStatus; }
    public boolean isEnabled() { return enabled; }

    @Override
    public String toString() {
        return "CapabilityMetadata{" +
                "name='" + name + '\'' +
                ", priority=" + priority +
                ", intents=" + supportedIntents.size() +
                ", version='" + version + '\'' +
                ", type=" + executionType +
                ", health=" + healthStatus +
                ", enabled=" + enabled +
                '}';
    }
}