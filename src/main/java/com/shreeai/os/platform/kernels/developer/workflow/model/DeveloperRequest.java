package com.shreeai.os.platform.kernels.developer.workflow.model;

import java.util.Map;
import java.util.Objects;

/**
 * <b>DeveloperRequest</b>
 *
 * <p>Immutable request record for the autonomous developer workflow engine.
 * Contains the project path, the natural-language instruction, and optional
 * metadata.</p>
 *
 * <p><b>Ownership:</b> Developer Workflow (Sprint-16)</p>
 *
 * @since Sprint-16
 */
public final class DeveloperRequest {

    private final String projectPath;
    private final String instruction;
    private final Map<String, Object> metadata;

    public DeveloperRequest(String projectPath, String instruction, Map<String, Object> metadata) {
        this.projectPath = Objects.requireNonNull(projectPath, "projectPath must not be null");
        this.instruction = Objects.requireNonNull(instruction, "instruction must not be null");
        this.metadata = Map.copyOf(metadata == null ? Map.of() : metadata);
    }

    public String projectPath() { return projectPath; }
    public String instruction() { return instruction; }
    public Map<String, Object> metadata() { return metadata; }

    /**
     * Returns a metadata value by key, or null if absent.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) metadata.get(key);
    }

    /**
     * Returns a metadata value by key, or a default if absent.
     */
    @SuppressWarnings("unchecked")
    public <T> T getOrDefault(String key, T defaultValue) {
        return metadata.containsKey(key) ? (T) metadata.get(key) : defaultValue;
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String projectPath;
        private String instruction;
        private Map<String, Object> metadata;

        public Builder projectPath(String v) { this.projectPath = v; return this; }
        public Builder instruction(String v) { this.instruction = v; return this; }
        public Builder metadata(Map<String, Object> v) { this.metadata = v; return this; }

        public DeveloperRequest build() {
            return new DeveloperRequest(projectPath, instruction, metadata);
        }
    }
}
