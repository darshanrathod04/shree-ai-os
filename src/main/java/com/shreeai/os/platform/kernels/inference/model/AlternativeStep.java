package com.shreeai.os.platform.kernels.inference.model;

import java.util.Objects;

/**
 * <b>AlternativeStep</b>
 *
 * <p>One immutable step of an {@link AlternativeCandidate}: a zero-based
 * deterministic position and the step title. Steps carry no duration, no cost,
 * no risk and no dependencies of their own - ordering and feasibility are
 * derived by the generation engine from the causal dependency DAG.</p>
 *
 * <p><b>Immutability:</b> This record is immutable.</p>
 * <p><b>Thread Safety:</b> Records are inherently thread-safe.</p>
 *
 * <p><b>Ownership:</b> Inference Kernel - I1 Alternative Generation</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param order the one-based deterministic position of the step within its
 *              candidate (must be &gt;= 1)
 * @param title the step title, carried over verbatim from verified knowledge
 *              (must not be null or blank)
 */
public record AlternativeStep(
        int order,
        String title) {

    /**
     * Creates a new AlternativeStep with validation.
     *
     * @throws NullPointerException     if title is null
     * @throws IllegalArgumentException if order is not positive or title is
     *                                  blank
     */
    public AlternativeStep {
        Objects.requireNonNull(title, "title must not be null");
        if (order < 1) {
            throw new IllegalArgumentException("order must be >= 1: " + order);
        }
        if (title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
    }

    @Override
    public String toString() {
        return String.format("AlternativeStep{%d=%s}", order, title);
    }
}
