package com.shreeai.os.platform.kernels.inference.model;

/**
 * <b>AlternativeType</b>
 *
 * <p>The closed, deterministic strategy categories of the I1 Alternative
 * Generation Engine. A type classifies <em>how</em> a solution alternative is
 * structured - it is a strategy category, never a quality label, never a
 * ranking and never a preference.</p>
 *
 * <p><b>Locked strategy semantics:</b></p>
 * <table border="1">
 *   <caption>Locked strategy semantics</caption>
 *   <tr><th>Type</th><th>Strategy</th></tr>
 *   <tr><td>{@link #SEQUENTIAL}</td><td>Strict prerequisite order - every step
 *       depends only on earlier steps.</td></tr>
 *   <tr><td>{@link #ACCELERATED}</td><td>Independent branches merged per
 *       dependency level - fewer, denser steps.</td></tr>
 *   <tr><td>{@link #PRACTICAL}</td><td>Project-first path - apply early, anchor
 *       progress with practice steps.</td></tr>
 *   <tr><td>{@link #THEORETICAL}</td><td>Concept-heavy ordering - foundation
 *       concepts that unlock the most dependents come first.</td></tr>
 *   <tr><td>{@link #BALANCED}</td><td>Mix of theory and practice - interleaves
 *       the sequential and theoretical orderings.</td></tr>
 * </table>
 *
 * <p><b>Locked output order</b> (defined by the generation engine, never by
 * score): BALANCED, SEQUENTIAL, PRACTICAL, ACCELERATED, THEORETICAL. Ranking
 * belongs to I2 Trade-off Analysis.</p>
 *
 * <p><b>Immutability:</b> Enums are inherently immutable.</p>
 * <p><b>Thread Safety:</b> Enums are inherently thread-safe.</p>
 *
 * <p><b>Ownership:</b> Inference Kernel - I1 Alternative Generation</p>
 * <p><b>Version:</b> 1.0</p>
 */
public enum AlternativeType {

    /** Strict prerequisite order - every step depends only on earlier steps. */
    SEQUENTIAL,

    /** Independent branches merged per dependency level - denser steps. */
    ACCELERATED,

    /** Project-first path - apply early with practice anchors. */
    PRACTICAL,

    /** Concept-heavy ordering - foundation concepts unlock the most. */
    THEORETICAL,

    /** Mix of theory and practice - interleaved orderings. */
    BALANCED
}
