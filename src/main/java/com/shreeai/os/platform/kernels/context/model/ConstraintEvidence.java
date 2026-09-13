package com.shreeai.os.platform.kernels.context.model;

import java.util.Objects;

/**
 * <b>ConstraintEvidence</b>
 *
 * <p>Provides explainability for extracted constraints by recording the source
 * text and position of each constraint in the original user input.</p>
 *
 * <p>This enables future Validation & Observability to trace where each
 * constraint originated, supporting debugging and user transparency.</p>
 *
 * <p><b>Immutability:</b> This record is immutable.</p>
 * <p><b>Thread Safety:</b> Records are inherently thread-safe.</p>
 *
 * <p><b>Ownership:</b> Context Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param field the constraint field name (e.g., "duration", "budget")
 * @param matchedText the exact text that was matched
 * @param startIndex the start position in the original input
 * @param endIndex the end position in the original input
 */
public record ConstraintEvidence(
        String field,
        String matchedText,
        int startIndex,
        int endIndex
) {
    /**
     * Creates a new ConstraintEvidence with validation.
     *
     * @param field the constraint field name (must not be null)
     * @param matchedText the exact text that was matched (must not be null)
     * @param startIndex the start position (must be non-negative)
     * @param endIndex the end position (must be >= startIndex)
     * @throws NullPointerException if field or matchedText is null
     * @throws IllegalArgumentException if indices are invalid
     */
    public ConstraintEvidence {
        Objects.requireNonNull(field, "field must not be null");
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
        return String.format("ConstraintEvidence{field='%s', matchedText='%s', start=%d, end=%d}",
                field, matchedText, startIndex, endIndex);
    }
}