package com.shreeai.os.platform.kernels.inference.model;

import java.util.List;
import java.util.Objects;

/**
 * <b>TradeoffAnalysisSet</b>
 *
 * <p>The canonical, immutable output artifact of the I2 Trade-off Analysis
 * Engine: one deterministic comparison matrix per alternative, in the same
 * order as the input {@link AlternativeSet}.</p>
 *
 * <p>This artifact <em>compares</em> - it never selects a winner. Winner
 * selection belongs to I3 Decision Optimization.</p>
 *
 * <p><b>Canonical form:</b> analyses follow the input alternative order exactly.
 * No score sorting, no ranking and no reordering is ever applied.</p>
 *
 * <p><b>Immutability:</b> This record is deeply immutable - the analysis list is
 * defensively copied and unmodifiable, and every analysis is immutable.</p>
 * <p><b>Thread Safety:</b> Records are inherently thread-safe.</p>
 *
 * <p><b>Ownership:</b> Inference Kernel - I2 Trade-off Analysis</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param analyses the stably-ordered trade-off analyses (never null)
 */
public record TradeoffAnalysisSet(
        List<TradeoffAnalysis> analyses) {

    /**
     * Creates a deeply-immutable TradeoffAnalysisSet with defensive copying.
     *
     * @throws NullPointerException if analyses is null
     */
    public TradeoffAnalysisSet {
        Objects.requireNonNull(analyses, "analyses must not be null");
        analyses = List.copyOf(analyses);
    }

    /**
     * Returns an empty analysis set.
     *
     * @return an empty TradeoffAnalysisSet (never null)
     */
    public static TradeoffAnalysisSet empty() {
        return new TradeoffAnalysisSet(List.of());
    }

    /**
     * Returns the number of analyses in this set.
     *
     * @return the analysis count (never negative)
     */
    public int size() {
        return analyses.size();
    }

    /**
     * Returns true when this set holds no analyses.
     *
     * @return true when there are no analyses
     */
    public boolean isEmpty() {
        return analyses.isEmpty();
    }

    @Override
    public String toString() {
        return "TradeoffAnalysisSet{analyses=" + analyses.size() + "}";
    }
}