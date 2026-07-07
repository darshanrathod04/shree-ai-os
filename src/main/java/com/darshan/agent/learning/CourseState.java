package com.darshan.agent.learning;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * Per-session course learning progress.
 * Stored within ConversationSession to ensure session isolation.
 * No static or singleton mutable state — each session owns its own CourseState.
 *
 * Tracks detailed progress: completed lessons, chapters, timestamps, and status.
 */
public class CourseState {

    public enum LearningStatus {
        NOT_STARTED,
        IN_PROGRESS,
        COMPLETED
    }

    private String courseName;
    private int currentChapterIndex;
    private int currentLessonIndex;
    private boolean completed;

    private Set<Integer> completedLessons;   // flat indices (chapter * 100 + lesson)
    private Set<Integer> completedChapters;  // chapter indices
    private Instant startedAt;
    private Instant lastAccess;
    private LearningStatus learningStatus;

    public CourseState() {
        this.currentChapterIndex = 0;
        this.currentLessonIndex = 0;
        this.completed = false;
        this.completedLessons = new HashSet<>();
        this.completedChapters = new HashSet<>();
        this.startedAt = null;
        this.lastAccess = null;
        this.learningStatus = LearningStatus.NOT_STARTED;
    }

    // --- Core accessors (unchanged signature) ---

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public int getCurrentChapterIndex() { return currentChapterIndex; }
    public void setCurrentChapterIndex(int currentChapterIndex) { this.currentChapterIndex = currentChapterIndex; }

    public int getCurrentLessonIndex() { return currentLessonIndex; }
    public void setCurrentLessonIndex(int currentLessonIndex) { this.currentLessonIndex = currentLessonIndex; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    // --- New fields ---

    public Set<Integer> getCompletedLessons() { return completedLessons; }
    public void setCompletedLessons(Set<Integer> completedLessons) { this.completedLessons = completedLessons; }

    public Set<Integer> getCompletedChapters() { return completedChapters; }
    public void setCompletedChapters(Set<Integer> completedChapters) { this.completedChapters = completedChapters; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public Instant getLastAccess() { return lastAccess; }
    public void setLastAccess(Instant lastAccess) { this.lastAccess = lastAccess; }

    public LearningStatus getLearningStatus() { return learningStatus; }
    public void setLearningStatus(LearningStatus learningStatus) { this.learningStatus = learningStatus; }

    // --- Convenience methods ---

    public void touch() {
        this.lastAccess = Instant.now();
    }

    public void markStarted() {
        if (startedAt == null) {
            startedAt = Instant.now();
        }
        lastAccess = Instant.now();
        learningStatus = LearningStatus.IN_PROGRESS;
        completed = false;
    }

    public void markCourseCompleted() {
        this.completed = true;
        this.learningStatus = LearningStatus.COMPLETED;
        this.lastAccess = Instant.now();
    }

    public void recordLessonCompleted(int chapterIdx, int lessonIdx) {
        int flatKey = chapterIdx * 1000 + lessonIdx;
        completedLessons.add(flatKey);
        touch();
    }

    public boolean isLessonCompleted(int chapterIdx, int lessonIdx) {
        int flatKey = chapterIdx * 1000 + lessonIdx;
        return completedLessons.contains(flatKey);
    }

    public void recordChapterCompleted(int chapterIdx) {
        completedChapters.add(chapterIdx);
        touch();
    }

    public boolean isChapterCompleted(int chapterIdx) {
        return completedChapters.contains(chapterIdx);
    }

    public int getTotalCompletedLessons() {
        return completedLessons != null ? completedLessons.size() : 0;
    }

    public int getTotalCompletedChapters() {
        return completedChapters != null ? completedChapters.size() : 0;
    }

    // --- Lifecycle ---

    public boolean hasActiveCourse() {
        return courseName != null && !courseName.isBlank();
    }

    public void reset() {
        this.courseName = null;
        this.currentChapterIndex = 0;
        this.currentLessonIndex = 0;
        this.completed = false;
        this.completedLessons = new HashSet<>();
        this.completedChapters = new HashSet<>();
        this.startedAt = null;
        this.lastAccess = null;
        this.learningStatus = LearningStatus.NOT_STARTED;
    }
}