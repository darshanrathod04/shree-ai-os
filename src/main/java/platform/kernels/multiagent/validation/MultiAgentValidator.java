package platform.kernels.multiagent.validation;

import java.util.ArrayList;
import java.util.List;

import platform.kernels.multiagent.model.AgentCommunication;
import platform.kernels.multiagent.model.AgentDescriptor;
import platform.kernels.multiagent.model.AgentRegistration;
import platform.kernels.multiagent.model.AgentRequest;
import platform.kernels.multiagent.model.AgentStatus;

/**
 * <b>MultiAgentValidator</b>
 *
 * <p>Validation façade for the Multi-Agent Kernel.
 * Delegates validation to specialized validators and aggregates results.</p>
 *
 * <p><b>Ownership:</b> Multi-Agent Kernel — Validation Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> MAGENT-103, EIO-ARCH-001</p>
 *
 * <p>MultiAgentValidator coordinates validation across all Multi-Agent components.
 * It does not perform validation logic directly — it delegates to specialized validators.</p>
 *
 * @since 1.0
 */
public final class MultiAgentValidator {
    private final AgentRegistrationValidator registrationValidator;
    private final AgentDiscoveryValidator discoveryValidator;
    private final CapabilityValidator capabilityValidator;
    private final LifecycleValidator lifecycleValidator;
    private final CommunicationValidator communicationValidator;
    private final MultiAgentCriteriaValidator criteriaValidator;

    /**
     * Creates a new MultiAgentValidator with all specialized validators.
     *
     * @param registrationValidator  the registration validator (must not be {@code null})
     * @param discoveryValidator     the discovery validator (must not be {@code null})
     * @param capabilityValidator    the capability validator (must not be {@code null})
     * @param lifecycleValidator     the lifecycle validator (must not be {@code null})
     * @param communicationValidator the communication validator (must not be {@code null})
     * @param criteriaValidator      the criteria validator (must not be {@code null})
     * @throws NullPointerException if any validator is {@code null}
     * @since 1.0
     */
    public MultiAgentValidator(
            AgentRegistrationValidator registrationValidator,
            AgentDiscoveryValidator discoveryValidator,
            CapabilityValidator capabilityValidator,
            LifecycleValidator lifecycleValidator,
            CommunicationValidator communicationValidator,
            MultiAgentCriteriaValidator criteriaValidator) {
        this.registrationValidator = registrationValidator;
        this.discoveryValidator = discoveryValidator;
        this.capabilityValidator = capabilityValidator;
        this.lifecycleValidator = lifecycleValidator;
        this.communicationValidator = communicationValidator;
        this.criteriaValidator = criteriaValidator;
    }

    /**
     * Validates an agent registration request.
     *
     * @param registration the registration to validate (must not be {@code null})
     * @return the validation result
     * @throws NullPointerException if registration is {@code null}
     * @since 1.0
     */
    public MultiAgentValidationResult validateRegistration(AgentRegistration registration) {
        return registrationValidator.validate(registration);
    }

    /**
     * Validates an agent discovery request.
     *
     * @param descriptor the descriptor to validate (must not be {@code null})
     * @return the validation result
     * @throws NullPointerException if descriptor is {@code null})
     * @since 1.0
     */
    public MultiAgentValidationResult validateDiscovery(AgentDescriptor descriptor) {
        return discoveryValidator.validate(descriptor);
    }

    /**
     * Validates a capability definition.
     *
     * @param request the request containing capabilities (must not be {@code null})
     * @return the validation result
     * @throws NullPointerException if request is {@code null})
     * @since 1.0
     */
    public MultiAgentValidationResult validateCapabilities(AgentRequest request) {
        return capabilityValidator.validate(request);
    }

    /**
     * Validates a lifecycle request.
     *
     * @param agentId the agent identifier (must not be {@code null} or empty)
     * @param status  the target status (must not be {@code null})
     * @return the validation result
     * @throws NullPointerException if agentId or status is {@code null})
     * @since 1.0
     */
    public MultiAgentValidationResult validateLifecycle(String agentId, AgentStatus status) {
        return lifecycleValidator.validate(agentId, status);
    }

    /**
     * Validates a communication request.
     *
     * @param communication the communication to validate (must not be {@code null})
     * @return the validation result
     * @throws NullPointerException if communication is {@code null})
     * @since 1.0
     */
    public MultiAgentValidationResult validateCommunication(AgentCommunication communication) {
        return communicationValidator.validate(communication);
    }

    /**
     * Validates shared criteria used across multiple operations.
     *
     * @param request the request containing criteria (must not be {@code null})
     * @return the validation result
     * @throws NullPointerException if request is {@code null})
     * @since 1.0
     */
    public MultiAgentValidationResult validateCriteria(AgentRequest request) {
        return criteriaValidator.validate(request);
    }

    /**
     * Performs comprehensive validation of all Multi-Agent components.
     * Aggregates results from all validators.
     *
     * @param registration   the registration to validate (may be {@code null})
     * @param descriptor     the descriptor to validate (may be {@code null})
     * @param request        the request to validate (may be {@code null})
     * @param communication  the communication to validate (may be {@code null})
     * @return the aggregated validation result
     * @since 1.0
     */
    public MultiAgentValidationResult validateAll(
            AgentRegistration registration,
            AgentDescriptor descriptor,
            AgentRequest request,
            AgentCommunication communication) {
        List<String> allIssues = new ArrayList<>();
        List<String> allWarnings = new ArrayList<>();

        if (registration != null) {
            MultiAgentValidationResult result = registrationValidator.validate(registration);
            allIssues.addAll(result.issues());
            allWarnings.addAll(result.warnings());
        }

        if (descriptor != null) {
            MultiAgentValidationResult result = discoveryValidator.validate(descriptor);
            allIssues.addAll(result.issues());
            allWarnings.addAll(result.warnings());
        }

        if (request != null) {
            MultiAgentValidationResult result = capabilityValidator.validate(request);
            allIssues.addAll(result.issues());
            allWarnings.addAll(result.warnings());

            result = criteriaValidator.validate(request);
            allIssues.addAll(result.issues());
            allWarnings.addAll(result.warnings());
        }

        if (communication != null) {
            MultiAgentValidationResult result = communicationValidator.validate(communication);
            allIssues.addAll(result.issues());
            allWarnings.addAll(result.warnings());
        }

        boolean valid = allIssues.isEmpty();
        return new MultiAgentValidationResult(valid, allIssues, allWarnings);
    }
}