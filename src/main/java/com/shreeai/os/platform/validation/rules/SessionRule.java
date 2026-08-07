package com.shreeai.os.platform.validation.rules;

import com.shreeai.os.platform.capability.CapabilityRegistry;
import com.shreeai.os.platform.cognition.Thought;
import com.shreeai.os.platform.context.ConversationSession;
import com.shreeai.os.platform.production.ResolvedContext;
import com.shreeai.os.platform.validation.ValidationOutcome;
import com.shreeai.os.platform.validation.ValidationRule;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates session consistency.
 *
 * <p>Checks:
 * <ul>
 *   <li>Session ID is present and valid</li>
 *   <li>Session is not expired</li>
 * </ul>
 * </p>
 *
 * @author Shree AI OS Team
 * @version 1.0
 * @since Sprint 5.1
 */
@Component
@Order(200)
public class SessionRule implements ValidationRule {

    @Override
    public ValidationOutcome validate(
            Thought decision,
            ConversationSession session,
            ResolvedContext resolvedContext,
            CapabilityRegistry capabilityRegistry
    ) {
        if (session == null) {
            return ValidationOutcome.successWithWarnings(
                    List.of("Session Null: Conversation session is null"),
                    "Session is null"
            );
        }

        List<String> warnings = new ArrayList<>();

        if (session.getSessionId() == null || session.getSessionId().isEmpty()) {
            warnings.add("Invalid Session: Session ID is null or empty");
        }

        if (session.isExpired()) {
            warnings.add("Session Expired: Session has expired");
        }

        if (warnings.isEmpty()) {
            return ValidationOutcome.success();
        } else {
            return ValidationOutcome.successWithWarnings(warnings, "Session validated with warnings");
        }
    }

    @Override
    public String getRuleName() {
        return "SessionRule";
    }
}