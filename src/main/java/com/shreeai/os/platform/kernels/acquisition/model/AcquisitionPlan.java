package com.shreeai.os.platform.kernels.acquisition.model;

import java.util.List;
import java.util.Objects;

/**
 * <b>AcquisitionPlan</b>
 *
 * <p>The canonical, immutable output artifact of the K0.6.2 Provider Router:
 * a deterministic routing decision assigning exactly one provider type to
 * every required knowledge topic.</p>
 *
 * <p>This artifact answers exactly one question - <em>which provider type
 * should satisfy each required knowledge topic?</em> - and nothing more. It
 * contains no URLs, no trust rankings, no documents and no ingestion
 * instructions; those belong to K0.6.3-K0.6.5.</p>
 *
 * <p><b>Canonical form:</b> targets are stably ordered (priority descending
 * CRITICAL&rarr;LOW, then topic name ascending) and deduplicated (one target
 * per topic). The plan is deeply immutable.</p>
 *
 * <p><b>Immutability:</b> This record is deeply immutable - the target list is
 * defensively copied and unmodifiable.</p>
 * <p><b>Thread Safety:</b> Records are inherently thread-safe.</p>
 *
 * <p><b>Ownership:</b> Knowledge Acquisition Kernel - K0.6.2 Provider Router</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param targets the stably-ordered acquisition targets (never null)
 */
public record AcquisitionPlan(
        List<AcquisitionTarget> targets) {

    /**
     * Creates a deeply-immutable AcquisitionPlan with defensive copying.
     *
     * @throws NullPointerException if targets is null
     */
    public AcquisitionPlan {
        Objects.requireNonNull(targets, "targets must not be null");
        targets = List.copyOf(targets);
    }

    /**
     * Returns an empty plan (no targets).
     *
     * @return an empty AcquisitionPlan (never null)
     */
    public static AcquisitionPlan empty() {
        return new AcquisitionPlan(List.of());
    }

    @Override
    public String toString() {
        return "AcquisitionPlan{targets=" + targets.size() + "}";
    }
}
