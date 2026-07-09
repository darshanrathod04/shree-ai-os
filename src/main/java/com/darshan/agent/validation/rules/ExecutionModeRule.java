package com.darshan.agent.validation.rules;

import com.darshan.agent.capability.Capability;
import com.darshan.agent.capability.CapabilityRegistry;
import com.darshan.agent.cognition.Thought;
import com.darshan.agent.context.ConversationSession;
import com.darshan.agent.production.ResolvedContext;
import com.darshan.agent.validation.ValidationOutcome;
import com.darshan.agent.validation.ValidationRule;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates execution mode of the capability.
 *
 * <p>Checks:
 * <ul>
 *   <li>Capability execution type is specified (if capability exists)</li>
 * </ul>
 * </p>
 *
 * @author Shree AI OS Team
 * @version 1.0
 * @since Sprint 5.1
 */
@Component
@Order(700)
public class ExecutionModeRule implements ValidationRule {

    @Override
    public ValidationOutcome validate(
            Thought decision,
            ConversationSession session,
            ResolvedContext resolvedContext,
            CapabilityRegistry capabilityRegistry
    ) {
        if (decision == null || decision.getIntent() == null || capabilityRegistry == null) {
            return ValidationOutcome.success(); // Other rules handle this
        }

        String intent = decision.getIntent();
        List<String> warnings = new ArrayList<>();

        try {
            var match = capabilityRegistry.findBestCapability(intent);
            if (match == null || match.getCapability() == null) {
                warnings.add("Fallback Selected: No capability matched, will use fallback execution");
                return ValidationOutcome.successWithWarnings(warnings, "No capability for execution mode check");
            }

            Capability capability = match.getCapability();
            Capability.ExecutionType execType = capability.getExecutionType();

            if (execType == null) {
                warnings.add("Unknown Execution Mode: Execution type not specified for " + capability.getName());
            }

            if (warnings.isEmpty()) {
                return ValidationOutcome.success();
            } else {
                return ValidationOutcome.successWithWarnings(warnings, "Execution mode validated with warnings");
            }

        } catch (Exception e) {
            return ValidationOutcome.successWithWarnings(
                    List.of("Execution Mode Check Error: " + e.getMessage()),
                    "Exception during execution mode check"
            );
        }
    }

    @Override
    public String getRuleName() {
        return "ExecutionModeRule";
    }
}