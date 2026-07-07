package com.darshan.agent.learning.quiz;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Domain model for a single quiz question.
 * Wraps curriculum QuizResource.QuizQuestion data.
 * Immutable and thread-safe.
 */
public final class QuizQuestion {

    public enum QuestionType {
        MCQ,
        TRUE_FALSE,
        FILL_BLANK,
        CODING
    }

    private final String id;
    private final QuestionType type;
    private final String question;
    private final List<String> options;
    private final Object correctAnswer;
    private final String explanation;

    public QuizQuestion(String id, QuestionType type, String question,
                        List<String> options, Object correctAnswer, String explanation) {
        this.id = id;
        this.type = type;
        this.question = question;
        this.options = options != null
                ? Collections.unmodifiableList(List.copyOf(options))
                : List.of();
        this.correctAnswer = correctAnswer;
        this.explanation = explanation != null ? explanation : "";
    }

    public String getId() { return id; }
    public QuestionType getType() { return type; }
    public String getQuestion() { return question; }
    public List<String> getOptions() { return options; }
    public Object getCorrectAnswer() { return correctAnswer; }
    public String getExplanation() { return explanation; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QuizQuestion that)) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "QuizQuestion{id='" + id + "', type=" + type + "}";
    }
}