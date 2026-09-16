package com.shreeai.os.platform.kernels.inference.engine;

import com.shreeai.os.platform.kernels.inference.model.AlternativeSet;
import com.shreeai.os.platform.kernels.reasoning.model.CausalGraph;
import com.shreeai.os.platform.kernels.reasoning.model.ReasoningGraph;
import com.shreeai.os.platform.kernels.reasoning.model.SynthesisGraph;
import com.shreeai.os.platform.kernels.reasoning.model.VerificationGraph;

/**
 * <b>AlternativeGenerationEngine</b>
 *
 * <p>The I1 contract that transforms verified knowledge into multiple
 * executable solution alternatives. The engine never chooses the best
 * alternative - selection belongs to I2 Trade-off Analysis; its single
 * responsibility is deterministic strategy generation.</p>
 *
 * <p><b>Locked generation pipeline:</b></p>
 * <ol>
 *   <li><b>Collect Verified Concepts</b> - only {@code VERIFIED} concept nodes
 *       of the {@link ReasoningGraph} (filtered through the
 *       {@link VerificationGraph}); synthesized facts are the deterministic
 *       fallback. The raw prompt is never used.</li>
 *   <li><b>Build Dependency DAG</b> - causal cause→effect edges of the
 *       {@link CausalGraph} become learn-prerequisite edges.</li>
 *   <li><b>Generate Strategy Variants</b> - at most five candidates, one per
 *       {@link com.shreeai.os.platform.kernels.inference.model.AlternativeType},
 *       each derived deterministically from the DAG.</li>
 *   <li><b>Feasibility</b> - the locked formula
 *       {@code completedPrerequisites / totalSteps}, clamped to {@code [0,1]}.
 *       No ML, no prediction.</li>
 *   <li><b>Stable Ordering</b> - BALANCED, SEQUENTIAL, PRACTICAL, ACCELERATED,
 *       THEORETICAL - never reordered by score.</li>
 * </ol>
 *
 * <p><b>Contract:</b> implementations must be stateless, thread-safe,
 * deterministic and immutable. No LLM, no randomness, no planning, no
 * optimization, no ranking - only alternatives.</p>
 *
 * <p><b>Ownership:</b> Inference Kernel - I1 Alternative Generation</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see DefaultAlternativeGenerationEngine
 */
public interface AlternativeGenerationEngine {

    /**
     * Generates the deterministic alternative set for the given verified
     * reasoning artifacts. Null graphs are treated as empty; with no verified
     * concepts the empty set is returned.
     *
     * @param reasoningGraph    the R1 multi-hop reasoning graph (may be null)
     * @param synthesisGraph    the R2 evidence synthesis graph (may be null)
     * @param causalGraph       the R3 causal graph defining the dependency DAG
     *                          (may be null)
     * @param verificationGraph the R4 verification graph filtering to verified
     *                          knowledge (may be null)
     * @return the canonical, immutable alternative set (never null)
     */
    AlternativeSet generate(ReasoningGraph reasoningGraph,
                            SynthesisGraph synthesisGraph,
                            CausalGraph causalGraph,
                            VerificationGraph verificationGraph);
}
