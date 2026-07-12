package platform.runtime.pipeline;

import platform.execution.ExecutionMetadata;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Immutable pipeline result.
 *
 * <p>This class represents the result of pipeline execution.
 * It contains the execution status, stage information, messages, and metadata.</p>
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
public final class PipelineResult {

    private final String resultId;
    private final boolean success;
    private final String status;
    private final String currentStage;
    private final List<String> completedStages;
    private final long processingTime;
    private final List<String> messages;
    private final ExecutionMetadata metadata;
    private final Instant timestamp;

    /**
     * Create PipelineResult with builder.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Private constructor - use builder for construction.
     */
    private PipelineResult(
            String resultId,
            boolean success,
            String status,
            String currentStage,
            List<String> completedStages,
            long processingTime,
            List<String> messages,
            ExecutionMetadata metadata,
            Instant timestamp
    ) {
        this.resultId = resultId;
        this.success = success;
        this.status = status;
        this.currentStage = currentStage;
        this.completedStages = completedStages != null
                ? Collections.unmodifiableList(new ArrayList<>(completedStages))
                : Collections.emptyList();
        this.processingTime = processingTime;
        this.messages = messages != null
                ? Collections.unmodifiableList(new ArrayList<>(messages))
                : Collections.emptyList();
        this.metadata = metadata;
        this.timestamp = timestamp;
    }

    // Getters
    public String getResultId() {
        return resultId;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getStatus() {
        return status;
    }

    public String getCurrentStage() {
        return currentStage;
    }

    public List<String> getCompletedStages() {
        return completedStages;
    }

    public long getProcessingTime() {
        return processingTime;
    }

    public List<String> getMessages() {
        return messages;
    }

    public ExecutionMetadata getMetadata() {
        return metadata;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Check if the pipeline execution failed.
     *
     * @return true if the pipeline failed
     */
    public boolean isFailed() {
        return !success;
    }

    /**
     * Check if the pipeline has messages.
     *
     * @return true if there are messages
     */
    public boolean hasMessages() {
        return !messages.isEmpty();
    }

    @Override
    public String toString() {
        return "PipelineResult{" +
                "resultId='" + resultId + '\'' +
                ", success=" + success +
                ", status='" + status + '\'' +
                ", currentStage='" + currentStage + '\'' +
                ", completedStages=" + completedStages +
                ", processingTime=" + processingTime + "ms" +
                ", messages=" + messages +
                ", metadata=" + metadata +
                ", timestamp=" + timestamp +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PipelineResult that = (PipelineResult) o;

        if (success != that.success) return false;
        if (processingTime != that.processingTime) return false;
        if (!resultId.equals(that.resultId)) return false;
        if (!status.equals(that.status)) return false;
        if (currentStage != null ? !currentStage.equals(that.currentStage) : that.currentStage != null) return false;
        if (!completedStages.equals(that.completedStages)) return false;
        if (!messages.equals(that.messages)) return false;
        if (metadata != null ? !metadata.equals(that.metadata) : that.metadata != null) return false;
        return timestamp.equals(that.timestamp);
    }

    @Override
    public int hashCode() {
        int result = resultId.hashCode();
        result = 31 * result + (success ? 1 : 0);
        result = 31 * result + status.hashCode();
        result = 31 * result + (currentStage != null ? currentStage.hashCode() : 0);
        result = 31 * result + completedStages.hashCode();
        result = 31 * result + (int) (processingTime ^ (processingTime >>> 32));
        result = 31 * result + messages.hashCode();
        result = 31 * result + (metadata != null ? metadata.hashCode() : 0);
        result = 31 * result + timestamp.hashCode();
        return result;
    }

    /**
     * Builder for PipelineResult.
     */
    public static class Builder {
        private String resultId = UUID.randomUUID().toString();
        private boolean success;
        private String status = "UNKNOWN";
        private String currentStage;
        private List<String> completedStages = new ArrayList<>();
        private long processingTime;
        private List<String> messages = new ArrayList<>();
        private ExecutionMetadata metadata;
        private Instant timestamp = Instant.now();

        /**
         * Set the result ID (defaults to new UUID if not set).
         *
         * @param resultId the result ID
         * @return this builder
         */
        public Builder resultId(String resultId) {
            this.resultId = resultId;
            return this;
        }

        /**
         * Set the success flag.
         *
         * @param success true if pipeline succeeded
         * @return this builder
         */
        public Builder success(boolean success) {
            this.success = success;
            return this;
        }

        /**
         * Set the status.
         *
         * @param status the status string
         * @return this builder
         */
        public Builder status(String status) {
            this.status = status;
            return this;
        }

        /**
         * Set the current stage.
         *
         * @param currentStage the current stage name
         * @return this builder
         */
        public Builder currentStage(String currentStage) {
            this.currentStage = currentStage;
            return this;
        }

        /**
         * Add a completed stage.
         *
         * @param stageName the stage name
         * @return this builder
         */
        public Builder addCompletedStage(String stageName) {
            this.completedStages.add(stageName);
            return this;
        }

        /**
         * Set all completed stages (replaces existing list).
         *
         * @param completedStages the completed stages list
         * @return this builder
         */
        public Builder completedStages(List<String> completedStages) {
            this.completedStages = completedStages != null ? new ArrayList<>(completedStages) : new ArrayList<>();
            return this;
        }

        /**
         * Set the processing time in milliseconds.
         *
         * @param processingTime the processing time
         * @return this builder
         */
        public Builder processingTime(long processingTime) {
            this.processingTime = processingTime;
            return this;
        }

        /**
         * Add a message.
         *
         * @param message the message
         * @return this builder
         */
        public Builder addMessage(String message) {
            this.messages.add(message);
            return this;
        }

        /**
         * Set all messages (replaces existing list).
         *
         * @param messages the messages list
         * @return this builder
         */
        public Builder messages(List<String> messages) {
            this.messages = messages != null ? new ArrayList<>(messages) : new ArrayList<>();
            return this;
        }

        /**
         * Set the execution metadata.
         *
         * @param metadata the execution metadata
         * @return this builder
         */
        public Builder metadata(ExecutionMetadata metadata) {
            this.metadata = metadata;
            return this;
        }

        /**
         * Set the timestamp (defaults to now if not set).
         *
         * @param timestamp the timestamp
         * @return this builder
         */
        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        /**
         * Build the PipelineResult instance.
         *
         * @return a new PipelineResult instance
         */
        public PipelineResult build() {
            return new PipelineResult(
                    resultId, success, status, currentStage, completedStages,
                    processingTime, messages, metadata, timestamp
            );
        }
    }
}