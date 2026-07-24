package platform.kernels.multiagent.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import platform.kernels.multiagent.model.AgentCommunication;

/**
 * <b>CommunicationValidator</b>
 *
 * <p>Validates communication metadata and enforces Chief-mediated communication.
 * Ensures structural validity before communication enters the Service Layer.</p>
 *
 * <p><b>Ownership:</b> Multi-Agent Kernel — Validation Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> MAGENT-103, EIO-ARCH-001</p>
 *
 * <p>CommunicationValidator validates communication metadata only.
 * It does NOT perform routing, transport, or networking.</p>
 *
 * <p><b>Architectural Invariant:</b></p>
 * <pre>
 * Agent A
 *     │
 *     ▼
 * Chief Kernel
 *     │
 *     ▼
 * Agent B
 * </pre>
 *
 * <p>Direct agent-to-agent communication is architecturally invalid and must be rejected.</p>
 *
 * @since 1.0
 */
public final class CommunicationValidator {
    /**
     * Creates a new CommunicationValidator.
     *
     * @since 1.0
     */
    public CommunicationValidator() {
        // Stateless validator — no dependencies required
    }

    /**
     * Validates a communication request.
     *
     * @param communication the communication to validate (must not be {@code null})
     * @return the validation result
     * @throws NullPointerException if communication is {@code null}
     * @since 1.0
     */
    public MultiAgentValidationResult validate(AgentCommunication communication) {
        List<String> issues = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        if (communication == null) {
            return MultiAgentValidationResult.failure("AgentCommunication must not be null");
        }

        // Validate correlationId
        if (communication.correlationId() == null || communication.correlationId().isBlank()) {
            issues.add("AgentCommunication correlationId must not be null or blank");
        }

        // Validate senderId
        if (communication.senderId() == null || communication.senderId().isBlank()) {
            issues.add("AgentCommunication senderId must not be null or blank");
        }

        // Validate receiverId
        if (communication.receiverId() == null || communication.receiverId().isBlank()) {
            issues.add("AgentCommunication receiverId must not be null or blank");
        }

        // Validate timestamp
        if (communication.timestamp() == null) {
            issues.add("AgentCommunication timestamp must not be null");
        }

        // Validate metadata
        if (communication.metadata() == null) {
            issues.add("AgentCommunication metadata must not be null");
        } else {
            validateMetadata(communication.metadata(), "AgentCommunication", issues, warnings);
            
            // Enforce Chief-mediated communication invariant
            validateChiefMediation(communication, issues, warnings);
        }

        boolean valid = issues.isEmpty();
        return new MultiAgentValidationResult(valid, issues, warnings);
    }

    /**
     * Validates that communication is Chief-mediated.
     * Rejects direct agent-to-agent communication structures.
     *
     * @param communication the communication to validate
     * @param issues        the list to add issues to
     * @param warnings      the list to add warnings to
     * @since 1.0
     */
    private void validateChiefMediation(AgentCommunication communication, List<String> issues, List<String> warnings) {
        Map<String, Object> metadata = communication.metadata();
        
        // Check for direct communication indicators
        // Direct communication would bypass the Chief Kernel
        if (metadata.containsKey("direct")) {
            Object directValue = metadata.get("direct");
            if (directValue instanceof Boolean && (Boolean) directValue) {
                issues.add("AgentCommunication must be Chief-mediated. Direct agent-to-agent communication is forbidden.");
            }
        }

        // Check for routing metadata that indicates direct communication
        if (metadata.containsKey("routing") && !metadata.containsKey("chiefId")) {
            warnings.add("AgentCommunication has routing metadata but no chiefId. All communication must flow through Chief Kernel.");
        }

        // Validate that metadata contains Chief routing information
        if (!metadata.containsKey("chiefId") && !metadata.containsKey("chiefKernelId")) {
            warnings.add("AgentCommunication metadata should contain chiefId or chiefKernelId to ensure Chief-mediated routing.");
        }

        // Check for transport indicators (forbidden in domain model)
        if (metadata.containsKey("transport") || metadata.containsKey("protocol")) {
            warnings.add("AgentCommunication metadata contains transport/protocol information. " +
                        "Transport logic belongs to future layers, not the domain model.");
        }

        // Check for networking indicators (forbidden in domain model)
        if (metadata.containsKey("endpoint") || metadata.containsKey("address")) {
            warnings.add("AgentCommunication metadata contains endpoint/address information. " +
                        "Networking logic belongs to future layers, not the domain model.");
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