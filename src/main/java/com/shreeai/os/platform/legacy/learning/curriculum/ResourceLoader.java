package com.shreeai.os.platform.legacy.learning.curriculum;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Loads curriculum resource files (lesson .md, quiz.json, assignment.json) from the classpath.
 * All resources live under src/main/resources/curriculum/{courseName}/.
 */
@Component
public class ResourceLoader {

    private static final Logger log = LoggerFactory.getLogger(ResourceLoader.class);
    private static final String CURRICULUM_BASE = "curriculum/";

    private final ObjectMapper mapper = new ObjectMapper()
            .findAndRegisterModules();

    public ResourceLoader() {
    }

    /**
     * Load a markdown lesson file from the classpath.
     * Path format: curriculum/{courseName}/chapter{chapterNumber}/lesson{lessonNumber}.md
     *
     * @param courseName    the course name (e.g., "java")
     * @param chapterNumber the 1-based chapter number
     * @param lessonNumber  the 1-based lesson number
     * @return the raw markdown content, or empty if not found
     */
    public Optional<String> loadLessonMarkdown(String courseName, int chapterNumber, int lessonNumber) {
        String path = CURRICULUM_BASE + courseName + "/chapter" + chapterNumber + "/lesson" + lessonNumber + ".md";
        return loadTextFile(path);
    }

    /**
     * Load a quiz.json file from the classpath.
     *
     * @param courseName    the course name
     * @param chapterNumber the 1-based chapter number
     * @return parsed QuizResource, or empty if not found
     */
    public Optional<QuizResource> loadQuiz(String courseName, int chapterNumber) {
        String path = CURRICULUM_BASE + courseName + "/chapter" + chapterNumber + "/quiz.json";
        return loadJson(path, QuizResource.class);
    }

    /**
     * Load an assignment.json file from the classpath.
     *
     * @param courseName    the course name
     * @param chapterNumber the 1-based chapter number
     * @return parsed AssignmentResource, or empty if not found
     */
    public Optional<AssignmentResource> loadAssignment(String courseName, int chapterNumber) {
        String path = CURRICULUM_BASE + courseName + "/chapter" + chapterNumber + "/assignment.json";
        return loadJson(path, AssignmentResource.class);
    }

    private Optional<String> loadTextFile(String path) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                log.warn("[CURRICULUM] Resource not found: {}", path);
                return Optional.empty();
            }
            String content = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
            log.info("[CURRICULUM] Loaded resource: {} ({} chars)", path, content.length());
            return Optional.of(content);
        } catch (IOException e) {
            log.error("[CURRICULUM] Failed to load resource: {} - {}", path, e.getMessage());
            return Optional.empty();
        }
    }

    private <T> Optional<T> loadJson(String path, Class<T> type) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                log.warn("[CURRICULUM] JSON resource not found: {}", path);
                return Optional.empty();
            }
            T value = mapper.readValue(is, type);
            log.info("[CURRICULUM] Loaded JSON resource: {}", path);
            return Optional.of(value);
        } catch (IOException e) {
            log.error("[CURRICULUM] Failed to load JSON resource: {} - {}", path, e.getMessage());
            return Optional.empty();
        }
    }
}