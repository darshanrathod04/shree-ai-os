package platform.learning.curriculum;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Immutable assignment resource loaded from assignment.json files.
 * Present for future Assignment Engine compatibility.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class AssignmentResource {

    private String title;
    private String problemStatement;
    private List<String> requirements;
    private List<String> expectedTopics;
    private List<String> hints;
    private String difficulty;

    public AssignmentResource() {}

    public String getTitle() { return title; }
    public String getProblemStatement() { return problemStatement; }
    public List<String> getRequirements() {
        return requirements != null ? Collections.unmodifiableList(requirements) : List.of();
    }
    public List<String> getExpectedTopics() {
        return expectedTopics != null ? Collections.unmodifiableList(expectedTopics) : List.of();
    }
    public List<String> getHints() {
        return hints != null ? Collections.unmodifiableList(hints) : List.of();
    }
    public String getDifficulty() { return difficulty; }

    public void setTitle(String title) { this.title = title; }
    public void setProblemStatement(String problemStatement) { this.problemStatement = problemStatement; }
    public void setRequirements(List<String> requirements) { this.requirements = requirements; }
    public void setExpectedTopics(List<String> expectedTopics) { this.expectedTopics = expectedTopics; }
    public void setHints(List<String> hints) { this.hints = hints; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
}