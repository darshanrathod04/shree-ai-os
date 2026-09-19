package com.shreeai.os.platform.runtime.interceptor;

import com.shreeai.os.platform.graph.NodeType;
import com.shreeai.os.platform.kernels.chief.model.DecisionContext;
import com.shreeai.os.platform.kernels.chief.model.DecisionResult;
import com.shreeai.os.platform.kernels.chief.validation.ChiefValidationResult;
import com.shreeai.os.platform.kernels.chief.validation.DecisionValidator;
import com.shreeai.os.platform.runtime.graph.ExecutionNodeResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <b>DefaultValidationInterceptor</b>
 *
 * <p>Deterministic, post-execution quality-verification interceptor.</p>
 *
 * <p>After every node execution it evaluates the produced
 * {@link ExecutionNodeResult} against a fixed set of quality checks.</p>
 *
 * <p>This interceptor never generates new answers and never calls an LLM.</p>
 *
 * <p><b>Ownership:</b> Runtime Kernel — Validation Interceptor</p>
 */
public final class DefaultValidationInterceptor implements ValidationInterceptor {

    private static final double MIN_CONFIDENCE_THRESHOLD = 0.05;

    private static final List<String> ERROR_MARKERS = List.of(
            "error:", "exception:", "failed to", "unable to process", "traceback");

    private static final List<String> MISSING_INFO_MARKERS = List.of(
            "insufficient information", "insufficient data", "not enough context",
            "cannot determine", "unable to determine", "no information available",
            "missing required");

    private static final List<String> INCOMPLETE_MARKERS = List.of(
            "partial result", "incomplete", "could not fully", "only partial");

    @Override
    public ValidationResult validate(ValidationContext context) {
        ExecutionNodeResult result = context.result();

        if (!result.isSuccess()) {
            return ValidationResult.pass();
        }

        List<String> issues = new ArrayList<>();

        // 1. Output exists
        String output = result.output();
        if (output == null || output.isBlank()) {
            issues.add("Output exists: completed node produced no output");
            return ValidationResult.fail(issues.toArray(new String[0]));
        }
        String normalized = output.toLowerCase();

        // 2. Correctness
        for (String marker : ERROR_MARKERS) {
            if (normalized.contains(marker)) {
                issues.add("Correctness: output contains error marker '" + marker + "'");
                break;
            }
        }

        // 3. Missing information
        for (String marker : MISSING_INFO_MARKERS) {
            if (normalized.contains(marker)) {
                issues.add("Missing information: output indicates '" + marker + "'");
                break;
            }
        }

        // 4. Goal completion
        for (String marker : INCOMPLETE_MARKERS) {
            if (normalized.contains(marker)) {
                issues.add("Goal completion: output indicates '" + marker + "'");
                break;
            }
        }

        // 5. Confidence
        // A completed node reporting very low confidence is suspicious
        // regardless of node type.
        if (result.confidence() < MIN_CONFIDENCE_THRESHOLD) {
            issues.add(String.format(
                    "Confidence: reported confidence %.2f is below threshold %.2f",
                    result.confidence(), MIN_CONFIDENCE_THRESHOLD));
        }

        // 6. Relevance
        String relevanceIssue = checkRelevance(context, normalized);
        if (relevanceIssue != null) {
            issues.add(relevanceIssue);
        }

        // 7. Contradictions
        String contradictionIssue = checkContradictions(context, normalized);
        if (contradictionIssue != null) {
            issues.add(contradictionIssue);
        }

        // 8. Domain-object structural validation (reuses DecisionValidator)
        validateDomainObjects(result.metadata(), issues);

        // Verdict
        if (!issues.isEmpty()) {
            if (context.attempt() >= 2) {
                return ValidationResult.fail(issues.toArray(new String[0]));
            }
            return ValidationResult.retry(issues.toArray(new String[0]));
        }
        return ValidationResult.pass();
    }

    /**
     * Checks whether the output is relevant to the request payload.
     *
     * <p>A simple heuristic: when the request carries a non-blank payload,
     * the output should contain at least one significant token from the
     * payload. Very short payloads (&le; 2 chars) are skipped to avoid
     * false positives. Structural nodes (ROOT/IDENTITY, CONTEXTUAL,
     * RETRIEVAL) are exempt because their output is structural, not a
     * user-facing answer.</p>
     */
    private String checkRelevance(ValidationContext context, String normalized) {
        // Only check relevance for node types that produce user-facing
        // natural-language answers. Structural nodes (plans, actions,
        // metadata) are exempt.
        if (!isAnswerProducing(context.node().nodeType())) {
            return null;
        }
        return context.request().map(req -> {
            String payload = req.payload();
            if (payload == null || payload.isBlank() || payload.trim().length() <= 2) {
                return null;
            }
            String[] tokens = payload.toLowerCase().split("\\W+");
            for (String token : tokens) {
                if (token.length() > 2 && normalized.contains(token)) {
                    return null;
                }
            }
            return "Relevance: output does not appear to address request payload";
        }).orElse(null);
    }

    /**
     * Returns true when the given node type is expected to produce a
     * user-facing natural-language answer (as opposed to structured
     * metadata like plans, actions, or retrieval results).
     */
    private boolean isAnswerProducing(NodeType nodeType) {
        return nodeType == NodeType.MODEL
                || nodeType == NodeType.COLLABORATIVE;
    }

    /**
     * Checks whether the output contradicts upstream node outputs.
     */
    private String checkContradictions(ValidationContext context, String normalized) {
        if (context.executionContext().isEmpty()) {
            return null;
        }
        List<String> contradictionMarkers = List.of(
                "contradicts", "contrary to", "inconsistent with",
                "conflicts with", "opposes");
        for (String marker : contradictionMarkers) {
            if (normalized.contains(marker)) {
                return "Contradictions: output " + marker + " prior node output";
            }
        }
        return null;
    }

    /**
     * When result metadata carries a DecisionContext or DecisionResult,
     * delegates structural validation to DecisionValidator.
     */
    private void validateDomainObjects(Map<String, Object> metadata, List<String> issues) {
        if (metadata == null || metadata.isEmpty()) {
            return;
        }
        Object dc = metadata.get("decisionContext");
        if (dc instanceof DecisionContext decisionContext) {
            ChiefValidationResult r = DecisionValidator.validate(decisionContext);
            for (String issue : r.issues()) {
                issues.add("DecisionContext: " + issue);
            }
        }
        Object dr = metadata.get("decisionResult");
        if (dr instanceof DecisionResult decisionResult) {
            ChiefValidationResult r = DecisionValidator.validate(decisionResult);
            for (String issue : r.issues()) {
                issues.add("DecisionResult: " + issue);
            }
        }
    }
}
