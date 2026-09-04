package com.shreeai.os.platform.kernels.response.model;

import com.shreeai.os.platform.kernels.developer.analyzer.*;
import com.shreeai.os.platform.kernels.developer.api.DeveloperIntent;
import com.shreeai.os.platform.kernels.developer.codegen.model.CodeGenerationResult;

import java.time.Instant;
import java.util.*;

/**
 * <b>DeveloperResponse</b>
 *
 * <p>Structured response produced by the Developer Agent. Contains the
 * parsed intent, impact analysis, architecture validation, implementation
 * plan, and test strategy — all deterministically generated without LLM calls.</p>
 *
 * <p><b>Ownership:</b> Developer Agent (Sprint-14)</p>
 *
 * @since Sprint-14
 */
public final class DeveloperResponse {

    private final String request;
    private final DeveloperIntent intent;
    private final ImpactReport impact;
    private final List<ValidationIssue> validationIssues;
    private final ImplementationPlan plan;
    private final TestStrategyGenerator.TestStrategy testStrategy;
    private final String formattedPlan;
    private final double confidence;
    private final Instant timestamp;
    /** Sprint-15: populated only when code generation is active. */
    private final CodeGenerationResult codeGeneration;

    private DeveloperResponse(Builder b) {
        this.request = Objects.requireNonNull(b.request, "request");
        this.intent = b.intent;
        this.impact = b.impact;
        this.validationIssues = List.copyOf(b.validationIssues == null ? List.of() : b.validationIssues);
        this.plan = b.plan;
        this.testStrategy = b.testStrategy;
        this.formattedPlan = b.formattedPlan == null ? formatPlan(b.intent, b.plan, b.impact) : b.formattedPlan;
        this.confidence = Math.max(0.0, Math.min(1.0, b.confidence));
        this.timestamp = b.timestamp == null ? java.time.Instant.now() : b.timestamp;
        this.codeGeneration = b.codeGeneration;
    }

    public String request() { return request; }
    public DeveloperIntent intent() { return intent; }
    public ImpactReport impact() { return impact; }
    public List<ValidationIssue> validationIssues() { return validationIssues; }
    public ImplementationPlan plan() { return plan; }
    public TestStrategyGenerator.TestStrategy testStrategy() { return testStrategy; }
    public String formattedPlan() { return formattedPlan; }
    public double confidence() { return confidence; }
    public java.time.Instant timestamp() { return timestamp; }

    /**
     * Sprint-15: the code generation result, if code generation was performed.
     * @return the code generation result, or null if not generated
     * @since Sprint-15
     */
    public CodeGenerationResult codeGeneration() { return codeGeneration; }

    /**
     * Returns the formatted plan as a structured text response.
     */
    public String toFormattedResponse() {
        return formattedPlan;
    }

    /**
     * Returns all data as a structured payload map for SDKResponse.
     */
    public Map<String, Object> toPayload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("request", request);
        payload.put("intent", intent != null ? intent.intent().name() : "");
        payload.put("intentLabel", intent != null ? intent.label() : "");
        payload.put("entity", intent != null ? intent.entity() : "");
        payload.put("domain", intent != null ? intent.domain() : "");
        payload.put("confidence", confidence);
        payload.put("timestamp", timestamp.toString());

        if (impact != null) {
            Map<String, Object> impactMap = new LinkedHashMap<>();
            impactMap.put("targetClass", impact.targetClass());
            impactMap.put("directlyAffected", impact.directlyAffected());
            impactMap.put("indirectlyAffected", impact.indirectlyAffected());
            impactMap.put("affectedEndpoints", impact.affectedEndpoints().stream()
                    .map(com.shreeai.os.platform.kernels.project.model.ProjectEndpoint::signature).toList());
            impactMap.put("affectedControllers", impact.affectedControllers().stream()
                    .map(com.shreeai.os.platform.kernels.project.model.ProjectClass::name).toList());
            impactMap.put("affectedServices", impact.affectedServices().stream()
                    .map(com.shreeai.os.platform.kernels.project.model.ProjectClass::name).toList());
            impactMap.put("affectedRepositories", impact.affectedRepositories().stream()
                    .map(com.shreeai.os.platform.kernels.project.model.ProjectClass::name).toList());
            impactMap.put("affectedEntities", impact.affectedEntities().stream()
                    .map(com.shreeai.os.platform.kernels.project.model.ProjectEntity::name).toList());
            impactMap.put("dependencyDepth", impact.dependencyDepth());
            impactMap.put("totalAffected", impact.totalAffected());
            payload.put("impact", impactMap);
        }

        if (plan != null) {
            Map<String, Object> planMap = new LinkedHashMap<>();
            planMap.put("phaseCount", plan.phases().size());
            planMap.put("phases", plan.phases().stream().map(p -> Map.of(
                    "number", p.number(),
                    "objective", p.objective(),
                    "description", p.description(),
                    "affectedFiles", p.affectedFiles(),
                    "dependencies", p.dependencies(),
                    "verification", p.verificationCriteria(),
                    "risks", p.riskNotes()
            )).toList());
            planMap.put("risks", plan.risks());
            planMap.put("confidence", plan.confidence());
            payload.put("plan", planMap);
        }

        if (testStrategy != null) {
            Map<String, Object> testMap = new LinkedHashMap<>();
            testMap.put("unitTests", testStrategy.unitTests());
            testMap.put("integrationTests", testStrategy.integrationTests());
            testMap.put("securityTests", testStrategy.securityTests());
            testMap.put("apiTests", testStrategy.apiTests());
            testMap.put("regressionTests", testStrategy.regressionTests());
            testMap.put("totalTestCount", testStrategy.totalTests());
            payload.put("testStrategy", testMap);
        }

        if (validationIssues != null && !validationIssues.isEmpty()) {
            payload.put("validationIssues", validationIssues.stream().map(i -> Map.of(
                    "severity", i.severity().name(),
                    "kind", i.kind().name(),
                    "message", i.message(),
                    "affectedFiles", i.affectedFiles(),
                    "recommendation", i.recommendation()
            )).toList());
        }

        // Sprint-15: code generation payload
        if (codeGeneration != null) {
            Map<String, Object> cgMap = new LinkedHashMap<>();
            cgMap.put("patchCount", codeGeneration.generatedPatches().size());
            cgMap.put("newFileCount", codeGeneration.generatedPatches().stream().filter(com.shreeai.os.platform.kernels.developer.codegen.model.GeneratedPatch::isNewFile).count());
            cgMap.put("modifiedFileCount", codeGeneration.generatedPatches().stream().filter(p -> !p.isNewFile()).count());
            cgMap.put("sourceLineCount", codeGeneration.totalSourceLines());
            cgMap.put("testSkeletonCount", codeGeneration.testSkeletons().size());
            cgMap.put("confidence", codeGeneration.confidence());
            if (codeGeneration.validation() != null) {
                cgMap.put("validationStatus", codeGeneration.validation().overallStatus().name());
                cgMap.put("errorCount", codeGeneration.validation().errors().size());
                cgMap.put("warningCount", codeGeneration.validation().warnings().size());
            }
            // List of new file paths and modified file paths
            cgMap.put("newFiles", codeGeneration.generatedPatches().stream()
                    .filter(com.shreeai.os.platform.kernels.developer.codegen.model.GeneratedPatch::isNewFile)
                    .map(com.shreeai.os.platform.kernels.developer.codegen.model.GeneratedPatch::targetFile)
                    .toList());
            cgMap.put("modifiedFiles", codeGeneration.generatedPatches().stream()
                    .filter(p -> !p.isNewFile())
                    .map(com.shreeai.os.platform.kernels.developer.codegen.model.GeneratedPatch::targetFile)
                    .toList());
            cgMap.put("testClasses", codeGeneration.testSkeletons().stream()
                    .map(com.shreeai.os.platform.kernels.developer.codegen.model.TestSkeleton::testClassName)
                    .toList());
            payload.put("codeGeneration", cgMap);
        }

        return payload;
    }

    // ─── Formatted text output ───────────────────────────────────────────────

    private static String formatPlan(DeveloperIntent intent, ImplementationPlan plan, ImpactReport impact) {
        StringBuilder sb = new StringBuilder();

        sb.append("# Developer Implementation Plan\n\n");
        sb.append("## Request\n").append(intent != null ? intent.originalRequest() : "").append("\n\n");
        if (intent != null) {
            sb.append("## Intent\n").append(intent.label()).append("\n\n");
        }
        if (impact != null) {
            sb.append("## Architecture Impact\n");
            sb.append(impactLevel(impact)).append("\n\n");
            sb.append("## Affected Files\n");
            int idx = 1;
            for (String fqn : impact.directlyAffected()) {
                sb.append(idx++).append(". ").append(fqn).append("\n");
            }
            sb.append("\n");
            if (!impact.affectedEndpoints().isEmpty()) {
                sb.append("## Impacted Endpoints\n");
                for (var ep : impact.affectedEndpoints()) {
                    sb.append("* ").append(ep.signature()).append("\n");
                }
                sb.append("\n");
            }
        }
        if (plan != null && !plan.phases().isEmpty()) {
            sb.append("## Implementation Phases\n\n");
            for (ImplementationPlan.Phase phase : plan.phases()) {
                sb.append("**Phase ").append(phase.number()).append("**\n");
                sb.append(phase.objective()).append("\n\n");
                if (!phase.affectedFiles().isEmpty()) {
                    sb.append("*Files:* ").append(String.join(", ", phase.affectedFiles())).append("\n");
                }
                if (!phase.dependencies().isEmpty()) {
                    sb.append("*Depends on:* ").append(String.join(", ", phase.dependencies())).append("\n");
                }
                if (!phase.verificationCriteria().isEmpty()) {
                    sb.append("*Verify:* ").append(String.join(", ", phase.verificationCriteria())).append("\n");
                }
                sb.append("\n");
            }
            if (!plan.risks().isEmpty()) {
                sb.append("## Risks\n");
                for (String risk : plan.risks()) {
                    sb.append("* ").append(risk).append("\n");
                }
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    private static String impactLevel(ImpactReport impact) {
        int total = impact.totalAffected();
        if (total > 20) return "High — " + total + " classes affected";
        if (total > 5) return "Medium — " + total + " classes affected";
        if (total > 0) return "Low — " + total + " classes affected";
        return "Minimal — isolated change";
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String request;
        private DeveloperIntent intent;
        private ImpactReport impact;
        private List<ValidationIssue> validationIssues;
        private ImplementationPlan plan;
        private TestStrategyGenerator.TestStrategy testStrategy;
        private String formattedPlan;
        private double confidence = 0.85;
        private java.time.Instant timestamp;
        private CodeGenerationResult codeGeneration;

        public Builder request(String v) { this.request = v; return this; }
        public Builder intent(DeveloperIntent v) { this.intent = v; return this; }
        public Builder impact(ImpactReport v) { this.impact = v; return this; }
        public Builder validationIssues(List<ValidationIssue> v) { this.validationIssues = v; return this; }
        public Builder plan(ImplementationPlan v) { this.plan = v; return this; }
        public Builder testStrategy(TestStrategyGenerator.TestStrategy v) { this.testStrategy = v; return this; }
        public Builder formattedPlan(String v) { this.formattedPlan = v; return this; }
        public Builder confidence(double v) { this.confidence = v; return this; }
        public Builder timestamp(java.time.Instant v) { this.timestamp = v; return this; }
        public Builder codeGeneration(CodeGenerationResult v) { this.codeGeneration = v; return this; }

        public DeveloperResponse build() { return new DeveloperResponse(this); }
    }
}