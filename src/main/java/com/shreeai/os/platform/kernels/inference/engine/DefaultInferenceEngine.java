package com.shreeai.os.platform.kernels.inference.engine;

import com.shreeai.os.platform.kernels.cognitive.model.ReasoningResult;
import com.shreeai.os.platform.kernels.inference.model.Hypothesis;
import com.shreeai.os.platform.kernels.inference.model.InferenceResult;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeNode;
import com.shreeai.os.platform.kernels.memory.model.Memory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * <b>DefaultInferenceEngine</b>
 *
 * <p>Production-grade inference engine that generates hypotheses from evidence.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Consumes ReasoningResult, Memory, and Knowledge</li>
 *   <li>Generates multiple hypotheses</li>
 *   <li>Ranks hypotheses by likelihood</li>
 *   <li>Identifies unknown information</li>
 *   <li>Recommends next investigation</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Inference Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 */
public final class DefaultInferenceEngine {

    /**
     * Performs inference using reasoning, memory, and knowledge.
     *
     * @param request the user request
     * @param reasoningResult the reasoning result
     * @param memories the recalled memories
     * @param knowledgeNodes the retrieved knowledge
     * @param context the pipeline context
     * @return InferenceResult with hypotheses
     */
    public InferenceResult infer(String request, ReasoningResult reasoningResult,
                                  List<Memory> memories, List<KnowledgeNode> knowledgeNodes,
                                  String context) {
        String inferenceId = "inf-" + UUID.randomUUID().toString().substring(0, 8);
        List<Hypothesis> hypotheses = new ArrayList<>();
        List<String> supportingEvidence = new ArrayList<>();
        List<String> contradictingEvidence = new ArrayList<>();
        List<String> unknownInformation = new ArrayList<>();

        // Step 1: Collect reasoning conclusion as evidence
        supportingEvidence.add("Reasoning conclusion: " + reasoningResult.conclusion());
        if (reasoningResult.confidence() > 0.5) {
            supportingEvidence.add("Reasoning confidence: " + String.format("%.2f", reasoningResult.confidence()));
        } else {
            contradictingEvidence.add("Low reasoning confidence: " + String.format("%.2f", reasoningResult.confidence()));
        }

        // Step 2: Gather memory evidence
        if (memories != null && !memories.isEmpty()) {
            for (int i = 0; i < Math.min(memories.size(), 3); i++) {
                Memory m = memories.get(i);
                supportingEvidence.add("Memory: " + truncate(m.content().text(), 100));
                double importance = m.metadata().importance();
                if (importance < 0.3) {
                    contradictingEvidence.add("Low-importance memory evidence");
                }
            }
        } else {
            unknownInformation.add("No relevant memories available");
        }

        // Step 3: Gather knowledge evidence
        if (knowledgeNodes != null && !knowledgeNodes.isEmpty()) {
            for (int i = 0; i < Math.min(knowledgeNodes.size(), 3); i++) {
                KnowledgeNode n = knowledgeNodes.get(i);
                supportingEvidence.add("Knowledge: " + n.getLabel() + " - " + truncate(n.getDescription() != null ? n.getDescription() : "", 100));
            }
        } else {
            unknownInformation.add("No relevant knowledge available");
        }

        // Step 4: Generate hypotheses
        String topic = extractTopic(request);
        hypotheses.addAll(generateHypotheses(request, reasoningResult, memories, knowledgeNodes, topic));

        // Step 5: Reject impossible / rank
        List<Hypothesis> ranked = rankHypotheses(hypotheses);

        // Step 6: Find missing information
        unknownInformation.addAll(findUnknownInformation(request, memories, knowledgeNodes, topic));

        // Step 7: Suggest investigation
        String nextInvestigation = suggestInvestigation(ranked, unknownInformation, topic);

        // Select best hypothesis
        Hypothesis best = ranked.isEmpty()
                ? new Hypothesis("h0", "No clear hypothesis", 0.1, List.of(), List.of(), "UNLIKELY", 0)
                : ranked.get(0);

        double overallConfidence = best.confidence();

        return new InferenceResult(
                inferenceId,
                List.copyOf(ranked),
                best,
                overallConfidence,
                List.copyOf(supportingEvidence),
                List.copyOf(contradictingEvidence),
                List.copyOf(unknownInformation),
                nextInvestigation,
                context,
                Instant.now()
        );
    }

    private List<Hypothesis> generateHypotheses(String request, ReasoningResult reasoningResult,
                                                  List<Memory> memories, List<KnowledgeNode> knowledgeNodes,
                                                  String topic) {
        List<Hypothesis> hypotheses = new ArrayList<>();
        int hIndex = 0;

        // Hypothesis 1: Based on reasoning conclusion
        {
            double conf = 0.5 + (reasoningResult.confidence() * 0.3);
            List<String> supporting = new ArrayList<>();
            supporting.add("Reasoning conclusion: " + reasoningResult.conclusion());
            if (memories != null && !memories.isEmpty()) supporting.add("Supported by memory evidence");
            if (knowledgeNodes != null && !knowledgeNodes.isEmpty()) supporting.add("Supported by knowledge evidence");
            List<String> opposing = new ArrayList<>();
            if (reasoningResult.confidence() < 0.5) opposing.add("Low reasoning confidence");
            String status = conf > 0.65 ? "LIKELY" : conf > 0.4 ? "POSSIBLE" : "UNCERTAIN";
            hypotheses.add(new Hypothesis("h" + (hIndex++),
                    "Primary hypothesis: " + truncate(reasoningResult.conclusion(), 80),
                    Math.min(0.95, conf),
                    supporting, opposing, status, 1));
        }

        // Hypothesis 2: Alternative based on request topic
        if (topic != null && !topic.isEmpty()) {
            double conf = 0.3 + (reasoningResult.confidence() * 0.2);
            List<String> supporting = new ArrayList<>();
            supporting.add("Based on request topic: " + topic);
            if (knowledgeNodes != null && knowledgeNodes.size() > 1) {
                supporting.add("Alternative knowledge source: " + knowledgeNodes.get(1).getLabel());
                conf += 0.1;
            }
            List<String> opposing = new ArrayList<>();
            opposing.add("Alternative to primary conclusion");
            String status = conf > 0.5 ? "POSSIBLE" : "UNCERTAIN";
            hypotheses.add(new Hypothesis("h" + (hIndex++),
                    "Alternative: " + topic + " may have different context",
                    Math.min(0.85, conf),
                    supporting, opposing, status, 2));
        }

        // Hypothesis 3: Memory-driven hypothesis
        if (memories != null && !memories.isEmpty()) {
            double conf = 0.3 + (memories.get(0).metadata().importance() * 0.3);
            List<String> supporting = new ArrayList<>();
            supporting.add("Driven by top memory: " + truncate(memories.get(0).content().text(), 80));
            List<String> opposing = new ArrayList<>();
            if (reasoningResult.confidence() > 0.7) opposing.add("Conflicts with high-confidence reasoning");
            String status = conf > 0.5 ? "POSSIBLE" : "UNCERTAIN";
            hypotheses.add(new Hypothesis("h" + (hIndex++),
                    "Memory-based hypothesis: previous pattern may repeat",
                    Math.min(0.8, conf),
                    supporting, opposing, status, 3));
        }

        // Hypothesis 4: Knowledge-driven hypothesis
        if (knowledgeNodes != null && !knowledgeNodes.isEmpty()) {
            KnowledgeNode top = knowledgeNodes.get(0);
            double conf = 0.3 + (top.getMetadata() != null && top.getMetadata().containsKey("confidence")
                    ? ((Number) top.getMetadata().get("confidence")).doubleValue() * 0.3 : 0.15);
            List<String> supporting = new ArrayList<>();
            supporting.add("Driven by knowledge: " + top.getLabel());
            List<String> opposing = new ArrayList<>();
            opposing.add("Knowledge may not directly address the request");
            String status = conf > 0.5 ? "POSSIBLE" : "UNCERTAIN";
            hypotheses.add(new Hypothesis("h" + (hIndex++),
                    "Knowledge-driven hypothesis: " + top.getLabel() + " context",
                    Math.min(0.8, conf),
                    supporting, opposing, status, 4));
        }

        // Fallback
        if (hypotheses.isEmpty()) {
            hypotheses.add(new Hypothesis("h0", "Insufficient evidence to form hypothesis", 0.1,
                    List.of(), List.of("No evidence available"), "UNLIKELY", 0));
        }

        return hypotheses;
    }

    private List<Hypothesis> rankHypotheses(List<Hypothesis> hypotheses) {
        return hypotheses.stream()
                .sorted((a, b) -> {
                    int cmp = Double.compare(b.confidence(), a.confidence());
                    if (cmp == 0) cmp = Integer.compare(a.priority(), b.priority());
                    return cmp;
                })
                .toList();
    }

    private List<String> findUnknownInformation(String request, List<Memory> memories,
                                                  List<KnowledgeNode> knowledgeNodes, String topic) {
        List<String> unknowns = new ArrayList<>();
        if (memories == null || memories.isEmpty()) {
            unknowns.add("No historical context available");
        } else {
            unknowns.add("Additional memory recall may provide more context");
        }
        if (knowledgeNodes == null || knowledgeNodes.isEmpty()) {
            unknowns.add("No domain knowledge available");
        } else {
            unknowns.add("Additional knowledge sources may provide more detail");
        }
        if (topic != null) {
            unknowns.add("More information about '" + topic + "' may refine hypothesis");
        }
        return unknowns;
    }

    private String suggestInvestigation(List<Hypothesis> ranked, List<String> unknowns, String topic) {
        StringBuilder sb = new StringBuilder();
        if (!ranked.isEmpty()) {
            sb.append("Investigate top hypothesis: ").append(ranked.get(0).description()).append(". ");
        }
        if (!unknowns.isEmpty()) {
            sb.append("Address unknowns: ").append(String.join("; ", unknowns)).append(". ");
        }
        if (topic != null) {
            sb.append("Gather more information about '").append(topic).append("'.");
        } else {
            sb.append("Gather more information to refine hypotheses.");
        }
        return sb.toString();
    }

    private String extractTopic(String request) {
        if (request == null || request.isBlank()) return null;
        String cleaned = request.replace("What is", "").replace("what is", "")
                .replace("Explain", "").replace("explain", "")
                .replace("Tell me about", "").replace("tell me about", "")
                .replace("Define", "").replace("define", "")
                .replace("?", "").replace("!", "").replace(".", "").trim();
        return cleaned.isBlank() ? null : cleaned;
    }

    private String truncate(String text, int max) {
        if (text == null) return "";
        return text.length() <= max ? text : text.substring(0, max) + "...";
    }
}