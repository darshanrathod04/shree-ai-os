package com.shreeai.os.platform.kernels.inference.model;

/**
 * <b>DecisionDimension</b>
 *
 * <p>The locked set of comparison dimensions reused from the I2
 * Trade-off Analysis. Each dimension carries the same semantic meaning
 * as its I2 counterpart but is weighted here to produce a single
 * optimization score.</p>
 *
 * <p><b>Locked dimensions:</b></p>
 * <ul>
 *   <li>{@link #TIME} - execution speed implied by the path length.</li>
 *   <li>{@link #COMPLEXITY} - prerequisite and dependency density.</li>
 *   <li>{@link #PRACTICALITY} - how directly the strategy applies the goal.</li>
 *   <li>{@link #LEARNING_DEPTH} - breadth of verified concepts covered.</li>
 *   <li>{@link #RISK} - exposure to verified uncertainty.</li>
 * </ul>
 *
 * <p><b>Immutability:</b> Enums are inherently immutable.</p>
 * <p><b>Thread Safety:</b> Enums are inherently thread-safe.</p>
 *
 * <p><b>Ownership:</b> Inference Kernel - I3 Decision Optimization</p>
 * <p><b>Version:</b> 1.0</p>
 */
public enum DecisionDimension {

    /** Execution speed implied by the path length. */
    TIME,

    /** Prerequisite and dependency density. */
    COMPLEXITY,

    /** How directly the strategy applies the goal. */
    PRACTICALITY,

    /** Breadth of verified concepts covered. */
    LEARNING_DEPTH,

    /** Exposure to verified uncertainty. */
    RISK
}