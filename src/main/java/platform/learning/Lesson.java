package platform.learning;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * A single lesson within a chapter.
 * Represents a self-contained learning unit with specific objectives.
 * Content will be populated by lesson definitions; this is a stub for the architecture.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Lesson {

    private String title;
    private String objective;
    private int estimatedMinutes;
    private String contentKey; // references full content in a future content store

    public Lesson() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getObjective() { return objective; }
    public void setObjective(String objective) { this.objective = objective; }

    public int getEstimatedMinutes() { return estimatedMinutes; }
    public void setEstimatedMinutes(int estimatedMinutes) { this.estimatedMinutes = estimatedMinutes; }

    public String getContentKey() { return contentKey; }
    public void setContentKey(String contentKey) { this.contentKey = contentKey; }
}