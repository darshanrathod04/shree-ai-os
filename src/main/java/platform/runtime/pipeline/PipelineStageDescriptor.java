package platform.runtime.pipeline;

/**
 * Immutable descriptor for pipeline stages.
 *
 * <p>This class provides metadata about a pipeline stage including
 * its name, priority, enabled status, version, and description.</p>
 *
 * <p>This class is thread-safe and immutable by design.
 * All fields are final and set via constructor or builder.</p>
 *
 * <p>This is part of the stable Runtime Pipeline contract for Shree AI OS.</p>
 *
 * @author Shree AI OS Team
 * @version 1.0
 * @since Sprint 6.2A
 */
public final class PipelineStageDescriptor {

    private final String stageName;
    private final int priority;
    private final boolean enabled;
    private final String version;
    private final String description;

    /**
     * Create PipelineStageDescriptor with builder.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Private constructor - use builder for construction.
     */
    private PipelineStageDescriptor(
            String stageName,
            int priority,
            boolean enabled,
            String version,
            String description
    ) {
        this.stageName = stageName;
        this.priority = priority;
        this.enabled = enabled;
        this.version = version;
        this.description = description;
    }

    // Getters
    public String getStageName() {
        return stageName;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "PipelineStageDescriptor{" +
                "stageName='" + stageName + '\'' +
                ", priority=" + priority +
                ", enabled=" + enabled +
                ", version='" + version + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PipelineStageDescriptor that = (PipelineStageDescriptor) o;

        if (priority != that.priority) return false;
        if (enabled != that.enabled) return false;
        if (!stageName.equals(that.stageName)) return false;
        if (version != null ? !version.equals(that.version) : that.version != null) return false;
        return description != null ? description.equals(that.description) : that.description == null;
    }

    @Override
    public int hashCode() {
        int result = stageName.hashCode();
        result = 31 * result + priority;
        result = 31 * result + (enabled ? 1 : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }

    /**
     * Builder for PipelineStageDescriptor.
     */
    public static class Builder {
        private String stageName;
        private int priority = 0;
        private boolean enabled = true;
        private String version = "1.0";
        private String description;

        /**
         * Set the stage name (required).
         *
         * @param stageName the stage name
         * @return this builder
         */
        public Builder stageName(String stageName) {
            this.stageName = stageName;
            return this;
        }

        /**
         * Set the priority (lower numbers execute first).
         *
         * @param priority the priority
         * @return this builder
         */
        public Builder priority(int priority) {
            this.priority = priority;
            return this;
        }

        /**
         * Set whether the stage is enabled.
         *
         * @param enabled true if enabled
         * @return this builder
         */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Set the version.
         *
         * @param version the version
         * @return this builder
         */
        public Builder version(String version) {
            this.version = version;
            return this;
        }

        /**
         * Set the description.
         *
         * @param description the description
         * @return this builder
         */
        public Builder description(String description) {
            this.description = description;
            return this;
        }

        /**
         * Build the PipelineStageDescriptor instance.
         *
         * @return a new PipelineStageDescriptor instance
         * @throws IllegalStateException if stageName is null or empty
         */
        public PipelineStageDescriptor build() {
            if (stageName == null || stageName.isBlank()) {
                throw new IllegalStateException("stageName is required");
            }
            return new PipelineStageDescriptor(
                    stageName, priority, enabled, version, description
            );
        }
    }
}