package com.darshan.agent.learning.curriculum;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Catalog of available courses based on curriculum course.json files.
 * Each course.json defines the course metadata and chapter structure.
 * The actual lesson content lives in .md files under each chapter directory.
 *
 * This is the curriculum-driven equivalent of CourseRepository.
 * It loads course.json files from src/main/resources/curriculum/ (per course subdirectory).
 */
@Component
public class CourseCatalog {

    private static final Logger log = LoggerFactory.getLogger(CourseCatalog.class);
    private static final String CATALOG_LOCATION = "classpath:curriculum/*/course.json";

    private final ObjectMapper mapper = new ObjectMapper()
            .findAndRegisterModules();
    private final Map<String, CurriculumCourse> catalog = new ConcurrentHashMap<>();
    private boolean loaded = false;

    public CourseCatalog() {
    }

    @PostConstruct
    public void init() {
        loadAll();
    }

    /**
     * Lazily load all course.json definitions from curriculum directories.
     */
    public synchronized void loadAll() {
        if (loaded) return;

        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources(CATALOG_LOCATION);

            for (Resource resource : resources) {
                try (InputStream is = resource.getInputStream()) {
                    CurriculumCourse course = mapper.readValue(is, CurriculumCourse.class);
                    if (course.getName() != null && !course.getName().isBlank()) {
                        catalog.put(course.getName().toLowerCase(), course);
                        log.info("[CURRICULUM] Course Loaded: {} ({})", course.getName(), course.getTitle());
                    }
                } catch (IOException e) {
                    log.error("[CURRICULUM] Failed to load course catalog from {}: {}", resource.getFilename(), e.getMessage());
                }
            }

            loaded = true;
            log.info("[CURRICULUM] Course catalog loaded: {} courses", catalog.size());
        } catch (IOException e) {
            log.error("[CURRICULUM] Failed to resolve curriculum resources: {}", e.getMessage());
        }
    }

    public Optional<CurriculumCourse> getCourse(String name) {
        if (!loaded) loadAll();
        return Optional.ofNullable(catalog.get(name.toLowerCase().trim()));
    }

    public List<String> listCourses() {
        if (!loaded) loadAll();
        return new ArrayList<>(catalog.keySet());
    }

    public boolean hasCourse(String name) {
        if (!loaded) loadAll();
        return catalog.containsKey(name.toLowerCase().trim());
    }

    public int getCourseCount() {
        if (!loaded) loadAll();
        return catalog.size();
    }

    /**
     * Internal model for curriculum course.json structure.
     * Contains only course metadata + chapter listing (no lesson details).
     * Lessons are loaded from .md files in chapter subdirectories.
     */
    public static final class CurriculumCourse {
        private String name;
        private String title;
        private String description;
        private String difficulty;
        private int estimatedHours;
        private List<String> prerequisites;
        private List<ChapterEntry> chapters;

        public CurriculumCourse() {
            this.prerequisites = new ArrayList<>();
            this.chapters = new ArrayList<>();
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getDifficulty() { return difficulty; }
        public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

        public int getEstimatedHours() { return estimatedHours; }
        public void setEstimatedHours(int estimatedHours) { this.estimatedHours = estimatedHours; }

        public List<String> getPrerequisites() { return prerequisites; }
        public void setPrerequisites(List<String> prerequisites) { this.prerequisites = prerequisites; }

        public List<ChapterEntry> getChapters() { return chapters; }
        public void setChapters(List<ChapterEntry> chapters) { this.chapters = chapters; }

        public int getTotalChapters() { return chapters != null ? chapters.size() : 0; }

        public static final class ChapterEntry {
            private String title;
            private String description;
            private int order;

            public ChapterEntry() {}

            public String getTitle() { return title; }
            public void setTitle(String title) { this.title = title; }

            public String getDescription() { return description; }
            public void setDescription(String description) { this.description = description; }

            public int getOrder() { return order; }
            public void setOrder(int order) { this.order = order; }
        }
    }
}