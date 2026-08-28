package com.shreeai.os.platform.legacy.learning;

import com.shreeai.os.platform.legacy.learning.curriculum.CurriculumService;
import com.shreeai.os.platform.legacy.learning.curriculum.LessonResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * LessonPromptBuilder generates teaching prompts using ONLY:
 * - Course (title, difficulty)
 * - Chapter (title, description)
 * - Lesson (title, objective, estimated time)
 * - Lesson markdown content (from curriculum .md files)
 * - Student question (optional)
 *
 * It does NOT include:
 * - Conversation history
 * - Knowledge graph
 * - Chief of staff
 * - Projects, roadmaps, goals
 * - Long memories / user profile
 *
 * Teaching prompts are kept under ~2500 characters.
 * The LLM only explains existing curriculum content — it never invents new syllabus.
 */
@Component
public class LessonPromptBuilder {

    private static final Logger log = LoggerFactory.getLogger(LessonPromptBuilder.class);
    private static final int MAX_PROMPT_LENGTH = 1200;

    private final CourseEngine courseEngine;
    private final CurriculumService curriculumService;

    public LessonPromptBuilder(CourseEngine courseEngine,
                                CurriculumService curriculumService) {
        this.courseEngine = courseEngine;
        this.curriculumService = curriculumService;
    }

    /**
     * Build a concise teaching prompt for the current lesson position.
     * Uses curriculum .md files for lesson content.
     *
     * @param courseState the session's course state
     * @return a prompt string < 2500 chars, ready for Ollama
     */
    public String buildLessonPrompt(CourseState courseState) {
        return buildLessonPrompt(courseState, null);
    }

    /**
     * Build a concise teaching prompt for the current lesson position,
     * including an optional user question (for TEACH_TOPIC).
     * Uses curriculum .md files for lesson content.
     *
     * @param courseState the session's course state
     * @param userQuestion optional user question (may be null)
     * @return a prompt string < 2500 chars, ready for Ollama
     */
    public String buildLessonPrompt(CourseState courseState, String userQuestion) {
        if (courseState == null || !courseState.hasActiveCourse()) {
            log.warn("[LessonPromptBuilder] No active course — returning fallback prompt");
            return "You are a tutor. Ask the user what they would like to learn.";
        }

        String courseName = courseState.getCourseName();
        int chapterNumber = courseState.getCurrentChapterIndex() + 1; // 1-based
        int lessonNumber = courseState.getCurrentLessonIndex() + 1;   // 1-based

        // Try to load lesson from curriculum first
        Optional<LessonResource> lessonOpt = curriculumService.loadLesson(
                courseName, chapterNumber, lessonNumber);

        if (lessonOpt.isPresent()) {
            return buildCurriculumPrompt(lessonOpt.get(), courseState, userQuestion);
        }

        // Fallback: build from course metadata (for courses without .md files yet)
        log.warn("[LessonPromptBuilder] No curriculum lesson found for {}/ch{}/lesson{} — using metadata fallback",
                courseName, chapterNumber, lessonNumber);
        return buildMetadataPrompt(courseState, userQuestion);
    }

    /**
     * Build prompt from a curriculum LessonResource (pre-written .md content).
     * The LLM only explains this existing content — it does not invent new material.
     */
    private String buildCurriculumPrompt(LessonResource lesson, CourseState courseState, String userQuestion) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Teach concisely. Course: ").append(courseState.getCourseName());
        prompt.append(" | Ch: ").append(courseState.getCurrentChapterIndex() + 1);
        prompt.append(" | Lesson: ").append(lesson.getTitle()).append("\n\n");

        // Lesson content from curriculum (compact)
        String content = lesson.toMarkdown();
        if (content.length() > 600) {
            content = content.substring(0, 600) + "\n[...]";
        }
        prompt.append(content).append("\n");

        // User question (for TEACH_TOPIC)
        if (userQuestion != null && !userQuestion.isBlank()) {
            prompt.append("Q: ").append(userQuestion).append("\n");
        }

        prompt.append("Explain this lesson only. Be concise.");

        // Enforce max length
        if (prompt.length() > MAX_PROMPT_LENGTH) {
            String truncated = prompt.substring(0, MAX_PROMPT_LENGTH - 50) +
                    "\n[truncated]";
            log.info("[PROMPT] Truncated from {} to {} chars", prompt.length(), truncated.length());
            return truncated;
        }

        log.info("[PROMPT] Curriculum prompt: {} chars for '{}'", prompt.length(), lesson.getTitle());
        return prompt.toString();
    }

    /**
     * Fallback: build prompt from course metadata (no .md file available).
     */
    private String buildMetadataPrompt(CourseState courseState, String userQuestion) {
        Optional<Course> courseOpt = courseEngine.getCourse(courseState.getCourseName());
        if (courseOpt.isEmpty()) {
            return "Course is no longer available.";
        }

        Course course = courseOpt.get();
        Optional<Chapter> chapterOpt = courseEngine.getCurrentChapter(courseState);
        if (chapterOpt.isEmpty()) {
            return "Unable to find the current chapter.";
        }

        Chapter chapter = chapterOpt.get();
        int lessonIdx = courseState.getCurrentLessonIndex();

        StringBuilder prompt = new StringBuilder();
        prompt.append("Teach concisely. ").append(course.getTitle());
        prompt.append(" | ").append(chapter.getTitle());

        if (lessonIdx >= 0 && lessonIdx < chapter.getLessons().size()) {
            Lesson lesson = chapter.getLessons().get(lessonIdx);
            prompt.append(" | ").append(lesson.getTitle());
            prompt.append(" | ").append(lesson.getObjective());
        }

        prompt.append("\n");
        if (userQuestion != null && !userQuestion.isBlank()) {
            prompt.append("Q: ").append(userQuestion).append("\n");
        }
        prompt.append("Teach now:");

        if (prompt.length() > MAX_PROMPT_LENGTH) {
            prompt = new StringBuilder(prompt.substring(0, MAX_PROMPT_LENGTH - 50) + "\n[truncated]");
        }

        log.info("[PROMPT] Metadata prompt: {} chars", prompt.length());
        return prompt.toString();
    }
}