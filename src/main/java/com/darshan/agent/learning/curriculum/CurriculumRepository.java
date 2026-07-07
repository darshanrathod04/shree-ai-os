package com.darshan.agent.learning.curriculum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * CurriculumRepository provides access to curriculum resources.
 * It delegates to CourseCatalog for course metadata and LessonContentLoader
 * for lesson content (pre-written .md files).
 *
 * This is the curriculum-driven replacement for the old CourseRepository approach.
 * Lessons are deterministic — based on .md files, not LLM-generated.
 */
@Repository
public class CurriculumRepository {

    private static final Logger log = LoggerFactory.getLogger(CurriculumRepository.class);

    private final CourseCatalog courseCatalog;
    private final LessonContentLoader lessonContentLoader;
    private final ResourceLoader resourceLoader;

    public CurriculumRepository(CourseCatalog courseCatalog,
                                 LessonContentLoader lessonContentLoader,
                                 ResourceLoader resourceLoader) {
        this.courseCatalog = courseCatalog;
        this.lessonContentLoader = lessonContentLoader;
        this.resourceLoader = resourceLoader;
    }

    /**
     * Get course metadata from the curriculum catalog.
     */
    public Optional<CourseCatalog.CurriculumCourse> getCourse(String name) {
        return courseCatalog.getCourse(name);
    }

    /**
     * List all available courses in the curriculum.
     */
    public java.util.List<String> listCourses() {
        return courseCatalog.listCourses();
    }

    /**
     * Load the lesson resource for a specific course position.
     *
     * @param courseName    the course name
     * @param chapterNumber 1-based chapter number
     * @param lessonNumber  1-based lesson number
     * @return the LessonResource, or empty if not found
     */
    public Optional<LessonResource> loadLesson(String courseName, int chapterNumber, int lessonNumber) {
        return lessonContentLoader.loadLesson(courseName, chapterNumber, lessonNumber);
    }

    /**
     * Load quiz.json for a chapter.
     */
    public Optional<QuizResource> loadQuiz(String courseName, int chapterNumber) {
        return resourceLoader.loadQuiz(courseName, chapterNumber);
    }

    /**
     * Load assignment.json for a chapter.
     */
    public Optional<AssignmentResource> loadAssignment(String courseName, int chapterNumber) {
        return resourceLoader.loadAssignment(courseName, chapterNumber);
    }

    /**
     * Check if a lesson file exists at the given position.
     */
    public boolean hasLesson(String courseName, int chapterNumber, int lessonNumber) {
        return lessonContentLoader.hasLesson(courseName, chapterNumber, lessonNumber);
    }

    /**
     * Check if a course exists in the curriculum.
     */
    public boolean hasCourse(String name) {
        return courseCatalog.hasCourse(name);
    }
}