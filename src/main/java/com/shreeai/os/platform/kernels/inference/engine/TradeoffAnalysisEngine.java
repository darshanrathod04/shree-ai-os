package com.shreeai.os.platform.kernels.inference.engine;

import com.shreeai.os.platform.kernels.context.model.UserConstraints;
import com.shreeai.os.platform.kernels.inference.model.AlternativeSet;
import com.shreeai.os.platform.kernels.inference.model.TradeoffAnalysisSet;
import com.shreeai.os.platform.kernels.reasoning.model.UncertaintyGraph;
import com.shreeai.os.platform.kernels.reasoning.model.VerificationGraph;

/**
 * <b>TradeoffAnalysisEngine</b>
 *
 * <p>The I2 contract that compares every alternative candidate using the user's
 * constraints and verified cognitive artifacts, producing explainable trade-offs.
 * The engine never chooses the winner - selection belongs to I3 Decision
 * Optimization; its single responsibility is deterministic trade-off
 * comparison.</p>
 *
 * <p><b>Locked evaluation pipeline:</b></p>
 * <ol>
 *   <li><b>Read Alternatives</b> - consume only {@code AlternativeSet},
 *       {@code UserConstraints}, {@code VerificationGraph} and
 *       {@code UncertaintyGraph}; never inspect the prompt.</li>
 *   <li><b>Time Score</b> - deterministic rule: more steps → lower time score.
 *       Example: 3 steps → 1.00, 4 steps → 0.80, 5 steps → 0.60, 6+ steps →
 *       0.40.</li>
 *   <li><b>Complexity Score</b> - based on verified dependency count.
 *       Higher prerequisite density → higher complexity (normalized 0–1).</li>
 *   <li><b>Practicality Score</b> - locked constants by strategy type:
 *       PRACTICAL=1.00, BALANCED=0.90, ACCELERATED=0.80, SEQUENTIAL=0.70,
 *       THEORETICAL=0.60.</li>
 *   <li><b>Learning Depth</b> - verified concepts covered by the candidate's
 *       steps; more unique concepts → higher score.</li>
 *   <li><b>Risk Score</b> - use {@code UncertaintyGraph}: higher uncertainty
 *       → lower risk score. Example: {@code risk = 1 - uncertainty} (clamped).</li>
 *   <li><b>Build Summary</b> - deterministic template only, never LLM.</li>
 * </ol>
 *
 * <p><b>Contract:</b> implementations must be stateless, thread-safe,
 * deterministic and immutable. No LLM, no ML, no randomness, no planning and
 * no optimization - only comparison.</p>
 *
 * <p><b>Ownership:</b> Inference Kernel - I2 Trade-off Analysis</p>
 * <p><b>Version:</b> 1.0</p>
 */
public interface TradeoffAnalysisEngine {

    /**
     * Produces the deterministic trade-off analysis set for the given inputs.
     *
     * @param alternatives    the I1-generated alternative set (may be null/empty)
     * @param constraints     the explicit user constraints (may be null)
     * @param verificationGraph the verified knowledge graph (may be null)
     * @param uncertaintyGraph  the verified uncertainty graph (may be null)
     * @return the canonical, immutable trade-off analysis set (never null)
     */
    TradeoffAnalysisSet analyze(
            AlternativeSet alternatives,
            UserConstraints constraints,
            VerificationGraph verificationGraph,
            UncertaintyGraph uncertaintyGraph);
}