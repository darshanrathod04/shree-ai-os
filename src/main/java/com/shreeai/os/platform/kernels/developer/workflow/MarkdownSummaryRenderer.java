package com.shreeai.os.platform.kernels.developer.workflow;

import com.shreeai.os.platform.kernels.developer.api.DeveloperIntent;
import com.shreeai.os.platform.kernels.developer.codegen.model.CodeGenerationResult;
import com.shreeai.os.platform.kernels.developer.codegen.model.GeneratedPatch;
import com.shreeai.os.platform.kernels.developer.codegen.model.TestSkeleton;
import com.shreeai.os.platform.kernels.developer.workflow.model.DeveloperWorkflow;
import com.shreeai.os.platform.kernels.project.model.ProjectSummary;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <b>MarkdownSummaryRenderer</b>
 *
 * <p>Renders a structured developer workflow result into a human-readable
 * markdown document. The output follows the exact format specified in
 * the Sprint-16 requirements:</p>
 *
 * <pre>
 * # Developer Workflow
 * ## Instruction
 * ## Intent
 * ## Project Summary
 * ## Impact Analysis
 * ## Generated Files
 * ## Validation
 * ## Next Step
 * </pre>
 *
 * <p><b>Ownership:</b> Developer Workflow (Sprint-16)</p>
 *
 * @since Sprint-16
 */
public final class MarkdownSummaryRenderer {

    /**
     * Renders the complete workflow result as a markdown string.
     *
     * @param workflow        the developer workflow
     * @param codeGenResult   the code generation result (may be null)
     * @param instruction     the original developer instruction
     * @param confidence      overall confidence score
     * @return a formatted markdown string
     */
    public String render(DeveloperWorkflow workflow,
                         CodeGenerationResult codeGenResult,
                         String instruction,
                         double confidence) {
        StringBuilder sb = new StringBuilder();

        // ── Header ──────────────────────────────────────────────────────────────
        sb.append("# Developer Workflow\n\n");

        // ── Instruction ─────────────────────────────────────────────────────────
        sb.append("## Instruction\n\n");
        sb.append(safeString(instruction)).append("\n\n");

        // ── Intent ──────────────────────────────────────────────────────────────
        sb.append("## Intent\n\n");
        DeveloperIntent intent = workflow != null ? workflow.intent() : null;
        if (intent != null) {
            sb.append(intent.intent().name()).append("\n\n");
        } else {
            sb.append("UNKNOWN\n\n");
        }

        // ── Project Summary ─────────────────────────────────────────────────────
        sb.append("## Project Summary\n\n");
        renderProjectSummary(sb, workflow);

        // ── Impact Analysis ─────────────────────────────────────────────────────
        sb.append("## Impact Analysis\n\n");
        renderImpactAnalysis(sb, workflow, codeGenResult);

        // ── Generated Files ─────────────────────────────────────────────────────
        sb.append("## Generated Files\n\n");
        renderGeneratedFiles(sb, codeGenResult);

        // ── Validation ──────────────────────────────────────────────────────────
        sb.append("## Validation\n\n");
        renderValidation(sb, codeGenResult, confidence);

        // ── Next Step ───────────────────────────────────────────────────────────
        sb.append("## Next Step\n\n");
        sb.append("Apply generated patch.\n");

        return sb.toString();
    }

    private void renderProjectSummary(StringBuilder sb, DeveloperWorkflow workflow) {
        ProjectSummary summary = workflow != null ? workflow.projectSummary() : null;
        if (summary != null) {
            var stats = summary.statistics();
            sb.append(String.format("Classes: %d%n", stats.classCount()));
            sb.append(String.format("Controllers: %d%n", stats.controllerCount()));
            sb.append(String.format("Services: %d%n", stats.serviceCount()));
            sb.append(String.format("Entities: %d%n", stats.entityCount()));
            sb.append("\n");
        } else {
            sb.append("Project not analyzed.\n\n");
        }
    }

    private void renderImpactAnalysis(StringBuilder sb,
                                      DeveloperWorkflow workflow,
                                      CodeGenerationResult codeGenResult) {
        var impact = workflow != null ? workflow.workflowImpact() : null;
        sb.append("Affected Files\n");
        if (impact != null && !impact.affectedFiles().isEmpty()) {
            for (String file : impact.affectedFiles()) {
                sb.append("- ").append(simpleName(file)).append("\n");
            }
        } else {
            sb.append("- (none)\n");
        }
        sb.append("\n");

        sb.append("Risk ");
        if (impact != null) {
            sb.append(impact.riskLevel().name()).append("\n\n");
            if (!impact.dependencyWarnings().isEmpty()) {
                sb.append("Warnings\n");
                for (String w : impact.dependencyWarnings()) {
                    sb.append("- ").append(w).append("\n");
                }
                sb.append("\n");
            }
        } else {
            sb.append("UNKNOWN\n\n");
        }
    }

    private void renderGeneratedFiles(StringBuilder sb, CodeGenerationResult codeGenResult) {
        if (codeGenResult != null && !codeGenResult.generatedPatches().isEmpty()) {
            List<GeneratedPatch> patches = codeGenResult.generatedPatches();
            for (int i = 0; i < patches.size(); i++) {
                GeneratedPatch patch = patches.get(i);
                sb.append(String.format("%d. %s%n", i + 1, simpleName(patch.targetFile())));
            }
        } else {
            sb.append("No files generated.\n");
        }
        sb.append("\n");

        // Also list test skeletons
        if (codeGenResult != null && !codeGenResult.testSkeletons().isEmpty()) {
            sb.append("### Test Skeletons\n\n");
            for (TestSkeleton t : codeGenResult.testSkeletons()) {
                sb.append(String.format("- `%s` (%s)%n",
                        t.testClassName(), t.framework().name()));
            }
            sb.append("\n");
        }
    }

    private void renderValidation(StringBuilder sb, CodeGenerationResult codeGenResult, double confidence) {
        sb.append(String.format("Confidence: %.2f%n%n", confidence));

        if (codeGenResult != null && codeGenResult.validation() != null) {
            var validation = codeGenResult.validation();
            sb.append(String.format("Status: %s%n%n", validation.overallStatus().name()));

            if (!validation.warnings().isEmpty()) {
                sb.append("Warnings\n");
                for (String w : validation.warnings()) {
                    sb.append("- ").append(w).append("\n");
                }
                sb.append("\n");
            }
            if (!validation.errors().isEmpty()) {
                sb.append("Errors\n");
                for (String e : validation.errors()) {
                    sb.append("- ").append(e).append("\n");
                }
                sb.append("\n");
            }
        } else {
            sb.append("Status: PENDING\n\n");
        }
    }

    /**
     * Returns the simple name (last segment) of a fully-qualified name or path.
     */
    private static String simpleName(String fqnOrPath) {
        if (fqnOrPath == null || fqnOrPath.isEmpty()) return "(unknown)";
        int lastDot = fqnOrPath.lastIndexOf('.');
        int lastSlash = Math.max(fqnOrPath.lastIndexOf('/'), fqnOrPath.lastIndexOf('\\'));
        int start = Math.max(lastDot, lastSlash) + 1;
        return start > 0 && start < fqnOrPath.length()
                ? fqnOrPath.substring(start)
                : fqnOrPath;
    }

    private static String safeString(String s) {
        return s == null ? "" : s;
    }
}
