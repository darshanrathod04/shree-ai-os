package com.shreeai.os.platform.legacy.learning.adaptive;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Per-session student learning profile.
 * NOT a singleton — each session owns its own instance.
 *
 * Thread-safe via ReentrantReadWriteLock.
 * All mutating methods are protected by write lock.
 * All read-only methods are protected by read lock.
 *
 * Contains:
 * - completedLessons (by flat key: chapterIdx * 1000 + lessonIdx)
 * - weakTopics (topic names where quiz score < 60%)
 * - strongTopics (topic names where quiz score >= 80%)
 * - averageQuizScore (running average)
 * - learningSpeed (lessons completed / sessions engaged)
 * - preferredDifficulty (default NORMAL, auto-adjusted)
 * - learningStyle (default TEXTUAL, inferred over time)
 * - recommendedNextTopic (set by RecommendationEngine)
 * - sessionEngagementCount (number of times session was used)
 */
public class StudentLearningProfile {

    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();

    private final Set<Integer> completedLessons = new HashSet<>();
    private final Set<String> weakTopics = new HashSet<>();
    private final Set<String> strongTopics = new HashSet<>();

    private double averageQuizScore = 0.0;
    private int totalQuizScores = 0;
    private double learningSpeed = 1.0;
    private int sessionEngagementCount = 0;

    private DifficultyLevel preferredDifficulty = DifficultyLevel.NORMAL;
    private LearningStyle learningStyle = LearningStyle.TEXTUAL;
    private String recommendedNextTopic = "";

    private Instant createdAt = Instant.now();
    private Instant lastUpdated = Instant.now();

    public StudentLearningProfile() {
    }

    // --- Lesson tracking ---

    public void recordLessonCompleted(int chapterIdx, int lessonIdx) {
        writeLock.lock();
        try {
            int flatKey = chapterIdx * 1000 + lessonIdx;
            completedLessons.add(flatKey);
            lastUpdated = Instant.now();
        } finally {
            writeLock.unlock();
        }
    }

    public boolean isLessonCompleted(int chapterIdx, int lessonIdx) {
        readLock.lock();
        try {
            int flatKey = chapterIdx * 1000 + lessonIdx;
            return completedLessons.contains(flatKey);
        } finally {
            readLock.unlock();
        }
    }

    public int getTotalCompletedLessons() {
        readLock.lock();
        try {
            return completedLessons.size();
        } finally {
            readLock.unlock();
        }
    }

    public Set<Integer> getCompletedLessonKeys() {
        readLock.lock();
        try {
            return Collections.unmodifiableSet(new HashSet<>(completedLessons));
        } finally {
            readLock.unlock();
        }
    }

    // --- Quiz scoring ---

    /**
     * Record a quiz score (0.0 to 100.0) and update running average.
     * Auto-adjusts difficulty based on performance trend.
     *
     * @param score    the quiz score percentage (0-100)
     * @param topic    the topic name for weak/strong classification
     */
    public void recordQuizScore(double score, String topic) {
        writeLock.lock();
        try {
            // Update running average
            totalQuizScores++;
            averageQuizScore = averageQuizScore + (score - averageQuizScore) / totalQuizScores;

            // Classify topic
            if (score < 60.0 && !topic.isBlank()) {
                weakTopics.add(topic);
                strongTopics.remove(topic);
            } else if (score >= 80.0 && !topic.isBlank()) {
                strongTopics.add(topic);
                weakTopics.remove(topic);
            }

            // Auto-adjust difficulty based on sustained performance
            if (averageQuizScore >= 85.0 && totalQuizScores >= 3) {
                preferredDifficulty = preferredDifficulty.increase();
            } else if (averageQuizScore < 50.0 && totalQuizScores >= 2) {
                preferredDifficulty = preferredDifficulty.decrease();
            }

            lastUpdated = Instant.now();
        } finally {
            writeLock.unlock();
        }
    }

    public double getAverageQuizScore() {
        readLock.lock();
        try {
            return averageQuizScore;
        } finally {
            readLock.unlock();
        }
    }

    public int getTotalQuizScores() {
        readLock.lock();
        try {
            return totalQuizScores;
        } finally {
            readLock.unlock();
        }
    }

    // --- Weak / strong topics ---

    public List<String> getWeakTopics() {
        readLock.lock();
        try {
            return List.copyOf(weakTopics);
        } finally {
            readLock.unlock();
        }
    }

    public List<String> getStrongTopics() {
        readLock.lock();
        try {
            return List.copyOf(strongTopics);
        } finally {
            readLock.unlock();
        }
    }

    public void addWeakTopic(String topic) {
        writeLock.lock();
        try {
            if (topic != null && !topic.isBlank()) {
                weakTopics.add(topic);
                strongTopics.remove(topic);
                lastUpdated = Instant.now();
            }
        } finally {
            writeLock.unlock();
        }
    }

    public void addStrongTopic(String topic) {
        writeLock.lock();
        try {
            if (topic != null && !topic.isBlank()) {
                strongTopics.add(topic);
                weakTopics.remove(topic);
                lastUpdated = Instant.now();
            }
        } finally {
            writeLock.unlock();
        }
    }

    // --- Learning speed ---

    /**
     * Increment session engagement count (called each time session is used).
     * Learning speed = completed lessons / session engagement count.
     */
    public void incrementSessionEngagement() {
        writeLock.lock();
        try {
            sessionEngagementCount++;
            recalculateLearningSpeed();
            lastUpdated = Instant.now();
        } finally {
            writeLock.unlock();
        }
    }

    private void recalculateLearningSpeed() {
        if (sessionEngagementCount > 0) {
            learningSpeed = (double) completedLessons.size() / sessionEngagementCount;
        }
    }

    public double getLearningSpeed() {
        readLock.lock();
        try {
            return learningSpeed;
        } finally {
            readLock.unlock();
        }
    }

    public int getSessionEngagementCount() {
        readLock.lock();
        try {
            return sessionEngagementCount;
        } finally {
            readLock.unlock();
        }
    }

    // --- Difficulty ---

    public DifficultyLevel getPreferredDifficulty() {
        readLock.lock();
        try {
            return preferredDifficulty;
        } finally {
            readLock.unlock();
        }
    }

    public void setPreferredDifficulty(DifficultyLevel difficulty) {
        writeLock.lock();
        try {
            this.preferredDifficulty = difficulty != null ? difficulty : DifficultyLevel.NORMAL;
            lastUpdated = Instant.now();
        } finally {
            writeLock.unlock();
        }
    }

    // --- Learning style ---

    public LearningStyle getLearningStyle() {
        readLock.lock();
        try {
            return learningStyle;
        } finally {
            readLock.unlock();
        }
    }

    public void setLearningStyle(LearningStyle style) {
        writeLock.lock();
        try {
            this.learningStyle = style != null ? style : LearningStyle.TEXTUAL;
            lastUpdated = Instant.now();
        } finally {
            writeLock.unlock();
        }
    }

    // --- Recommendations ---

    public String getRecommendedNextTopic() {
        readLock.lock();
        try {
            return recommendedNextTopic;
        } finally {
            readLock.unlock();
        }
    }

    public void setRecommendedNextTopic(String topic) {
        writeLock.lock();
        try {
            this.recommendedNextTopic = topic != null ? topic : "";
            lastUpdated = Instant.now();
        } finally {
            writeLock.unlock();
        }
    }

    // --- Timestamps ---

    public Instant getCreatedAt() {
        readLock.lock();
        try {
            return createdAt;
        } finally {
            readLock.unlock();
        }
    }

    public Instant getLastUpdated() {
        readLock.lock();
        try {
            return lastUpdated;
        } finally {
            readLock.unlock();
        }
    }

    // --- Snapshot ---

    /**
     * Returns an immutable snapshot of current metrics for analysis.
     */
    public LearningMetrics toMetrics() {
        readLock.lock();
        try {
            return new LearningMetrics(
                    averageQuizScore,
                    learningSpeed,
                    completedLessons.size(),
                    totalQuizScores,
                    List.copyOf(weakTopics),
                    List.copyOf(strongTopics)
            );
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public String toString() {
        readLock.lock();
        try {
            return "StudentLearningProfile{" +
                    "completed=" + completedLessons.size() +
                    ", weakTopics=" + weakTopics +
                    ", strongTopics=" + strongTopics +
                    ", avgQuizScore=" + String.format("%.1f", averageQuizScore) +
                    ", speed=" + String.format("%.2f", learningSpeed) +
                    ", difficulty=" + preferredDifficulty +
                    ", style=" + learningStyle +
                    ", nextTopic='" + recommendedNextTopic + '\'' +
                    '}';
        } finally {
            readLock.unlock();
        }
    }
}