package platform.validation;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Immutable validation result for a decision.
 *
 * <p>This class is thread-safe and immutable by design.
 * All fields are final and set via constructor.</p>
 *
 * @author Shree AI OS Team
 * @version 1.0
 * @since Sprint 5.1
 */
public final class ValidationResult {

    private final String validationId;
    private final String decisionId;
    private final boolean valid;
    private final ValidationStatus status;
    private final ValidationStrategy strategy;
    private final RiskLevel riskLevel;
    private final double confidence;
    private final List<String> warnings;
    private final List<String> errors;
    private final Map<String, Object> metadata;
    private final Instant timestamp;
    private final ValidationTrace trace;

    /**
     * Risk levels for decisions.
     */
    public enum RiskLevel {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL,
        UNKNOWN
    }

    public ValidationResult(
            String validationId,
            String decisionId,
            boolean valid,
            ValidationStatus status,
            ValidationStrategy strategy,
            RiskLevel riskLevel,
            double confidence,
            List<String> warnings,
            List<String> errors,
            Map<String, Object> metadata,
            Instant timestamp,
            ValidationTrace trace
    ) {
        this.validationId = validationId;
        this.decisionId = decisionId;
        this.valid = valid;
        this.status = status;
        this.strategy = strategy;
        this.riskLevel = riskLevel;
        this.confidence = confidence;
        this.warnings = warnings != null ? Collections.unmodifiableList(warnings) : Collections.emptyList();
        this.errors = errors != null ? Collections.unmodifiableList(errors) : Collections.emptyList();
        this.metadata = metadata != null ? Collections.unmodifiableMap(metadata) : Collections.emptyMap();
        this.timestamp = timestamp;
        this.trace = trace;
    }

    // Getters
    public String getValidationId() { return validationId; }
    public String getDecisionId() { return decisionId; }
    public boolean isValid() { return valid; }
    public ValidationStatus getStatus() { return status; }
    public ValidationStrategy getStrategy() { return strategy; }
    public RiskLevel getRiskLevel() { return riskLevel; }
    public double getConfidence() { return confidence; }
    public List<String> getWarnings() { return warnings; }
    public List<String> getErrors() { return errors; }
    public Map<String, Object> getMetadata() { return metadata; }
    public Instant getTimestamp() { return timestamp; }
    public ValidationTrace getTrace() { return trace; }

    /**
     * Create a builder for ValidationResult.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for ValidationResult.
     */
    public static class Builder {
        private String validationId;
        private String decisionId;
        private boolean valid;
        private ValidationStatus status = ValidationStatus.UNKNOWN;
        private ValidationStrategy strategy = ValidationStrategy.UNKNOWN;
        private RiskLevel riskLevel = RiskLevel.UNKNOWN;
        private double confidence = 0.0;
        private List<String> warnings = Collections.emptyList();
        private List<String> errors = Collections.emptyList();
        private Map<String, Object> metadata = Collections.emptyMap();
        private Instant timestamp = Instant.now();
        private ValidationTrace trace;

        public Builder validationId(String validationId) {
            this.validationId = validationId;
            return this;
        }

        public Builder decisionId(String decisionId) {
            this.decisionId = decisionId;
            return this;
        }

        public Builder valid(boolean valid) {
            this.valid = valid;
            return this;
        }

        public Builder status(ValidationStatus status) {
            this.status = status;
            return this;
        }

        public Builder strategy(ValidationStrategy strategy) {
            this.strategy = strategy;
            return this;
        }

        public Builder riskLevel(RiskLevel riskLevel) {
            this.riskLevel = riskLevel;
            return this;
        }

        public Builder confidence(double confidence) {
            this.confidence = confidence;
            return this;
        }

        public Builder warnings(List<String> warnings) {
            this.warnings = warnings;
            return this;
        }

        public Builder errors(List<String> errors) {
            this.errors = errors;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder trace(ValidationTrace trace) {
            this.trace = trace;
            return this;
        }

        public ValidationResult build() {
            return new ValidationResult(
                    validationId, decisionId, valid, status, strategy,
                    riskLevel, confidence, warnings, errors, metadata, timestamp, trace
            );
        }
    }

    @Override
    public String toString() {
        return "ValidationResult{" +
                "validationId='" + validationId + '\'' +
                ", decisionId='" + decisionId + '\'' +
                ", valid=" + valid +
                ", status=" + status +
                ", strategy=" + strategy +
                ", riskLevel=" + riskLevel +
                ", confidence=" + confidence +
                ", warnings=" + warnings +
                ", errors=" + errors +
                ", metadata=" + metadata +
                ", timestamp=" + timestamp +
                ", trace=" + (trace != null ? trace.getSteps().size() + " steps" : "null") +
                '}';
    }
}