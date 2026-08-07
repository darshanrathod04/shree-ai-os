package com.shreeai.os.platform.kernels.inference.model;

import com.shreeai.os.platform.kernels.cognitive.model.ReasoningResult;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeNode;
import com.shreeai.os.platform.kernels.memory.model.Memory;

import java.util.List;
import java.util.Objects;

/**
 * <b>InferenceRequest</b>
 *
 * <p>Represents an inference request containing all evidence for hypothesis generation.</p>
 *
 * <p><b>Ownership:</b> Inference Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 */
public record InferenceRequest(
        String request,
        ReasoningResult reasoningResult,
        List<Memory> memories,
        List<KnowledgeNode> knowledgeNodes,
        String context
) {
    public InferenceRequest {
        Objects.requireNonNull(request, "request must not be null");
        Objects.requireNonNull(reasoningResult, "reasoningResult must not be null");
        Objects.requireNonNull(memories, "memories must not be null");
        Objects.requireNonNull(knowledgeNodes, "knowledgeNodes must not be null");
        Objects.requireNonNull(context, "context must not be null");
        memories = List.copyOf(memories);
        knowledgeNodes = List.copyOf(knowledgeNodes);
    }
}