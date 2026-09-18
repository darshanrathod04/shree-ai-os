package com.shreeai.os.platform.kernels.inference.engine;

import com.shreeai.os.platform.kernels.context.model.UserConstraints;
import com.shreeai.os.platform.kernels.inference.model.AlternativeSet;
import com.shreeai.os.platform.kernels.inference.model.CalibratedDecision;
import com.shreeai.os.platform.kernels.inference.model.OptimizedDecision;
import com.shreeai.os.platform.kernels.inference.model.TradeoffAnalysisSet;
import com.shreeai.os.platform.kernels.reasoning.model.UncertaintyGraph;
import com.shreeai.os.platform.kernels.reasoning.model.VerificationGraph;

/**
 * <b>ConfidenceCalibrationEngine</b>
 *
 * <p>The I4 contract that measures how trustworthy an {@link OptimizedDecision}
 * is, using only verified evidence and uncertainty. The engine never changes
 * the decision - it only calibrates confidence. It never uses LLM reasoning,
 * randomness, ML models, or hidden heuristics.</p>
 *
 * <p><b>Locked calibration pipeline:</b></p>
 * <ol>
 *   <li><b>Verified Evidence</b> - fraction of verified concepts:
 *       {@code verifiedConcepts / totalConcepts}.</li>
 *   <li><b>Uncertainty Penalty</b> - {@code 1 - averageUncertainty}, read from
 *       the {@link UncertaintyGraph}.</li>
 *   <li><b>Tradeoff Margin</b> - gap between the best and second-best
 *       optimization scores.</li>
 *   <li><b>Coverage</b> - verification completeness from the
 *       {@link VerificationGraph}.</li>
 *   <li><b>Final Confidence</b> - locked weighted formula clamped to
 *       {@code [0, 1]} and rounded to 4 decimals.</li>
 *   <li><b>Confidence Level</b> - mapped from the final confidence score.</li>
 * </ol>
 *
 * <p><b>Contract:</b> implementations must be stateless, thread-safe,
 * deterministic and immutable. No LLM, no ML, no randomness, no planning
 * and no execution - only confidence calibration.</p>
 *
 * <p><b>Ownership:</b> Inference Kernel - I4 Confidence Calibration</p>
 * <p><b>Version:</b> 1.0</p>
 */
public interface ConfidenceCalibrationEngine {

    /**
     * Produces the deterministic calibrated confidence for the given
     * optimized decision.
     *
     * <p>The calibration never alters the decision - it only measures
     * trustworthiness using verified evidence and uncertainty.
     *
     * @param decision           the I3-optimized decision to calibrate
     *                           (must not be null)
     * @param analysisSet        the I2 trade-off analysis set, for margin
     *                           computation (may be null/empty)
     * @param alternativeSet     the I1-generated alternative set, for tie
     *                           resolution in margin computation (may be null)
     * @param constraints        the explicit user constraints, for weight
     *                           profile reproduction (may be null)
     * @param verificationGraph  the verified knowledge graph (may be null)
     * @param uncertaintyGraph   the verified uncertainty graph (may be null)
     * @return the canonical, immutable calibrated decision (never null)
     */
    CalibratedDecision calibrate(
            OptimizedDecision decision,
            TradeoffAnalysisSet analysisSet,
            AlternativeSet alternativeSet,
            UserConstraints constraints,
            VerificationGraph verificationGraph,
            UncertaintyGraph uncertaintyGraph);
}