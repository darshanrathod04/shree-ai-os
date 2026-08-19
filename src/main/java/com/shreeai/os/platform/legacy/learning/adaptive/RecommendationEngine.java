package com.shreeai.os.platform.legacy.learning.adaptive;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * RecommendationEngine generates deterministic, structured recommendations
 * based on the student's learning profile and curriculum context.
 *
 * CRITICAL RULES:
 * - NEVER calls Ollama or any LLM.
 * - NEVER generates free text using a language model.
 * - Always returns structured Recommendation objects.
 * - All logic is deterministic (same input → same output).
 *
 * Used by AdaptiveLearningEngine to decide next actions.
 */
@Component
public class RecommendationEngine {

    private static final Logger log = LoggerFactory.getLogger(RecommendationEngine.class);

    private static final double WEAK_THRESHOLD = 60.0;
    private static final double STRONG_THRESHOLD = 80.0;

    public RecommendationEngine() {
        log.info("[RECOMMENDATION] RecommendationEngine initialized");
    }

    /**
     * Generate a recommendation for the next learning step.
     *
     * @param profile      the student's learning profile (never null)
     * @param courseName   the current course name
     * @param currentChapterIdx current chapter index (0-based)
     * @param currentLessonIdx  current lesson index (0-based)
     * @param hasMoreLessons    whether there are more lessons after the current one
     * @param isCourseComplete  whether the entire course is completed
     * @return a deterministic Recommendation, never null
     */
    public Recommendation recommendNext(StudentLearningProfile profile,
                                         String courseName,
                                         int currentChapterIdx,
                                         int currentLessonIdx,
                                         boolean hasMoreLessons,
                                         boolean isCourseComplete) {
        log.info("[RECOMMENDATION] Generating recommendation for course='{}' chapter={} lesson={}",
                courseName, currentChapterIdx + 1, currentLessonIdx + 1);

        if (profile == null) {
            log.warn("[RECOMMENDATION] Null profile — returning CONTINUE default");
            return new Recommendation(
                    Recommendation.Action.CONTINUE,
                    "",
                    DifficultyLevel.NORMAL,
                    List.of(),
                    "Start your learning journey."
            );
        }

        // Case 1: Course is complete
        if (isCourseComplete) {
            log.info("[RECOMMENDATION] Course='{}' is complete", courseName);
            return new Recommendation(
                    Recommendation.Action.COURSE_COMPLETE,
                    "",
                    profile.getPreferredDifficulty(),
                    profile.getWeakTopics(),
                    "Congratulations! You have completed the course. Review weak topics if needed."
            );
        }

        // Case 2: Weak topics exist — recommend revision
        List<String> weakTopics = profile.getWeakTopics();
        if (!weakTopics.isEmpty()) {
            String primaryWeakTopic = weakTopics.get(0);
            log.info("[RECOMMENDATION] Weak topics detected: {} — recommending revision", weakTopics);
            return new Recommendation(
                    Recommendation.Action.REVISE_WEAK,
                    primaryWeakTopic,
                    DifficultyLevel.EASY,
                    weakTopics,
                    "You scored below " + (int) WEAK_THRESHOLD + "% on: "
                            + String.join(", ", weakTopics)
                            + ". Revise these topics before continuing."
            );
        }

        // Case 3: High performance → consider skipping ahead
        List<String> strongTopics = profile.getStrongTopics();
        double avgScore = profile.getAverageQuizScore();
        if (avgScore >= STRONG_THRESHOLD && hasMoreLessons && profile.getTotalCompletedLessons() >= 2) {
            log.info("[RECOMMENDATION] Strong performance (avg={}) — recommending skip ahead", String.format("%.1f", avgScore));
            return new Recommendation(
                    Recommendation.Action.SKIP_AHEAD,
                    "",
                    profile.getPreferredDifficulty().increase(),
                    List.of(),
                    "Your average quiz score is " + String.format("%.1f", avgScore)
                            + "%. You are ready for more advanced content."
            );
        }

        // Case 4: Low performance → repeat current lesson
        if (avgScore < WEAK_THRESHOLD && profile.getTotalQuizScores() > 0) {
            log.info("[RECOMMENDATION] Low performance (avg={}) — recommending repeat", String.format("%.1f", avgScore));
            return new Recommendation(
                    Recommendation.Action.REPEAT,
                    "",
                    profile.getPreferredDifficulty().decrease(),
                    List.of(),
                    "Your quiz score is " + String.format("%.1f", avgScore)
                            + "%. Repeat this lesson with simpler explanations."
            );
        }

        // Case 5: Default — continue to next lesson
        log.info("[RECOMMENDATION] Default recommendation — CONTINUE");
        return new Recommendation(
                Recommendation.Action.CONTINUE,
                "",
                profile.getPreferredDifficulty(),
                List.of(),
                "Proceed to the next lesson in sequence."
        );
    }

    /**
     * Generate a list of topics recommended for revision.
     *
     * @param profile the student's learning profile
     * @return list of weak topic names (may be empty)
     */
    public List<String> recommendRevisionTopics(StudentLearningProfile profile) {
        if (profile == null) {
            return List.of();
        }
        List<String> weakTopics = profile.getWeakTopics();
        log.info("[RECOMMENDATION] Revision topics: {}", weakTopics);
        return weakTopics;
    }

    /**
     * Determine the appropriate difficulty level for the next lesson.
     *
     * @param profile the student's learning profile
     * @return the recommended DifficultyLevel
     */
    public DifficultyLevel recommendDifficulty(StudentLearningProfile profile) {
        if (profile == null) {
            return DifficultyLevel.NORMAL;
        }
        DifficultyLevel preferred = profile.getPreferredDifficulty();
        log.info("[RECOMMENDATION] Recommended difficulty: {}", preferred);
        return preferred;
    }
}