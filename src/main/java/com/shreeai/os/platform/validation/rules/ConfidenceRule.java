package com.shreeai.os.platform.validation.rules;

import com.shreeai.os.platform.kernels.cognitive.model.Thought;
import com.shreeai.os.platform.kernels.context.model.ConversationSession;
import com.shreeai.os.platform.kernels.context.model.ResolvedContext;
import com.shreeai.os.platform.kernels.execution.service.CapabilityRegistry;
import com.shreeai.os.platform.validation.ValidationOutcome;
import com.shreeai.os.platform.validation.ValidationRule;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates confidence level of the decision.
 *
 * <p>Checks:
 * <ul>
 *   <li>Decision confidence is above minimum threshold (0.5)</li>
 * </ul>
 * </p>
 *
 * <p>Note: Currently uses default confidence of 0.8 since DecisionEngine
 * returns action string only. Future: extract confidence from DecisionEngine.</p>
 *
 * @author Shree AI OS Team
 * @version 1.0
 * @since Sprint 5.1
 */
@Component
@Order(500)
public class ConfidenceRule implements ValidationRule {

    private static final double MIN_CONFIDENCE = 0.5;
    private static final double DEFAULT_CONFIDENCE = 0.8;

    @Override
    public ValidationOutcome validate(
            Thought decision,
            ConversationSession session,
            ResolvedContext resolvedContext,
            CapabilityRegistry capabilityRegistry
    ) {
        if (decision == null) {
            return ValidationOutcome.success(); // DecisionExistsRule handles this
        }

        double confidence = DEFAULT_CONFIDENCE;

        if (confidence < MIN_CONFIDENCE) {
            List<String> warnings = new ArrayList<>();
            warnings.add(String.format("Low Confidence: Decision confidence %.0f%% is below threshold %.0f%%",
                    confidence * 100, MIN_CONFIDENCE * 100));
            return ValidationOutcome.successWithWarnings(warnings, "Confidence below threshold");
        }

        return ValidationOutcome.success();
    }

    @Override
    public String getRuleName() {
        return "ConfidenceRule";
    }
}