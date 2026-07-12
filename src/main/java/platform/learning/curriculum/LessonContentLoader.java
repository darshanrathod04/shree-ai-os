package platform.learning.curriculum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Facade for loading lesson content from curriculum resources.
 * Parses markdown lesson files into structured LessonResource objects.
 * Course content is deterministic — all lessons come from pre-written .md files.
 */
@Component
public class LessonContentLoader {

    private static final Logger log = LoggerFactory.getLogger(LessonContentLoader.class);

    private final ResourceLoader resourceLoader;

    public LessonContentLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * Load a lesson resource from the curriculum for the given course/position.
     *
     * @param courseName    the course name (e.g., "java")
     * @param chapterNumber 1-based chapter number
     * @param lessonNumber  1-based lesson number
     * @return parsed LessonResource, or empty if not found
     */
    public Optional<LessonResource> loadLesson(String courseName, int chapterNumber, int lessonNumber) {
        Optional<String> markdownOpt = resourceLoader.loadLessonMarkdown(courseName, chapterNumber, lessonNumber);
        if (markdownOpt.isEmpty()) {
            log.warn("[CURRICULUM] Lesson not found: {}/chapter{}/lesson{}", courseName, chapterNumber, lessonNumber);
            return Optional.empty();
        }

        String markdown = markdownOpt.get();
        try {
            LessonResource lesson = parseLessonMarkdown(markdown);
            log.info("[CURRICULUM] Lesson Loaded: course={} chapter={} lesson={} title='{}'",
                    courseName, chapterNumber, lessonNumber, lesson.getTitle());
            return Optional.of(lesson);
        } catch (IllegalArgumentException e) {
            log.error("[CURRICULUM] Failed to parse lesson: {}/chapter{}/lesson{} - {}",
                    courseName, chapterNumber, lessonNumber, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Parse markdown lesson content into a structured LessonResource.
     * Expected format with sections:
     *   Title: ...
     *   Objective: ...
     *   ## Explanation
     *   ## Example
     *   ## Real World Example
     *   ## Summary
     *   ## Practice
     */
    private LessonResource parseLessonMarkdown(String markdown) {
        String title = extractField(markdown, "Title:");
        String objective = extractField(markdown, "Objective:");
        String explanation = extractSection(markdown, "## Explanation", "## Example");
        String example = extractSection(markdown, "## Example", "## Real World Example");
        String realWorldExample = extractSection(markdown, "## Real World Example", "## Summary");
        String summary = extractSection(markdown, "## Summary", "## Practice");
        String practice = extractSection(markdown, "## Practice", null);

        return new LessonResource(title, objective, explanation, example, realWorldExample, summary, practice);
    }

    private String extractField(String markdown, String fieldName) {
        int idx = markdown.indexOf(fieldName);
        if (idx == -1) return "";
        int start = idx + fieldName.length();
        int end = markdown.indexOf('\n', start);
        if (end == -1) end = markdown.length();
        return markdown.substring(start, end).trim();
    }

    private String extractSection(String markdown, String sectionStart, String nextSection) {
        int startIdx = markdown.indexOf(sectionStart);
        if (startIdx == -1) return "";
        startIdx = markdown.indexOf('\n', startIdx);
        if (startIdx == -1) return "";

        int endIdx;
        if (nextSection != null) {
            endIdx = markdown.indexOf("\n" + nextSection, startIdx);
            if (endIdx == -1) endIdx = markdown.indexOf(nextSection, startIdx);
        } else {
            endIdx = markdown.length();
        }
        if (endIdx == -1) endIdx = markdown.length();

        return markdown.substring(startIdx, endIdx).trim();
    }

    /**
     * Check if a lesson file exists for the given course/position.
     */
    public boolean hasLesson(String courseName, int chapterNumber, int lessonNumber) {
        return resourceLoader.loadLessonMarkdown(courseName, chapterNumber, lessonNumber).isPresent();
    }
}