package com.shreeai.os.platform.learning.curriculum;

import java.util.Objects;

/**
 * Immutable lesson resource loaded from a .md file on the classpath.
 * Contains ONLY the pre-written curriculum content:
 * title, objective, explanation, example, real world example, summary, practice.
 */
public final class LessonResource {

    private final String title;
    private final String objective;
    private final String explanation;
    private final String example;
    private final String realWorldExample;
    private final String summary;
    private final String practice;

    public LessonResource(String title, String objective, String explanation,
                          String example, String realWorldExample,
                          String summary, String practice) {
        this.title = Objects.requireNonNull(title, "title must not be null");
        this.objective = Objects.requireNonNull(objective, "objective must not be null");
        this.explanation = Objects.requireNonNull(explanation, "explanation must not be null");
        this.example = Objects.requireNonNull(example, "example must not be null");
        this.realWorldExample = Objects.requireNonNull(realWorldExample, "realWorldExample must not be null");
        this.summary = Objects.requireNonNull(summary, "summary must not be null");
        this.practice = Objects.requireNonNull(practice, "practice must not be null");
    }

    public String getTitle() { return title; }
    public String getObjective() { return objective; }
    public String getExplanation() { return explanation; }
    public String getExample() { return example; }
    public String getRealWorldExample() { return realWorldExample; }
    public String getSummary() { return summary; }
    public String getPractice() { return practice; }

    /**
     * Build the full markdown content for this lesson.
     * This is the content that LessonPromptBuilder uses.
     */
    public String toMarkdown() {
        StringBuilder sb = new StringBuilder();
        sb.append("# ").append(title).append("\n\n");
        sb.append("## Objective\n").append(objective).append("\n\n");
        sb.append("## Explanation\n").append(explanation).append("\n\n");
        sb.append("## Example\n").append(example).append("\n\n");
        sb.append("## Real World Example\n").append(realWorldExample).append("\n\n");
        sb.append("## Summary\n").append(summary).append("\n\n");
        sb.append("## Practice\n").append(practice).append("\n");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LessonResource that)) return false;
        return title.equals(that.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title);
    }

    @Override
    public String toString() {
        return "LessonResource{title='" + title + "'}";
    }
}