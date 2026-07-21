package platform.kernels.multiagent.validation;

import java.util.ArrayList;
import java.util.List;

import platform.kernels.multiagent.model.AgentStatus;

/**
 * <b>LifecycleValidator</b>
 *
 * <p>Validates agent lifecycle requests and transitions.
 * Ensures structural validity before lifecycle operations enter the Service Layer.</p>
 *
 * <p><b>Ownership:</b> Multi-Agent Kernel — Validation Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> MAGENT-103, EIO-ARCH-001</p>
 *
 * <p>LifecycleValidator validates lifecycle state transitions only.
 * It does NOT start, stop, pause, or resume agents.</p>
 *
 * @since 1.0
 */
public final class LifecycleValidator {
    /**
     * Creates a new LifecycleValidator.
     *
     * @since 1.0
     */
    public LifecycleValidator() {
        // Stateless validator — no dependencies required
    }

    /**
     * Validates a lifecycle transition request.
     *
     * @param agentId the agent identifier (must not be {@code null} or empty)
     * @param status  the target status (must not be {@code null})
     * @return the validation result
     * @throws NullPointerException if agentId or status is {@code null}
     * @since 1.0
     */
    public MultiAgentValidationResult validate(String agentId, AgentStatus status) {
        List<String> issues = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // Validate agentId
        if (agentId == null || agentId.isBlank()) {
            issues.add("Lifecycle agentId must not be null or blank");
        }

        // Validate status
        if (status == null) {
            issues.add("Lifecycle status must not be null");
        } else {
            validateStatus(status, issues, warnings);
        }

        boolean valid = issues.isEmpty();
        return new MultiAgentValidationResult(valid, issues, warnings);
    }

    /**
     * Validates an AgentStatus for consistency.
     *
     * @param status   the status to validate (must not be {@code null})
     * @param issues   the list to add issues to
     * @param warnings the list to add warnings to
     * @since 1.0
     */
    public void validateStatus(AgentStatus status, List<String> issues, List<String> warnings) {
        if (status == null) {
            issues.add("AgentStatus must not be null");
            return;
        }

        // Validate agentId in status
        if (status.agentId() == null || status.agentId().isBlank()) {
            issues.add("AgentStatus agentId must not be null or blank");
        }

        // Validate state
        if (status.state() == null || status.state().isBlank()) {
            issues.add("AgentStatus state must not be null or blank");
        } else {
            // Validate state value
            validateStateValue(status.state(), issues, warnings);
        }

        // Validate updatedAt timestamp
        if (status.updatedAt() == null) {
            issues.add("AgentStatus updatedAt must not be null");
        }

        // Validate metadata
        if (status.metadata() == null) {
            issues.add("AgentStatus metadata must not be null");
        } else {
            validateMetadata(status.metadata(), "AgentStatus", issues, warnings);
        }
    }

    /**
     * Validates a state value.
     *
     * @param state    the state to validate (must not be {@code null} or blank)
     * @param issues   the list to add issues to
     * @param warnings the list to add warnings to
     * @since 1.0
     */
    private void validateStateValue(String state, List<String> issues, List<String> warnings) {
        // Define valid lifecycle states
        String[] validStates = {
            "REGISTERED",
            "STARTING",
            "RUNNING",
            "PAUSED",
            "STOPPED",
            "UNREGISTERED"
        };

        boolean isValid = false;
        for (String validState : validStates) {
            if (validState.equals(state)) {
                isValid = true;
                break;
            }
        }

        if (!isValid) {
            warnings.add("AgentStatus state '" + state + "' is not a recognized lifecycle state. " +
                        "Valid states are: REGISTERED, STARTING, RUNNING, PAUSED, STOPPED, UNREGISTERED");
        }
    }

    /**
     * Validates metadata map structure.
     *
     * @param metadata  the metadata to validate (must not be {@code null})
     * @param context   the validation context for error messages
     * @param issues    the list to add issues to
     * @param warnings  the list to add warnings to
     * @since 1.0
     */
    private void validateMetadata(java.util.Map<String, Object> metadata, String context, List<String> issues, List<String> warnings) {
        // Check for null values in metadata
        for (java.util.Map.Entry<String, Object> entry : metadata.entrySet()) {
            if (entry.getKey() == null || entry.getKey().isBlank()) {
                issues.add(context + " metadata contains null or blank key");
            }
            if (entry.getValue() == null) {
                warnings.add(context + " metadata contains null value for key: " + entry.getKey());
            }
        }
    }
}