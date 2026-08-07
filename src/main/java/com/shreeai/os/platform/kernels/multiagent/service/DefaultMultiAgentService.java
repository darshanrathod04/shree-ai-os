package com.shreeai.os.platform.kernels.multiagent.service;

import java.util.List;

import com.shreeai.os.platform.kernels.multiagent.api.MultiAgentService;
import com.shreeai.os.platform.kernels.multiagent.error.AgentDiscoveryException;
import com.shreeai.os.platform.kernels.multiagent.error.AgentRegistrationException;
import com.shreeai.os.platform.kernels.multiagent.error.CommunicationException;
import com.shreeai.os.platform.kernels.multiagent.error.MultiAgentError;
import com.shreeai.os.platform.kernels.multiagent.error.MultiAgentErrorCode;
import com.shreeai.os.platform.kernels.multiagent.error.MultiAgentException;
import com.shreeai.os.platform.kernels.multiagent.error.MultiAgentValidationException;
import com.shreeai.os.platform.kernels.multiagent.model.AgentCommunication;
import com.shreeai.os.platform.kernels.multiagent.model.AgentDescriptor;
import com.shreeai.os.platform.kernels.multiagent.model.AgentRegistration;
import com.shreeai.os.platform.kernels.multiagent.model.AgentRequest;
import com.shreeai.os.platform.kernels.multiagent.model.AgentResponse;
import com.shreeai.os.platform.kernels.multiagent.model.MultiAgentMetrics;
import com.shreeai.os.platform.kernels.multiagent.engine.MultiAgentProcessingEngine;
import com.shreeai.os.platform.kernels.multiagent.validation.MultiAgentValidationResult;
import com.shreeai.os.platform.kernels.multiagent.validation.MultiAgentValidator;

/**
 * <b>DefaultMultiAgentService</b>
 *
 * <p>Default implementation of the Multi-Agent Service façade.
 * Coordinates validation and processing for all Multi-Agent operations.</p>
 *
 * <p><b>Ownership:</b> Multi-Agent Kernel — Service Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> MAGENT-105, EIO-ARCH-001</p>
 *
 * <p>DefaultMultiAgentService is a thin orchestration façade that:
 * - Delegates validation to MultiAgentValidator
 * - Delegates processing to MultiAgentProcessingEngine
 * - Translates failures into canonical exceptions
 * - Remains stateless and thread-safe</p>
 *
 * @param validator       the validation façade (must not be {@code null})
 * @param processingEngine the processing engine (must not be {@code null})
 *
 * @since 1.0
 */
public final class DefaultMultiAgentService implements MultiAgentService {
    private final MultiAgentValidator validator;
    private final MultiAgentProcessingEngine processingEngine;

    /**
     * Creates a new DefaultMultiAgentService with the specified dependencies.
     *
     * @param validator        the validation façade (must not be {@code null})
     * @param processingEngine the processing engine (must not be {@code null})
     * @throws NullPointerException if any dependency is {@code null}
     * @since 1.0
     */
    public DefaultMultiAgentService(
            MultiAgentValidator validator,
            MultiAgentProcessingEngine processingEngine) {
        this.validator = validator;
        this.processingEngine = processingEngine;
    }

    /**
     * Registers a new agent with the kernel.
     *
     * <p>Processing flow: Request → Validation → Processing Engine → Response</p>
     *
     * @param request the agent registration request (must not be {@code null})
     * @return the agent registration response
     * @throws IllegalArgumentException if request is {@code null}
     * @throws AgentRegistrationException if registration fails
     * @since 1.0
     */
    @Override
    public AgentResponse registerAgent(AgentRequest request) {
        // Validate request
        MultiAgentValidationResult validationResult = validator.validateCriteria(request);
        if (!validationResult.valid()) {
            throw createValidationException(request.agentId(), validationResult);
        }

        try {
            // Delegate to processing engine
            return processingEngine.registerAgent(new AgentRegistration(
                request.agentId(),
                request.agentType(),
                request.capabilities(),
                java.time.Instant.now(),
                request.metadata()
            ));
        } catch (Exception e) {
            throw createRegistrationException(request.agentId(), "Registration failed", e);
        }
    }

    /**
     * Unregisters an agent from the kernel.
     *
     * <p>Processing flow: Request → Validation → Processing Engine → Response</p>
     *
     * @param agentId the agent identifier (must not be {@code null} or empty)
     * @return the unregistration response
     * @throws IllegalArgumentException if agentId is {@code null} or empty
     * @throws AgentRegistrationException if unregistration fails
     * @since 1.0
     */
    @Override
    public AgentResponse unregisterAgent(String agentId) {
        // Validate agentId
        if (agentId == null || agentId.isBlank()) {
            throw new IllegalArgumentException("AgentId must not be null or blank");
        }

        try {
            // Delegate to processing engine
            return processingEngine.unregisterAgent(agentId);
        } catch (Exception e) {
            throw createRegistrationException(agentId, "Unregistration failed", e);
        }
    }

    /**
     * Discovers agents based on criteria.
     *
     * <p>Processing flow: Request → Validation → Processing Engine → Response</p>
     *
     * @param criteria the discovery criteria (must not be {@code null})
     * @return list of matching agent descriptors
     * @throws IllegalArgumentException if criteria is {@code null}
     * @throws AgentDiscoveryException if discovery fails
     * @since 1.0
     */
    @Override
    public List<AgentDescriptor> discoverAgents(AgentRequest criteria) {
        // Validate criteria
        MultiAgentValidationResult validationResult = validator.validateCriteria(criteria);
        if (!validationResult.valid()) {
            throw createValidationException(criteria.agentId(), validationResult);
        }

        try {
            // Delegate to processing engine
            return processingEngine.discoverAgents(criteria);
        } catch (Exception e) {
            throw createDiscoveryException(criteria.agentId(), "Discovery failed", e);
        }
    }

    /**
     * Sends a communication message through the Chief Kernel.
     *
     * <p>Processing flow: Request → Validation → Processing Engine → Response</p>
     *
     * @param communication the communication message (must not be {@code null})
     * @return the communication response
     * @throws IllegalArgumentException if communication is {@code null}
     * @throws CommunicationException if communication fails
     * @since 1.0
     */
    @Override
    public AgentResponse communicate(AgentCommunication communication) {
        // Validate communication
        MultiAgentValidationResult validationResult = validator.validateCommunication(communication);
        if (!validationResult.valid()) {
            throw createValidationException(communication.senderId(), validationResult);
        }

        try {
            // Delegate to processing engine
            return processingEngine.communicate(communication);
        } catch (Exception e) {
            throw createCommunicationException(communication.senderId(), "Communication failed", e);
        }
    }

    /**
     * Retrieves the health status of the Multi-Agent Kernel.
     *
     * @return kernel health metrics
     * @since 1.0
     */
    @Override
    public MultiAgentMetrics getKernelHealth() {
        try {
            // Delegate to processing engine
            return processingEngine.getKernelHealth();
        } catch (Exception e) {
            throw createMultiAgentException("Failed to retrieve kernel health", e);
        }
    }

    /**
     * Creates a validation exception from validation result.
     *
     * @param agentId     the agent identifier (may be {@code null})
     * @param validationResult the validation result (must not be {@code null})
     * @return the validation exception
     * @throws NullPointerException if validationResult is {@code null}
     * @since 1.0
     */
    private MultiAgentValidationException createValidationException(String agentId, MultiAgentValidationResult validationResult) {
        String message = String.join("; ", validationResult.issues());
        java.util.Map<String, Object> details = java.util.Map.of(
            "validationIssues", validationResult.issues(),
            "validationWarnings", validationResult.warnings()
        );
        
        MultiAgentError error = new MultiAgentError(
            MultiAgentErrorCode.VALIDATION_ERROR,
            message,
            agentId,
            java.time.Instant.now(),
            details
        );
        
        return new MultiAgentValidationException(error);
    }

    /**
     * Creates a registration exception.
     *
     * @param agentId the agent identifier (may be {@code null})
     * @param message the error message (must not be {@code null} or blank)
     * @param cause   the cause (may be {@code null})
     * @return the registration exception
     * @since 1.0
     */
    private AgentRegistrationException createRegistrationException(String agentId, String message, Throwable cause) {
        java.util.Map<String, Object> details = java.util.Map.of(
            "cause", cause != null ? cause.getMessage() : "unknown"
        );
        
        MultiAgentError error = new MultiAgentError(
            MultiAgentErrorCode.REGISTRATION_ERROR,
            message,
            agentId,
            java.time.Instant.now(),
            details
        );
        
        return new AgentRegistrationException(error, cause);
    }

    /**
     * Creates a discovery exception.
     *
     * @param agentId the agent identifier (may be {@code null})
     * @param message the error message (must not be {@code null} or blank)
     * @param cause   the cause (may be {@code null})
     * @return the discovery exception
     * @since 1.0
     */
    private AgentDiscoveryException createDiscoveryException(String agentId, String message, Throwable cause) {
        java.util.Map<String, Object> details = java.util.Map.of(
            "cause", cause != null ? cause.getMessage() : "unknown"
        );
        
        MultiAgentError error = new MultiAgentError(
            MultiAgentErrorCode.DISCOVERY_ERROR,
            message,
            agentId,
            java.time.Instant.now(),
            details
        );
        
        return new AgentDiscoveryException(error, cause);
    }

    /**
     * Creates a communication exception.
     *
     * @param agentId the agent identifier (may be {@code null})
     * @param message the error message (must not be {@code null} or blank)
     * @param cause   the cause (may be {@code null})
     * @return the communication exception
     * @since 1.0
     */
    private CommunicationException createCommunicationException(String agentId, String message, Throwable cause) {
        java.util.Map<String, Object> details = java.util.Map.of(
            "cause", cause != null ? cause.getMessage() : "unknown"
        );
        
        MultiAgentError error = new MultiAgentError(
            MultiAgentErrorCode.COMMUNICATION_ERROR,
            message,
            agentId,
            java.time.Instant.now(),
            details
        );
        
        return new CommunicationException(error, cause);
    }

    /**
     * Creates a general Multi-Agent exception.
     *
     * @param message the error message (must not be {@code null} or blank)
     * @param cause   the cause (may be {@code null})
     * @return the Multi-Agent exception
     * @since 1.0
     */
    private MultiAgentException createMultiAgentException(String message, Throwable cause) {
        java.util.Map<String, Object> details = java.util.Map.of(
            "cause", cause != null ? cause.getMessage() : "unknown"
        );
        
        MultiAgentError error = new MultiAgentError(
            MultiAgentErrorCode.MULTI_AGENT_ERROR,
            message,
            null,
            java.time.Instant.now(),
            details
        );
        
        return new MultiAgentException(error, cause);
    }
}