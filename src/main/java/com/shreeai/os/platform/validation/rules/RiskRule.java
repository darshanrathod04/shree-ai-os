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
 * Validates risk level of the decision action.
 *
 * <p>Checks:
 * <ul>
 *   <li>Action is not null</li>
 *   <li>High-risk actions (EXECUTE, RUN, DEPLOY, DELETE) generate warnings</li>
 *   <li>Medium-risk actions (MODIFY, UPDATE, CHANGE) generate warnings</li>
 * </ul>
 * </p>
 *
 * @author Shree AI OS Team
 * @version 1.0
 * @since Sprint 5.1
 */
@Component
@Order(600)
public class RiskRule implements ValidationRule {

    @Override
    public ValidationOutcome validate(
            Thought decision,
            ConversationSession session,
            ResolvedContext resolvedContext,
            CapabilityRegistry capabilityRegistry
    ) {
        if (decision == null || decision.getAction() == null) {
            return ValidationOutcome.success(); // DecisionExistsRule handles this
        }

        String action = decision.getAction();
        List<String> warnings = new ArrayList<>();
        String upperAction = action.toUpperCase();

        if (upperAction.contains("EXECUTE") || upperAction.contains("RUN") ||
            upperAction.contains("DEPLOY") || upperAction.contains("DELETE")) {
            warnings.add("High Risk Action: Action '" + action + "' is high risk");
        } else if (upperAction.contains("MODIFY") || upperAction.contains("UPDATE") ||
                   upperAction.contains("CHANGE")) {
            warnings.add("Medium Risk Action: Action '" + action + "' is medium risk");
        }

        if (warnings.isEmpty()) {
            return ValidationOutcome.success();
        } else {
            return ValidationOutcome.successWithWarnings(warnings, "Risk assessment completed");
        }
    }

    @Override
    public String getRuleName() {
        return "RiskRule";
    }
}