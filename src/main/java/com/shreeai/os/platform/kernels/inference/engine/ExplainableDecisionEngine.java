package com.shreeai.os.platform.kernels.inference.engine;

import com.shreeai.os.platform.kernels.context.model.GoalStructure;
import com.shreeai.os.platform.kernels.context.model.UserConstraints;
import com.shreeai.os.platform.kernels.inference.model.AlternativeSet;
import com.shreeai.os.platform.kernels.inference.model.CalibratedDecision;
import com.shreeai.os.platform.kernels.inference.model.ExplainableDecision;
import com.shreeai.os.platform.kernels.inference.model.OptimizedDecision;
import com.shreeai.os.platform.kernels.inference.model.TradeoffAnalysisSet;
import com.shreeai.os.platform.kernels.reasoning.model.VerificationGraph;

/**
 * <b>ExplainableDecisionEngine</b>
 *
 * <p>The I5 contract that converts every cognitive artifact into a single
 * {@link ExplainableDecision} object that any application can display. The
 * engine does not generate natural language - it generates structured
 * explanations. The UI, SDK or an LLM may later render the artifact into
 * English, Hindi, JSON, PDF or any other format.</p>
 *
 * <p><b>Locked explanation pipeline:</b></p>
 * <ol>
 *   <li><b>Goal Summary</b> - primary goal and explicit constraints, read
 *       from the context artifacts only.</li>
 *   <li><b>Selected Strategy</b> - the selected candidate's ordered steps.</li>
 *   <li><b>Trade-off Summary</b> - deterministic dimension descriptors for
 *       the selected candidate.</li>
 *   <li><b>Confidence Summary</b> - calibrated level, score and factor
 *       descriptors.</li>
 *   <li><b>Evidence Summary</b> - verified concept and contradiction counts
 *       with evidence coverage.</li>
 * </ol>
 *
 * <p><b>Contract:</b> implementations must be stateless, thread-safe,
 * deterministic and immutable. No LLM, no ML, no randomness, no planning and
 * no execution - only structured explanation assembly.</p>
 *
 * <p><b>Ownership:</b> Inference Kernel - I5 Explainable Decision</p>
 * <p><b>Version:</b> 1.0</p>
 */
public interface ExplainableDecisionEngine {

    /**
     * Produces the deterministic, structured explanation for the given
     * optimized and calibrated decision.
     *
     * <p>The explanation never alters any artifact - it only assembles the
     * locked five-section structure from existing cognitive artifacts.
     *
     * @param decision           the I3-optimized decision to explain (must
     *                           not be null)
     * @param calibratedDecision the I4-calibrated confidence (must not be
     *                           null)
     * @param alternativeSet     the I1-generated alternative set, for the
     *                           strategy steps (may be null)
     * @param analysisSet        the I2 trade-off analysis set, for the
     *                           trade-off summary (may be null)
     * @param goalStructure      the context goal structure, for the goal
     *                           summary (may be null)
     * @param constraints        the explicit user constraints, for the goal
     *                           summary (may be null)
     * @param verificationGraph  the R4 verification graph, for the evidence
     *                           summary (may be null)
     * @return the canonical, immutable explainable decision (never null)
     */
    ExplainableDecision explain(
            OptimizedDecision decision,
            CalibratedDecision calibratedDecision,
            AlternativeSet alternativeSet,
            TradeoffAnalysisSet analysisSet,
            GoalStructure goalStructure,
            UserConstraints constraints,
            VerificationGraph verificationGraph);
}