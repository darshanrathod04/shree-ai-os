package com.shreeai.os.platform.kernels.inference.engine;

import com.shreeai.os.platform.kernels.context.model.UserConstraints;
import com.shreeai.os.platform.kernels.inference.model.AlternativeSet;
import com.shreeai.os.platform.kernels.inference.model.OptimizedDecision;
import com.shreeai.os.platform.kernels.inference.model.TradeoffAnalysisSet;
import com.shreeai.os.platform.kernels.reasoning.model.UncertaintyGraph;
import com.shreeai.os.platform.kernels.reasoning.model.VerificationGraph;

/**
 * <b>DecisionOptimizationEngine</b>
 *
 * <p>The I3 contract that selects a single optimized decision from the
 * trade-off analysis set using deterministic weighted optimization.
 * The engine never uses LLM reasoning, randomness, ML models, or hidden
 * heuristics.</p>
 *
 * <p><b>Locked optimization pipeline:</b></p>
 * <ol>
 *   <li><b>Read Cognitive Artifacts</b> - consume only
 *       {@code AlternativeSet}, {@code TradeoffAnalysisSet},
 *       {@code UserConstraints}, {@code VerificationGraph} and
 *       {@code UncertaintyGraph}; never inspect the prompt.</li>
 *   <li><b>Determine Weight Profile</b> - explicit constraints
 *       override defaults; otherwise the default profile is used.
 *       No inferred priorities.</li>
 *   <li><b>Compute Optimization Score</b> - locked formula:
 *       {@code Σ(score_i × weight_i)}, clamped to {@code [0,1]},
 *       rounded to 4 decimals.</li>
 *   <li><b>Tie Resolution</b> - deterministic only: higher score,
 *       higher practicality, higher learning depth, fewer steps,
 *       candidate id (lexicographic).</li>
 *   <li><b>Generate Justifications</b> - deterministic template
 *       reasons based on score thresholds.</li>
 * </ol>
 *
 * <p><b>Contract:</b> implementations must be stateless, thread-safe,
 * deterministic and immutable. No LLM, no ML, no randomness, no
 * planning and no execution - only decision.</p>
 *
 * <p><b>Ownership:</b> Inference Kernel - I3 Decision Optimization</p>
 * <p><b>Version:</b> 1.0</p>
 */
public interface DecisionOptimizationEngine {

    /**
     * Produces the deterministic optimized decision from the given
     * trade-off analysis set.
     *
     * @param alternativeSet    the I1-generated alternative set (may be null/empty)
     * @param analysisSet       the I2-generated trade-off analysis set (may be null/empty)
     * @param constraints       the explicit user constraints (may be null)
     * @param verificationGraph the verified knowledge graph (may be null)
     * @param uncertaintyGraph  the verified uncertainty graph (may be null)
     * @return the canonical, immutable optimized decision (never null)
     */
    OptimizedDecision decide(
            AlternativeSet alternativeSet,
            TradeoffAnalysisSet analysisSet,
            UserConstraints constraints,
            VerificationGraph verificationGraph,
            UncertaintyGraph uncertaintyGraph);
}