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
                reasoningResult = reconstructReasoningResult();
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
             * 7. Store the inference artifact in the immutable cognitive
             *    state (P0.2). The reasoning artifact stays untouched —
             *    inference never rewrites another stage's output. The
             *    evidence lists remain part of the InferenceResult itself;
             *    they are no longer duplicated into the metadata map.
             * -------------------------------------------------------------
             */
            state.updateCognitiveState(cs -> cs.withInference(result));

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
     * Returns the authoritative reasoning result from the immutable
     * cognitive state (P0.2). The reasoning artifact is no longer mirrored
     * in the metadata map.
     */
    private ReasoningResult readReasoningResult(
            PipelineExecutionState state) {

        return state.getCognitiveState().reasoning();
    }

    /**
     * Reconstructs a fallback ReasoningResult for compatibility with
     * alternate chains that execute inference without a reasoning stage.
     *
     * <p>Values match the previous metadata-derived defaults exactly, so
     * the inference engine observes an identical fallback object.</p>
     */
    private ReasoningResult reconstructReasoningResult() {

        return new ReasoningResult(
                "inference-fallback",
                "Reasoning summary",
                List.of(),
                List.of(),
                "No conclusion",
                0.0,
                List.of(),
                List.of(),
                "general",
                "EVIDENCE_BASED_REASONING",
                0,
                java.util.Map.of(),
                java.time.Instant.now()
        );
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