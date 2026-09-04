package com.shreeai.os.platform.kernels.developer.codegen.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>CodeGenerationResult</b> — the complete, structured output of the
 * Sprint-15 code generation pipeline.
 *
 * <p>Carries the {@link PatchPlan}, the validated list of {@link GeneratedPatch}
 * source strings, the validated {@link ValidationResult}, and the list of
 * generated {@link TestSkeleton} descriptors.</p>
 *
 * <p>This class is additive to the existing {@code DeveloperResponse}; it is
 * only populated when the Developer Agent's code generation pipeline is active.
 * All SDK APIs remain backward compatible.</p>
 *
 * <p><b>Ownership:</b> Developer Agent (Sprint-15)</p>
 *
 * @since Sprint-15
 */
public final class CodeGenerationResult {

    private final String request;
    private final String intent;
    private final String entity;
    private final PatchPlan patchPlan;
    private final List<GeneratedPatch> generatedPatches;
    private final ValidationResult validation;
    private final List<TestSkeleton> testSkeletons;
    private final double confidence;
    private final Instant generatedAt;

    private CodeGenerationResult(Builder b) {
        this.request = Objects.requireNonNull(b.request, "request");
        this.intent = b.intent == null ? "" : b.intent;
        this.entity = b.entity == null ? "" : b.entity;
        this.patchPlan = b.patchPlan;
        this.generatedPatches = List.copyOf(b.generatedPatches == null ? List.of() : b.generatedPatches);
        this.validation = b.validation;
        this.testSkeletons = List.copyOf(b.testSkeletons == null ? List.of() : b.testSkeletons);
        this.confidence = Math.max(0.0, Math.min(1.0, b.confidence));
        this.generatedAt = b.generatedAt == null ? Instant.now() : b.generatedAt;
    }

    public String request() { return request; }
    public String intent() { return intent; }
    public String entity() { return entity; }
    public PatchPlan patchPlan() { return patchPlan; }
    public List<GeneratedPatch> generatedPatches() { return generatedPatches; }
    public ValidationResult validation() { return validation; }
    public List<TestSkeleton> testSkeletons() { return testSkeletons; }
    public double confidence() { return confidence; }
    public Instant generatedAt() { return generatedAt; }

    /**
     * Returns the total number of generated source lines across all patches.
     */
    public int totalSourceLines() {
        return generatedPatches.stream().mapToInt(GeneratedPatch::lineCount).sum();
    }

    /**
     * Returns a developer-friendly summary string.
     */
    public String summary() {
        int newFiles = (int) generatedPatches.stream().filter(GeneratedPatch::isNewFile).count();
        int modFiles = generatedPatches.size() - newFiles;
        return String.format(
                "%d patch(es) generated (%d new, %d modified) — %d source lines, validation: %s",
                generatedPatches.size(), newFiles, modFiles,
                totalSourceLines(),
                validation == null ? "N/A" : validation.overallStatus()
        );
    }

    /**
     * Returns all data as a structured payload map for embedding in SDK responses.
     */
    public Map<String, Object> toPayload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("request", request);
        payload.put("intent", intent);
        payload.put("entity", entity);
        payload.put("patchCount", generatedPatches.size());
        payload.put("newFileCount", generatedPatches.stream().filter(GeneratedPatch::isNewFile).count());
        payload.put("modifiedFileCount", generatedPatches.stream().filter(p -> !p.isNewFile()).count());
        payload.put("sourceLineCount", totalSourceLines());
        payload.put("testSkeletonCount", testSkeletons.size());
        payload.put("confidence", confidence);
        payload.put("generatedAt", generatedAt.toString());

        if (validation != null) {
            Map<String, Object> vMap = new LinkedHashMap<>();
            vMap.put("status", validation.overallStatus().name());
            vMap.put("errorCount", validation.errors().size());
            vMap.put("warningCount", validation.warnings().size());
            if (!validation.errors().isEmpty()) vMap.put("errors", validation.errors());
            if (!validation.warnings().isEmpty()) vMap.put("warnings", validation.warnings());
            payload.put("validation", vMap);
        }

        if (patchPlan != null) {
            List<String> patchNames = new ArrayList<>();
            for (FilePatch p : patchPlan.patches()) {
                patchNames.add(p.targetFile());
            }
            payload.put("patchFiles", patchNames);
        }

        List<String> testNames = new ArrayList<>();
        for (TestSkeleton t : testSkeletons) {
            testNames.add(t.testClassName());
        }
        payload.put("testClasses", testNames);

        return payload;
    }

    /**
     * Formats the result as a clean, human-readable plan document.
     * This is the primary output format shown to the developer.
     */
    public String toFormattedResponse() {
        StringBuilder sb = new StringBuilder();
        sb.append("# Implementation Ready\n\n");
        sb.append("**Request:** ").append(request).append("\n\n");

        if (!generatedPatches.isEmpty()) {
            List<String> newFiles = generatedPatches.stream()
                    .filter(GeneratedPatch::isNewFile)
                    .map(GeneratedPatch::targetFile)
                    .toList();
            List<String> modFiles = generatedPatches.stream()
                    .filter(p -> !p.isNewFile())
                    .map(GeneratedPatch::targetFile)
                    .toList();

            if (!newFiles.isEmpty()) {
                sb.append("## Files to Create\n\n");
                for (String f : newFiles) sb.append("* `").append(f).append("`\n");
                sb.append("\n");
            }
            if (!modFiles.isEmpty()) {
                sb.append("## Files to Modify\n\n");
                for (String f : modFiles) sb.append("* `").append(f).append("`\n");
                sb.append("\n");
            }
        }

        if (!generatedPatches.isEmpty()) {
            sb.append("## Generated Patches\n\n");
            for (GeneratedPatch p : generatedPatches) {
                String label = p.isNewFile() ? "Create" : "Modify";
                sb.append("### ").append(label).append(": ").append(p.targetFile()).append("\n\n");
                sb.append(p.source()).append("\n\n");
            }
        }

        if (!testSkeletons.isEmpty()) {
            sb.append("## Test Skeletons\n\n");
            for (TestSkeleton t : testSkeletons) {
                sb.append("* `").append(t.testClassName())
                        .append("` (").append(t.framework()).append(")\n");
            }
            sb.append("\n");
        }

        if (validation != null) {
            sb.append("## Validation\n\n");
            sb.append("**Status:** `").append(validation.overallStatus()).append("`\n\n");
            if (!validation.warnings().isEmpty()) {
                sb.append("**Warnings:**\n");
                for (String w : validation.warnings()) sb.append("* ").append(w).append("\n");
            }
            if (!validation.errors().isEmpty()) {
                sb.append("**Errors:**\n");
                for (String e : validation.errors()) sb.append("* ").append(e).append("\n");
            }
        }

        return sb.toString();
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String request;
        private String intent;
        private String entity;
        private PatchPlan patchPlan;
        private List<GeneratedPatch> generatedPatches;
        private ValidationResult validation;
        private List<TestSkeleton> testSkeletons;
        private double confidence = 0.8;
        private Instant generatedAt;

        public Builder request(String v) { this.request = v; return this; }
        public Builder intent(String v) { this.intent = v; return this; }
        public Builder entity(String v) { this.entity = v; return this; }
        public Builder patchPlan(PatchPlan v) { this.patchPlan = v; return this; }
        public Builder generatedPatches(List<GeneratedPatch> v) { this.generatedPatches = v; return this; }
        public Builder validation(ValidationResult v) { this.validation = v; return this; }
        public Builder testSkeletons(List<TestSkeleton> v) { this.testSkeletons = v; return this; }
        public Builder confidence(double v) { this.confidence = v; return this; }
        public Builder generatedAt(Instant v) { this.generatedAt = v; return this; }

        public CodeGenerationResult build() { return new CodeGenerationResult(this); }
    }
}
