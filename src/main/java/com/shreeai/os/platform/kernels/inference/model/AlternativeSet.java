package com.shreeai.os.platform.kernels.inference.model;

import java.util.List;
import java.util.Objects;

/**
 * <b>AlternativeSet</b>
 *
 * <p>The canonical, immutable output artifact of the I1 Alternative Generation
 * Engine: every deterministic, executable solution alternative for one verified
 * problem, in the locked output order.</p>
 *
 * <p>This artifact answers exactly one question and nothing more - <em>what
 * are the possible ways to reach the goal?</em> It contains no winner, no
 * ranking, no trade-offs and no decision; selection belongs to I2 Trade-off
 * Analysis.</p>
 *
 * <p><b>Canonical form:</b> candidates follow the locked output order
 * (BALANCED, SEQUENTIAL, PRACTICAL, ACCELERATED, THEORETICAL) - never a score
 * order. At most one candidate exists per strategy type, and never more than
 * five candidates.</p>
 *
 * <p><b>Immutability:</b> This record is deeply immutable - the candidate list
 * is defensively copied and unmodifiable, and every candidate is immutable.</p>
 * <p><b>Thread Safety:</b> Records are inherently thread-safe.</p>
 *
 * <p><b>Ownership:</b> Inference Kernel - I1 Alternative Generation</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param alternatives the stably-ordered solution alternatives in the locked
 *                     output order (never null)
 */
public record AlternativeSet(
        List<AlternativeCandidate> alternatives) {

    /**
     * Creates a deeply-immutable AlternativeSet with defensive copying.
     *
     * @throws NullPointerException if alternatives is null
     */
    public AlternativeSet {
        Objects.requireNonNull(alternatives, "alternatives must not be null");
        alternatives = List.copyOf(alternatives);
    }

    /**
     * Returns an empty alternative set (no candidates).
     *
     * @return an empty AlternativeSet (never null)
     */
    public static AlternativeSet empty() {
        return new AlternativeSet(List.of());
    }

    /**
     * Returns the number of alternatives in this set.
     *
     * @return the candidate count (never negative)
     */
    public int size() {
        return alternatives.size();
    }

    /**
     * Returns true when this set holds no alternatives.
     *
     * @return true when there are no candidates
     */
    public boolean isEmpty() {
        return alternatives.isEmpty();
    }

    @Override
    public String toString() {
        return "AlternativeSet{alternatives=" + alternatives.size() + "}";
    }
}
