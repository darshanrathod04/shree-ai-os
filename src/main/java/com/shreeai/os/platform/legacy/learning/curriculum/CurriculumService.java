package com.shreeai.os.platform.legacy.learning.curriculum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for curriculum-driven course access.
 * Separated from CurriculumRepository to maintain Repository-Service separation.
 * All lesson content comes from pre-written curriculum resources (.md files).
 * The LLM explains existing lessons — it never invents new syllabus content.
 */
@Service
public class CurriculumService {

    private static final Logger log = LoggerFactory.getLogger(CurriculumService.class);

    private final CurriculumRepository curriculumRepository;

    public CurriculumService(CurriculumRepository curriculumRepository) {
        this.curriculumRepository = curriculumRepository;
    }

    /**
     * Get course metadata from the curriculum catalog.
     */
    public Optional<CourseCatalog.CurriculumCourse> getCourse(String name) {
        return curriculumRepository.getCourse(name);
    }

    /**
     * List all available course names.
     */
    public List<String> listCourses() {
        return curriculumRepository.listCourses();
    }

    /**
     * Check if a course exists in the curriculum.
     */
    public boolean hasCourse(String name) {
        return curriculumRepository.hasCourse(name);
    }

    /**
     * Load the lesson resource for a specific course position.
     * This is the primary method used by TeachingEngine to get lesson content.
     *
     * @param courseName    the course name
     * @param chapterNumber 1-based chapter number
     * @param lessonNumber  1-based lesson number
     * @return the LessonResource, or empty if not found
     */
    public Optional<LessonResource> loadLesson(String courseName, int chapterNumber, int lessonNumber) {
        return curriculumRepository.loadLesson(courseName, chapterNumber, lessonNumber);
    }

    /**
     * Get the total number of lessons in a specific chapter.
     * Counts by checking for lesson1.md, lesson2.md, etc. until not found.
     */
    public int getLessonCount(String courseName, int chapterNumber) {
        int count = 0;
        while (curriculumRepository.hasLesson(courseName, chapterNumber, count + 1)) {
            count++;
        }
        return count;
    }

    /**
     * Check if a lesson exists at the given position.
     */
    public boolean hasLesson(String courseName, int chapterNumber, int lessonNumber) {
        return curriculumRepository.hasLesson(courseName, chapterNumber, lessonNumber);
    }

    /**
     * Get a formatted course overview string.
     */
    public String getCourseOverview(String courseName) {
        Optional<CourseCatalog.CurriculumCourse> courseOpt = getCourse(courseName);
        if (courseOpt.isEmpty()) {
            return "Course '" + courseName + "' not found.";
        }

        CourseCatalog.CurriculumCourse course = courseOpt.get();
        StringBuilder sb = new StringBuilder();
        sb.append("📚 **").append(course.getTitle()).append("**\n\n");
        sb.append(course.getDescription()).append("\n\n");
        sb.append("Difficulty: ").append(course.getDifficulty()).append("\n");
        sb.append("Estimated: ").append(course.getEstimatedHours()).append(" hours\n");
        sb.append("Chapters: ").append(course.getTotalChapters()).append("\n\n");

        if (course.getPrerequisites() != null && !course.getPrerequisites().isEmpty()) {
            sb.append("Prerequisites:\n");
            for (String prereq : course.getPrerequisites()) {
                sb.append("  • ").append(prereq).append("\n");
            }
            sb.append("\n");
        }

        int i = 1;
        for (CourseCatalog.CurriculumCourse.ChapterEntry chapter : course.getChapters()) {
            int lessonCount = getLessonCount(courseName, i);
            sb.append("Chapter ").append(i).append(": ").append(chapter.getTitle());
            sb.append(" (").append(lessonCount).append(" lessons)\n");
            i++;
        }

        return sb.toString();
    }
}