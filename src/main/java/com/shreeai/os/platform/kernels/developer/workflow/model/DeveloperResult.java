package com.shreeai.os.platform.kernels.developer.workflow.model;

import com.shreeai.os.platform.kernels.developer.codegen.model.TestSkeleton;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>DeveloperResult</b>
 *
 * <p>The complete, structured output of the Sprint-16 autonomous developer
 * workflow engine. Carries the aggregated workflow, all generated artifacts
 * (Java source, tests, config), test skeletons, confidence score, and
 * a human-readable markdown summary.</p>
 *
 * <p>This is the entry point returned by {@code ProjectSDK.build()} and
 * represents the end-to-end output of the pipeline:</p>
 * <pre>
 * DeveloperIntent → ProjectIntelligence → ImpactIntelligence →
 * PatchPlan → CodeGen → Validation → TestSkeletons → MarkdownSummary
 * </pre>
 *
 * <p><b>Ownership:</b> Developer Workflow (Sprint-16)</p>
 *
 * @since Sprint-16
 */
public final class DeveloperResult {

    private final DeveloperWorkflow workflow;
    private final List<GeneratedArtifact> generatedArtifacts;
    private final List<TestSkeleton> testSkeletons;
    private final double confidence;
    private final String markdownSummary;
    private final Instant completedAt;

    private DeveloperResult(Builder b) {
        this.workflow = b.workflow;
        this.generatedArtifacts = List.copyOf(b.generatedArtifacts == null ? List.of() : b.generatedArtifacts);
        this.testSkeletons = List.copyOf(b.testSkeletons == null ? List.of() : b.testSkeletons);
        this.confidence = Math.max(0.0, Math.min(1.0, b.confidence));
        this.markdownSummary = b.markdownSummary == null ? "" : b.markdownSummary;
        this.completedAt = b.completedAt == null ? Instant.now() : b.completedAt;
    }

    public DeveloperWorkflow workflow() { return workflow; }
    public List<GeneratedArtifact> generatedArtifacts() { return generatedArtifacts; }
    public List<TestSkeleton> testSkeletons() { return testSkeletons; }
    public double confidence() { return confidence; }
    public String markdownSummary() { return markdownSummary; }
    public Instant completedAt() { return completedAt; }

    /**
     * Returns the total number of generated source lines across all artifacts.
     */
    public int totalSourceLines() {
        return generatedArtifacts.stream().mapToInt(GeneratedArtifact::lineCount).sum();
    }

    /**
     * Returns a count of artifacts by type.
     */
    public int artifactCount(GeneratedArtifact.Type type) {
        return (int) generatedArtifacts.stream().filter(a -> a.type() == type).count();
    }

    /**
     * Returns a developer-friendly summary string.
     */
    public String summary() {
        int javaCount = artifactCount(GeneratedArtifact.Type.JAVA);
        int testCount = artifactCount(GeneratedArtifact.Type.TEST);
        int configCount = artifactCount(GeneratedArtifact.Type.CONFIG);
        return String.format(
                "DeveloperResult[confidence=%.2f, artifacts=%d (Java:%d, Test:%d, Config:%d), " +
                "skeletons=%d, lines=%d]",
                confidence, generatedArtifacts.size(), javaCount, testCount, configCount,
                testSkeletons.size(), totalSourceLines()
        );
    }

    /**
     * Returns all data as a structured payload map for embedding in SDK responses.
     */
    public Map<String, Object> toPayload() {
        return Map.of(
                "workflow", workflow != null ? workflow.toMap() : Map.of(),
                "artifactCount", generatedArtifacts.size(),
                "testSkeletonCount", testSkeletons.size(),
                "confidence", confidence,
                "totalSourceLines", totalSourceLines(),
                "markdownSummary", markdownSummary
        );
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private DeveloperWorkflow workflow;
        private List<GeneratedArtifact> generatedArtifacts;
        private List<TestSkeleton> testSkeletons;
        private double confidence = 0.8;
        private String markdownSummary;
        private Instant completedAt;

        public Builder workflow(DeveloperWorkflow v) { this.workflow = v; return this; }
        public Builder generatedArtifacts(List<GeneratedArtifact> v) { this.generatedArtifacts = v; return this; }
        public Builder testSkeletons(List<TestSkeleton> v) { this.testSkeletons = v; return this; }
        public Builder confidence(double v) { this.confidence = v; return this; }
        public Builder markdownSummary(String v) { this.markdownSummary = v; return this; }
        public Builder completedAt(Instant v) { this.completedAt = v; return this; }

        public DeveloperResult build() { return new DeveloperResult(this); }
    }
}
