package platform.learning.adaptive;

/**
 * Learning style preferences detected or configured for a student.
 * Used by AdaptiveLearningEngine to tailor content delivery.
 *
 * VISUAL     → Prefers diagrams, charts, code snippets, visual examples
 * TEXTUAL    → Prefers reading, written explanations, documentation-style
 * PRACTICAL  → Prefers hands-on examples, exercises, real-world code
 * CONCEPTUAL → Prefers theory, underlying principles, "why" explanations
 */
public enum LearningStyle {

    VISUAL,
    TEXTUAL,
    PRACTICAL,
    CONCEPTUAL
}