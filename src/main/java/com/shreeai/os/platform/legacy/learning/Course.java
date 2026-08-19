package com.shreeai.os.platform.legacy.learning;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;

/**
 * A structured course consisting of ordered chapters.
 * Loaded from JSON course definitions and cached by CourseRepository.
 * Immutable after construction — all content is predefined.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Course {

    private String name;
    private String title;
    private String description;
    private String difficulty; // BEGINNER, INTERMEDIATE, ADVANCED
    private int estimatedHours;
    private List<String> prerequisites;
    private List<Chapter> chapters;

    public Course() {
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

    public List<Chapter> getChapters() { return chapters; }
    public void setChapters(List<Chapter> chapters) { this.chapters = chapters; }

    public int getTotalChapters() {
        return chapters != null ? chapters.size() : 0;
    }
}