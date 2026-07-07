package com.darshan.agent.learning;

import com.darshan.agent.llm.OllamaClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * TeachingEngine is responsible ONLY for:
 * - building lesson prompts (via LessonPromptBuilder — NOT PromptBuilder)
 * - invoking Ollama for lesson content generation
 * - returning formatted lesson explanations
 *
 * This engine does NOT manage session state — that is the sole
 * responsibility of LearningSessionEngine.
 *
 * TeachingEngine is stateless. All methods are session-safe.
 *
 * Flow:
 *   Lesson → LessonPromptBuilder → Ollama → Formatted Lesson Response
 */
@Component
public class TeachingEngine {

    private static final Logger log = LoggerFactory.getLogger(TeachingEngine.class);

    private final LessonPromptBuilder lessonPromptBuilder;
    private final OllamaClient ollamaClient;

    public TeachingEngine(LessonPromptBuilder lessonPromptBuilder,
                          OllamaClient ollamaClient) {
        this.lessonPromptBuilder = lessonPromptBuilder;
        this.ollamaClient = ollamaClient;
    }

    /**
     * Generate lesson content by sending a prompt to Ollama.
     * Uses LessonPromptBuilder (NOT PromptBuilder) to build the prompt.
     *
     * @param courseState the session's course progress state
     * @return a formatted lesson explanation string
     */
    public String generateLesson(CourseState courseState) {
        return generateLesson(courseState, null);
    }

    /**
     * Generate lesson content with an optional user question (for TEACH_TOPIC).
     *
     * @param courseState the session's course progress state
     * @param userQuestion optional user question (may be null)
     * @return a formatted lesson explanation string
     */
    public String generateLesson(CourseState courseState, String userQuestion) {
        long start = System.currentTimeMillis();

        String prompt = lessonPromptBuilder.buildLessonPrompt(courseState, userQuestion);

        log.info("[TEACHING] PromptLength={} Model=phi3", prompt.length());

        try {
            String lessonContent = ollamaClient.generateDirect(prompt);
            long elapsed = System.currentTimeMillis() - start;
            log.info("[TEACHING] Duration={}ms Model=phi3", elapsed);
            log.info("[LESSON] Generated for course='{}' chapterIdx={} lessonIdx={}",
                    courseState.getCourseName(),
                    courseState.getCurrentChapterIndex(),
                    courseState.getCurrentLessonIndex());
            return formatLessonResponse(lessonContent, courseState);
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            log.error("[TEACHING] Failed after {}ms: {}", elapsed, e.getMessage());
            return "I encountered an error generating the lesson content. Please try again.\nError: " + e.getMessage();
        }
    }

    /**
     * Format the raw LLM response into a clean lesson display.
     * Adds navigation hints and progress indicators.
     */
    private String formatLessonResponse(String rawContent, CourseState courseState) {
        if (rawContent == null || rawContent.isBlank()) {
            return "I wasn't able to generate lesson content. Please try again.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("📘 **Lesson ").append(courseState.getCurrentLessonIndex() + 1)
                .append("**\n\n");
        sb.append(rawContent.trim()).append("\n\n");
        sb.append("---\n");
        sb.append("Say **'complete'** to mark this lesson done and continue.\n");
        sb.append("Say **'exit course'** to leave the course.\n");
        sb.append("Say **'progress'** to see your course progress.");

        return sb.toString();
    }
}