package com.shreeai.os.platform.kernels.cognitive.engine;

import java.util.Objects;

/**
 * <b>ReflectionInput</b>
 *
 * <p>Immutable input to a post-execution reflection: what was requested,
 * what the pipeline planned and executed, and how it concluded.</p>
 *
 * <p><b>Ownership:</b> Cognitive Kernel</p>
 *
 * @param requestId        the execution request id
 * @param requestText      the original user request text
 * @param planStepCount    number of planned steps (0 when unplanned)
 * @param actionStatus     status reported by the action execution stage
 * @param executionSuccess whether execution completed successfully
 * @param responseSummary  summary of the produced response / conclusion
 * @param confidence       confidence of the produced conclusion (0.0-1.0)
 */
public record ReflectionInput(
        String requestId,
        String requestText,
        int planStepCount,
        String actionStatus,
        boolean executionSuccess,
        String responseSummary,
        double confidence) {

    /**
     * Creates a ReflectionInput with normalisation and validation.
     *
     * @throws NullPointerException if requestId is null
     */
    public ReflectionInput {
        Objects.requireNonNull(requestId, "requestId must not be null");
        requestText = requestText == null ? "" : requestText;
        actionStatus = actionStatus == null ? "UNKNOWN" : actionStatus;
        responseSummary = responseSummary == null ? "" : responseSummary;
    }
}