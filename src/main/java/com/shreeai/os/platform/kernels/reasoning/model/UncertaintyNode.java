package com.shreeai.os.platform.kernels.reasoning.model;

import java.util.Objects;

/**
 * <b>UncertaintyNode</b>
 *
 * <p>An immutable node in an {@link UncertaintyGraph}, carrying the
 * deterministic certainty score and level for a single verified reasoning
 * node.</p>
 *
 * <p><b>Ownership:</b> Reasoning Kernel - R5 Uncertainty Modeling</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param nodeId         the deterministic node id (never null or blank)
 * @param title          the resolved node title (never null or blank)
 * @param level          the locked certainty level (never null)
 * @param certaintyScore the deterministic certainty score within {@code [0.0,
 *                       1.0]}
 */
public record UncertaintyNode(
        String nodeId,
        String title,
        UncertaintyLevel level,
        double certaintyScore
) {

    /**
     * Compact constructor validating all fields.
     *
     * @throws NullPointerException     if nodeId title or level is null
     * @throws IllegalArgumentException if nodeId or title is blank or the
     *                                  certainty score is out of range
     */
    public UncertaintyNode {
        Objects.requireNonNull(nodeId, "nodeId must not be null");
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(level, "level must not be null");
        if (nodeId.isBlank()) {
            throw new IllegalArgumentException("nodeId must not be blank");
        }
        if (title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        if (certaintyScore < 0.0 || certaintyScore > 1.0) {
            throw new IllegalArgumentException(
                    "certaintyScore must be within [0.0, 1.0]: " + certaintyScore);
        }
    }

    @Override
    public String toString() {
        return String.format("UncertaintyNode{level=%s, title=%s, certainty=%.4f}",
                level, title, certaintyScore);
    }
}
