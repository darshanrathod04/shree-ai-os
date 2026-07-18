package platform.kernels.context.validation;

import platform.kernels.context.model.ConversationContext;
import platform.kernels.context.model.ContextId;
import platform.kernels.context.model.ContextState;
import platform.kernels.context.model.ContextType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <b>ConversationContextValidator</b>
 *
 * <p>A utility validator for ConversationContext domain models.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates active conversation state and structure.</li>
 *   <li>Validates participant information and conversation metadata.</li>
 *   <li>Validates snapshot consistency.</li>
 *   <li>Provides pure validation without side effects.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Static methods only - no instance state.</li>
 *   <li>Stateless and thread-safe.</li>
 *   <li>Pure validation - never mutates objects.</li>
 *   <li>No business logic, persistence, or side effects.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Context Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-CTX-103</p>
 *
 * @see ContextValidationResult
 * @see ConversationContext
 */
public final class ConversationContextValidator {

    /**
     * Private constructor to prevent instantiation.
     */
    private ConversationContextValidator() {
        // Utility class - prevent instantiation
    }

    /**
     * Validates a ConversationContext instance.
     *
     * <p>Validates all ConversationContext fields including active conversation state,
     * participant information, conversation metadata, and snapshot consistency.</p>
     *
     * @param context the ConversationContext to validate (must not be null)
     * @return the validation result
     * @throws NullPointerException if context is null
     */
    public static ContextValidationResult validate(ConversationContext context) {
        List<String> violations = new ArrayList<>();

        // Validate base Context fields directly
        if (context.id() == null) {
            violations.add("Context id must not be null");
        } else {
            ContextValidationResult idResult = ContextValidator.validateContextId(context.id());
            if (!idResult.isValid()) {
                violations.addAll(idResult.getViolations());
            }
        }

        if (context.type() == null) {
            violations.add("Context type must not be null");
        } else {
            ContextValidationResult typeResult = ContextValidator.validateContextType(context.type());
            if (!typeResult.isValid()) {
                violations.addAll(typeResult.getViolations());
            }
        }

        if (context.state() == null) {
            violations.add("Context state must not be null");
        } else {
            ContextValidationResult stateResult = ContextValidator.validateContextState(context.state());
            if (!stateResult.isValid()) {
                violations.addAll(stateResult.getViolations());
            }
        }

        if (context.createdAt() == null) {
            violations.add("Context createdAt must not be null");
        }
        if (context.updatedAt() == null) {
            violations.add("Context updatedAt must not be null");
        } else if (context.createdAt() != null && context.updatedAt().isBefore(context.createdAt())) {
            violations.add("Context updatedAt must not be before createdAt");
        }
        if (context.data() == null) {
            violations.add("Context data must not be null");
        }

        // Validate type is CONVERSATION
        if (context.type() != ContextType.CONVERSATION) {
            violations.add("ConversationContext type must be CONVERSATION, got: " + context.type());
        }

        // Validate conversationId
        if (context.conversationId() == null || context.conversationId().isBlank()) {
            violations.add("ConversationContext conversationId must not be null or blank");
        }

        // Validate participantId
        if (context.participantId() == null || context.participantId().isBlank()) {
            violations.add("ConversationContext participantId must not be null or blank");
        }

        // Validate turnCount is not negative
        if (context.turnCount() < 0) {
            violations.add("ConversationContext turnCount must not be negative, got: " + context.turnCount());
        }

        // Validate active conversation state
        if (context.state() == ContextState.ACTIVE) {
            // Active conversations should have valid participants and conversation IDs
            if (context.participantId() == null || context.participantId().isBlank()) {
                violations.add("Active conversation must have a valid participantId");
            }
            if (context.conversationId() == null || context.conversationId().isBlank()) {
                violations.add("Active conversation must have a valid conversationId");
            }
        }

        // Validate snapshot consistency
        if (context.createdAt() != null && context.updatedAt() != null) {
            if (context.updatedAt().isBefore(context.createdAt())) {
                violations.add("ConversationContext updatedAt must not be before createdAt");
            }
        }

        Map<String, Object> metadata = Map.of(
                "contextType", context.type() != null ? context.type().name() : "null",
                "conversationId", context.conversationId() != null ? context.conversationId() : "null",
                "participantId", context.participantId() != null ? context.participantId() : "null",
                "turnCount", context.turnCount()
        );

        return new ContextValidationResult(
                violations.isEmpty(),
                violations,
                Instant.now(),
                metadata
        );
    }

    /**
     * Validates participant information.
     *
     * <p>Validates that participant information is present and valid.</p>
     *
     * @param context the ConversationContext to validate (must not be null)
     * @return the validation result
     */
    public static ContextValidationResult validateParticipantInfo(ConversationContext context) {
        List<String> violations = new ArrayList<>();

        if (context == null) {
            violations.add("ConversationContext must not be null");
            return new ContextValidationResult(false, violations, Instant.now(), Map.of());
        }

        if (context.participantId() == null || context.participantId().isBlank()) {
            violations.add("Participant ID must not be null or blank");
        }

        return new ContextValidationResult(
                violations.isEmpty(),
                violations,
                Instant.now(),
                Map.of("participantId", context.participantId() != null ? context.participantId() : "null")
        );
    }

    /**
     * Validates conversation metadata.
     *
     * <p>Validates that conversation metadata is present and valid.</p>
     *
     * @param context the ConversationContext to validate (must not be null)
     * @return the validation result
     */
    public static ContextValidationResult validateConversationMetadata(ConversationContext context) {
        List<String> violations = new ArrayList<>();

        if (context == null) {
            violations.add("ConversationContext must not be null");
            return new ContextValidationResult(false, violations, Instant.now(), Map.of());
        }

        if (context.conversationId() == null || context.conversationId().isBlank()) {
            violations.add("Conversation ID must not be null or blank");
        }

        if (context.turnCount() < 0) {
            violations.add("Turn count must not be negative, got: " + context.turnCount());
        }

        return new ContextValidationResult(
                violations.isEmpty(),
                violations,
                Instant.now(),
                Map.of(
                        "conversationId", context.conversationId() != null ? context.conversationId() : "null",
                        "turnCount", context.turnCount()
                )
        );
    }

    /**
     * Validates snapshot consistency.
     *
     * <p>Validates that the context snapshot is internally consistent.</p>
     *
     * @param context the ConversationContext to validate (must not be null)
     * @return the validation result
     */
    public static ContextValidationResult validateSnapshotConsistency(ConversationContext context) {
        List<String> violations = new ArrayList<>();

        if (context == null) {
            violations.add("ConversationContext must not be null");
            return new ContextValidationResult(false, violations, Instant.now(), Map.of());
        }

        // Validate timestamp consistency
        if (context.createdAt() != null && context.updatedAt() != null) {
            if (context.updatedAt().isBefore(context.createdAt())) {
                violations.add("updatedAt must not be before createdAt");
            }
        }

        // Validate state consistency
        if (context.state() == null) {
            violations.add("State must not be null");
        }

        return new ContextValidationResult(
                violations.isEmpty(),
                violations,
                Instant.now(),
                Map.of(
                        "createdAt", context.createdAt() != null ? context.createdAt().toString() : "null",
                        "updatedAt", context.updatedAt() != null ? context.updatedAt().toString() : "null",
                        "state", context.state() != null ? context.state().name() : "null"
                )
        );
    }
}