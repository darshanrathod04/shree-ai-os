package com.shreeai.os.platform.kernels.inference.model;

/**
 * <b>ExplanationSectionType</b>
 *
 * <p>The locked, closed set of section types used by the I5 Explainable
 * Decision Engine to structure the canonical {@link ExplainableDecision}
 * artifact.</p>
 *
 * <p><b>Locked section order:</b></p>
 * <ol>
 *   <li>{@link #GOAL} - the captured user goal and constraints</li>
 *   <li>{@link #STRATEGY} - the selected strategy and its ordered steps</li>
 *   <li>{@link #TRADEOFF} - the deterministic trade-off summary</li>
 *   <li>{@link #CONFIDENCE} - the calibrated confidence summary</li>
 *   <li>{@link #EVIDENCE} - the verified evidence summary</li>
 * </ol>
 *
 * <p><b>Immutability:</b> Enums are inherently immutable.</p>
 * <p><b>Thread Safety:</b> Enums are inherently thread-safe.</p>
 *
 * <p><b>Ownership:</b> Inference Kernel - I5 Explainable Decision</p>
 * <p><b>Version:</b> 1.0</p>
 */
public enum ExplanationSectionType {

    /** The captured user goal and constraints (context artifacts only). */
    GOAL,

    /** The selected strategy and its ordered execution steps. */
    STRATEGY,

    /** The deterministic trade-off summary of the selected candidate. */
    TRADEOFF,

    /** The calibrated confidence summary of the optimized decision. */
    CONFIDENCE,

    /** The verified evidence summary from the verification graph. */
    EVIDENCE
}