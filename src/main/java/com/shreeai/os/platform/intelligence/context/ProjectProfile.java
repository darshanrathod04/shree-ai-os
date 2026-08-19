package com.shreeai.os.platform.intelligence.context;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>ProjectProfile</b>
 *
 * <p>Structured representation of a software project's identity and observable
 * architecture facts. This replaces the current practice of reconstructing project
 * information (technologies, file counts, layers) from raw request text via
 * substring matching in {@code DefaultReasoningEngine}.</p>
 *
 * <p>Project facts carried here are treated as evidence by the intelligence
 * pipeline; they are never hardcoded or inferred. If a fact is unavailable it is
 * simply absent (empty collections / {@code null}).</p>
 *
 * <p><b>Ownership:</b> Intelligence Foundation</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param projectId the stable project identifier (may be {@code null} if unknown)
 * @param projectName the project name (must not be null or blank)
 * @param technologies detected/declared technologies (defensively copied)
 * @param totalFiles total file count ({@code -1} if unknown)
 * @param sourceFiles source file count ({@code -1} if unknown)
 * @param testFiles test file count ({@code -1} if unknown)
 * @param configurationFiles configuration file count ({@code -1} if unknown)
 * @param documentationFiles documentation file count ({@code -1} if unknown)
 * @param layers the architectural layers/components declared in the project
 * @param importantFiles the notable project files (e.g. entry points, configs)
 * @param metadata additional project metadata (defensively copied)
 */
public record ProjectProfile(
        String projectId,
        String projectName,
        List<String> technologies,
        int totalFiles,
        int sourceFiles,
        int testFiles,
        int configurationFiles,
        int documentationFiles,
        List<String> layers,
        List<String> importantFiles,
        Map<String, Object> metadata
) {

    /**
     * Creates a new ProjectProfile with validation.
     *
     * @throws NullPointerException if projectName is null
     * @throws IllegalArgumentException if projectName is blank
     */
    public ProjectProfile {
        Objects.requireNonNull(projectName, "projectName must not be null");
        if (projectName.isBlank()) {
            throw new IllegalArgumentException("projectName must not be blank");
        }
        technologies = technologies != null ? List.copyOf(technologies) : List.of();
        layers = layers != null ? List.copyOf(layers) : List.of();
        importantFiles = importantFiles != null ? List.copyOf(importantFiles) : List.of();
        metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
    }

    /**
     * Returns a basic ProjectProfile carrying only a name.
     *
     * @param projectName the project name
     * @return a minimal ProjectProfile
     */
    public static ProjectProfile named(String projectName) {
        return new ProjectProfile(
                null,
                projectName,
                List.of(),
                -1,
                -1,
                -1,
                -1,
                -1,
                List.of(),
                List.of(),
                Map.of()
        );
    }

    /**
     * Returns a builder for ProjectProfile.
     *
     * @param projectName the project name (required)
     * @return a new builder
     */
    public static Builder builder(String projectName) {
        return new Builder(projectName);
    }

    /**
     * Fluent builder for ProjectProfile.
     */
    public static final class Builder {
        private final String projectName;
        private String projectId;
        private List<String> technologies = List.of();
        private int totalFiles = -1;
        private int sourceFiles = -1;
        private int testFiles = -1;
        private int configurationFiles = -1;
        private int documentationFiles = -1;
        private List<String> layers = List.of();
        private List<String> importantFiles = List.of();
        private Map<String, Object> metadata = Map.of();

        private Builder(String projectName) {
            this.projectName = Objects.requireNonNull(projectName, "projectName must not be null");
        }

        public Builder projectId(String projectId) {
            this.projectId = projectId;
            return this;
        }

        public Builder technologies(List<String> technologies) {
            this.technologies = technologies != null ? List.copyOf(technologies) : List.of();
            return this;
        }

        public Builder totalFiles(int totalFiles) {
            this.totalFiles = totalFiles;
            return this;
        }

        public Builder sourceFiles(int sourceFiles) {
            this.sourceFiles = sourceFiles;
            return this;
        }

        public Builder testFiles(int testFiles) {
            this.testFiles = testFiles;
            return this;
        }

        public Builder configurationFiles(int configurationFiles) {
            this.configurationFiles = configurationFiles;
            return this;
        }

        public Builder documentationFiles(int documentationFiles) {
            this.documentationFiles = documentationFiles;
            return this;
        }

        public Builder layers(List<String> layers) {
            this.layers = layers != null ? List.copyOf(layers) : List.of();
            return this;
        }

        public Builder importantFiles(List<String> importantFiles) {
            this.importantFiles = importantFiles != null ? List.copyOf(importantFiles) : List.of();
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
            return this;
        }

        public ProjectProfile build() {
            return new ProjectProfile(
                    projectId, projectName, technologies, totalFiles, sourceFiles,
                    testFiles, configurationFiles, documentationFiles,
                    layers, importantFiles, metadata
            );
        }
    }
}