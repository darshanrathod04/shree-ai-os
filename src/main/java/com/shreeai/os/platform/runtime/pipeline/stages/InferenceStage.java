package com.shreeai.os.platform.runtime.pipeline.stages;

import com.shreeai.os.platform.kernels.cognitive.model.ReasoningResult;
import com.shreeai.os.platform.kernels.inference.engine.DefaultInferenceEngine;
import com.shreeai.os.platform.kernels.inference.model.InferenceResult;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeNode;
import com.shreeai.os.platform.kernels.memory.model.Memory;
import com.shreeai.os.platform.runtime.pipeline.ExecutionChain;
import com.shreeai.os.platform.runtime.pipeline.ExecutionStage;
import com.shreeai.os.platform.runtime.pipeline.PipelineContext;
import com.shreeai.os.platform.runtime.pipeline.PipelineExecutionState;
import com.shreeai.os.platform.runtime.pipeline.PipelineResult;
import com.shreeai.os.platform.runtime.pipeline.PipelineStageDescriptor;

import java.util.ArrayList;
import java.util.List;

/**
 * InferenceStage - Performs real inference and hypothesis generation.
 *
 * <p>This stage consumes the authoritative {@link ReasoningResult} produced by
 * the reasoning stage and converts it into an {@link InferenceResult} while
 * preserving the reasoning provenance required by downstream stages.</p>
 *
 * <p>Architectural responsibility:</p>
 * <ul>
 *   <li>Consume the authoritative reasoning result.</li>
 *   <li>Generate and rank hypotheses.</li>
 *   <li>Preserve reasoning provenance.</li>
 *   <li>Preserve supporting and contradicting evidence.</li>
 *   <li>Identify unknown information.</li>
 *   <li>Recommend the next investigation.</li>
 * </ul>
 *
 * <p>The stage must not silently discard information produced by the
 * reasoning kernel.</p>
 *
 * @author Shree AI OS Team
 * @version 2.0
 */
public final class InferenceStage implements ExecutionStage {

    private static final PipelineStageDescriptor DESCRIPTOR =
            PipelineStageDescriptor.builder()
                    .stageName("Inference")
                    .priority(6)
                    .enabled(true)
                    .version("2.0")
                    .description(
                            "Performs inference and hypothesis generation while preserving reasoning provenance"
                    )
                    .build();

    private static final String REASONING_RESULT_KEY = "reasoningResult";
    private static final String REASONING_CONCLUSION_KEY = "reasoningConclusion";
    private static final String REASONING_CONFIDENCE_KEY = "reasoningConfidence";
    private static final String SUPPORTING_EVIDENCE_KEY = "supportingEvidence";

    private final DefaultInferenceEngine inferenceEngine;

    /**
     * Creates a new inference stage.
     *
     * @param inferenceEngine inference engine
     */
    public InferenceStage(DefaultInferenceEngine inferenceEngine) {
        if (inferenceEngine == null) {
            throw new IllegalArgumentException(
                    "InferenceStage requires a non-null inferenceEngine"
            );
        }

        this.inferenceEngine = inferenceEngine;
    }

    /**
     * Default constructor.
     *
     * <p>Creates the canonical production inference engine.</p>
     */
    public InferenceStage() {
        this(new DefaultInferenceEngine());
    }

    @Override
    public PipelineResult process(
            PipelineContext context,
            ExecutionChain chain,
            PipelineExecutionState state) {

        try {
            if (context == null) {
                return failure("Inference stage requires a non-null PipelineContext");
            }

            if (state == null) {
                return failure("Inference stage requires a non-null PipelineExecutionState");
            }

            if (chain == null) {
                return failure("Inference stage requires a non-null ExecutionChain");
            }

            /*
             * -------------------------------------------------------------
             * 1. Extract request information
             * -------------------------------------------------------------
             */
            String requestText = "";

            if (context.getExecutionRequest() != null
                    && context.getExecutionRequest().getUserInput() != null) {

                requestText = context.getExecutionRequest()
                        .getUserInput();
            }

            String requestId = "unknown";

            if (context.getExecutionRequest() != null
                    && context.getExecutionRequest().getRequestId() != null) {

                requestId = context.getExecutionRequest()
                        .getRequestId();
            }

            /*
             * -------------------------------------------------------------
             * 2. Retrieve ranked memory evidence
             * -------------------------------------------------------------
             */
            List<Memory> rankedMemories = readListMetadata(
                    state,
                    "rankedMemories",
                    Memory.class
            );

            /*
             * -------------------------------------------------------------
             * 3. Retrieve ranked knowledge evidence
             * -------------------------------------------------------------
             */
            List<KnowledgeNode> rankedKnowledge = readListMetadata(
                    state,
                    "rankedKnowledge",
                    KnowledgeNode.class
            );

            /*
             * -------------------------------------------------------------
             * 4. Retrieve the authoritative ReasoningResult
             * -------------------------------------------------------------
             *
             * ReasoningResult is the canonical source of reasoning
             * conclusion, confidence, risks, alternatives and evidence.
             *
             * The stage first consumes the complete object preserved by
             * ReasoningStage.
             */
            ReasoningResult reasoningResult =
                    readReasoningResult(state);

            /*
             * -------------------------------------------------------------
             * 5. Backward-compatible reconstruction
             * -------------------------------------------------------------
             *
             * Older pipeline paths may only contain decomposed reasoning
             * metadata. Preserve compatibility without allowing the
             * inference stage to operate without a reasoning object.
             */
            if (reasoningResult == null) {
                reasoningResult = reconstructReasoningResult(state);
            }

            /*
             * -------------------------------------------------------------
             * 6. Execute inference
             * -------------------------------------------------------------
             */
            InferenceResult result = inferenceEngine.infer(
                    requestText,
                    reasoningResult,
                    rankedMemories,
                    rankedKnowledge,
                    "request-" + requestId
            );

            if (result == null) {
                return failure("Inference engine returned a null result");
            }

            /*
             * -------------------------------------------------------------
             * 7. Preserve supporting evidence
             * -------------------------------------------------------------
             *
             * The inference engine is expected to carry forward the
             * reasoning conclusion. However, the runtime boundary must
             * protect against accidental information loss.
             *
             * If the inference result contains no supporting evidence,
             * recover the authoritative reasoning conclusion.
             *
             * This is NOT fabricated evidence:
             *
             * ReasoningResult.conclusion()
             * is an actual upstream cognitive result.
             */
            List<String> supportingEvidence =
                    preserveSupportingEvidence(result, reasoningResult);

            /*
             * -------------------------------------------------------------
             * 8. Store complete inference state
             * -------------------------------------------------------------
             */
            state.addMetadata(
                    "inferenceId",
                    result.inferenceId()
            );

            state.addMetadata(
                    "hypotheses",
                    result.hypotheses()
            );

            state.addMetadata(
                    "bestHypothesis",
                    result.bestHypothesis().description()
            );

            state.addMetadata(
                    "inferenceConfidence",
                    result.confidence()
            );

            state.addMetadata(
                    SUPPORTING_EVIDENCE_KEY,
                    supportingEvidence
            );

            state.addMetadata(
                    "contradictingEvidence",
                    safeList(result.contradictingEvidence())
            );

            state.addMetadata(
                    "unknowns",
                    safeList(result.unknownInformation())
            );

            state.addMetadata(
                    "nextInvestigation",
                    result.recommendedNextInvestigation()
            );

            /*
             * Preserve the complete reasoning object for downstream
             * planning/reflection/verification stages.
             */
            state.addMetadata(
                    REASONING_RESULT_KEY,
                    reasoningResult
            );

            state.addMetadata(
                    REASONING_CONCLUSION_KEY,
                    reasoningResult.conclusion()
            );

            state.addMetadata(
                    REASONING_CONFIDENCE_KEY,
                    reasoningResult.confidence()
            );

            state.addMetadata(
                    "inferenceCompleted",
                    true
            );

            /*
             * Explicit provenance marker.
             *
             * This allows future intelligence layers to distinguish
             * evidence inherited from reasoning from evidence introduced
             * by inference itself.
             */
            state.addMetadata(
                    "inferenceEvidenceProvenance",
                    "REASONING_RESULT_PRESERVED"
            );

            state.addMessage(
                    "Inference completed: "
                            + result.bestHypothesis().description()
            );

            /*
             * -------------------------------------------------------------
             * 9. Continue canonical pipeline
             * -------------------------------------------------------------
             */
            return chain.next(context, state);

        } catch (Exception e) {

            state.markFailure(
                    "Inference failed: "
                            + safeMessage(e)
            );

            return PipelineResult.builder()
                    .success(false)
                    .status("INFERENCE_FAILED")
                    .addMessage(
                            "Inference stage failed: "
                                    + safeMessage(e)
                    )
                    .build();
        }
    }

    /**
     * Returns the authoritative reasoning result from pipeline state.
     */
    private ReasoningResult readReasoningResult(
            PipelineExecutionState state) {

        Object value = state.getMetadata()
                .get(REASONING_RESULT_KEY);

        if (value instanceof ReasoningResult reasoningResult) {
            return reasoningResult;
        }

        return null;
    }

    /**
     * Reconstructs a ReasoningResult for compatibility with older pipeline
     * paths that only stored decomposed reasoning metadata.
     */
    @SuppressWarnings("unchecked")
    private ReasoningResult reconstructReasoningResult(
            PipelineExecutionState state) {

        String reasoningConclusion =
                (String) state.getMetadata()
                        .get(REASONING_CONCLUSION_KEY);

        Double reasoningConfidence =
                readDouble(
                        state.getMetadata()
                                .get(REASONING_CONFIDENCE_KEY)
                );

        if (reasoningConfidence == null) {
            reasoningConfidence = 0.0;
        }

        List<String> reasoningFindings =
                readStringList(
                        state.getMetadata()
                                .get("reasoningFindings")
                );

        List<String> reasoningEvidence =
                readStringList(
                        state.getMetadata()
                                .get("reasoningEvidence")
                );

        List<String> reasoningRisks =
                readStringList(
                        state.getMetadata()
                                .get("reasoningRisk")
                );

        List<String> reasoningAlternatives =
                readStringList(
                        state.getMetadata()
                                .get("reasoningAlternatives")
                );

        Object reasoningStepsValue =
                state.getMetadata()
                        .get("reasoningSteps");

        int reasoningSteps =
                reasoningStepsValue instanceof Integer steps
                        ? steps
                        : 0;

        String reasoningId =
                (String) state.getMetadata()
                        .get("reasoningId");

        String reasoningSummary =
                (String) state.getMetadata()
                        .getOrDefault(
                                "reasoningSummary",
                                "Reasoning summary"
                        );

        String reasoningScope =
                (String) state.getMetadata()
                        .getOrDefault(
                                "reasoningScope",
                                "general"
                        );

        String reasoningType =
                (String) state.getMetadata()
                        .getOrDefault(
                                "reasoningType",
                                "EVIDENCE_BASED_REASONING"
                        );

        return new ReasoningResult(
                reasoningId,
                reasoningSummary,
                reasoningFindings,
                reasoningEvidence,
                reasoningConclusion != null
                        ? reasoningConclusion
                        : "No conclusion",
                reasoningConfidence,
                reasoningRisks,
                reasoningAlternatives,
                reasoningScope,
                reasoningType,
                reasoningSteps,
                java.util.Map.of(),
                java.time.Instant.now()
        );
    }

    /**
     * Preserves inference evidence while guaranteeing that an actual
     * reasoning conclusion is not lost at the runtime boundary.
     */
    private List<String> preserveSupportingEvidence(
            InferenceResult result,
            ReasoningResult reasoningResult) {

        List<String> existing =
                safeList(result.supportingEvidence());

        if (!existing.isEmpty()) {
            return existing;
        }

        String conclusion =
                reasoningResult != null
                        ? reasoningResult.conclusion()
                        : null;

        if (conclusion == null || conclusion.isBlank()) {
            return List.of();
        }

        List<String> recovered =
                new ArrayList<>(1);

        recovered.add(
                "Reasoning conclusion: "
                        + conclusion
        );

        return List.copyOf(recovered);
    }

    /**
     * Reads a typed list from pipeline metadata.
     */
    private <T> List<T> readListMetadata(
            PipelineExecutionState state,
            String key,
            Class<T> elementType) {

        Object value =
                state.getMetadata().get(key);

        if (!(value instanceof List<?> rawList)) {
            return List.of();
        }

        List<T> result =
                new ArrayList<>(rawList.size());

        for (Object item : rawList) {
            if (item != null && elementType.isInstance(item)) {
                result.add(elementType.cast(item));
            }
        }

        return List.copyOf(result);
    }

    /**
     * Reads a string list safely.
     */
    private List<String> readStringList(Object value) {

        if (!(value instanceof List<?> rawList)) {
            return List.of();
        }

        List<String> result =
                new ArrayList<>(rawList.size());

        for (Object item : rawList) {
            if (item instanceof String text) {
                result.add(text);
            }
        }

        return List.copyOf(result);
    }

    /**
     * Returns an immutable safe list.
     */
    private <T> List<T> safeList(List<T> value) {

        if (value == null || value.isEmpty()) {
            return List.of();
        }

        return List.copyOf(value);
    }

    /**
     * Safely reads a numeric confidence value.
     */
    private Double readDouble(Object value) {

        if (value instanceof Number number) {
            return number.doubleValue();
        }

        return null;
    }

    /**
     * Creates a standardized pipeline failure.
     */
    private PipelineResult failure(String message) {

        return PipelineResult.builder()
                .success(false)
                .status("INFERENCE_FAILED")
                .addMessage(message)
                .build();
    }

    /**
     * Extracts a safe exception message.
     */
    private String safeMessage(Exception exception) {

        if (exception == null) {
            return "Unknown inference error";
        }

        String message = exception.getMessage();

        if (message == null || message.isBlank()) {
            return exception.getClass().getSimpleName();
        }

        return message;
    }

    @Override
    public PipelineStageDescriptor getDescriptor() {
        return DESCRIPTOR;
    }
}