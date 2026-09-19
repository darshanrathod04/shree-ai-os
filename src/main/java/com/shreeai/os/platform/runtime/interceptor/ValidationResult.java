package com.shreeai.os.platform.runtime.interceptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>ValidationResult</b>
 *
 * <p>Immutable outcome of the post-execution validation interceptor.</p>
 *
 * <p>Encapsulates the verdict (PASS / RETRY / FAIL), the issues that drove
 * the verdict, non-fatal warnings, and diagnostic metadata. It carries no
 * validation logic — it is a pure value object.</p>
 *
 * <p><b>Ownership:</b> Runtime Kernel — Validation Interceptor</p>
 */
public final class ValidationResult {

    /**
     * The validation verdict.
     */
    public enum Outcome {
        /** Output is acceptable — continue graph execution. */
        PASS,
        /** Output is questionable — retry the node (subject to retry policy). */
        RETRY,
        /** Output is unacceptable — stop the graph. */
        FAIL
    }

    private final Outcome outcome;
    private final List<String> issues;
    private final List<String> warnings;
    private final Map<String, Object> metadata;

    private ValidationResult(
            Outcome outcome,
            List<String> issues,
            List<String> warnings,
            Map<String, Object> metadata) {
        Objects.requireNonNull(outcome, "ValidationResult outcome must not be null");
        this.outcome = outcome;
        this.issues = Collections.unmodifiableList(new ArrayList<>(issues));
        this.warnings = Collections.unmodifiableList(new ArrayList<>(warnings));
        this.metadata = Collections.unmodifiableMap(new LinkedHashMap<>(metadata));
    }

    /**
     * Creates a {@link Outcome#PASS} result with no issues or warnings.
     *
     * @return a passing result
     */
    public static ValidationResult pass() {
        return new ValidationResult(Outcome.PASS, List.of(), List.of(), Map.of());
    }

    /**
     * Creates a {@link Outcome#PASS} result carrying informational warnings.
     *
     * @param warnings the warnings (must not be null)
     * @return a passing result with warnings
     */
    public static ValidationResult passWithWarnings(List<String> warnings) {
        return new ValidationResult(Outcome.PASS, List.of(),
                warnings != null ? warnings : List.of(), Map.of());
    }

    /**
     * Creates a {@link Outcome#RETRY} result with the given reasons.
     *
     * @param reasons why the node should be retried
     * @return a retry result
     */
    public static ValidationResult retry(String... reasons) {
        List<String> list = new ArrayList<>();
        for (String r : reasons) {
            if (r != null && !r.isBlank()) {
                list.add(r);
            }
        }
        Map<String, Object> md = Map.of("validator", "DefaultValidationInterceptor");
        return new ValidationResult(Outcome.RETRY, list, List.of(), md);
    }

    /**
     * Creates a {@link Outcome#FAIL} result with the given reasons.
     *
     * @param reasons why validation failed
     * @return a failing result
     */
    public static ValidationResult fail(String... reasons) {
        List<String> list = new ArrayList<>();
        for (String r : reasons) {
            if (r != null && !r.isBlank()) {
                list.add(r);
            }
        }
        Map<String, Object> md = Map.of("validator", "DefaultValidationInterceptor");
        return new ValidationResult(Outcome.FAIL, list, List.of(), md);
    }

    /**
     * Returns the validation verdict.
     *
     * @return the outcome (never null)
     */
    public Outcome outcome() {
        return outcome;
    }

    /**
     * Returns an unmodifiable list of validation issues that drove a
     * non-PASS verdict.
     *
     * @return the issues (never null)
     */
    public List<String> issues() {
        return issues;
    }

    /**
     * Returns an unmodifiable list of non-fatal warnings.
     *
     * @return the warnings (never null)
     */
    public List<String> warnings() {
        return warnings;
    }

    /**
     * Returns an unmodifiable view of diagnostic metadata.
     *
     * @return the metadata (never null)
     */
    public Map<String, Object> metadata() {
        return metadata;
    }

    /**
     * Shortcut for {@code outcome() == Outcome.PASS}.
     *
     * @return true if the result is PASS
     */
    public boolean isPass() {
        return outcome == Outcome.PASS;
    }

    /**
     * Shortcut for {@code outcome() == Outcome.RETRY}.
     *
     * @return true if the result is RETRY
     */
    public boolean isRetry() {
        return outcome == Outcome.RETRY;
    }

    /**
     * Shortcut for {@code outcome() == Outcome.FAIL}.
     *
     * @return true if the result is FAIL
     */
    public boolean isFail() {
        return outcome == Outcome.FAIL;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ValidationResult that = (ValidationResult) obj;
        return outcome == that.outcome
                && Objects.equals(issues, that.issues)
                && Objects.equals(warnings, that.warnings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(outcome, issues, warnings);
    }

    @Override
    public String toString() {
        return "ValidationResult{outcome=" + outcome
                + ", issues=" + issues.size()
                + ", warnings=" + warnings.size() + '}';
    }
}
