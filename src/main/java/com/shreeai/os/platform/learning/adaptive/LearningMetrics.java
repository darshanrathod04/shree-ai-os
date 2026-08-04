package com.shreeai.os.platform.learning.adaptive;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Immutable snapshot of learning metrics for a session.
 * Captures quiz performance, topic strengths/weaknesses, and learning speed.
 *
 * This is a value object — never mutated after creation.
 * Thread-safe by design (all fields are final and immutable).
 */
public final class LearningMetrics {

    private final double averageQuizScore;
    private final double learningSpeed;          // lessons per session
    private final int totalLessonsCompleted;
    private final int totalQuizzesTaken;
    private final List<String> weakTopics;
    private final List<String> strongTopics;

    public LearningMetrics(double averageQuizScore,
                           double learningSpeed,
                           int totalLessonsCompleted,
                           int totalQuizzesTaken,
                           List<String> weakTopics,
                           List<String> strongTopics) {
        this.averageQuizScore = averageQuizScore;
        this.learningSpeed = learningSpeed;
        this.totalLessonsCompleted = totalLessonsCompleted;
        this.totalQuizzesTaken = totalQuizzesTaken;
        this.weakTopics = weakTopics != null
                ? Collections.unmodifiableList(List.copyOf(weakTopics))
                : List.of();
        this.strongTopics = strongTopics != null
                ? Collections.unmodifiableList(List.copyOf(strongTopics))
                : List.of();
    }

    public double getAverageQuizScore() { return averageQuizScore; }
    public double getLearningSpeed() { return learningSpeed; }
    public int getTotalLessonsCompleted() { return totalLessonsCompleted; }
    public int getTotalQuizzesTaken() { return totalQuizzesTaken; }
    public List<String> getWeakTopics() { return weakTopics; }
    public List<String> getStrongTopics() { return strongTopics; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LearningMetrics that)) return false;
        return Double.compare(averageQuizScore, that.averageQuizScore) == 0
                && Double.compare(learningSpeed, that.learningSpeed) == 0
                && totalLessonsCompleted == that.totalLessonsCompleted
                && totalQuizzesTaken == that.totalQuizzesTaken
                && weakTopics.equals(that.weakTopics)
                && strongTopics.equals(that.strongTopics);
    }

    @Override
    public int hashCode() {
        return Objects.hash(averageQuizScore, learningSpeed,
                totalLessonsCompleted, totalQuizzesTaken,
                weakTopics, strongTopics);
    }

    @Override
    public String toString() {
        return "LearningMetrics{" +
                "avgScore=" + String.format("%.1f", averageQuizScore) +
                ", speed=" + String.format("%.2f", learningSpeed) +
                ", completed=" + totalLessonsCompleted +
                ", quizzes=" + totalQuizzesTaken +
                ", weakTopics=" + weakTopics +
                ", strongTopics=" + strongTopics +
                '}';
    }
}