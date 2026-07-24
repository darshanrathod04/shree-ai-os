package platform.kernels.multiagent.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import platform.kernels.multiagent.model.AgentRequest;

/**
 * <b>MultiAgentCriteriaValidator</b>
 *
 * <p>Validates shared criteria used across registration, discovery, lifecycle, and communication.
 * Ensures structural validity before criteria enter the Service Layer.</p>
 *
 * <p><b>Ownership:</b> Multi-Agent Kernel — Validation Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> MAGENT-103, EIO-ARCH-001</p>
 *
 * <p>MultiAgentCriteriaValidator validates shared criteria only.
 * It contains NO business logic.</p>
 *
 * @since 1.0
 */
public final class MultiAgentCriteriaValidator {
    /**
     * Creates a new MultiAgentCriteriaValidator.
     *
     * @since 1.0
     */
    public MultiAgentCriteriaValidator() {
        // Stateless validator — no dependencies required
    }

    /**
     * Validates a request containing criteria.
     *
     * @param request the request to validate (must not be {@code null})
     * @return the validation result
     * @throws NullPointerException if request is {@code null}
     * @since 1.0
     */
    public MultiAgentValidationResult validate(AgentRequest request) {
        List<String> issues = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // Validate agentId
        if (request.agentId() == null || request.agentId().isBlank()) {
            issues.add("AgentRequest agentId must not be null or blank");
        }

        // Validate agentType
        if (request.agentType() == null || request.agentType().isBlank()) {
            issues.add("AgentRequest agentType must not be null or blank");
        }

        // Validate capabilities
        if (request.capabilities() == null) {
            issues.add("AgentRequest capabilities must not be null");
        } else {
            validateCapabilities(request.capabilities(), issues, warnings);
        }

        // Validate metadata
        if (request.metadata() == null) {
            issues.add("AgentRequest metadata must not be null");
        } else {
            validateMetadata(request.metadata(), "AgentRequest", issues, warnings);
            validateCriteriaFields(request.metadata(), issues, warnings);
        }

        boolean valid = issues.isEmpty();
        return new MultiAgentValidationResult(valid, issues, warnings);
    }

    /**
     * Validates a list of capabilities for structure.
     *
     * @param capabilities the list of capabilities (must not be {@code null})
     * @param issues       the list to add issues to
     * @param warnings     the list to add warnings to
     * @since 1.0
     */
    private void validateCapabilities(List<platform.kernels.multiagent.model.AgentCapability> capabilities, List<String> issues, List<String> warnings) {
        for (int i = 0; i < capabilities.size(); i++) {
            platform.kernels.multiagent.model.AgentCapability capability = capabilities.get(i);
            
            if (capability == null) {
                issues.add("AgentRequest capability at index " + i + " is null");
                continue;
            }

            // Validate capability structure
            if (capability.name() == null || capability.name().isBlank()) {
                issues.add("AgentRequest capability[" + i + "] name must not be null or blank");
            }

            if (capability.version() == null || capability.version().isBlank()) {
                issues.add("AgentRequest capability[" + i + "] version must not be null or blank");
            }

            if (capability.metadata() == null) {
                issues.add("AgentRequest capability[" + i + "] metadata must not be null");
            }
        }
    }

    /**
     * Validates criteria-specific fields in metadata.
     *
     * @param metadata  the metadata to validate (must not be {@code null})
     * @param issues    the list to add issues to
     * @param warnings  the list to add warnings to
     * @since 1.0
     */
    private void validateCriteriaFields(Map<String, Object> metadata, List<String> issues, List<String> warnings) {
        // Validate priority field if present
        if (metadata.containsKey("priority")) {
            Object priority = metadata.get("priority");
            if (priority == null) {
                warnings.add("AgentRequest priority is null");
            } else if (priority.toString().isBlank()) {
                issues.add("AgentRequest priority must not be blank if specified");
            }
        }

        // Validate status field if present
        if (metadata.containsKey("status")) {
            Object status = metadata.get("status");
            if (status == null) {
                warnings.add("AgentRequest status is null");
            } else if (status.toString().isBlank()) {
                issues.add("AgentRequest status must not be blank if specified");
            } else {
                validateStatusValue(status.toString(), issues, warnings);
            }
        }

        // Validate tags field if present
        if (metadata.containsKey("tags")) {
            Object tags = metadata.get("tags");
            if (tags != null && !(tags instanceof List)) {
                issues.add("AgentRequest tags must be a List if specified");
            }
        }

        // Validate time-based criteria
        if (metadata.containsKey("registeredAfter")) {
            Object registeredAfter = metadata.get("registeredAfter");
            if (registeredAfter != null && !(registeredAfter instanceof java.time.Instant)) {
                warnings.add("AgentRequest registeredAfter should be an Instant if specified");
            }
        }

        if (metadata.containsKey("registeredBefore")) {
            Object registeredBefore = metadata.get("registeredBefore");
            if (registeredBefore != null && !(registeredBefore instanceof java.time.Instant)) {
                warnings.add("AgentRequest registeredBefore should be an Instant if specified");
            }
        }
    }

    /**
     * Validates a status value.
     *
     * @param status    the status to validate (must not be {@code null} or blank)
     * @param issues    the list to add issues to
     * @param warnings  the list to add warnings to
     * @since 1.0
     */
    private void validateStatusValue(String status, List<String> issues, List<String> warnings) {
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
            if (validState.equals(status)) {
                isValid = true;
                break;
            }
        }

        if (!isValid) {
            warnings.add("AgentRequest status '" + status + "' is not a recognized lifecycle state. " +
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
    private void validateMetadata(Map<String, Object> metadata, String context, List<String> issues, List<String> warnings) {
        // Check for null values in metadata
        for (Map.Entry<String, Object> entry : metadata.entrySet()) {
            if (entry.getKey() == null || entry.getKey().isBlank()) {
                issues.add(context + " metadata contains null or blank key");
            }
            if (entry.getValue() == null) {
                warnings.add(context + " metadata contains null value for key: " + entry.getKey());
            }
        }
    }
}