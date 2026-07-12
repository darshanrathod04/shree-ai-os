package platform.validation.rules;

import platform.capability.CapabilityRegistry;
import platform.cognition.Thought;
import platform.context.ConversationSession;
import platform.production.ResolvedContext;
import platform.validation.ValidationOutcome;
import platform.validation.ValidationRule;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Validates that decision exists and is well-formed.
 *
 * <p>Checks:
 * <ul>
 *   <li>Decision object is not null</li>
 *   <li>Decision action is not null or empty</li>
 *   <li>Decision intent is not null or empty</li>
 * </ul>
 * </p>
 *
 * @author Shree AI OS Team
 * @version 1.0
 * @since Sprint 5.1
 */
@Component
@Order(100)
public class DecisionExistsRule implements ValidationRule {

    @Override
    public ValidationOutcome validate(
            Thought decision,
            ConversationSession session,
            ResolvedContext resolvedContext,
            CapabilityRegistry capabilityRegistry
    ) {
        long startTime = System.nanoTime();
        try {
            if (decision == null) {
                return ValidationOutcome.failure(
                        java.util.List.of("Null Decision: Decision object is null"),
                        "Decision is null"
                );
            }

            if (decision.getAction() == null || decision.getAction().isEmpty()) {
                return ValidationOutcome.failure(
                        java.util.List.of("Invalid Decision: Action is null or empty"),
                        "Decision action is missing"
                );
            }

            if (decision.getIntent() == null || decision.getIntent().isEmpty()) {
                return ValidationOutcome.failure(
                        java.util.List.of("Invalid Decision: Intent is null or empty"),
                        "Decision intent is missing"
                );
            }

            return ValidationOutcome.success();
        } finally {
            long duration = System.nanoTime() - startTime;
            if (duration > 100_000) { // > 100µs
                // Log slow rule execution if needed
            }
        }
    }

    @Override
    public String getRuleName() {
        return "DecisionExistsRule";
    }
}