package com.shreeai.os.platform.kernels.context.model;

import java.util.Objects;

/**
 * <b>GoalEvidence</b>
 *
 * <p>Provides explainability for identified goals by recording the source
 * text and position of each goal in the original user input.</p>
 *
 * <p><b>Immutability:</b> This record is immutable.</p>
 * <p><b>Thread Safety:</b> Records are inherently thread-safe.</p>
 *
 * <p><b>Ownership:</b> Context Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param matchedText the exact text that was matched
 * @param startIndex the start position in the original input
 * @param endIndex the end position in the original input
 */
public record GoalEvidence(
        String matchedText,
        int startIndex,
        int endIndex
) {
    /**
     * Creates a new GoalEvidence with validation.
     *
     * @param matchedText the exact text that was matched (must not be null)
     * @param startIndex the start position (must be non-negative)
     * @param endIndex the end position (must be >= startIndex)
     * @throws NullPointerException if matchedText is null
     * @throws IllegalArgumentException if indices are invalid
     */
    public GoalEvidence {
        Objects.requireNonNull(matchedText, "matchedText must not be null");
        if (startIndex < 0) {
            throw new IllegalArgumentException("startIndex must be non-negative");
        }
        if (endIndex < startIndex) {
            throw new IllegalArgumentException("endIndex must be >= startIndex");
        }
    }

    @Override
    public String toString() {
        return String.format("GoalEvidence{matchedText='%s', start=%d, end=%d}",
                matchedText, startIndex, endIndex);
    }
}