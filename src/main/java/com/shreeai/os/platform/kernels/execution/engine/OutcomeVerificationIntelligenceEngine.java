package com.shreeai.os.platform.kernels.execution.engine;

import com.shreeai.os.platform.kernels.execution.model.ExecutionRequest;
import com.shreeai.os.platform.kernels.execution.model.ExecutionResult;
import com.shreeai.os.platform.kernels.execution.model.ExecutionStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Advanced deterministic outcome-verification intelligence for the Execution Kernel.
 *
 * <p>This engine closes the semantic gap between:</p>
 * <pre>
 * planned != attempted != executed != succeeded != verified
 * </pre>
 *
 * <p>It evaluates an actual {@link ExecutionResult} against the originating
 * {@link ExecutionRequest}. It does not execute work, mutate execution state,
 * fabricate evidence, or claim that an outcome is verified merely because an
 * execution status is {@code COMPLETED}.</p>
 *
 * <p>The engine supports:</p>
 * <ul>
 *   <li>execution-status interpretation</li>
 *   <li>expected-vs-actual outcome comparison</li>
 *   <li>explicit verification signal detection</li>
 *   <li>error and partial-result detection</li>
 *   <li>retry and performance anomaly analysis</li>
 *   <li>verification confidence</li>
 *   <li>next-action recommendations</li>
 *   <li>machine-readable verification metadata</li>
 * </ul>
 *
 * <p>Expected outcome data may be supplied through the existing generic maps
 * using the key {@code expectedOutcome}. No new domain contract is required.</p>
 *
 * <p><b>Ownership:</b> Execution Kernel — Intelligence Layer</p>
 * <p><b>Version:</b> 1.0</p>
 * <p><b>Legacy dependency:</b> None</p>
 */
public final class OutcomeVerificationIntelligenceEngine {

    private static final double MIN_CONFIDENCE = 0.05;
    private static final double MAX_CONFIDENCE = 0.95;

    private static final Set<String> SUCCESS_MARKERS = Set.of(
            "success",
            "successful",
            "verified",
            "verificationPassed",
            "verification_passed"
    );

    private static final Set<String> FAILURE_MARKERS = Set.of(
            "error",
            "errors",
            "exception",
            "failure",
            "failed",
            "errorMessage",
            "error_message"
    );

    /**
     * Verifies the semantic outcome of an execution.
     *
     * @param request the originating execution request
     * @param result the actual execution result
     * @return immutable outcome assessment
     */
    public OutcomeAssessment verify(
            ExecutionRequest request,
            ExecutionResult result) {

        Objects.requireNonNull(request, "Execution request must not be null");
        Objects.requireNonNull(result, "Execution result must not be null");

        if (!request.executionId().equals(result.executionId())) {
            return buildMismatchAssessment(request, result);
        }

        Map<String, Object> requestOptions = request.options().options();
        Map<String, Object> requestParameters = request.parameters();
        Map<String, Object> contextData = request.context().contextData();
        Map<String, Object> resultData = result.resultData();

        Map<String, Object> expectedOutcome = mapValue(
                firstValue(
                        requestOptions,
                        requestParameters,
                        contextData,
                        "expectedOutcome"
                )
        );

        Map<String, Object> explicitActualOutcome = mapValue(
                firstValue(
                        resultData,
                        resultData,
                        resultData,
                        "actualOutcome"
                )
        );

        List<String> evidence = new ArrayList<>();
        List<String> findings = new ArrayList<>();
        List<String> risks = new ArrayList<>();
        List<String> missingInformation = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();

        VerificationState state = stateFromStatus(result.status());
        boolean explicitVerification = explicitVerificationSignal(resultData);
        boolean explicitFailure = explicitFailureSignal(resultData);
        boolean partial = partialSignal(request, resultData);

        if (result.status() == ExecutionStatus.COMPLETED) {
            evidence.add("Execution lifecycle reports COMPLETED");
        } else {
            evidence.add("Execution lifecycle reports " + result.status());
        }

        if (explicitVerification) {
            evidence.add("Execution result contains an explicit verification signal");
        }

        if (explicitFailure) {
            evidence.add("Execution result contains an explicit failure/error signal");
        }

        if (!expectedOutcome.isEmpty()) {
            evidence.add("An expected outcome contract is available for comparison");
        } else {
            missingInformation.add("Expected outcome definition");
        }

        OutcomeComparison comparison = compareExpectedToActual(
                expectedOutcome,
                explicitActualOutcome.isEmpty() ? resultData : explicitActualOutcome
        );

        if (comparison.hasExpectation()) {
            evidence.add(
                    "Expected outcome comparison evaluated "
                            + comparison.comparedFields()
                            + " field(s)"
            );

            if (comparison.allMatched()) {
                findings.add("All supplied expected outcome fields matched the actual result");
            } else if (comparison.matchedFields() > 0) {
                findings.add(
                        comparison.matchedFields()
                                + " expected outcome field(s) matched, while "
                                + comparison.mismatchedFields()
                                + " field(s) did not"
                );
                risks.add("Expected outcome was only partially satisfied");
            } else {
                risks.add("Expected outcome fields did not match the actual result");
            }
        }

        if (partial) {
            state = VerificationState.PARTIAL;
            risks.add("Execution result indicates partial completion");
            recommendations.add("Complete or reconcile the remaining execution work before treating the objective as achieved");
        }

        if (result.status() == ExecutionStatus.COMPLETED
                && !partial
                && !explicitFailure
                && ((comparison.hasExpectation() && comparison.allMatched())
                || explicitVerification)) {
            state = VerificationState.VERIFIED_SUCCESS;
            findings.add("The available verification evidence supports successful outcome achievement");
        }

        if (explicitFailure) {
            state = VerificationState.FAILED;
            recommendations.add("Inspect the execution error and determine whether retry, rollback, compensation, or replanning is appropriate");
        }

        if (result.status() == ExecutionStatus.COMPLETED
                && expectedOutcome.isEmpty()
                && !explicitVerification) {
            state = VerificationState.EXECUTED_UNVERIFIED;
            findings.add("Execution completed, but the platform has not established that the expected outcome was achieved");
            risks.add("Completion status alone is insufficient proof of goal attainment");
            recommendations.add("Provide an expected outcome or explicit post-execution verification signal");
        }

        long configuredTimeout = request.options().timeoutMs();
        long actualDuration = result.metrics().durationMs();

        if (configuredTimeout > 0 && actualDuration > configuredTimeout) {
            risks.add("Execution duration exceeded the configured timeout");
            recommendations.add("Investigate timeout pressure and consider a better execution strategy or timeout configuration");
        }

        if (result.metrics().retryCount() > 0) {
            evidence.add("Execution required " + result.metrics().retryCount() + " retry attempt(s)");
            if (result.metrics().retryCount() > request.options().maxRetries()) {
                risks.add("Observed retry count exceeds the configured retry allowance");
            }
        }

        if (resultData.isEmpty()) {
            missingInformation.add("Execution result data");
        }

        if (state == VerificationState.VERIFIED_SUCCESS) {
            recommendations.add("Record the verified outcome as evidence for downstream reflection and learning");
        }

        double confidence = calculateConfidence(
                result.status(),
                explicitVerification,
                comparison,
                explicitFailure,
                partial,
                !resultData.isEmpty(),
                !expectedOutcome.isEmpty(),
                risks.size()
        );

        String confidenceBand = confidenceBand(confidence);
        String verdict = buildVerdict(
                state,
                comparison,
                explicitVerification,
                explicitFailure,
                partial
        );

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("engine", "Outcome Verification Intelligence");
        metadata.put("version", "1.0");
        metadata.put("executionId", result.executionId().value());
        metadata.put("actionId", request.actionId());
        metadata.put("verificationState", state.name());
        metadata.put("confidence", confidence);
        metadata.put("confidenceBand", confidenceBand);
        metadata.put("explicitVerification", explicitVerification);
        metadata.put("explicitFailure", explicitFailure);
        metadata.put("partial", partial);
        metadata.put("expectedOutcomePresent", !expectedOutcome.isEmpty());
        metadata.put("expectedFieldsCompared", comparison.comparedFields());
        metadata.put("expectedFieldsMatched", comparison.matchedFields());
        metadata.put("expectedFieldsMismatched", comparison.mismatchedFields());
        metadata.put("executionStatus", result.status().name());
        metadata.put("durationMs", actualDuration);
        metadata.put("configuredTimeoutMs", configuredTimeout);
        metadata.put("retryCount", result.metrics().retryCount());
        metadata.put("generatedAt", Instant.now().toString());

        return new OutcomeAssessment(
                result.executionId().value(),
                request.actionId(),
                state,
                verdict,
                confidence,
                confidenceBand,
                comparison,
                List.copyOf(new LinkedHashSet<>(evidence)),
                List.copyOf(new LinkedHashSet<>(findings)),
                List.copyOf(new LinkedHashSet<>(risks)),
                List.copyOf(new LinkedHashSet<>(missingInformation)),
                List.copyOf(new LinkedHashSet<>(recommendations)),
                Map.copyOf(metadata)
        );
    }

    private OutcomeAssessment buildMismatchAssessment(
            ExecutionRequest request,
            ExecutionResult result) {

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("engine", "Outcome Verification Intelligence");
        metadata.put("version", "1.0");
        metadata.put("verificationState", VerificationState.INVALID_CORRELATION.name());
        metadata.put("requestExecutionId", request.executionId().value());
        metadata.put("resultExecutionId", result.executionId().value());
        metadata.put("generatedAt", Instant.now().toString());

        return new OutcomeAssessment(
                result.executionId().value(),
                request.actionId(),
                VerificationState.INVALID_CORRELATION,
                "Execution result does not belong to the supplied execution request",
                0.05,
                "MINIMAL",
                OutcomeComparison.empty(),
                List.of(),
                List.of("Execution request/result correlation failed"),
                List.of("The execution result identifier does not match the request identifier"),
                List.of("Correlated execution result"),
                List.of("Discard the mismatched result and obtain the correct execution result before evaluating the outcome"),
                Map.copyOf(metadata)
        );
    }

    private VerificationState stateFromStatus(ExecutionStatus status) {
        return switch (status) {
            case COMPLETED -> VerificationState.EXECUTED_UNVERIFIED;
            case FAILED -> VerificationState.FAILED;
            case CANCELLED -> VerificationState.CANCELLED;
            case WAITING -> VerificationState.BLOCKED;
            case PAUSED -> VerificationState.PAUSED;
            case RETRYING -> VerificationState.RETRYING;
            case RUNNING -> VerificationState.RUNNING;
            case PENDING -> VerificationState.NOT_EXECUTED;
        };
    }

    private double calculateConfidence(
            ExecutionStatus status,
            boolean explicitVerification,
            OutcomeComparison comparison,
            boolean explicitFailure,
            boolean partial,
            boolean resultDataPresent,
            boolean expectedOutcomePresent,
            int riskCount) {

        if (explicitFailure || status == ExecutionStatus.FAILED) {
            return 0.20;
        }

        if (status != ExecutionStatus.COMPLETED) {
            return 0.10;
        }

        double confidence = 0.25;

        if (resultDataPresent) {
            confidence += 0.10;
        }

        if (expectedOutcomePresent) {
            confidence += 0.15;
        }

        if (comparison.allMatched()) {
            confidence += 0.30;
        } else if (comparison.matchedFields() > 0) {
            confidence += 0.12;
        }

        if (explicitVerification) {
            confidence += 0.20;
        }

        if (partial) {
            confidence -= 0.20;
        }

        confidence -= Math.min(0.25, riskCount * 0.05);

        return clamp(confidence, MIN_CONFIDENCE, MAX_CONFIDENCE);
    }

    private String confidenceBand(double confidence) {
        if (confidence < 0.25) {
            return "MINIMAL";
        }
        if (confidence < 0.50) {
            return "LOW";
        }
        if (confidence < 0.75) {
            return "MODERATE";
        }
        return "HIGH";
    }

    private String buildVerdict(
            VerificationState state,
            OutcomeComparison comparison,
            boolean explicitVerification,
            boolean explicitFailure,
            boolean partial) {

        if (explicitFailure) {
            return "Execution failed; outcome achievement is not established";
        }

        if (partial) {
            return "Execution is incomplete; the objective cannot be considered fully achieved";
        }

        if (state == VerificationState.BLOCKED
                || state == VerificationState.NOT_EXECUTED
                || state == VerificationState.RUNNING
                || state == VerificationState.PAUSED
                || state == VerificationState.RETRYING) {
            return "Execution has not reached a verified terminal outcome";
        }

        if (comparison.hasExpectation() && comparison.allMatched()) {
            return "Expected outcome matches the available actual result evidence";
        }

        if (explicitVerification) {
            return "Execution produced an explicit verification signal supporting outcome achievement";
        }

        return "Execution completed, but outcome achievement remains unverified";
    }

    private boolean explicitVerificationSignal(Map<String, Object> resultData) {
        for (String key : SUCCESS_MARKERS) {
            Object value = resultData.get(key);
            if (value instanceof Boolean booleanValue && booleanValue) {
                return true;
            }
            if (value instanceof String stringValue
                    && Boolean.parseBoolean(stringValue)) {
                return true;
            }
        }

        Object verification = resultData.get("verification");
        if (verification instanceof Map<?, ?> map) {
            Object passed = map.get("passed");
            if (passed instanceof Boolean booleanValue && booleanValue) {
                return true;
            }
            if (passed instanceof String stringValue
                    && Boolean.parseBoolean(stringValue)) {
                return true;
            }
        }

        return false;
    }

    private boolean explicitFailureSignal(Map<String, Object> resultData) {
        for (String key : FAILURE_MARKERS) {
            if (resultData.containsKey(key)
                    && resultData.get(key) != null
                    && !String.valueOf(resultData.get(key)).isBlank()) {
                Object value = resultData.get(key);
                if (value instanceof Boolean booleanValue) {
                    if (booleanValue) {
                        return true;
                    }
                    continue;
                }
                return true;
            }
        }

        return false;
    }

    private boolean partialSignal(
            ExecutionRequest request,
            Map<String, Object> resultData) {

        Object explicit = firstValue(
                resultData,
                request.options().options(),
                request.parameters(),
                "partial"
        );

        if (explicit instanceof Boolean booleanValue) {
            return booleanValue;
        }

        if (explicit instanceof String stringValue) {
            return Boolean.parseBoolean(stringValue);
        }

        Object completedItems = resultData.get("completedItems");
        Object expectedItems = resultData.get("expectedItems");

        if (completedItems instanceof Number completed
                && expectedItems instanceof Number expected) {
            return completed.intValue() < expected.intValue();
        }

        return false;
    }

    private OutcomeComparison compareExpectedToActual(
            Map<String, Object> expected,
            Map<String, Object> actual) {

        if (expected.isEmpty()) {
            return OutcomeComparison.empty();
        }

        int compared = 0;
        int matched = 0;
        List<String> mismatchedKeys = new ArrayList<>();

        for (Map.Entry<String, Object> entry : expected.entrySet()) {
            compared++;

            if (actual.containsKey(entry.getKey())
                    && deepEquals(entry.getValue(), actual.get(entry.getKey()))) {
                matched++;
            } else {
                mismatchedKeys.add(entry.getKey());
            }
        }

        return new OutcomeComparison(
                true,
                compared,
                matched,
                compared - matched,
                List.copyOf(mismatchedKeys)
        );
    }

    private boolean deepEquals(Object expected, Object actual) {
        if (Objects.equals(expected, actual)) {
            return true;
        }

        if (expected instanceof Number expectedNumber
                && actual instanceof Number actualNumber) {
            return Double.compare(
                    expectedNumber.doubleValue(),
                    actualNumber.doubleValue()
            ) == 0;
        }

        if (expected instanceof Map<?, ?> expectedMap
                && actual instanceof Map<?, ?> actualMap) {
            if (expectedMap.size() != actualMap.size()) {
                return false;
            }
            for (Map.Entry<?, ?> entry : expectedMap.entrySet()) {
                if (!actualMap.containsKey(entry.getKey())
                        || !deepEquals(entry.getValue(), actualMap.get(entry.getKey()))) {
                    return false;
                }
            }
            return true;
        }

        if (expected instanceof List<?> expectedList
                && actual instanceof List<?> actualList) {
            if (expectedList.size() != actualList.size()) {
                return false;
            }
            for (int i = 0; i < expectedList.size(); i++) {
                if (!deepEquals(expectedList.get(i), actualList.get(i))) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }

    private Object firstValue(
            Map<String, Object> primary,
            Map<String, Object> secondary,
            Map<String, Object> tertiary,
            String key) {

        if (primary.containsKey(key)) {
            return primary.get(key);
        }
        if (secondary.containsKey(key)) {
            return secondary.get(key);
        }
        return tertiary.get(key);
    }

    private Map<String, Object> mapValue(Object value) {
        if (!(value instanceof Map<?, ?> map)) {
            return Map.of();
        }

        Map<String, Object> normalized = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getKey() != null) {
                normalized.put(String.valueOf(entry.getKey()), entry.getValue());
            }
        }
        return Collections.unmodifiableMap(normalized);
    }

    private double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    /** Semantic verification state. */
    public enum VerificationState {
        NOT_EXECUTED,
        RUNNING,
        RETRYING,
        BLOCKED,
        PAUSED,
        EXECUTED_UNVERIFIED,
        VERIFIED_SUCCESS,
        PARTIAL,
        FAILED,
        CANCELLED,
        INVALID_CORRELATION
    }

    /** Immutable expected-vs-actual comparison. */
    public record OutcomeComparison(
            boolean hasExpectation,
            int comparedFields,
            int matchedFields,
            int mismatchedFields,
            List<String> mismatchedKeys) {

        public OutcomeComparison {
            mismatchedKeys = mismatchedKeys == null
                    ? List.of()
                    : List.copyOf(mismatchedKeys);
        }

        public boolean allMatched() {
            return hasExpectation && comparedFields > 0 && mismatchedFields == 0;
        }

        public static OutcomeComparison empty() {
            return new OutcomeComparison(
                    false,
                    0,
                    0,
                    0,
                    List.of()
            );
        }
    }

    /** Immutable intelligence result exposed to developers and higher kernels. */
    public record OutcomeAssessment(
            String executionId,
            String actionId,
            VerificationState state,
            String verdict,
            double confidence,
            String confidenceBand,
            OutcomeComparison comparison,
            List<String> evidence,
            List<String> findings,
            List<String> risks,
            List<String> missingInformation,
            List<String> recommendations,
            Map<String, Object> metadata) {

        public OutcomeAssessment {
            Objects.requireNonNull(executionId, "executionId must not be null");
            Objects.requireNonNull(actionId, "actionId must not be null");
            Objects.requireNonNull(state, "state must not be null");
            Objects.requireNonNull(verdict, "verdict must not be null");
            Objects.requireNonNull(comparison, "comparison must not be null");
            evidence = evidence == null ? List.of() : List.copyOf(evidence);
            findings = findings == null ? List.of() : List.copyOf(findings);
            risks = risks == null ? List.of() : List.copyOf(risks);
            missingInformation = missingInformation == null
                    ? List.of()
                    : List.copyOf(missingInformation);
            recommendations = recommendations == null
                    ? List.of()
                    : List.copyOf(recommendations);
            metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        }

        /** Returns whether the outcome has been positively verified. */
        public boolean verified() {
            return state == VerificationState.VERIFIED_SUCCESS;
        }

        /** Returns whether further verification or recovery is required. */
        public boolean requiresFollowUp() {
            return !verified()
                    || !missingInformation.isEmpty()
                    || !risks.isEmpty();
        }
    }
}