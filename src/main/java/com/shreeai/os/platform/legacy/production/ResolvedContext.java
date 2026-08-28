package com.shreeai.os.platform.legacy.production;

/**
 * Immutable resolved context object.
 * Single source of truth for the current mode and state.
 * Produced by ContextResolutionEngine.
 */
public final class ResolvedContext {

    public enum Mode {
        CHAT,
        LEARNING,
        QUIZ,
        ROADMAP,
        PLANNING,
        AUTONOMOUS,
        GREETING
    }

    private final Mode mode;
    private final String courseName;
    private final int chapter;
    private final int lesson;
    private final boolean hasActiveQuiz;
    private final boolean hasActiveRoadmap;
    private final String goalName;
    private final boolean courseComplete;

    public ResolvedContext(Mode mode, String courseName, int chapter, int lesson,
                           boolean hasActiveQuiz, boolean hasActiveRoadmap,
                           String goalName, boolean courseComplete) {
        this.mode = mode;
        this.courseName = courseName;
        this.chapter = chapter;
        this.lesson = lesson;
        this.hasActiveQuiz = hasActiveQuiz;
        this.hasActiveRoadmap = hasActiveRoadmap;
        this.goalName = goalName;
        this.courseComplete = courseComplete;
    }

    public Mode getMode() { return mode; }
    public String getCourseName() { return courseName; }
    public int getChapter() { return chapter; }
    public int getLesson() { return lesson; }
    public boolean hasActiveQuiz() { return hasActiveQuiz; }
    public boolean hasActiveRoadmap() { return hasActiveRoadmap; }
    public String getGoalName() { return goalName; }
    public boolean isCourseComplete() { return courseComplete; }

    @Override
    public String toString() {
        return "ResolvedContext{mode=" + mode
                + ", course='" + courseName + '\''
                + ", chapter=" + chapter
                + ", lesson=" + lesson
                + ", quiz=" + hasActiveQuiz
                + ", roadmap=" + hasActiveRoadmap
                + ", goal='" + goalName + '\''
                + ", complete=" + courseComplete
                + '}';
    }
}