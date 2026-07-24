package platform.kernels.multiagent.validation;

import java.util.ArrayList;
import java.util.List;

import platform.kernels.multiagent.model.AgentCapability;
import platform.kernels.multiagent.model.AgentRegistration;

/**
 * <b>AgentRegistrationValidator</b>
 *
 * <p>Validates agent registration metadata.
 * Ensures structural validity before registration enters the Service Layer.</p>
 *
 * <p><b>Ownership:</b> Multi-Agent Kernel — Validation Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> MAGENT-103, EIO-ARCH-001</p>
 *
 * <p>AgentRegistrationValidator validates registration metadata only.
 * It does NOT register agents or update the registry.</p>
 *
 * @since 1.0
 */
public final class AgentRegistrationValidator {
    /**
     * Creates a new AgentRegistrationValidator.
     *
     * @since 1.0
     */
    public AgentRegistrationValidator() {
        // Stateless validator — no dependencies required
    }

    /**
     * Validates an agent registration.
     *
     * @param registration the registration to validate (must not be {@code null})
     * @return the validation result
     * @throws NullPointerException if registration is {@code null}
     * @since 1.0
     */
    public MultiAgentValidationResult validate(AgentRegistration registration) {
        List<String> issues = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // Validate agentId
        if (registration.agentId() == null || registration.agentId().isBlank()) {
            issues.add("AgentRegistration agentId must not be null or blank");
        }

        // Validate agentType
        if (registration.agentType() == null || registration.agentType().isBlank()) {
            issues.add("AgentRegistration agentType must not be null or blank");
        }

        // Validate capabilities
        if (registration.capabilities() == null) {
            issues.add("AgentRegistration capabilities must not be null");
        } else if (registration.capabilities().isEmpty()) {
            warnings.add("AgentRegistration has no capabilities defined");
        } else {
            // Validate individual capabilities
            for (int i = 0; i < registration.capabilities().size(); i++) {
                AgentCapability capability = registration.capabilities().get(i);
                if (capability == null) {
                    issues.add("AgentRegistration capability at index " + i + " is null");
                } else {
                    MultiAgentValidationResult capabilityResult = validateCapability(capability, i);
                    issues.addAll(capabilityResult.issues());
                    warnings.addAll(capabilityResult.warnings());
                }
            }
        }

        // Validate registeredAt timestamp
        if (registration.registeredAt() == null) {
            issues.add("AgentRegistration registeredAt must not be null");
        }

        // Validate metadata
        if (registration.metadata() == null) {
            issues.add("AgentRegistration metadata must not be null");
        }

        boolean valid = issues.isEmpty();
        return new MultiAgentValidationResult(valid, issues, warnings);
    }

    /**
     * Validates a single capability within a registration.
     *
     * @param capability the capability to validate (must not be {@code null})
     * @param index      the capability index
     * @return the validation result
     * @since 1.0
     */
    private MultiAgentValidationResult validateCapability(AgentCapability capability, int index) {
        List<String> issues = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        if (capability.name() == null || capability.name().isBlank()) {
            issues.add("AgentRegistration capability[" + index + "] name must not be null or blank");
        }

        if (capability.version() == null || capability.version().isBlank()) {
            issues.add("AgentRegistration capability[" + index + "] version must not be null or blank");
        }

        if (capability.metadata() == null) {
            issues.add("AgentRegistration capability[" + index + "] metadata must not be null");
        }

        boolean valid = issues.isEmpty();
        return new MultiAgentValidationResult(valid, issues, warnings);
    }
}