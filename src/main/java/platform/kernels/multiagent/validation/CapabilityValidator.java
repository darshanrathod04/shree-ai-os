package platform.kernels.multiagent.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import platform.kernels.multiagent.model.AgentCapability;
import platform.kernels.multiagent.model.AgentRequest;

/**
 * <b>CapabilityValidator</b>
 *
 * <p>Validates capability definitions and metadata.
 * Ensures structural validity before capabilities enter the Service Layer.</p>
 *
 * <p><b>Ownership:</b> Multi-Agent Kernel — Validation Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> MAGENT-103, EIO-ARCH-001</p>
 *
 * <p>CapabilityValidator validates capability metadata only.
 * It does NOT register capabilities or modify capabilities.</p>
 *
 * @since 1.0
 */
public final class CapabilityValidator {
    /**
     * Creates a new CapabilityValidator.
     *
     * @since 1.0
     */
    public CapabilityValidator() {
        // Stateless validator — no dependencies required
    }

    /**
     * Validates a request containing capabilities.
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
        } else if (request.capabilities().isEmpty()) {
            warnings.add("AgentRequest has no capabilities defined");
        } else {
            // Validate individual capabilities and check for uniqueness
            validateCapabilities(request.capabilities(), issues, warnings);
        }

        // Validate metadata
        if (request.metadata() == null) {
            issues.add("AgentRequest metadata must not be null");
        } else {
            validateMetadata(request.metadata(), "AgentRequest", issues, warnings);
        }

        boolean valid = issues.isEmpty();
        return new MultiAgentValidationResult(valid, issues, warnings);
    }

    /**
     * Validates a single capability definition.
     *
     * @param capability the capability to validate (must not be {@code null})
     * @return the validation result
     * @throws NullPointerException if capability is {@code null}
     * @since 1.0
     */
    public MultiAgentValidationResult validateCapability(AgentCapability capability) {
        List<String> issues = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        if (capability == null) {
            return MultiAgentValidationResult.failure("Capability must not be null");
        }

        // Validate name
        if (capability.name() == null || capability.name().isBlank()) {
            issues.add("AgentCapability name must not be null or blank");
        }

        // Validate version
        if (capability.version() == null || capability.version().isBlank()) {
            issues.add("AgentCapability version must not be null or blank");
        }

        // Validate metadata
        if (capability.metadata() == null) {
            issues.add("AgentCapability metadata must not be null");
        } else {
            validateMetadata(capability.metadata(), "AgentCapability", issues, warnings);
        }

        boolean valid = issues.isEmpty();
        return new MultiAgentValidationResult(valid, issues, warnings);
    }

    /**
     * Validates a list of capabilities for uniqueness and structure.
     *
     * @param capabilities the list of capabilities (must not be {@code null})
     * @param issues       the list to add issues to
     * @param warnings     the list to add warnings to
     * @since 1.0
     */
    private void validateCapabilities(List<AgentCapability> capabilities, List<String> issues, List<String> warnings) {
        // Check for duplicate capabilities
        for (int i = 0; i < capabilities.size(); i++) {
            AgentCapability current = capabilities.get(i);
            
            if (current == null) {
                issues.add("AgentRequest capability at index " + i + " is null");
                continue;
            }

            // Validate individual capability
            MultiAgentValidationResult result = validateCapability(current);
            issues.addAll(result.issues());
            warnings.addAll(result.warnings());

            // Check for duplicates
            for (int j = i + 1; j < capabilities.size(); j++) {
                AgentCapability other = capabilities.get(j);
                if (other != null && current.name().equals(other.name()) && current.version().equals(other.version())) {
                    warnings.add("AgentRequest contains duplicate capability: " + current.name() + ":" + current.version());
                }
            }
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