package com.shreeai.os.platform.validation.rules;

import com.shreeai.os.platform.kernels.cognitive.model.Thought;
import com.shreeai.os.platform.kernels.context.model.ConversationSession;
import com.shreeai.os.platform.kernels.context.model.ResolvedContext;
import com.shreeai.os.platform.kernels.execution.model.Capability;
import com.shreeai.os.platform.kernels.execution.model.CapabilityHealthStatus;
import com.shreeai.os.platform.kernels.execution.service.CapabilityRegistry;
import com.shreeai.os.platform.validation.ValidationOutcome;
import com.shreeai.os.platform.validation.ValidationRule;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates capability availability and health.
 *
 * <p>Checks:
 * <ul>
 *   <li>Capability exists for the intent</li>
 *   <li>Capability health status (HEALTHY, DEGRADED, UNHEALTHY, UNKNOWN)</li>
 * </ul>
 * </p>
 *
 * @author Shree AI OS Team
 * @version 1.0
 * @since Sprint 5.1
 */
@Component
@Order(400)
public class CapabilityRule implements ValidationRule {

    @Override
    public ValidationOutcome validate(
            Thought decision,
            ConversationSession session,
            ResolvedContext resolvedContext,
            CapabilityRegistry capabilityRegistry
    ) {
        if (decision == null || decision.getIntent() == null) {
            return ValidationOutcome.success(); // DecisionExistsRule handles this
        }

        String intent = decision.getIntent();
        List<String> warnings = new ArrayList<>();

        if (capabilityRegistry == null) {
            return ValidationOutcome.successWithWarnings(
                    List.of("Capability Registry: Registry is null"),
                    "Cannot validate capability"
            );
        }

        try {
            var matchOpt = capabilityRegistry.findBestCapability(intent);
            if (matchOpt.isEmpty()) {
                warnings.add("Unknown Capability: No capability registered for intent '" + intent + "'");
                return ValidationOutcome.successWithWarnings(warnings, "Capability not found");
            }
            var match = matchOpt.get();

            Capability capability = match.getCapability();
            CapabilityHealthStatus health = capability.getHealthStatus();

            if (health == CapabilityHealthStatus.UNHEALTHY) {
                warnings.add("Capability Unhealthy: " + capability.getName() + " is marked as unhealthy");
            } else if (health == CapabilityHealthStatus.DEGRADED) {
                warnings.add("Capability Degraded: " + capability.getName() + " is in degraded state");
            } else if (health == CapabilityHealthStatus.UNKNOWN) {
                warnings.add("Capability Unknown: Health status unknown for " + capability.getName());
            }

            if (warnings.isEmpty()) {
                return ValidationOutcome.success();
            } else {
                return ValidationOutcome.successWithWarnings(warnings, "Capability validated with warnings");
            }

        } catch (Exception e) {
            return ValidationOutcome.successWithWarnings(
                    List.of("Capability Lookup Error: " + e.getMessage()),
                    "Exception during capability lookup"
            );
        }
    }

    @Override
    public String getRuleName() {
        return "CapabilityRule";
    }
}