package com.shreeai.os.platform.validation.rules;

import com.shreeai.os.platform.capability.CapabilityRegistry;
import com.shreeai.os.platform.cognition.Thought;
import com.shreeai.os.platform.context.ConversationSession;
import com.shreeai.os.platform.production.ResolvedContext;
import com.shreeai.os.platform.validation.ValidationOutcome;
import com.shreeai.os.platform.validation.ValidationRule;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Validates context availability.
 *
 * <p>Checks:
 * <ul>
 *   <li>Resolved context is available</li>
 * </ul>
 * </p>
 *
 * <p>Note: In shadow mode, context may be minimal. This rule ensures
 * context is present for future context-aware validation.</p>
 *
 * @author Shree AI OS Team
 * @version 1.0
 * @since Sprint 5.1
 */
@Component
@Order(300)
public class ContextRule implements ValidationRule {

    @Override
    public ValidationOutcome validate(
            Thought decision,
            ConversationSession session,
            ResolvedContext resolvedContext,
            CapabilityRegistry capabilityRegistry
    ) {
        if (resolvedContext == null) {
            return ValidationOutcome.successWithWarnings(
                    List.of("Unknown Context: Resolved context is null"),
                    "Context is null"
            );
        }

        // Context is available
        // Future: Add context validation rules (mode consistency, state validity, etc.)
        return ValidationOutcome.success();
    }

    @Override
    public String getRuleName() {
        return "ContextRule";
    }
}