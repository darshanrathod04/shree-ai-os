package platform.learning.quiz;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Immutable quiz result produced after quiz completion.
 * Contains total questions, correct/incorrect counts, percentage,
 * weak/strong topics, and pass/fail status.
 *
 * Thread-safe by design (all fields are final and immutable).
 */
public final class QuizResult {

    private final String quizTitle;
    private final int totalQuestions;
    private final int correctAnswers;
    private final int incorrectAnswers;
    private final double percentage;
    private final List<String> weakTopics;
    private final List<String> strongTopics;
    private final boolean passed;
    private static final double PASS_THRESHOLD = 60.0;

    public QuizResult(String quizTitle, int totalQuestions, int correctAnswers,
                      List<String> weakTopics, List<String> strongTopics) {
        this.quizTitle = quizTitle != null ? quizTitle : "Untitled Quiz";
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
        this.incorrectAnswers = totalQuestions - correctAnswers;
        this.percentage = totalQuestions > 0
                ? (double) correctAnswers / totalQuestions * 100.0
                : 0.0;
        this.weakTopics = weakTopics != null
                ? Collections.unmodifiableList(List.copyOf(weakTopics))
                : List.of();
        this.strongTopics = strongTopics != null
                ? Collections.unmodifiableList(List.copyOf(strongTopics))
                : List.of();
        this.passed = this.percentage >= PASS_THRESHOLD;
    }

    public String getQuizTitle() { return quizTitle; }
    public int getTotalQuestions() { return totalQuestions; }
    public int getCorrectAnswers() { return correctAnswers; }
    public int getIncorrectAnswers() { return incorrectAnswers; }
    public double getPercentage() { return percentage; }
    public List<String> getWeakTopics() { return weakTopics; }
    public List<String> getStrongTopics() { return strongTopics; }
    public boolean isPassed() { return passed; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QuizResult that)) return false;
        return totalQuestions == that.totalQuestions
                && correctAnswers == that.correctAnswers
                && Double.compare(percentage, that.percentage) == 0
                && passed == that.passed
                && quizTitle.equals(that.quizTitle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(quizTitle, totalQuestions, correctAnswers, percentage, passed);
    }

    @Override
    public String toString() {
        return "QuizResult{title='" + quizTitle + "', score=" + String.format("%.1f", percentage)
                + "%, passed=" + passed + ", correct=" + correctAnswers + "/" + totalQuestions + "}";
    }
}