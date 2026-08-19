package com.shreeai.os.platform.legacy.learning.quiz;

import java.time.Instant;
import java.util.Objects;

/**
 * Records a single answer attempt for a quiz question.
 * Immutable value object. Thread-safe.
 */
public final class QuizAttempt {

    private final String questionId;
    private final Object submittedAnswer;
    private final boolean correct;
    private final Instant timestamp;

    public QuizAttempt(String questionId, Object submittedAnswer, boolean correct) {
        this.questionId = Objects.requireNonNull(questionId, "questionId must not be null");
        this.submittedAnswer = submittedAnswer;
        this.correct = correct;
        this.timestamp = Instant.now();
    }

    public String getQuestionId() { return questionId; }
    public Object getSubmittedAnswer() { return submittedAnswer; }
    public boolean isCorrect() { return correct; }
    public Instant getTimestamp() { return timestamp; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QuizAttempt that)) return false;
        return questionId.equals(that.questionId) && timestamp.equals(that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(questionId, timestamp);
    }

    @Override
    public String toString() {
        return "QuizAttempt{question='" + questionId + "', correct=" + correct + "}";
    }
}