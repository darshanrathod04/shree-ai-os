package com.shreeai.os.platform.runtime.agents;

import com.shreeai.os.platform.runtime.execution.ExecutionRequest;
import com.shreeai.os.platform.runtime.model.AgentDecision;
import com.shreeai.os.platform.runtime.model.AgentDecision.Agent;
import com.shreeai.os.platform.runtime.model.AgentDecision.Action;
import com.shreeai.os.platform.runtime.model.DiagnosticReport;
import com.shreeai.os.platform.runtime.model.EvidenceBundle;
import com.shreeai.os.platform.runtime.model.EvidenceItem;
import com.shreeai.os.platform.runtime.model.EvidenceItem.SourceType;
import com.shreeai.os.platform.runtime.pipeline.PipelineExecutionState;

import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeNode;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>EvidenceAgent</b>
 *
 * <p>Transforms structured kernel outputs from {@code PipelineExecutionState}
 * into a canonical {@code EvidenceBundle}. Each bundle item is a structured
 * fact (not a markdown string) attributed to a specific kernel.</p>
 *
 * <p><b>Supported Evidence Sources (Sprint 18):</b></p>
 * <table>
 *   <tr><th>Metadata Key       </th><th>SourceType </th><th>Extracted Fields</th></tr>
 *   <tr><td>knowledgeResults   </td><td>KNOWLEDGE  </td><td>label, description, citations</td></tr>
 *   <tr><td>reasoningConclusion</td><td>REASONING  </td><td>conclusion</td></tr>
 *   <tr><td>inferenceResult   </td><td>INFERENCE  </td><td>topHypothesis</td></tr>
 *   <tr><td>planningResult    </td><td>PLANNING   </td><td>planSummary</td></tr>
 *   <tr><td>memoryResults     </td><td>MEMORY     </td><td>content summary</td></tr>
 *   <tr><td>reflectionResult  </td><td>REFLECTION </td><td>outcome</td></tr>
 *   <tr><td>projectSummary    </td><td>PROJECT    </td><td>projectName, structure summary</td></tr>
 *   <tr><td>executionResult   </td><td>EXECUTION  </td><td>taskId, status</td></tr>
 * </table>
 *
 * @since Sprint 18
 */
public final class EvidenceAgent {

    /** Metadata keys written by each kernel stage. */
    private static final String KEY_KNOWLEDGE_RESULTS = "knowledgeResults";
    private static final String KEY_REASONING_CONCLUSION = "reasoningConclusion";
    private static final String KEY_REASONING_CONFIDENCE = "reasoningConfidence";
    private static final String KEY_INFERENCE_RESULT = "inferenceResult";
    private static final String KEY_PLANNING_RESULT = "planningResult";
    private static final String KEY_PLAN_SUMMARY = "planSummary";
    private static final String KEY_MEMORY_RESULTS = "memoryResults";
    private static final String KEY_REFLECTION_RESULT = "reflectionResult";
    private static final String KEY_PROJECT_SUMMARY = "projectSummary";
    private static final String KEY_PROJECT_NAME = "projectName";
    private static final String KEY_EXECUTION_RESULT = "executionResult";
    private static final String KEY_TASK_ID = "taskId";
    private static final String KEY_EXECUTION_STATUS = "executionStatus";
    private static final String KEY_CITATIONS = "knowledgeCitations";
    private static final String KEY_GROUNDING_SCORE = "knowledgeGroundingScore";
    private static final String KEY_SUPPORTING_EVIDENCE = "supportingEvidence";

    public EvidenceAgent() {}

    /**
     * Extracts structured evidence from pipeline execution state.
     *
     * <p>Reads known metadata keys written by each kernel stage and converts
     * them into a canonical {@code EvidenceBundle}. Returns an empty bundle
     * when no evidence is available.</p>
     *
     * @param request    the original execution request (provides context)
     * @param diagnostics the diagnostic report (skips missing evidence sources)
     * @return a fully-populated EvidenceBundle (never null)
     */
    public EvidenceBundle extract(ExecutionRequest request, DiagnosticReport diagnostics) {
        Objects.requireNonNull(request, "request must not be null");

        EvidenceBundle.Builder builder = EvidenceBundle.builder()
                .addMetadata("requestId", request.getRequestId())
                .addMetadata("diagnosticReportId", diagnostics != null ? diagnostics.reportId() : "none");

        Map<String, Object> metadata = request.getMetadata();
        if (metadata == null) {
            metadata = Map.of();
        }

        // Extract evidence from each source
        extractKnowledgeEvidence(builder, metadata);
        extractReasoningEvidence(builder, metadata);
        extractInferenceEvidence(builder, metadata);
        extractPlanningEvidence(builder, metadata);
        extractMemoryEvidence(builder, metadata);
        extractReflectionEvidence(builder, metadata);
        extractProjectEvidence(builder, metadata);
        extractExecutionEvidence(builder, metadata);

        return builder.build();
    }

    /**
     * Extracts evidence from pipeline state (convenience overload).
     *
     * @param state the pipeline execution state (never null)
     * @return a fully-populated EvidenceBundle (never null)
     */
    public EvidenceBundle extractFromPipelineState(PipelineExecutionState state) {
        Objects.requireNonNull(state, "state must not be null");
        return extractFromMetadata(state.getMetadata());
    }

    /**
     * Extracts evidence from raw metadata map.
     *
     * @param metadata pipeline metadata map (never null)
     * @return a fully-populated EvidenceBundle (never null)
     */
    public EvidenceBundle extractFromMetadata(Map<String, Object> metadata) {
        Objects.requireNonNull(metadata, "metadata must not be null");

        EvidenceBundle.Builder builder = EvidenceBundle.builder();

        extractKnowledgeEvidence(builder, metadata);
        extractReasoningEvidence(builder, metadata);
        extractInferenceEvidence(builder, metadata);
        extractPlanningEvidence(builder, metadata);
        extractMemoryEvidence(builder, metadata);
        extractReflectionEvidence(builder, metadata);
        extractProjectEvidence(builder, metadata);
        extractExecutionEvidence(builder, metadata);

        return builder.build();
    }

    // ─── Per-source extraction ────────────────────────────────────────────────

    private void extractKnowledgeEvidence(EvidenceBundle.Builder builder, Map<String, Object> metadata) {
        Object raw = metadata.get(KEY_KNOWLEDGE_RESULTS);
        if (!(raw instanceof List<?> list) || list.isEmpty()) return;

        List<String> citations = extractStringList(metadata.get(KEY_CITATIONS));
        double groundingScore = readDouble(metadata.get(KEY_GROUNDING_SCORE));
        String groundingNote = groundingScore > 0.0
                ? String.format(" (grounding=%.2f)", groundingScore)
                : "";

        for (Object item : list) {
            // Sprint-19 hotfix: KnowledgeNode is a proper class with typed accessors,
            // not a plain Map. Handle it directly to extract getLabel() / getDescription().
            String label;
            String description;
            if (item instanceof KnowledgeNode node) {
                label = node.getLabel() != null ? node.getLabel() : "";
                description = node.getDescription() != null ? node.getDescription() : "";
            } else {
                label = extractString(item, "label");
                description = extractString(item, "description");
            }

            if (label.isBlank() && description.isBlank()) continue;

            builder.addItem(EvidenceItem.builder()
                    .sourceType(SourceType.KNOWLEDGE)
                    .title(label.isBlank() ? "Knowledge Node" : label)
                    .content(description.isBlank() ? label : description)
                    .citations(citations)
                    .confidenceHint(groundingScore > 0.0 ? groundingScore : 0.80)
                    .addAttribute("groundingScore", groundingScore)
                    .build());
        }
    }

    private void extractReasoningEvidence(EvidenceBundle.Builder builder, Map<String, Object> metadata) {
        String conclusion = String.valueOf(metadata.getOrDefault(KEY_REASONING_CONCLUSION, ""));
        if (conclusion.isBlank() || "null".equals(conclusion)) return;

        double confidence = readDouble(metadata.get(KEY_REASONING_CONFIDENCE));
        List<String> supporting = extractStringList(metadata.get(KEY_SUPPORTING_EVIDENCE));

        builder.addItem(EvidenceItem.builder()
                .sourceType(SourceType.REASONING)
                .title("Reasoning Conclusion")
                .content(conclusion)
                .confidenceHint(confidence > 0.0 ? confidence : 0.60)
                .citations(supporting)
                .build());
    }

    private void extractInferenceEvidence(EvidenceBundle.Builder builder, Map<String, Object> metadata) {
        Object inference = metadata.get(KEY_INFERENCE_RESULT);
        if (inference == null) return;

        String hypothesis = extractString(inference, "topHypothesis");
        if (hypothesis.isBlank() || "null".equals(hypothesis)) return;

        String conclusion = extractString(inference, "conclusion");
        String content = hypothesis;
        if (!conclusion.isBlank() && !conclusion.equals(hypothesis)) {
            content = hypothesis + "\n" + conclusion;
        }

        builder.addItem(EvidenceItem.builder()
                .sourceType(SourceType.INFERENCE)
                .title("Inference Hypothesis")
                .content(content)
                .confidenceHint(0.60)
                .build());
    }

    private void extractPlanningEvidence(EvidenceBundle.Builder builder, Map<String, Object> metadata) {
        Object planning = metadata.get(KEY_PLANNING_RESULT);
        if (planning == null) return;

        String summary = extractString(planning, KEY_PLAN_SUMMARY);
        if (summary.isBlank() || "null".equals(summary)) {
            summary = extractString(planning, "description");
        }
        if (summary.isBlank() || "null".equals(summary)) {
            summary = String.valueOf(planning);
        }

        builder.addItem(EvidenceItem.builder()
                .sourceType(SourceType.PLANNING)
                .title("Planning Result")
                .content(summary)
                .confidenceHint(0.70)
                .build());
    }

    private void extractMemoryEvidence(EvidenceBundle.Builder builder, Map<String, Object> metadata) {
        Object raw = metadata.get(KEY_MEMORY_RESULTS);
        if (!(raw instanceof List<?> list) || list.isEmpty()) return;

        for (Object item : list) {
            String content = extractString(item, "content");
            String summary = extractString(item, "summary");
            if (content.isBlank() && summary.isBlank()) continue;

            builder.addItem(EvidenceItem.builder()
                    .sourceType(SourceType.MEMORY)
                    .title("Memory Recall")
                    .content(content.isBlank() ? summary : content)
                    .confidenceHint(0.60)
                    .build());
        }
    }

    private void extractReflectionEvidence(EvidenceBundle.Builder builder, Map<String, Object> metadata) {
        Object reflection = metadata.get(KEY_REFLECTION_RESULT);
        if (reflection == null) return;

        String outcome = extractString(reflection, "outcome");
        String lessons = extractString(reflection, "lessons");

        String content = outcome;
        if (!lessons.isBlank()) {
            content = outcome + "\nLessons: " + lessons;
        }

        builder.addItem(EvidenceItem.builder()
                .sourceType(SourceType.REFLECTION)
                .title("Reflection Outcome")
                .content(content)
                .confidenceHint(0.60)
                .build());
    }

    private void extractProjectEvidence(EvidenceBundle.Builder builder, Map<String, Object> metadata) {
        Object project = metadata.get(KEY_PROJECT_SUMMARY);
        if (project == null) return;

        String name = extractString(project, KEY_PROJECT_NAME);
        String structure = extractString(project, "structure");
        String summary = extractString(project, "summary");

        String content = summary.isBlank() ? structure : summary;
        if (content.isBlank()) {
            content = String.valueOf(project);
        }

        builder.addItem(EvidenceItem.builder()
                .sourceType(SourceType.PROJECT)
                .title(name.isBlank() ? "Project Analysis" : name)
                .content(content)
                .confidenceHint(0.95)
                .addAttribute("projectName", name)
                .build());
    }

    private void extractExecutionEvidence(EvidenceBundle.Builder builder, Map<String, Object> metadata) {
        Object exec = metadata.get(KEY_EXECUTION_RESULT);
        if (exec == null) return;

        String taskId = extractString(exec, KEY_TASK_ID);
        String status = extractString(exec, KEY_EXECUTION_STATUS);

        String content = "Task: " + taskId + " | Status: " + status;

        builder.addItem(EvidenceItem.builder()
                .sourceType(SourceType.EXECUTION)
                .title("Execution Result")
                .content(content)
                .confidenceHint(0.75)
                .addAttribute(KEY_TASK_ID, taskId)
                .addAttribute(KEY_EXECUTION_STATUS, status)
                .build());
    }

    // ─── Utilities ───────────────────────────────────────────────────────────

    private String extractString(Object target, String field) {
        if (target instanceof Map<?, ?> map) {
            Object val = map.get(field);
            return val != null ? String.valueOf(val).trim() : "";
        }
        return target != null ? String.valueOf(target).trim() : "";
    }

    private List<String> extractStringList(Object value) {
        if (value instanceof List<?> list) {
            List<String> result = new ArrayList<>();
            for (Object item : list) {
                if (item != null && !String.valueOf(item).isBlank()) {
                    result.add(String.valueOf(item).trim());
                }
            }
            return result;
        }
        return List.of();
    }

    private double readDouble(Object value) {
        if (value instanceof Number n) {
            return n.doubleValue();
        }
        return 0.0;
    }

    /**
     * Returns an AgentDecision describing this extraction run.
     */
    public AgentDecision toDecision(EvidenceBundle bundle) {
        return AgentDecision.builder()
                .agent(Agent.EVIDENCE)
                .action(Action.EXTRACT)
                .rationale(String.format("Extracted %d evidence items from pipeline state", bundle.size()))
                .confidence(0.95)
                .addMetadata("bundleId", bundle.bundleId())
                .addMetadata("itemCount", bundle.size())
                .build();
    }
}
