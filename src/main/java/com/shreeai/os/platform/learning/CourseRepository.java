package com.shreeai.os.platform.learning;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads predefined courses from JSON files under src/main/resources/courses/.
 * Lazily loads and caches courses in memory.
 * No singleton mutable learning state — courses are read-only after loading.
 */
@Repository
public class CourseRepository {

    private static final String COURSES_LOCATION = "classpath:courses/*.json";

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.INDENT_OUTPUT);

    private final Map<String, Course> courseCache = new ConcurrentHashMap<>();
    private boolean loaded = false;

    @PostConstruct
    public void init() {
        loadAll();
    }

    /**
     * Lazily load all course definitions from classpath resources.
     * Safe to call multiple times — only loads once.
     */
    public synchronized void loadAll() {
        if (loaded) return;

        try {
            PathMatchingResourcePatternResolver resolver =
                    new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources(COURSES_LOCATION);

            for (Resource resource : resources) {
                try (InputStream is = resource.getInputStream()) {
                    Course course = mapper.readValue(is, Course.class);
                    if (course.getName() != null && !course.getName().isBlank()) {
                        courseCache.put(course.getName().toLowerCase(), course);
                        System.out.println("[CourseRepo] Loaded course: " + course.getName());
                    }
                } catch (IOException e) {
                    System.err.println("[CourseRepo] Failed to load " + resource.getFilename() + ": " + e.getMessage());
                }
            }

            loaded = true;
            System.out.println("[CourseRepo] Loaded " + courseCache.size() + " courses");
        } catch (IOException e) {
            System.err.println("[CourseRepo] Failed to resolve course resources: " + e.getMessage());
        }
    }

    /**
     * Get a course by name (case-insensitive).
     * Returns empty if course not found.
     */
    public Optional<Course> getCourse(String name) {
        if (!loaded) loadAll();
        return Optional.ofNullable(courseCache.get(name.toLowerCase().trim()));
    }

    /**
     * List all available course names.
     */
    public List<String> listCourses() {
        if (!loaded) loadAll();
        return new ArrayList<>(courseCache.keySet());
    }

    /**
     * Get all courses as a list.
     */
    public List<Course> getAllCourses() {
        if (!loaded) loadAll();
        return new ArrayList<>(courseCache.values());
    }

    /**
     * Check if a specific course exists.
     */
    public boolean hasCourse(String name) {
        if (!loaded) loadAll();
        return courseCache.containsKey(name.toLowerCase().trim());
    }

    /**
     * Get total number of loaded courses.
     */
    public int getCourseCount() {
        if (!loaded) loadAll();
        return courseCache.size();
    }
}