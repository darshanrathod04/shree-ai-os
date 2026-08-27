package com.shreeai.os.platform.sdk;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * <b>SDKResponse</b>
 *
 * <p>Immutable response model for the Shree AI OS SDK.</p>
 *
 * <p><b>Ownership:</b> SDK</p>
 * <p><b>Version:</b> 1.0.0-V1</p>
 */
public final class SDKResponse {

    private final String answer;
    private final double confidence;
    private final boolean reasoningAvailable;
    private final String metadata;
    private final Map<String, Object> structuredPayload;
    private final Instant timestamp;

    private SDKResponse(Builder builder) {
        this.answer = builder.answer;
        this.confidence = builder.confidence;
        this.reasoningAvailable = builder.reasoningAvailable;
        this.metadata = builder.metadata;
        this.structuredPayload = builder.structuredPayload;
        this.timestamp = builder.timestamp;
    }

    public String answer() { return answer; }
    public double confidence() { return confidence; }
    public boolean reasoningAvailable() { return reasoningAvailable; }
    public String metadata() { return metadata; }

    /**
     * Returns the structured payload attached to this SDK response.
     *
     * <p>The structured payload is an additive, backward-compatible extension that
     * carries rich structured data (e.g. an {@code IntelligenceContext} with evidence,
     * provenance, and project profile) alongside the legacy flat {@code answer} string.
     * It is empty when no structured data was supplied.</p>
     *
     * @return the structured payload map (never null, may be empty)
     */
    public Map<String, Object> structuredPayload() { return structuredPayload; }

    public Instant timestamp() { return timestamp; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String answer;
        private double confidence = 0.0;
        private boolean reasoningAvailable = false;
        private String metadata = "";
        private Map<String, Object> structuredPayload = Map.of();
        private Instant timestamp = Instant.now();

        private Builder() {}

        public Builder answer(String answer) {
            this.answer = Objects.requireNonNull(answer, "answer must not be null");
            return this;
        }

        public Builder confidence(double confidence) {
            if (confidence < 0.0 || confidence > 1.0) {
                throw new IllegalArgumentException("confidence must be between 0.0 and 1.0");
            }
            this.confidence = confidence;
            return this;
        }

        public Builder reasoningAvailable(boolean reasoningAvailable) {
            this.reasoningAvailable = reasoningAvailable;
            return this;
        }

        public Builder metadata(String metadata) {
            this.metadata = Objects.requireNonNull(metadata, "metadata must not be null");
            return this;
        }

        /**
         * Sets the structured payload map (defensively copied).
         *
         * @param structuredPayload the structured payload
         * @return this builder
         */
        public Builder structuredPayload(Map<String, Object> structuredPayload) {
            this.structuredPayload = structuredPayload != null
                    ? Map.copyOf(structuredPayload)
                    : Map.of();
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = Objects.requireNonNull(timestamp, "timestamp must not be null");
            return this;
        }

        public SDKResponse build() {
            if (answer == null || answer.isBlank()) {
                throw new IllegalArgumentException("answer must not be null or blank");
            }
            return new SDKResponse(this);
        }
    }


    public String getAnswer() {
        return answer;
    }

    public double getConfidence() {
        return confidence;
    }

    public boolean isReasoningAvailable() {
        return reasoningAvailable;
    }

    public String getMetadata() {
        return metadata;
    }

    public Map<String, Object> getStructuredPayload() {
        return structuredPayload;
    }

    public Instant getTimestamp() {
        return timestamp;
    }


}