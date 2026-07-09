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
            var match = capabilityRegistry.findBestCapability(intent);
            if (match == null || match.getCapability() == null) {
                warnings.add("Unknown Capability: No capability registered for intent '" + intent + "'");
                return ValidationOutcome.successWithWarnings(warnings, "Capability not found");
            }

            Capability capability = match.getCapability();
            Capability.HealthStatus health = capability.getHealthStatus();

            if (health == Capability.HealthStatus.UNHEALTHY) {
                warnings.add("Capability Unhealthy: " + capability.getName() + " is marked as unhealthy");
            } else if (health == Capability.HealthStatus.DEGRADED) {
                warnings.add("Capability Degraded: " + capability.getName() + " is in degraded state");
            } else if (health == Capability.HealthStatus.UNKNOWN) {
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