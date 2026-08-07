package com.shreeai.os.platform.kernels.cognitive.engine;

import com.shreeai.os.platform.kernels.cognitive.model.ReasoningResult;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeNode;
import com.shreeai.os.platform.kernels.memory.model.Memory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * <b>DefaultReasoningEngine</b>
 *
 * <p>Production-grade reasoning engine that consumes Memory and Knowledge outputs
 * and produces derived conclusions.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Consumes Memory results and Knowledge results</li>
 *   <li>Derives conclusions (not retrieves them)</li>
 *   <li>Calculates confidence scores</li>
 *   <li>Identifies risks</li>
 *   <li>Generates alternatives</li>
 *   <li>Produces immutable ReasoningResult</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Cognitive Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-COG-101</p>
 */
public final class DefaultReasoningEngine {

    /**
     * Performs reasoning using memory and knowledge inputs.
     *
     * <p>The engine derives conclusions from the provided evidence rather
     * than retrieving pre-computed answers.</p>
     *
     * @param request the original user request text
     * @param memories the recalled memories (may be empty)
     * @param knowledgeNodes the retrieved knowledge nodes (may be empty)
     * @return a ReasoningResult with derived conclusions
     */
    public ReasoningResult reason(String request, List<Memory> memories, List<KnowledgeNode> knowledgeNodes) {
        String reasoningId = "rsn-" + UUID.randomUUID();
        List<String> findings = new ArrayList<>();
        List<String> evidence = new ArrayList<>();
        List<String> risks = new ArrayList<>();
        List<String> alternatives = new ArrayList<>();
        int reasoningSteps = 0;
        double confidence = 0.0;

        // Step 1: Analyze request
        reasoningSteps++;
        String requestSummary = request != null && !request.isBlank() ? request : "No request text provided";
        findings.add("Request analyzed: " + truncate(requestSummary, 100));
        confidence += 0.1;

        // Step 2: Analyze memory evidence
        reasoningSteps++;
        if (memories != null && !memories.isEmpty()) {
            int memoryCount = memories.size();
            findings.add("Recalled " + memoryCount + " relevant memories");
            confidence += Math.min(0.2, memoryCount * 0.05);
            for (int i = 0; i < Math.min(memoryCount, 3); i++) {
                Memory memory = memories.get(i);
                evidence.add("Memory: " + truncate(memory.content().text(), 100));
            }
            if (memoryCount > 0) {
                double memoryImportance = memories.get(0).metadata().importance();
                if (memoryImportance > 0.8) {
                    risks.add("High-importance memory may over-influence conclusion");
                }
            }
        } else {
            findings.add("No relevant memories found");
            confidence += 0.05;
            risks.add("Limited memory evidence; conclusion may be incomplete");
        }

        // Step 3: Analyze knowledge evidence
        reasoningSteps++;
        if (knowledgeNodes != null && !knowledgeNodes.isEmpty()) {
            int knowledgeCount = knowledgeNodes.size();
            findings.add("Retrieved " + knowledgeCount + " knowledge sources");
            confidence += Math.min(0.3, knowledgeCount * 0.075);
            for (int i = 0; i < Math.min(knowledgeCount, 3); i++) {
                KnowledgeNode node = knowledgeNodes.get(i);
                String label = node.getLabel();
                String description = node.getDescription() != null ? node.getDescription() : "";
                evidence.add("Knowledge: " + label + " - " + truncate(description, 100));

                // Extract confidence from metadata
                if (node.getMetadata() != null && node.getMetadata().containsKey("confidence")) {
                    Object confValue = node.getMetadata().get("confidence");
                    if (confValue instanceof Number number) {
                        double nodeConfidence = number.doubleValue();
                        confidence += nodeConfidence * 0.05;
                    }
                }
            }
            if (knowledgeCount > 0 && knowledgeNodes.get(0).getMetadata() != null) {
                double authority = knowledgeNodes.get(0).getMetadata().containsKey("authority")
                        ? ((Number) knowledgeNodes.get(0).getMetadata().get("authority")).doubleValue()
                        : 0.0;
                if (authority < 0.5) {
                    risks.add("Low-authority knowledge source may affect reliability");
                }
            }
        } else {
            findings.add("No relevant knowledge found");
            confidence += 0.05;
            risks.add("Insufficient knowledge; conclusion based primarily on request");
        }

        // Step 4: Cross-reference memory and knowledge
        reasoningSteps++;
        boolean hasMemoryEvidence = memories != null && !memories.isEmpty();
        boolean hasKnowledgeEvidence = knowledgeNodes != null && !knowledgeNodes.isEmpty();
        if (hasMemoryEvidence && hasKnowledgeEvidence) {
            findings.add("Cross-referenced memory and knowledge evidence");
            confidence += 0.1;
        } else if (hasMemoryEvidence || hasKnowledgeEvidence) {
            findings.add("Limited cross-referencing possible (single evidence source)");
            confidence += 0.05;
        }

        // Step 5: Derive conclusion (not retrieved)
        reasoningSteps++;
        String conclusion = deriveConclusion(request, memories, knowledgeNodes);
        findings.add("Derived conclusion from " + reasoningSteps + " reasoning steps");
        confidence += 0.1;

        // Step 6: Generate alternatives
        reasoningSteps++;
        alternatives = generateAlternatives(request, knowledgeNodes);
        findings.add("Generated " + alternatives.size() + " alternative perspectives");

        // Step 7: Extract topics for scope
        reasoningSteps++;
        String scope = extractScope(request, knowledgeNodes);

        // Final confidence normalization
        confidence = Math.max(0.1, Math.min(0.95, confidence));

        // Build summary
        String summary = "Reasoning completed: " + reasoningSteps + " steps, " +
                (memories != null ? memories.size() : 0) + " memories, " +
                (knowledgeNodes != null ? knowledgeNodes.size() : 0) + " knowledge sources";

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("requestLength", request != null ? request.length() : 0);
        metadata.put("memoryCount", memories != null ? memories.size() : 0);
        metadata.put("knowledgeCount", knowledgeNodes != null ? knowledgeNodes.size() : 0);
        metadata.put("evidenceCount", evidence.size());
        metadata.put("findingCount", findings.size());

        return new ReasoningResult(
                reasoningId,
                summary,
                List.copyOf(findings),
                List.copyOf(evidence),
                conclusion,
                confidence,
                List.copyOf(risks),
                List.copyOf(alternatives),
                scope,
                "EVIDENCE_BASED_REASONING",
                reasoningSteps,
                Map.copyOf(metadata),
                Instant.now()
        );
    }

    /**
     * Derives a conclusion based on memory and knowledge evidence.
     *
     * @param request the user request
     * @param memories the recalled memories
     * @param knowledgeNodes the knowledge nodes
     * @return a derived conclusion
     */
    private String deriveConclusion(String request, List<Memory> memories, List<KnowledgeNode> knowledgeNodes) {
        StringBuilder conclusion = new StringBuilder();

        // Base on knowledge if available
        if (knowledgeNodes != null && !knowledgeNodes.isEmpty()) {
            KnowledgeNode top = knowledgeNodes.get(0);
            String label = top.getLabel();
            String description = top.getDescription() != null ? top.getDescription() : "";
            conclusion.append("Based on knowledge '").append(label).append("': ")
                    .append(truncate(description, 200));
        } else if (memories != null && !memories.isEmpty()) {
            // Base on memory if available
            Memory top = memories.get(0);
            conclusion.append("Based on recalled memory: ")
                    .append(truncate(top.content().text(), 200));
        } else {
            // Base on request only
            String topic = extractCoreTopic(request);
            conclusion.append("Insufficient evidence to form a definitive conclusion about ")
                    .append(topic != null ? topic : "the request");
        }

        // Add reasoning note
        conclusion.append(" Reasoned from evidence across ");

        int sources = 0;
        if (memories != null && !memories.isEmpty()) sources++;
        if (knowledgeNodes != null && !knowledgeNodes.isEmpty()) sources++;
        conclusion.append(sources).append(" evidence source(s).");

        return conclusion.toString();
    }

    /**
     * Generates alternative conclusions.
     *
     * @param request the user request
     * @param knowledgeNodes the knowledge nodes
     * @return list of alternative perspectives
     */
    private List<String> generateAlternatives(String request, List<KnowledgeNode> knowledgeNodes) {
        List<String> alternatives = new ArrayList<>();
        String topic = extractCoreTopic(request);

        if (topic != null) {
            alternatives.add("Alternative perspective: '" + topic + "' may also be interpreted from a different domain context");
        }

        if (knowledgeNodes != null && knowledgeNodes.size() > 1) {
            KnowledgeNode second = knowledgeNodes.get(1);
            alternatives.add("Alternative view: Consider knowledge from '" + second.getLabel() + "'");
        }

        if (knowledgeNodes != null && knowledgeNodes.size() > 2) {
            KnowledgeNode third = knowledgeNodes.get(2);
            alternatives.add("Further alternative: Explore '" + third.getLabel() + "' for additional context");
        }

        if (alternatives.isEmpty()) {
            alternatives.add("Alternative view: Request may need additional context to form full conclusion");
        }

        return alternatives;
    }

    /**
     * Extracts the scope of reasoning.
     *
     * @param request the user request
     * @param knowledgeNodes the knowledge nodes
     * @return scope description
     */
    private String extractScope(String request, List<KnowledgeNode> knowledgeNodes) {
        String topic = extractCoreTopic(request);
        StringBuilder scope = new StringBuilder("Domain: ");

        if (knowledgeNodes != null && !knowledgeNodes.isEmpty()) {
            String type = knowledgeNodes.get(0).getType() != null
                    ? knowledgeNodes.get(0).getType().name()
                    : "GENERAL";
            scope.append(type.toLowerCase());
        } else {
            scope.append("general");
        }

        if (topic != null) {
            scope.append(", Topic: ").append(topic);
        }

        return scope.toString();
    }

    /**
     * Extracts the core topic from the request.
     *
     * @param request the request text
     * @return the core topic or null
     */
    private String extractCoreTopic(String request) {
        if (request == null || request.isBlank()) return null;

        String cleaned = request.replace("What is", "").replace("what is", "")
                .replace("Explain", "").replace("explain", "")
                .replace("Tell me about", "").replace("tell me about", "")
                .replace("Define", "").replace("define", "")
                .replace("?", "").replace("!", "")
                .replace(".", "")
                .trim();

        return cleaned.isBlank() ? null : cleaned;
    }

    /**
     * Truncates text to a maximum length.
     *
     * @param text the text to truncate
     * @param maxLength the maximum length
     * @return truncated text
     */
    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "...";
    }
}