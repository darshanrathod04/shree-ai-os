package platform.learning.adaptive;

/**
 * Difficulty levels for adaptive lesson content.
 * Used by AdaptiveLearningEngine to adjust explanation complexity.
 *
 * EASY   → Simplified explanations, fewer technical terms, more examples
 * NORMAL → Standard explanations (default)
 * ADVANCED → Deeper dives, technical depth, edge cases, advanced concepts
 */
public enum DifficultyLevel {

    EASY,
    NORMAL,
    ADVANCED;

    /**
     * Returns the next harder difficulty level.
     */
    public DifficultyLevel increase() {
        return switch (this) {
            case EASY -> NORMAL;
            case NORMAL -> ADVANCED;
            case ADVANCED -> ADVANCED;
        };
    }

    /**
     * Returns the next easier difficulty level.
     */
    public DifficultyLevel decrease() {
        return switch (this) {
            case EASY -> EASY;
            case NORMAL -> EASY;
            case ADVANCED -> NORMAL;
        };
    }
}