package com.darshan.agent.learning.curriculum;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Immutable quiz resource loaded from quiz.json files.
 * Represents a chapter-level quiz with multiple question types.
 * Present for future Quiz Engine compatibility.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class QuizResource {

    private String title;
    private String difficulty;
    private List<QuizQuestion> questions;

    public QuizResource() {}

    public String getTitle() { return title; }
    public String getDifficulty() { return difficulty; }
    public List<QuizQuestion> getQuestions() {
        return questions != null ? Collections.unmodifiableList(questions) : List.of();
    }

    public void setTitle(String title) { this.title = title; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public void setQuestions(List<QuizQuestion> questions) { this.questions = questions; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class QuizQuestion {
        private String type;
        private String question;
        private List<String> options;
        private Object correctAnswer;
        private String explanation;

        public QuizQuestion() {}

        public String getType() { return type; }
        public String getQuestion() { return question; }
        public List<String> getOptions() { return options != null ? Collections.unmodifiableList(options) : List.of(); }
        public Object getCorrectAnswer() { return correctAnswer; }
        public String getExplanation() { return explanation; }

        public void setType(String type) { this.type = type; }
        public void setQuestion(String question) { this.question = question; }
        public void setOptions(List<String> options) { this.options = options; }
        public void setCorrectAnswer(Object correctAnswer) { this.correctAnswer = correctAnswer; }
        public void setExplanation(String explanation) { this.explanation = explanation; }
    }
}