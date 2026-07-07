package com.darshan.agent.learning;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * LearningSessionEngine is responsible ONLY for learning session business logic.
 * It delegates to CourseEngine for course structure access and manages
 * per-session CourseState transitions. For lesson content generation,
 * it delegates to TeachingEngine → LessonPromptBuilder → Ollama.
 *
 * This ensures teaching requests go through:
 *   Intent → START_COURSE / CONTINUE_LESSON / TEACH_TOPIC
 *   → LearningSessionEngine → TeachingEngine → LessonPromptBuilder → Ollama
 *
 * NOT through ChatSkill or PromptBuilder.
 *
 * All state is session-isolated via CourseState (owned by ConversationSession).
 * No static mutable state.
 */
@Component
public class LearningSessionEngine {

    private static final Logger log = LoggerFactory.getLogger(LearningSessionEngine.class);

    private final CourseEngine courseEngine;
    private final TeachingEngine teachingEngine;

    public LearningSessionEngine(CourseEngine courseEngine,
                                  TeachingEngine teachingEngine) {
        this.courseEngine = courseEngine;
        this.teachingEngine = teachingEngine;
    }

    /**
     * Start a course for the given session's CourseState.
     * Initializes the course and generates the first lesson via TeachingEngine.
     */
    public String startCourse(String courseName, CourseState courseState) {
        if (courseState == null) {
            return "No session state available.";
        }

        // Check if course exists before mutating state
        Optional<Course> courseOpt = courseEngine.getCourse(courseName);
        if (courseOpt.isEmpty()) {
            List<String> available = courseEngine.listCourses();
            return "Course '" + courseName + "' not found. Available courses: "
                    + String.join(", ", available);
        }

        // Initialize session state
        String courseNameResolved = courseOpt.get().getName();
        courseState.setCourseName(courseNameResolved);
        courseState.markStarted();
        courseState.setCurrentChapterIndex(0);
        courseState.setCurrentLessonIndex(0);
        courseState.touch();

        log.info("[LEARNING] Intent=START_COURSE Course={} Chapter=1 Lesson=1",
                courseNameResolved);

        // Generate the first lesson via TeachingEngine
        return teachingEngine.generateLesson(courseState);
    }

    /**
     * Continue to the next lesson/chapter in the active course.
     * Advances position and generates content via TeachingEngine.
     */
    public String continueLesson(CourseState courseState) {
        if (courseState == null || !courseState.hasActiveCourse()) {
            return "No active course. Say 'start course <name>' to begin.";
        }

        // Advance to next lesson via CourseEngine
        String advanceResult = courseEngine.advanceLesson(courseState);
        courseState.touch();

        log.info("[LEARNING] Intent=CONTINUE_LESSON Course={} Chapter={} Lesson={}",
                courseState.getCourseName(),
                courseState.getCurrentChapterIndex() + 1,
                courseState.getCurrentLessonIndex() + 1);

        // If advance returned a completion message, return it (course completed)
        if (advanceResult.contains("completed") && advanceResult.contains("🎉")) {
            return advanceResult;
        }

        // Generate the next lesson via TeachingEngine
        return teachingEngine.generateLesson(courseState);
    }

    /**
     * Mark the current lesson as completed and advance to the next.
     * Records the completed lesson in the session's CourseState.
     * Returns the next lesson generated via TeachingEngine.
     */
    public String completeLesson(CourseState courseState) {
        if (courseState == null || !courseState.hasActiveCourse()) {
            return "No active course. Say 'start course <name>' to begin.";
        }

        // Record current lesson as completed
        int chapterIdx = courseState.getCurrentChapterIndex();
        int lessonIdx = courseState.getCurrentLessonIndex();

        Optional<Chapter> chapterOpt = courseEngine.getCurrentChapter(courseState);
        if (chapterOpt.isPresent()) {
            Chapter chapter = chapterOpt.get();
            if (lessonIdx < chapter.getLessons().size()) {
                Lesson lesson = chapter.getLessons().get(lessonIdx);
                courseState.recordLessonCompleted(chapterIdx, lessonIdx);

                // Check if all lessons in this chapter are completed
                boolean allLessonsDone = true;
                for (int i = 0; i < chapter.getLessons().size(); i++) {
                    if (!courseState.isLessonCompleted(chapterIdx, i)) {
                        allLessonsDone = false;
                        break;
                    }
                }
                if (allLessonsDone) {
                    courseState.recordChapterCompleted(chapterIdx);
                }
            }
        }

        courseState.touch();

        // Advance to next lesson
        String advanceResult = courseEngine.advanceLesson(courseState);

        if (advanceResult.contains("completed") && advanceResult.contains("🎉")) {
            return advanceResult;
        }

        // Generate next lesson via TeachingEngine
        return teachingEngine.generateLesson(courseState);
    }

    /**
     * Get the current lesson display for the active course.
     * Generates content via TeachingEngine if needed.
     */
    public String currentLesson(CourseState courseState) {
        if (courseState == null || !courseState.hasActiveCourse()) {
            return "No active course. Say 'start course <name>' to begin.";
        }

        courseState.touch();

        log.info("[LEARNING] Intent=CURRENT_LESSON Course={} Chapter={} Lesson={}",
                courseState.getCourseName(),
                courseState.getCurrentChapterIndex() + 1,
                courseState.getCurrentLessonIndex() + 1);

        // Generate current lesson via TeachingEngine
        return teachingEngine.generateLesson(courseState);
    }

    /**
     * Explain a specific topic within the current lesson context.
     * Uses TeachingEngine with a user question.
     */
    public String teachTopic(String topic, CourseState courseState) {
        if (courseState == null || !courseState.hasActiveCourse()) {
            return "No active course. Say 'start course <name>' to begin.";
        }

        courseState.touch();

        log.info("[LEARNING] Intent=TEACH_TOPIC Course={} Topic='{}'",
                courseState.getCourseName(), topic);

        // Generate lesson with user question via TeachingEngine
        return teachingEngine.generateLesson(courseState, topic);
    }

    /**
     * Repeat the current lesson.
     * Re-generates the same lesson via TeachingEngine.
     */
    public String repeatLesson(CourseState courseState) {
        if (courseState == null || !courseState.hasActiveCourse()) {
            return "No active course. Say 'start course <name>' to begin.";
        }

        courseState.touch();

        log.info("[LEARNING] Intent=REPEAT_LESSON Course={} Chapter={} Lesson={}",
                courseState.getCourseName(),
                courseState.getCurrentChapterIndex() + 1,
                courseState.getCurrentLessonIndex() + 1);

        // Re-generate current lesson via TeachingEngine
        return teachingEngine.generateLesson(courseState);
    }

    /**
     * Get a detailed progress summary for the active course.
     * Includes completed lessons/chapters, timestamps, and status.
     */
    public String progress(CourseState courseState) {
        if (courseState == null || !courseState.hasActiveCourse()) {
            return "No active course.";
        }

        courseState.touch();

        // Get the basic progress summary from CourseEngine
        String basicSummary = courseEngine.getProgressSummary(courseState);

        // Append detailed tracking info
        StringBuilder sb = new StringBuilder(basicSummary);
        sb.append("\n\n📈 **Detailed Progress**\n");
        sb.append("Completed Lessons: ").append(courseState.getTotalCompletedLessons()).append("\n");
        sb.append("Completed Chapters: ").append(courseState.getTotalCompletedChapters()).append("\n");
        sb.append("Status: ").append(courseState.getLearningStatus()).append("\n");

        if (courseState.getStartedAt() != null) {
            sb.append("Started: ").append(courseState.getStartedAt()).append("\n");
        }
        if (courseState.getLastAccess() != null) {
            sb.append("Last Access: ").append(courseState.getLastAccess()).append("\n");
        }

        return sb.toString();
    }

    /**
     * Exit the active course and reset the session's course state.
     */
    public String exitCourse(CourseState courseState) {
        if (courseState == null || !courseState.hasActiveCourse()) {
            return "No active course to exit.";
        }

        String courseName = courseState.getCourseName();
        courseState.reset();
        return "Exited course '" + courseName + "'. Your progress has been saved.";
    }

    /**
     * Check if the session has an active course.
     */
    public boolean hasActiveCourse(CourseState courseState) {
        return courseState != null && courseState.hasActiveCourse();
    }
}