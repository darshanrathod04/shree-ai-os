package com.darshan.agent.learning;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * CourseEngine integrates with AgentBrain to provide structured course functionality.
 * Responsibilities:
 * - Load course definitions (delegated to CourseRepository)
 * - Initialize per-session learning state (delegated to CourseService with CourseState)
 * - Return deterministic course structure
 * - Does NOT call Ollama
 * - Does NOT modify existing planning architecture
 *
 * All learning state is session-isolated via CourseState objects.
 */
@Component
public class CourseEngine {

    private final CourseService courseService;

    public CourseEngine(CourseService courseService) {
        this.courseService = courseService;
    }

    /**
     * Start a course for a given session's CourseState.
     *
     * @param courseName the name of the course to start
     * @param courseState the per-session course state to initialize
     * @return formatted course overview
     */
    public String startCourse(String courseName, CourseState courseState) {
        return courseService.startCourse(courseName, courseState);
    }

    /**
     * Get the current chapter for a session's CourseState.
     */
    public Optional<Chapter> getCurrentChapter(CourseState courseState) {
        return courseService.getCurrentChapter(courseState);
    }

    /**
     * Advance to the next lesson/chapter for a session's CourseState.
     */
    public String advanceLesson(CourseState courseState) {
        return courseService.advanceLesson(courseState);
    }

    /**
     * List all available courses.
     */
    public List<String> listCourses() {
        return courseService.listCourses();
    }

    /**
     * Get a course by name.
     */
    public Optional<Course> getCourse(String name) {
        return courseService.getCourse(name);
    }

    /**
     * Get a formatted progress summary for a session's CourseState.
     */
    public String getProgressSummary(CourseState courseState) {
        return courseService.getProgressSummary(courseState);
    }

    /**
     * Check if a session has an active course.
     */
    public boolean hasActiveCourse(CourseState courseState) {
        return courseState != null && courseState.hasActiveCourse();
    }

    /**
     * Get the current chapter/lesson as a formatted string for display.
     */
    public String getCurrentLessonDisplay(CourseState courseState) {
        if (!hasActiveCourse(courseState)) {
            return "No active course. Say 'start course <name>' to begin.";
        }

        Optional<Chapter> chapterOpt = getCurrentChapter(courseState);
        if (chapterOpt.isEmpty()) {
            return "Unable to find current chapter.";
        }

        Chapter chapter = chapterOpt.get();
        int lessonIdx = courseState.getCurrentLessonIndex();

        if (lessonIdx < chapter.getLessons().size()) {
            Lesson lesson = chapter.getLessons().get(lessonIdx);
            return courseService.formatLesson(chapter, lesson, courseState);
        }

        return courseService.formatChapter(chapter, courseState);
    }
}