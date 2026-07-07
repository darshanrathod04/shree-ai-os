package com.darshan.agent.learning.adaptive;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * AdaptiveLearningEngine is the central decision-making component for
 * adaptive learning. It determines what the student should do next based on
 * their learning profile and performance metrics.
 *
 * RESPONSIBILITIES:
 * - Record lesson completion
 * - Record quiz scores
 * - Detect weak topics (quiz score < 60%)
 * - Detect strong topics (quiz score >= 80%)
 * - Recommend revision of weak topics
 * - Recommend next lesson
 * - Decide whether to repeat or skip content
 * - Adjust explanation difficulty (Easy / Normal / Advanced)
 *
 * ARCHITECTURE RULES:
 * - TeachingEngine explains. AdaptiveLearningEngine decides. RecommendationEngine recommends.
 * - Constructor injection only.
 * - No static mutable state.
 * - Thread-safe (delegates to thread-safe StudentLearningProfile).
 * - Per-session learning profile only (no singleton user state).
 *
 * RUNTIME LOGS:
 * [ADAPTIVE] for engine-level decisions
 * [LEARNING_PROFILE] for profile state changes
 * [RECOMMENDATION] for recommendation decisions (delegated to RecommendationEngine)
 */
@Component
public class AdaptiveLearningEngine {

    private static final Logger log = LoggerFactory.getLogger(AdaptiveLearningEngine.class);

    private final RecommendationEngine recommendationEngine;

    public AdaptiveLearningEngine(RecommendationEngine recommendationEngine) {
        this.recommendationEngine = recommendationEngine;
        log.info("[ADAPTIVE] AdaptiveLearningEngine initialized");
    }

    // ============================================================
    // Lesson tracking
    // ============================================================

    /**
     * Record a lesson as completed in the student's learning profile.
     *
     * @param profile    the per-session learning profile
     * @param chapterIdx the chapter index (0-based)
     * @param lessonIdx  the lesson index (0-based)
     */
    public void recordLessonCompleted(StudentLearningProfile profile,
                                       int chapterIdx,
                                       int lessonIdx) {
        if (profile == null) {
            log.warn("[ADAPTIVE] Cannot record lesson completion: profile is null");
            return;
        }
        profile.recordLessonCompleted(chapterIdx, lessonIdx);
        profile.incrementSessionEngagement();
        log.info("[LEARNING_PROFILE] Lesson recorded: chapter={} lesson={} total={}",
                chapterIdx + 1, lessonIdx + 1, profile.getTotalCompletedLessons());
    }

    // ============================================================
    // Quiz scoring
    // ============================================================

    /**
     * Record a quiz score and classify the topic as weak or strong.
     * Automatically adjusts preferred difficulty based on sustained performance.
     *
     * @param profile the per-session learning profile
     * @param score   the quiz score (0.0 to 100.0)
     * @param topic   the topic name for classification
     */
    public void recordQuizScore(StudentLearningProfile profile,
                                 double score,
                                 String topic) {
        if (profile == null) {
            log.warn("[ADAPTIVE] Cannot record quiz score: profile is null");
            return;
        }
        if (score < 0.0 || score > 100.0) {
            log.warn("[ADAPTIVE] Invalid quiz score: {} — must be 0-100", score);
            return;
        }
        profile.recordQuizScore(score, topic);
        log.info("[LEARNING_PROFILE] Quiz recorded: score={} topic='{}' avg={}",
                String.format("%.1f", score),
                topic,
                String.format("%.1f", profile.getAverageQuizScore()));
    }

    // ============================================================
    // Weak / strong topic detection
    // ============================================================

    /**
     * Detect weak topics from the learning profile.
     * Weak = quiz score < 60%.
     *
     * @param profile the per-session learning profile
     * @return list of weak topic names (may be empty)
     */
    public List<String> detectWeakTopics(StudentLearningProfile profile) {
        if (profile == null) {
            return List.of();
        }
        List<String> weakTopics = profile.getWeakTopics();
        if (!weakTopics.isEmpty()) {
            log.info("[ADAPTIVE] Weak topics detected: {}", weakTopics);
        }
        return weakTopics;
    }

    /**
     * Detect strong topics from the learning profile.
     * Strong = quiz score >= 80%.
     *
     * @param profile the per-session learning profile
     * @return list of strong topic names (may be empty)
     */
    public List<String> detectStrongTopics(StudentLearningProfile profile) {
        if (profile == null) {
            return List.of();
        }
        List<String> strongTopics = profile.getStrongTopics();
        if (!strongTopics.isEmpty()) {
            log.info("[ADAPTIVE] Strong topics detected: {}", strongTopics);
        }
        return strongTopics;
    }

    // ============================================================
    // Recommendations
    // ============================================================

    /**
     * Get a recommendation for the next learning step.
     * Delegates to RecommendationEngine for deterministic logic.
     *
     * @param profile          the per-session learning profile
     * @param courseName       the current course name
     * @param currentChapterIdx current chapter index (0-based)
     * @param currentLessonIdx  current lesson index (0-based)
     * @param hasMoreLessons   whether more lessons exist after current
     * @param isCourseComplete whether the course is complete
     * @return a structured Recommendation, never null
     */
    public Recommendation recommendNext(StudentLearningProfile profile,
                                         String courseName,
                                         int currentChapterIdx,
                                         int currentLessonIdx,
                                         boolean hasMoreLessons,
                                         boolean isCourseComplete) {
        Recommendation recommendation = recommendationEngine.recommendNext(
                profile, courseName, currentChapterIdx, currentLessonIdx,
                hasMoreLessons, isCourseComplete);

        // Update profile with recommended next topic
        if (profile != null && !recommendation.getRecommendedTopic().isBlank()) {
            profile.setRecommendedNextTopic(recommendation.getRecommendedTopic());
        }

        log.info("[ADAPTIVE] Recommendation: action={} topic='{}' difficulty={}",
                recommendation.getRecommendedAction(),
                recommendation.getRecommendedTopic(),
                recommendation.getSuggestedDifficulty());

        return recommendation;
    }

    /**
     * Get topics recommended for revision.
     *
     * @param profile the per-session learning profile
     * @return list of topic names to revise
     */
    public List<String> recommendRevisionTopics(StudentLearningProfile profile) {
        return recommendationEngine.recommendRevisionTopics(profile);
    }

    // ============================================================
    // Difficulty adjustment
    // ============================================================

    /**
     * Get the recommended difficulty level for the next lesson.
     *
     * @param profile the per-session learning profile
     * @return the recommended DifficultyLevel
     */
    public DifficultyLevel recommendDifficulty(StudentLearningProfile profile) {
        return recommendationEngine.recommendDifficulty(profile);
    }

    // ============================================================
    // Decision helpers
    // ============================================================

    /**
     * Decide whether the student should repeat the current lesson.
     *
     * @param profile the per-session learning profile
     * @return true if the lesson should be repeated
     */
    public boolean shouldRepeatLesson(StudentLearningProfile profile) {
        if (profile == null || profile.getTotalQuizScores() == 0) {
            return false;
        }
        boolean shouldRepeat = profile.getAverageQuizScore() < 60.0;
        if (shouldRepeat) {
            log.info("[ADAPTIVE] Decision: REPEAT lesson (avg score={})",
                    String.format("%.1f", profile.getAverageQuizScore()));
        }
        return shouldRepeat;
    }

    /**
     * Decide whether the student should skip ahead to more advanced content.
     *
     * @param profile the per-session learning profile
     * @return true if the student is ready to skip ahead
     */
    public boolean shouldSkipAhead(StudentLearningProfile profile) {
        if (profile == null || profile.getTotalQuizScores() < 3) {
            return false;
        }
        boolean shouldSkip = profile.getAverageQuizScore() >= 80.0
                && profile.getTotalCompletedLessons() >= 2;
        if (shouldSkip) {
            log.info("[ADAPTIVE] Decision: SKIP AHEAD (avg score={})",
                    String.format("%.1f", profile.getAverageQuizScore()));
        }
        return shouldSkip;
    }

    /**
     * Get a formatted summary of the student's learning profile for display.
     *
     * @param profile the per-session learning profile
     * @return a formatted string summary
     */
    public String getProfileSummary(StudentLearningProfile profile) {
        if (profile == null) {
            return "No learning profile available.";
        }

        LearningMetrics metrics = profile.toMetrics();
        StringBuilder sb = new StringBuilder();
        sb.append("📊 **Learning Profile**\n\n");
        sb.append("• Lessons Completed: ").append(metrics.getTotalLessonsCompleted()).append("\n");
        sb.append("• Average Quiz Score: ").append(String.format("%.1f", metrics.getAverageQuizScore())).append("%\n");
        sb.append("• Learning Speed: ").append(String.format("%.2f", metrics.getLearningSpeed())).append(" lessons/session\n");
        sb.append("• Preferred Difficulty: ").append(profile.getPreferredDifficulty()).append("\n");
        sb.append("• Learning Style: ").append(profile.getLearningStyle()).append("\n");

        if (!metrics.getWeakTopics().isEmpty()) {
            sb.append("• ⚠️ Weak Topics: ").append(String.join(", ", metrics.getWeakTopics())).append("\n");
        }
        if (!metrics.getStrongTopics().isEmpty()) {
            sb.append("• ✅ Strong Topics: ").append(String.join(", ", metrics.getStrongTopics())).append("\n");
        }
        if (!profile.getRecommendedNextTopic().isBlank()) {
            sb.append("• Next Recommended: ").append(profile.getRecommendedNextTopic()).append("\n");
        }

        log.info("[LEARNING_PROFILE] Summary generated: {} lessons, {}% avg",
                metrics.getTotalLessonsCompleted(),
                String.format("%.1f", metrics.getAverageQuizScore()));

        return sb.toString();
    }
}