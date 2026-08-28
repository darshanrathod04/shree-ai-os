package com.shreeai.os.platform.legacy.learning;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;

/**
 * A chapter within a structured course.
 * Contains an ordered list of lessons covering a specific topic area.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Chapter {

    private String title;
    private String description;
    private int order;
    private List<Lesson> lessons;

    public Chapter() {
        this.lessons = new ArrayList<>();
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }

    public List<Lesson> getLessons() { return lessons; }
    public void setLessons(List<Lesson> lessons) { this.lessons = lessons; }

    public int getTotalLessons() {
        return lessons != null ? lessons.size() : 0;
    }
}