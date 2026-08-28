package com.shreeai.os.platform.kernels.execution.engine;

import com.shreeai.os.platform.kernels.execution.model.ExecutionRequest;
import com.shreeai.os.platform.kernels.execution.model.ExecutionStatus;
import com.shreeai.os.platform.kernels.execution.model.RecoveryStrategy;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Advanced deterministic intelligence layer for the Execution Kernel.
 *
 * <p>This component does not perform external execution. It evaluates an
 * execution request before/during deterministic processing and produces an
 * explainable execution-intelligence assessment.</p>
 *
 * <p>The assessment covers readiness, configuration quality, risk signals,
 * precondition signals, retry posture, verification requirements, recovery
 * posture and execution confidence.</p>
 *
 * <p>Optional application-specific signals are read from the existing generic
 * request/context/options maps. No legacy package is referenced and no new
 * domain contract is required for this intelligence layer.</p>
 *
 * <p><b>Important invariant:</b> this engine never claims that an external
 * action was actually performed. It describes the quality and readiness of
 * the execution request and the deterministic processing outcome.</p>
 *
 * <p><b>Ownership:</b> Execution Kernel - Intelligence Layer</p>
 * <p><b>Version:</b> 1.0</p>
 */
public final class ExecutionIntelligenceEngine {

    private static final double MIN_CONFIDENCE = 0.05;
    private static final double MAX_CONFIDENCE = 0.95;

    /**
     * Analyzes an execution request for a specific execution kind.
     *
     * @param request validated execution request
     * @param executionKind action, task or workflow
     * @return immutable intelligence analysis
     */
    public ExecutionIntelligenceAnalysis analyze(
            ExecutionRequest request,
            String executionKind) {

        Objects.requireNonNull(request, "Execution request must not be null");

        String kind = normalizeKind(executionKind);

        Map<String, Object> contextData = request.context().contextData();
        Map<String, Object> options = request.options().options();
        Map<String, Object> parameters = request.parameters();

        List<String> strengths = new ArrayList<>();
        List<String> risks = new ArrayList<>();
        List<String> missingInformation = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();
        List<String> verificationChecks = new ArrayList<>();

        int readinessSignals = 0;
        int positiveSignals = 0;

        // -------------------------------------------------------------
        // Identity / intent quality
        // -------------------------------------------------------------

        if (hasText(request.actionId())) {
            readinessSignals++;
            positiveSignals++;
            strengths.add("Execution action is explicitly identified");
        } else {
            risks.add("Execution action identifier is missing");
            missingInformation.add("Action identifier");
        }

        if (hasText(request.context().planId())) {
            readinessSignals++;
            positiveSignals++;
            strengths.add("Execution is linked to a plan");
        } else {
            risks.add("Execution is not linked to a plan");
        }

        if (hasText(request.context().objectiveId())) {
            readinessSignals++;
            positiveSignals++;
            strengths.add("Execution is linked to an objective");
        } else {
            risks.add("Execution objective is missing");
            missingInformation.add("Execution objective");
        }

        if (!contextData.isEmpty()) {
            readinessSignals++;
            positiveSignals++;
            strengths.add("Execution context contains contextual data");
        } else {
            risks.add("Execution context contains no contextual data");
            missingInformation.add("Execution context data");
        }

        if (!parameters.isEmpty()) {
            readinessSignals++;
            positiveSignals++;
            strengths.add("Execution request contains parameters");
        } else {
            risks.add("Execution request contains no parameters");
        }

        // -------------------------------------------------------------
        // Timeout / retry intelligence
        // -------------------------------------------------------------

        long timeoutMs = request.options().timeoutMs();
        int maxRetries = request.options().maxRetries();
        long retryDelayMs = request.options().retryDelayMs();

        if (timeoutMs > 0) {
            readinessSignals++;
            positiveSignals++;
            strengths.add("Execution timeout is explicitly configured");
        } else {
            risks.add("Execution timeout is not positively configured");
            missingInformation.add("Positive execution timeout");
        }

        if (maxRetries >= 0) {
            readinessSignals++;
            positiveSignals++;
        }

        if (maxRetries > 0 && retryDelayMs <= 0) {
            risks.add("Retries are enabled without a positive retry delay");
            recommendations.add("Configure a positive retry delay before repeated recovery attempts");
        }

        if (maxRetries > 3) {
            risks.add("High retry allowance may amplify side effects or duplicate work");
            recommendations.add("Confirm that the action is idempotent before allowing repeated retries");
        }

        if (maxRetries == 0) {
            recommendations.add("No automatic retry is configured; recovery must be handled explicitly");
        }

        // -------------------------------------------------------------
        // Optional execution policy signals
        // -------------------------------------------------------------

        Boolean dryRun = booleanValue(
                firstValue(options, parameters, contextData, "dryRun")
        );

        Boolean idempotent = booleanValue(
                firstValue(options, parameters, contextData, "idempotent")
        );

        Boolean destructive = booleanValue(
                firstValue(options, parameters, contextData, "destructive")
        );

        Boolean confirmationRequired = booleanValue(
                firstValue(options, parameters, contextData, "requiresConfirmation")
        );

        Boolean preconditionsSatisfied = booleanValue(
                firstValue(options, parameters, contextData, "preconditionsSatisfied")
        );

        Boolean dependencySatisfied = booleanValue(
                firstValue(options, parameters, contextData, "dependencySatisfied")
        );

        Boolean verificationRequired = booleanValue(
                firstValue(options, parameters, contextData, "verificationRequired")
        );

        String riskLevel = stringValue(
                firstValue(options, parameters, contextData, "riskLevel")
        );

        if (Boolean.TRUE.equals(dryRun)) {
            strengths.add("Execution is explicitly marked as dry-run");
            recommendations.add("Treat dry-run output as simulation until a real execution request is issued");
        }

        if (Boolean.TRUE.equals(destructive)) {
            risks.add("Execution is marked as potentially destructive");
            verificationChecks.add("Require explicit confirmation or authorization for destructive execution");
            recommendations.add("Verify authorization and idempotency before execution");
        }

        if (Boolean.TRUE.equals(confirmationRequired)) {
            verificationChecks.add("Explicit confirmation is required before execution");
        }

        if (Boolean.TRUE.equals(idempotent)) {
            strengths.add("Action is explicitly marked idempotent");
        } else if (maxRetries > 0) {
            risks.add("Retries are configured but idempotency has not been explicitly established");
            verificationChecks.add("Verify idempotency before retrying the action");
        }

        if (preconditionsSatisfied != null) {
            readinessSignals++;
            if (preconditionsSatisfied) {
                positiveSignals++;
                strengths.add("Execution preconditions are reported as satisfied");
            } else {
                risks.add("Execution preconditions are reported as unsatisfied");
                verificationChecks.add("Resolve execution preconditions before proceeding");
            }
        } else {
            missingInformation.add("Explicit precondition status");
        }

        if (dependencySatisfied != null) {
            readinessSignals++;
            if (dependencySatisfied) {
                positiveSignals++;
                strengths.add("Execution dependencies are reported as satisfied");
            } else {
                risks.add("Execution dependency is reported as unsatisfied");
                verificationChecks.add("Wait for required dependencies before execution");
            }
        } else {
            missingInformation.add("Explicit dependency status");
        }

        if (verificationRequired != null && verificationRequired) {
            verificationChecks.add("Post-execution verification is explicitly required");
        }

        if (riskLevel != null) {
            switch (riskLevel.toUpperCase(Locale.ROOT)) {
                case "CRITICAL" -> {
                    risks.add("Execution is marked CRITICAL risk");
                    verificationChecks.add("Require strongest available authorization and post-execution verification");
                }
                case "HIGH" -> {
                    risks.add("Execution is marked HIGH risk");
                    verificationChecks.add("Require authorization and post-execution verification");
                }
                case "MEDIUM" -> recommendations.add("Apply normal execution verification");
                case "LOW" -> strengths.add("Execution is marked LOW risk");
                default -> risks.add("Unrecognized execution risk level: " + riskLevel);
            }
        } else {
            missingInformation.add("Explicit risk level");
        }

        // -------------------------------------------------------------
        // Execution mode / source provenance
        // -------------------------------------------------------------

        String executionMode = stringValue(
                firstValue(options, parameters, contextData, "executionMode")
        );

        String source = stringValue(
                firstValue(options, parameters, contextData, "source")
        );

        if (executionMode != null) {
            strengths.add("Execution mode is explicitly identified as " + executionMode);
        } else {
            missingInformation.add("Execution mode");
        }

        if (source != null) {
            strengths.add("Execution request has a declared source");
        } else {
            missingInformation.add("Execution request source");
        }

        // -------------------------------------------------------------
        // Confidence / readiness
        // -------------------------------------------------------------

        double completeness = readinessSignals == 0
                ? 0.05
                : (double) positiveSignals / readinessSignals;

        double riskPenalty = calculateRiskPenalty(risks, riskLevel);
        double verificationPenalty = verificationChecks.isEmpty() ? 0.0 : 0.05;
        double missingPenalty = Math.min(0.25, missingInformation.size() * 0.025);

        double confidence = clamp(
                0.35
                        + (completeness * 0.45)
                        - riskPenalty
                        - verificationPenalty
                        - missingPenalty,
                MIN_CONFIDENCE,
                MAX_CONFIDENCE
        );

        boolean executionReady =
                !hasBlockingRisk(preconditionsSatisfied, dependencySatisfied, riskLevel)
                        && timeoutMs > 0
                        && hasText(request.actionId())
                        && hasText(request.context().planId())
                        && hasText(request.context().objectiveId());

        if (!executionReady) {
            recommendations.add("Treat the request as execution-not-ready until blocking conditions are resolved");
        } else {
            recommendations.add("Execution request satisfies the deterministic readiness baseline");
        }

        if (verificationChecks.isEmpty()) {
            verificationChecks.add("No explicit verification requirement was supplied; verify outcome according to application policy");
        }

        RecoveryRecommendation recoveryRecommendation =
                recommendRecoveryStrategy(
                        riskLevel,
                        idempotent,
                        destructive,
                        maxRetries
                );

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("engine", "ExecutionIntelligenceEngine");
        metadata.put("version", "1.0");
        metadata.put("executionKind", kind);
        metadata.put("executionId", request.executionId().value());
        metadata.put("actionId", request.actionId());
        metadata.put("planId", request.context().planId());
        metadata.put("objectiveId", request.context().objectiveId());
        metadata.put("executionMode", executionMode);
        metadata.put("source", source);
        metadata.put("timeoutMs", timeoutMs);
        metadata.put("maxRetries", maxRetries);
        metadata.put("retryDelayMs", retryDelayMs);
        metadata.put("allowPartial", request.options().allowPartial());
        metadata.put("continueOnError", request.options().continueOnError());
        metadata.put("dryRun", dryRun);
        metadata.put("idempotent", idempotent);
        metadata.put("destructive", destructive);
        metadata.put("requiresConfirmation", confirmationRequired);
        metadata.put("preconditionsSatisfied", preconditionsSatisfied);
        metadata.put("dependencySatisfied", dependencySatisfied);
        metadata.put("verificationRequired", verificationRequired);
        metadata.put("riskLevel", riskLevel);
        metadata.put("executionReady", executionReady);
        metadata.put("confidence", confidence);
        metadata.put("confidenceBand", confidenceBand(confidence));
        metadata.put("riskPenalty", riskPenalty);
        metadata.put("verificationCheckCount", verificationChecks.size());
        metadata.put("missingInformationCount", missingInformation.size());
        metadata.put("recommendedRecoveryStrategy", recoveryRecommendation.strategy().name());
        metadata.put("recoveryRationale", recoveryRecommendation.rationale());
        metadata.put(
                "processingBoundary",
                "INTELLIGENCE_ONLY_EXTERNAL_EXECUTION_NOT_CLAIMED"
        );

        return new ExecutionIntelligenceAnalysis(
                kind,
                executionReady,
                confidence,
                confidenceBand(confidence),
                List.copyOf(new LinkedHashSet<>(strengths)),
                List.copyOf(new LinkedHashSet<>(risks)),
                List.copyOf(new LinkedHashSet<>(missingInformation)),
                List.copyOf(new LinkedHashSet<>(recommendations)),
                List.copyOf(new LinkedHashSet<>(verificationChecks)),
                recoveryRecommendation.strategy(),
                recoveryRecommendation.rationale(),
                Map.copyOf(metadata)
        );
    }

    /**
     * Analyzes a recovery request without pretending that recovery has executed.
     */
    public RecoveryAnalysis analyzeRecovery(
            String executionId,
            RecoveryStrategy strategy) {

        if (executionId == null || executionId.isBlank()) {
            throw new IllegalArgumentException("executionId must not be null or blank");
        }

        Objects.requireNonNull(strategy, "recoveryStrategy must not be null");

        List<String> risks = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();

        switch (strategy) {
            case RETRY -> recommendations.add(
                    "Retry only after verifying idempotency or retry safety"
            );
            case ROLLBACK -> recommendations.add(
                    "Rollback requires a known recoverable prior state"
            );
            case COMPENSATE -> recommendations.add(
                    "Compensation requires an available compensating operation"
            );
            case SKIP -> risks.add(
                    "Skipping may leave the plan partially completed"
            );
            case FAIL -> recommendations.add(
                    "Failing preserves failure visibility and prevents further dependent work"
            );
            case DEFAULT -> recommendations.add(
                    "Select recovery using execution-specific failure evidence"
            );
        }

        double confidence = strategy == RecoveryStrategy.DEFAULT
                ? 0.45
                : 0.70;

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("engine", "ExecutionIntelligenceEngine");
        metadata.put("analysis", "RECOVERY_POSTURE");
        metadata.put("executionId", executionId);
        metadata.put("strategy", strategy.name());
        metadata.put("confidence", confidence);
        metadata.put("executionPerformed", false);

        return new RecoveryAnalysis(
                executionId,
                strategy,
                confidence,
                List.copyOf(risks),
                List.copyOf(recommendations),
                Map.copyOf(metadata)
        );
    }

    private RecoveryRecommendation recommendRecoveryStrategy(
            String riskLevel,
            Boolean idempotent,
            Boolean destructive,
            int maxRetries) {

        if (Boolean.TRUE.equals(destructive)) {
            return new RecoveryRecommendation(
                    RecoveryStrategy.FAIL,
                    "Destructive actions should default to explicit failure handling rather than blind retry"
            );
        }

        if (Boolean.TRUE.equals(idempotent) && maxRetries > 0) {
            return new RecoveryRecommendation(
                    RecoveryStrategy.RETRY,
                    "The action is explicitly idempotent and retries are configured"
            );
        }

        if (riskLevel != null
                && riskLevel.equalsIgnoreCase("HIGH")) {
            return new RecoveryRecommendation(
                    RecoveryStrategy.DEFAULT,
                    "High-risk execution requires failure-specific recovery evidence"
            );
        }

        if (maxRetries > 0) {
            return new RecoveryRecommendation(
                    RecoveryStrategy.DEFAULT,
                    "Retries exist but idempotency is not explicitly established"
            );
        }

        return new RecoveryRecommendation(
                RecoveryStrategy.DEFAULT,
                "No execution-specific recovery evidence is available"
        );
    }

    private boolean hasBlockingRisk(
            Boolean preconditionsSatisfied,
            Boolean dependencySatisfied,
            String riskLevel) {

        if (Boolean.FALSE.equals(preconditionsSatisfied)
                || Boolean.FALSE.equals(dependencySatisfied)) {
            return true;
        }

        return riskLevel != null
                && riskLevel.equalsIgnoreCase("CRITICAL");
    }

    private double calculateRiskPenalty(
            List<String> risks,
            String riskLevel) {

        double penalty = Math.min(0.25, risks.size() * 0.025);

        if (riskLevel == null) {
            return penalty;
        }

        return switch (riskLevel.toUpperCase(Locale.ROOT)) {
            case "CRITICAL" -> Math.min(0.45, penalty + 0.25);
            case "HIGH" -> Math.min(0.35, penalty + 0.15);
            case "MEDIUM" -> Math.min(0.30, penalty + 0.05);
            default -> penalty;
        };
    }

    private Object firstValue(
            Map<String, Object> options,
            Map<String, Object> parameters,
            Map<String, Object> contextData,
            String key) {

        if (options.containsKey(key)) {
            return options.get(key);
        }

        if (parameters.containsKey(key)) {
            return parameters.get(key);
        }

        return contextData.get(key);
    }

    private Boolean booleanValue(Object value) {
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }

        if (value instanceof String string) {
            if ("true".equalsIgnoreCase(string.trim())) {
                return true;
            }
            if ("false".equalsIgnoreCase(string.trim())) {
                return false;
            }
        }

        return null;
    }

    private String stringValue(Object value) {
        if (value == null) {
            return null;
        }

        String result = String.valueOf(value).trim();
        return result.isBlank() ? null : result;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String normalizeKind(String executionKind) {
        if (executionKind == null || executionKind.isBlank()) {
            return "UNKNOWN";
        }

        return executionKind.trim().toUpperCase(Locale.ROOT);
    }

    private String confidenceBand(double confidence) {
        if (confidence >= 0.80) {
            return "HIGH";
        }

        if (confidence >= 0.60) {
            return "MODERATE";
        }

        if (confidence >= 0.40) {
            return "LIMITED";
        }

        return "LOW";
    }

    private double clamp(
            double value,
            double minimum,
            double maximum) {

        return Math.max(
                minimum,
                Math.min(maximum, value)
        );
    }

    /** Immutable execution intelligence result. */
    public record ExecutionIntelligenceAnalysis(
            String executionKind,
            boolean executionReady,
            double confidence,
            String confidenceBand,
            List<String> strengths,
            List<String> risks,
            List<String> missingInformation,
            List<String> recommendations,
            List<String> verificationChecks,
            RecoveryStrategy recommendedRecoveryStrategy,
            String recoveryRationale,
            Map<String, Object> metadata) {

        public ExecutionIntelligenceAnalysis {
            Objects.requireNonNull(executionKind);
            Objects.requireNonNull(confidenceBand);
            Objects.requireNonNull(strengths);
            Objects.requireNonNull(risks);
            Objects.requireNonNull(missingInformation);
            Objects.requireNonNull(recommendations);
            Objects.requireNonNull(verificationChecks);
            Objects.requireNonNull(recommendedRecoveryStrategy);
            Objects.requireNonNull(recoveryRationale);
            Objects.requireNonNull(metadata);

            if (confidence < 0.0 || confidence > 1.0) {
                throw new IllegalArgumentException("confidence must be between 0.0 and 1.0");
            }

            strengths = List.copyOf(strengths);
            risks = List.copyOf(risks);
            missingInformation = List.copyOf(missingInformation);
            recommendations = List.copyOf(recommendations);
            verificationChecks = List.copyOf(verificationChecks);
            metadata = Map.copyOf(metadata);
        }
    }

    /** Immutable recovery posture analysis. */
    public record RecoveryAnalysis(
            String executionId,
            RecoveryStrategy strategy,
            double confidence,
            List<String> risks,
            List<String> recommendations,
            Map<String, Object> metadata) {

        public RecoveryAnalysis {
            Objects.requireNonNull(executionId);
            Objects.requireNonNull(strategy);
            Objects.requireNonNull(risks);
            Objects.requireNonNull(recommendations);
            Objects.requireNonNull(metadata);

            risks = List.copyOf(risks);
            recommendations = List.copyOf(recommendations);
            metadata = Map.copyOf(metadata);
        }
    }

    private record RecoveryRecommendation(
            RecoveryStrategy strategy,
            String rationale) {
    }
}