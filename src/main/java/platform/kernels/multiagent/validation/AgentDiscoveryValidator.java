package platform.kernels.multiagent.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import platform.kernels.multiagent.model.AgentDescriptor;

/**
 * <b>AgentDiscoveryValidator</b>
 *
 * <p>Validates agent discovery criteria and metadata.
 * Ensures structural validity before discovery enters the Service Layer.</p>
 *
 * <p><b>Ownership:</b> Multi-Agent Kernel — Validation Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> MAGENT-103, EIO-ARCH-001</p>
 *
 * <p>AgentDiscoveryValidator validates discovery criteria only.
 * It does NOT discover agents, execute searches, or perform filtering.</p>
 *
 * @since 1.0
 */
public final class AgentDiscoveryValidator {
    /**
     * Creates a new AgentDiscoveryValidator.
     *
     * @since 1.0
     */
    public AgentDiscoveryValidator() {
        // Stateless validator — no dependencies required
    }

    /**
     * Validates an agent descriptor for discovery.
     *
     * @param descriptor the descriptor to validate (must not be {@code null})
     * @return the validation result
     * @throws NullPointerException if descriptor is {@code null}
     * @since 1.0
     */
    public MultiAgentValidationResult validate(AgentDescriptor descriptor) {
        List<String> issues = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // Validate agentId
        if (descriptor.agentId() == null || descriptor.agentId().isBlank()) {
            issues.add("AgentDescriptor agentId must not be null or blank");
        }

        // Validate agentType
        if (descriptor.agentType() == null || descriptor.agentType().isBlank()) {
            issues.add("AgentDescriptor agentType must not be null or blank");
        }

        // Validate capabilities
        if (descriptor.capabilities() == null) {
            issues.add("AgentDescriptor capabilities must not be null");
        } else if (descriptor.capabilities().isEmpty()) {
            warnings.add("AgentDescriptor has no capabilities defined");
        }

        // Validate priority
        if (descriptor.priority() == null || descriptor.priority().isBlank()) {
            issues.add("AgentDescriptor priority must not be null or blank");
        }

        // Validate tags
        if (descriptor.tags() == null) {
            issues.add("AgentDescriptor tags must not be null");
        }

        // Validate metadata
        if (descriptor.metadata() == null) {
            issues.add("AgentDescriptor metadata must not be null");
        } else {
            // Validate metadata structure
            validateMetadata(descriptor.metadata(), "AgentDescriptor", issues, warnings);
        }

        boolean valid = issues.isEmpty();
        return new MultiAgentValidationResult(valid, issues, warnings);
    }

    /**
     * Validates discovery criteria metadata.
     *
     * @param criteria  the criteria metadata (must not be {@code null})
     * @param context   the validation context for error messages
     * @param issues    the list to add issues to
     * @param warnings  the list to add warnings to
     * @since 1.0
     */
    public void validateCriteriaMetadata(Map<String, Object> criteria, String context, List<String> issues, List<String> warnings) {
        if (criteria == null) {
            issues.add(context + " criteria must not be null");
            return;
        }

        // Validate metadata structure
        validateMetadata(criteria, context, issues, warnings);

        // Check for required discovery fields
        if (criteria.containsKey("agentType") && (criteria.get("agentType") == null || criteria.get("agentType").toString().isBlank())) {
            issues.add(context + " agentType must not be blank if specified");
        }

        if (criteria.containsKey("status") && (criteria.get("status") == null || criteria.get("status").toString().isBlank())) {
            issues.add(context + " status must not be blank if specified");
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