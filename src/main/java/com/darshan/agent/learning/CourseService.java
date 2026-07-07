package com.darshan.agent.learning;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Manages course access and per-session course progress.
 * All learning state is session-isolated via CourseState objects passed as parameters.
 * No singleton mutable learning state — state lives in ConversationSession.
 */
@Service
public class CourseService {

    private final CourseRepository repository;

    public CourseService(CourseRepository repository) {
        this.repository = repository;
    }

    /**
     * Get a course by name (case-insensitive).
     */
    public Optional<Course> getCourse(String name) {
        return repository.getCourse(name);
    }

    /**
     * List all available course names.
     */
    public List<String> listCourses() {
        return repository.listCourses();
    }

    /**
     * Start a course for a specific session's CourseState.
     * Returns the course overview. Does NOT modify any global state.
     *
     * @param courseName the name of the course to start
     * @param courseState the per-session course state to initialize
     * @return a formatted course overview string
     */
    public String startCourse(String courseName, CourseState courseState) {
        Optional<Course> courseOpt = repository.getCourse(courseName);
        if (courseOpt.isEmpty()) {
            return "Course '" + courseName + "' not found. Available courses: " + String.join(", ", repository.listCourses());
        }

        Course course = courseOpt.get();
        courseState.setCourseName(course.getName());
        courseState.setCurrentChapterIndex(0);
        courseState.setCurrentLessonIndex(0);
        courseState.setCompleted(false);

        StringBuilder sb = new StringBuilder();
        sb.append("📚 Starting Course: ").append(course.getTitle()).append("\n\n");
        sb.append(course.getDescription()).append("\n\n");
        sb.append("Difficulty: ").append(course.getDifficulty()).append("\n");
        sb.append("Estimated: ").append(course.getEstimatedHours()).append(" hours\n");
        sb.append("Chapters: ").append(course.getTotalChapters()).append("\n\n");

        if (!course.getPrerequisites().isEmpty()) {
            sb.append("Prerequisites:\n");
            for (String prereq : course.getPrerequisites()) {
                sb.append("  • ").append(prereq).append("\n");
            }
            sb.append("\n");
        }

        sb.append("Start with chapter 1: ").append(course.getChapters().get(0).getTitle());

        return sb.toString();
    }

    /**
     * Get the current chapter for a given session's CourseState.
     * Returns empty if no active course or course not found.
     */
    public Optional<Chapter> getCurrentChapter(CourseState courseState) {
        if (!courseState.hasActiveCourse()) {
            return Optional.empty();
        }

        Optional<Course> courseOpt = repository.getCourse(courseState.getCourseName());
        if (courseOpt.isEmpty()) {
            return Optional.empty();
        }

        Course course = courseOpt.get();
        int index = courseState.getCurrentChapterIndex();
        if (index < 0 || index >= course.getChapters().size()) {
            return Optional.empty();
        }

        return Optional.of(course.getChapters().get(index));
    }

    /**
     * Advance to the next chapter/lesson for a session's CourseState.
     * Returns a formatted string with the next step, or completion message.
     */
    public String advanceLesson(CourseState courseState) {
        if (!courseState.hasActiveCourse()) {
            return "No active course. Say 'start course <name>' to begin.";
        }

        Optional<Course> courseOpt = repository.getCourse(courseState.getCourseName());
        if (courseOpt.isEmpty()) {
            return "Course no longer available.";
        }

        Course course = courseOpt.get();
        int chapterIdx = courseState.getCurrentChapterIndex();
        int lessonIdx = courseState.getCurrentLessonIndex();

        if (chapterIdx >= course.getChapters().size()) {
            courseState.setCompleted(true);
            return "🎉 Congratulations! You've completed the '" + course.getTitle() + "' course!";
        }

        Chapter currentChapter = course.getChapters().get(chapterIdx);

        // Try advancing to next lesson in current chapter
        if (lessonIdx + 1 < currentChapter.getLessons().size()) {
            courseState.setCurrentLessonIndex(lessonIdx + 1);
            Lesson lesson = currentChapter.getLessons().get(lessonIdx + 1);
            return formatLesson(currentChapter, lesson, courseState);
        }

        // Move to next chapter
        if (chapterIdx + 1 < course.getChapters().size()) {
            courseState.setCurrentChapterIndex(chapterIdx + 1);
            courseState.setCurrentLessonIndex(0);
            Chapter nextChapter = course.getChapters().get(chapterIdx + 1);
            return formatChapter(nextChapter, courseState);
        }

        // Course completed
        courseState.setCompleted(true);
        return "🎉 Congratulations! You've completed the '" + course.getTitle() + "' course!";
    }

    /**
     * Format a chapter overview for display.
     */
    public String formatChapter(Chapter chapter, CourseState courseState) {
        StringBuilder sb = new StringBuilder();
        sb.append("📖 Chapter ").append(courseState.getCurrentChapterIndex() + 1)
                .append(": ").append(chapter.getTitle()).append("\n\n");
        sb.append(chapter.getDescription()).append("\n\n");

        if (!chapter.getLessons().isEmpty()) {
            sb.append("Lessons (").append(chapter.getTotalLessons()).append("):\n");
            for (int i = 0; i < chapter.getLessons().size(); i++) {
                Lesson lesson = chapter.getLessons().get(i);
                sb.append("  ").append(i + 1).append(". ").append(lesson.getTitle());
                if (lesson.getEstimatedMinutes() > 0) {
                    sb.append(" (").append(lesson.getEstimatedMinutes()).append(" min)");
                }
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    /**
     * Format a specific lesson for display.
     */
    public String formatLesson(Chapter chapter, Lesson lesson, CourseState courseState) {
        StringBuilder sb = new StringBuilder();
        sb.append("📘 Chapter ").append(courseState.getCurrentChapterIndex() + 1)
                .append(" — ").append(lesson.getTitle()).append("\n\n");
        sb.append("Objective: ").append(lesson.getObjective()).append("\n");
        if (lesson.getEstimatedMinutes() > 0) {
            sb.append("Duration: ").append(lesson.getEstimatedMinutes()).append(" minutes\n");
        }
        sb.append("\nSay 'next' to continue or 'chapter overview' for the full chapter plan.");
        return sb.toString();
    }

    /**
     * Get a formatted progress summary for the session's course state.
     */
    public String getProgressSummary(CourseState courseState) {
        if (!courseState.hasActiveCourse()) {
            return "No active course.";
        }

        Optional<Course> courseOpt = repository.getCourse(courseState.getCourseName());
        if (courseOpt.isEmpty()) {
            return "Course no longer available.";
        }

        Course course = courseOpt.get();
        int chapterIdx = courseState.getCurrentChapterIndex();
        int totalChapters = course.getTotalChapters();
        double progress = totalChapters > 0 ? (chapterIdx * 100.0) / totalChapters : 0;

        String chapterName = chapterIdx < totalChapters
                ? course.getChapters().get(chapterIdx).getTitle()
                : "Completed";

        StringBuilder sb = new StringBuilder();
        sb.append("📊 Course Progress\n\n");
        sb.append("Course: ").append(course.getTitle()).append("\n");
        sb.append("Chapter: ").append(Math.min(chapterIdx + 1, totalChapters))
                .append(" / ").append(totalChapters).append("\n");
        sb.append("Current: ").append(chapterName).append("\n");
        sb.append("Progress: ").append(String.format("%.0f%%", Math.min(progress, 100))).append("\n");

        if (courseState.isCompleted()) {
            sb.append("\n🎉 Course completed!");
        }

        return sb.toString();
    }
}