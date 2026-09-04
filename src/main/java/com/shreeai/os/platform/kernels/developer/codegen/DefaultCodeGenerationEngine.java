package com.shreeai.os.platform.kernels.developer.codegen;

import com.shreeai.os.platform.kernels.developer.analyzer.ImpactReport;
import com.shreeai.os.platform.kernels.developer.api.DeveloperIntent;
import com.shreeai.os.platform.kernels.developer.codegen.model.*;
import com.shreeai.os.platform.kernels.project.model.ProjectClass;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * <b>DefaultCodeGenerationEngine</b> — top-level orchestrator for the
 * Sprint-15 Autonomous Code Generation pipeline.
 *
 * <p>Pipeline:</p>
 * <ol>
 *   <li>{@link PatchPlanner} produces a {@link PatchPlan}</li>
 *   <li>{@link JavaCodeGenerator} renders each patch into source code</li>
 *   <li>{@link PatchValidator} checks every patch and returns SAFE/WARNING/INVALID</li>
 *   <li>{@link TestSkeletonGenerator} produces test class descriptors</li>
 *   <li>{@link JavaCodeGenerator} renders the test skeletons</li>
 *   <li>All results are aggregated into a {@link CodeGenerationResult}</li>
 * </ol>
 *
 * <p>This engine is a pure in-memory orchestrator. It never writes files.</p>
 *
 * <p><b>Ownership:</b> Developer Agent (Sprint-15)</p>
 *
 * @since Sprint-15
 */
public final class DefaultCodeGenerationEngine {

    private final PatchPlanner planner;
    private final JavaCodeGenerator codeGenerator;
    private final PatchValidator validator;
    private final TestSkeletonGenerator testGen;

    public DefaultCodeGenerationEngine() {
        this(new PatchPlanner(), new JavaCodeGenerator(),
             new PatchValidator(), new TestSkeletonGenerator());
    }

    public DefaultCodeGenerationEngine(PatchPlanner planner,
                                       JavaCodeGenerator codeGenerator,
                                       PatchValidator validator,
                                       TestSkeletonGenerator testGen) {
        this.planner = Objects.requireNonNull(planner, "planner");
        this.codeGenerator = Objects.requireNonNull(codeGenerator, "codeGenerator");
        this.validator = Objects.requireNonNull(validator, "validator");
        this.testGen = Objects.requireNonNull(testGen, "testGen");
    }

    /**
     * Generates a complete {@link CodeGenerationResult} for the given intent.
     */
    public CodeGenerationResult generate(DeveloperIntent intent,
                                          ImpactReport impact,
                                          List<ProjectClass> allClasses) {
        Objects.requireNonNull(intent, "intent");

        // Step 1: Plan
        PatchPlan plan = planner.plan(intent, impact, allClasses);

        // Step 2: Render code
        List<GeneratedPatch> generated = codeGenerator.generatePatches(plan);

        // Step 3: Validate
        ValidationResult validation = validator.validate(plan);

        // Step 4: Generate test skeletons
        List<TestSkeleton> testSkeletons = testGen.generate(intent, plan, allClasses);

        // Step 5: Compute confidence
        double confidence = computeConfidence(intent, validation, generated.size());

        return CodeGenerationResult.builder()
                .request(intent.originalRequest())
                .intent(intent.intent().name())
                .entity(intent.entity())
                .patchPlan(plan)
                .generatedPatches(generated)
                .validation(validation)
                .testSkeletons(testSkeletons)
                .confidence(confidence)
                .generatedAt(Instant.now())
                .build();
    }

    /**
     * Convenience overload that handles a null impact report.
     */
    public CodeGenerationResult generate(DeveloperIntent intent,
                                          List<ProjectClass> allClasses) {
        return generate(intent, null, allClasses);
    }

    private double computeConfidence(DeveloperIntent intent,
                                     ValidationResult validation,
                                     int patchCount) {
        double base = intent.confidence();
        if (validation != null) {
            switch (validation.overallStatus()) {
                case INVALID -> base -= 0.25;
                case WARNING -> base -= 0.10;
                case SAFE -> { /* no penalty */ }
            }
        }
        // Very small or very large plans get a confidence penalty
        if (patchCount == 0) base -= 0.20;
        if (patchCount > 10) base -= 0.05;
        return Math.max(0.1, Math.min(0.95, base));
    }
}
