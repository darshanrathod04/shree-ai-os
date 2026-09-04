package com.shreeai.os.platform.kernels.context.model;

public final class ResolvedContext {
    public enum Mode {
        COURSE_MODE,
        QUIZ_MODE,
        ROADMAP_MODE,
        DEFAULT_MODE
    }
    private final Mode mode;
    private final Object course;
    private final Object quiz;
    private final Object roadmap;

    private ResolvedContext(Mode mode, Object course, Object quiz, Object roadmap) {
        this.mode = mode != null ? mode : Mode.DEFAULT_MODE;
        this.course = course;
        this.quiz = quiz;
        this.roadmap = roadmap;
    }

    public Mode getMode() { return mode; }
    public Object getCourse() { return course; }
    public Object getQuiz() { return quiz; }
    public Object getRoadmap() { return roadmap; }

    public static ResolvedContext of(Mode mode, Object course, Object quiz, Object roadmap) {
        return new ResolvedContext(mode, course, quiz, roadmap);
    }

    public static ResolvedContext defaultContext() {
        return new ResolvedContext(Mode.DEFAULT_MODE, null, null, null);
    }

    @Override public String toString() {
        return "ResolvedContext{mode=" + mode + "}";
    }
}
