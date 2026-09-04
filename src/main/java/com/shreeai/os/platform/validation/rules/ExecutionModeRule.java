package com.shreeai.os.platform.validation.rules;

import com.shreeai.os.platform.kernels.cognitive.model.Thought;
import com.shreeai.os.platform.kernels.context.model.ConversationSession;
import com.shreeai.os.platform.kernels.context.model.ResolvedContext;
import com.shreeai.os.platform.kernels.execution.model.Capability;
import com.shreeai.os.platform.kernels.execution.model.CapabilityExecutionType;
import com.shreeai.os.platform.kernels.execution.service.CapabilityRegistry;
import com.shreeai.os.platform.validation.ValidationOutcome;
import com.shreeai.os.platform.validation.ValidationRule;
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
            var matchOpt = capabilityRegistry.findBestCapability(intent);
            if (matchOpt.isEmpty()) {
                warnings.add("Fallback Selected: No capability matched, will use fallback execution");
                return ValidationOutcome.successWithWarnings(warnings, "No capability for execution mode check");
            }
            var match = matchOpt.get();

            Capability capability = match.getCapability();
            CapabilityExecutionType execType = capability.getExecutionType();

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