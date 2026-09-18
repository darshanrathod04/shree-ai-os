package com.shreeai.os.platform.kernels.acquisition.model;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * <b>SourceSelectionPlan</b>
 *
 * <p>The canonical, immutable output artifact of the K0.6.3 Trust &amp; Source
 * Selection Engine: a deterministic decision binding every acquisition target
 * to exactly one concrete, trusted source.</p>
 *
 * <p>This artifact answers exactly one question and nothing more - <em>among
 * all available providers, which concrete source should be selected?</em> It
 * contains no downloaded content, no crawled pages, no parsed documents and no
 * ingestion instructions; those belong to K0.6.4+.</p>
 *
 * <p><b>Canonical form:</b> selections follow the canonical order of the
 * {@link AcquisitionPlan} they were derived from (priority descending
 * CRITICAL&rarr;LOW, then topic name ascending), so the plan is stable and
 * reproducible. Topics for which no eligible source exists in the registry are
 * omitted - the engine never fabricates a source.</p>
 *
 * <p><b>Immutability:</b> This record is deeply immutable - the selection list
 * is defensively copied and unmodifiable, and every contained
 * {@link SelectedSource} (and its {@link SourceCandidate}) is immutable.</p>
 * <p><b>Thread Safety:</b> Records are inherently thread-safe.</p>
 *
 * <p><b>Ownership:</b> Knowledge Acquisition Kernel - K0.6.3 Trust &amp; Source Selection</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param selections the stably-ordered source selections (never null)
 */
public record SourceSelectionPlan(
        List<SelectedSource> selections) {

    /**
     * Creates a deeply-immutable SourceSelectionPlan with defensive copying.
     *
     * @throws NullPointerException if selections is null
     */
    public SourceSelectionPlan {
        Objects.requireNonNull(selections, "selections must not be null");
        selections = List.copyOf(selections);
    }

    /**
     * Returns an empty selection plan (no selections).
     *
     * @return an empty SourceSelectionPlan (never null)
     */
    public static SourceSelectionPlan empty() {
        return new SourceSelectionPlan(List.of());
    }

    /**
     * Returns the number of source selections in this plan.
     *
     * @return the selection count
     */
    public int size() {
        return selections.size();
    }

    /**
     * Looks up the selection made for a specific topic id.
     *
     * @param topicId the deterministic topic identifier
     * @return the matching selection, or empty when the topic has no selected
     *         source (never null)
     */
    public Optional<SelectedSource> selectionFor(String topicId) {
        if (topicId == null) {
            return Optional.empty();
        }
        return selections.stream()
                .filter(selection -> selection.topicId().equals(topicId))
                .findFirst();
    }

    /**
     * Returns the selected topic names in canonical plan order.
     *
     * @return an immutable list of topic names (never null)
     */
    public List<String> selectedTopicNames() {
        return selections.stream().map(SelectedSource::topicName).toList();
    }

    /**
     * Returns the selected source names in canonical plan order.
     *
     * @return an immutable list of source names (never null)
     */
    public List<String> selectedSourceNames() {
        return selections.stream().map(SelectedSource::sourceName).toList();
    }

    @Override
    public String toString() {
        return "SourceSelectionPlan{selections=" + selections.size() + "}";
    }
}