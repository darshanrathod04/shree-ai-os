package com.shreeai.os.platform.runtime.interceptor;

import com.shreeai.os.platform.graph.ExecutionNode;
import com.shreeai.os.platform.graph.NodeType;
import com.shreeai.os.platform.kernels.chief.model.ChiefId;
import com.shreeai.os.platform.kernels.chief.model.DecisionContext;
import com.shreeai.os.platform.kernels.chief.model.DecisionResult;
import com.shreeai.os.platform.resolver.CapabilityType;
import com.shreeai.os.platform.runtime.execution.ExecutionRequest;
import com.shreeai.os.platform.runtime.graph.ExecutionNodeResult;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link DefaultValidationInterceptor}.
 */
class DefaultValidationInterceptorTest {

    private final DefaultValidationInterceptor interceptor =
            new DefaultValidationInterceptor();

    private ExecutionNode node() {
        return ExecutionNode.builder()
                .nodeId(CapabilityType.IDENTITY.name())
                .capability(CapabilityType.IDENTITY)
                .nodeType(NodeType.ROOT)
                .build();
    }

    private ExecutionNodeResult completed(String output, double confidence) {
        return ExecutionNodeResult.completed(
                CapabilityType.IDENTITY.name(),
                CapabilityType.IDENTITY,
                output, confidence, 0L);
    }

    private ExecutionNodeResult failedResult() {
        return ExecutionNodeResult.failed(
                CapabilityType.IDENTITY.name(),
                CapabilityType.IDENTITY,
                "some failure", 0L);
    }

    private ValidationContext context(ExecutionNodeResult result, String payload) {
        ExecutionRequest req = ExecutionRequest.builder()
                .requestId("test").payload(payload).build();
        return ValidationContext.of(node(), result, req, Map.of(), 1);
    }

    @Test
    void passOnGoodOutput() {
        ValidationResult r = interceptor.validate(
                context(completed("The capital of France is Paris", 0.9), "France"));
        assertTrue(r.isPass(), "good output should PASS");
    }

    @Test
    void failOnEmptyOutput() {
        ValidationResult r = interceptor.validate(
                context(completed("", 0.9), "test"));
        assertTrue(r.isFail(), "empty output should FAIL");
        assertTrue(r.issues().get(0).contains("no output"));
    }

    @Test
    void retryOnErrorMarker() {
        ValidationResult r = interceptor.validate(
                context(completed("error: something went wrong", 0.9), "test"));
        assertTrue(r.isRetry(), "error marker should RETRY on first attempt");
    }

    @Test
    void failOnFinalAttemptWithError() {
        ValidationResult r = interceptor.validate(
                ValidationContext.of(node(), completed("error: failed", 0.9),
                        ExecutionRequest.builder().requestId("t").payload("test").build(),
                        Map.of(), 2));
        assertTrue(r.isFail(), "error marker on attempt>=2 should FAIL");
    }

    @Test
    void retryOnMissingInfo() {
        ValidationResult r = interceptor.validate(
                context(completed("insufficient information to answer", 0.9), "test"));
        assertTrue(r.isRetry(), "missing-info marker should RETRY");
    }

    @Test
    void retryOnLowConfidence() {
        ValidationResult r = interceptor.validate(
                context(completed("some answer about Java", 0.0), "Java"));
        assertTrue(r.isRetry(), "low confidence should RETRY");
        assertTrue(r.issues().stream().anyMatch(i -> i.contains("confidence")));
    }

    private ExecutionNode modelNode() {
        return ExecutionNode.builder()
                .nodeId(CapabilityType.MODELS.name())
                .capability(CapabilityType.MODELS)
                .nodeType(NodeType.MODEL)
                .build();
    }

    @Test
    void retryOnIrrelevantOutput() {
        ExecutionRequest req = ExecutionRequest.builder()
                .requestId("t").payload("quantum physics").build();
        ValidationResult r = interceptor.validate(
                ValidationContext.of(modelNode(), completed(
                        "banana mango citrus fruits are healthy", 0.9),
                        req, Map.of(), 1));
        assertTrue(r.isRetry(), "irrelevant output should RETRY");
    }

    @Test
    void passOnFailedNodeResult() {
        ValidationResult r = interceptor.validate(
                context(failedResult(), "test"));
        assertTrue(r.isPass(), "non-completed result passes through");
    }

    @Test
    void retryOnContradiction() {
        ExecutionRequest req = ExecutionRequest.builder()
                .requestId("t").payload("test").build();
        Map<String, Object> ctx = Map.of("node:KNOWLEDGE", "Paris is the capital");
        ValidationResult r = interceptor.validate(
                ValidationContext.of(node(), completed(
                        "this contradicts the prior finding", 0.9),
                        req, ctx, 1));
        assertTrue(r.isRetry(), "contradiction should RETRY");
    }

    @Test
    void validateDecisionContextInMetadata() {
        DecisionContext dc = new DecisionContext(
                new ChiefId("chief-1"), "coordinate",
                List.of("kernel-a"), "global",
                Map.of("key", "value"), Map.of());
        ExecutionNodeResult base = completed("answer about Java", 0.9);
        ExecutionNodeResult withMeta = base.withMetadata(Map.of("decisionContext", dc));
        ExecutionRequest req = ExecutionRequest.builder()
                .requestId("t").payload("Java").build();
        ValidationResult r = interceptor.validate(
                ValidationContext.of(node(), withMeta, req, Map.of(), 1));
        assertNotNull(r);
    }

    @Test
    void validateDecisionResultInMetadata() {
        DecisionResult dr = new DecisionResult(
                new ChiefId("chief-1"), true, "path-a",
                List.of("k1"), Instant.now(), Map.of());
        ExecutionNodeResult base = completed("answer", 0.9);
        ExecutionNodeResult withMeta = base.withMetadata(Map.of("decisionResult", dr));
        ExecutionRequest req = ExecutionRequest.builder()
                .requestId("t").payload("test").build();
        ValidationResult r = interceptor.validate(
                ValidationContext.of(node(), withMeta, req, Map.of(), 1));
        assertNotNull(r);
    }
}
